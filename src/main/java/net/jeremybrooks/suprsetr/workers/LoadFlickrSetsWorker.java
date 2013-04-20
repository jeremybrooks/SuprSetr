/*
 * SuprSetr is Copyright 2010-2011 by Jeremy Brooks
 *
 * This file is part of SuprSetr.
 *
 *  SuprSetr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  SuprSetr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with SuprSetr.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.jeremybrooks.suprsetr.workers;

//import com.aetrion.flickr.photosets.Photoset;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import net.jeremybrooks.jinx.api.PhotosetsApi;
import net.jeremybrooks.jinx.dto.Photoset;
import net.jeremybrooks.jinx.dto.Photosets;
import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.MainWindow;
import net.jeremybrooks.suprsetr.SSConstants;
import net.jeremybrooks.suprsetr.SSPhotoset;
import net.jeremybrooks.suprsetr.dao.PhotosetDAO;
import net.jeremybrooks.suprsetr.flickr.FlickrHelper;
import net.jeremybrooks.suprsetr.flickr.PhotosetHelper;
import org.apache.log4j.Logger;


/**
 * This class loads photosets from Flickr, adding them to the database as
 * needed.
 *
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 *
 * @author jeremyb
 */
public class LoadFlickrSetsWorker extends SwingWorker<Void, SSPhotoset> {

    /** Logging. */
    private Logger logger = Logger.getLogger(LoadFlickrSetsWorker.class);

    /** The blocker used for feedback. */
    private BlockerPanel blocker;


    /**
     * Create a new instance of LoadFlickrSets.
     *
     * @param blocker the blocker.
     */
    public LoadFlickrSetsWorker(BlockerPanel blocker) {
	this.blocker = blocker;
    }


    /**
     * Execute the Flickr operation and database operations on a background
     * thread.
     *
     * @return this method does not return any data.
     */
    @Override
    protected Void doInBackground() {
	blocker.updateMessage("Getting photosets from Flickr...");
	String nsid = FlickrHelper.getInstance().getNSID();
	Photosets photosets = null;

	long sync = System.currentTimeMillis();

	try {
	    photosets = PhotosetsApi.getInstance().getList(nsid, true);
	    for (Photoset p : photosets.getPhotosetList()) {
		blocker.updateMessage("Processing \"" + p.getTitle() + "\"");

		SSPhotoset ssp = PhotosetDAO.getPhotosetForId(p.getId());

		if (ssp == null) {
		    // NEW SET, ADD TO DATABASE
		    ssp = new SSPhotoset();
		    // set fields inherited from Photoset
		    ssp.setDescription(p.getDescription());
		    ssp.setFarm(p.getFarm());
		    ssp.setId(p.getId());
		    ssp.setPhotos(p.getPhotos());
		    ssp.setPrimary(p.getPrimary());
		    ssp.setSecret(p.getSecret());
		    ssp.setServer(p.getServer());
		    ssp.setTitle(p.getTitle());

		    // build the URL -- it is not correct in the getUrl() call
		    StringBuffer sb = new StringBuffer();
		    sb.append("http://www.flickr.com/photos/");
		    sb.append(nsid);
		    sb.append("/sets/");
		    sb.append(p.getId());
		    sb.append("/");
		    ssp.setUrl(sb.toString());

		    // set custom fields
		    ssp.setManaged(false);
		    ssp.setMatchTakenDates(false);
		    ssp.setMatchUploadDates(false);
		    ssp.setPrimaryPhotoIcon(PhotosetHelper.getInstance().getIconForPhotoset(p));
		    ssp.setSyncTimestamp(sync);
		    ssp.setTagMatchMode("NONE");
		    ssp.setTags("");
		    ssp.setSendTweet(false);
		    ssp.setTweetTemplate(SSConstants.DEFAULT_TWEET_TEMPLATE);

		    PhotosetDAO.insertPhotoset(ssp);


		} else {

		    ssp.setFarm(p.getFarm());
		    ssp.setPhotos(p.getPhotos());

		    if (!p.getPrimary().equals(ssp.getPrimary())) {
			ssp.setPrimary(p.getPrimary());
			ssp.setPrimaryPhotoIcon(PhotosetHelper.getInstance().getIconForPhotoset(p));
		    }


		    ssp.setSecret(p.getSecret());
		    ssp.setServer(p.getServer());
		    ssp.setSyncTimestamp(sync);
		    ssp.setUrl(p.getUrl());		

		    // SAVE THE UPDATED SET TO THE DATABASE
		    PhotosetDAO.updatePhotoset(ssp);

		}

	    }

	    // NOW, DELETE RECORDS THAT NO LONGER EXIST ON FLICKR
	    List<SSPhotoset> allData = PhotosetDAO.getPhotosetListOrderByTitle();
	    for (SSPhotoset ssp : allData) {
		if (ssp.getSyncTimestamp() != sync) {
		    PhotosetDAO.delete(ssp);
		}
	    }


	} catch (Exception e) {
	    logger.error("ERROR GETTING PHOTOSET LIST.", e);

	    JOptionPane.showMessageDialog(null,
		    "There was an error while getting photosets.\n"
		    + "The error was " + e.getMessage() + "\n\n"
		    + "See the log for details.",
		    "Error",
		    JOptionPane.ERROR_MESSAGE);
	}

	return null;
    }


    /**
     * Finished, so update the GUI and unblock.
     */
    @Override
    protected void done() {
	// UPDATE THE LIST MODEL
	try {
	    MainWindow.getMainWindow().setMasterList(PhotosetDAO.getPhotosetListOrderByManagedAndTitle(), null);
	    	    
	} catch (Exception e) {
	    logger.error("ERROR WHILE TRYING TO UPDATE LIST MODEL.", e);
	    JOptionPane.showMessageDialog(null,
		    "There was an error while trying to update the list.\n"
		    + "However, the new set has been created successfully,\n"
		    + "and should appear in the list next time you start SuprSetr.",
		    "Error Updating GUI", JOptionPane.WARNING_MESSAGE);
	}
	blocker.unBlock();
    }

}
