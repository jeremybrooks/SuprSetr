/*
 * SuprSetr is Copyright 2010 by Jeremy Brooks
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import net.jeremybrooks.jinx.JinxConstants;
import net.jeremybrooks.jinx.JinxException;
import net.jeremybrooks.jinx.dto.Photo;
import net.jeremybrooks.jinx.dto.PhotoInfo;
import net.jeremybrooks.jinx.dto.Photos;
import net.jeremybrooks.jinx.dto.SearchParameters;
import net.jeremybrooks.jinx.dto.Tag;
import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.LogWindow;
import net.jeremybrooks.suprsetr.MainWindow;
import net.jeremybrooks.suprsetr.SSConstants;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import net.jeremybrooks.suprsetr.flickr.FlickrHelper;
import net.jeremybrooks.suprsetr.flickr.PhotoHelper;
import org.apache.log4j.Logger;


/**
 * This class scans a user's photos, adding favxx tags depending on how many
 * times a photo has been faved.
 *
 * <p>This operation can take a very long time!</p>
 *
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 *
 * @author jeremyb
 */
public class FavDeleteWorker extends SwingWorker<Void, Void> {

    /** Logging. */
    private Logger logger = Logger.getLogger(FavDeleteWorker.class);

    /** The blocker to provide user with feedback. */
    private BlockerPanel blocker;

    /** Count of how many photos had tags added. */
    private int count = 0;

    /** Flag to indicate if there were errors. */
    private boolean hasErrors = false;


    /**
     * Create a new instance of FavrTagr.
     *
     * @param blocker the blocker instance.
     */
    public FavDeleteWorker(BlockerPanel blocker) {
	this.blocker = blocker;
    }


    /**
     * Execute the Flickr operations on a background thread.
     *
     * <p>This method searches all the users photos, then looks at the tags. If
     * a tag is found that matches the "favxx" pattern, it is deleted.</p>
     *
     * @return this method does not return any data.
     */
    @Override
    protected Void doInBackground() {
	Photos photos = null;
//	List<String> newFaves = null;
	int processed = 0;
	int total = 0;

	try {
	    blocker.setTitle("Fav Tag Delete Running");
	    blocker.updateMessage("Getting a list of your photos....");

	    // Search for:
	    //    All media types
	    //    Uploaded from the beginning of time until tomorrow
	    //    Return tags as well
	    SearchParameters params = new SearchParameters();
	    params.setUserId(FlickrHelper.getInstance().getNSID());
	    params.setMedia(JinxConstants.MEDIA_ALL);
	    params.setMinUploadDate(new Date(0));
	    params.setMaxUploadDate(new Date(System.currentTimeMillis() + 86400000));
	    params.setExtras(JinxConstants.EXTRAS_TAGS);
	    photos = PhotoHelper.getInstance().getPhotos(params);

	    total = photos.getTotal();

	    logger.info("Got " + total + " photos.");

	    blocker.updateMessage("Looking for fav tags...");

	    blocker.setTitle("Fav Tag Delete Processed " + processed + "/" + total);

	    // iterate through all photos
	    for (Photo p : photos.getPhotos()) {

		// if it looks like we might have some fav tags, get the photo info
		if (p.getTags().contains("fav")) {
		    PhotoInfo pi = PhotoHelper.getInstance().getPhotoInfo(p);

		    // Look for "favxx" tags, and delete them.
		    for (Tag tag : pi.getTagList()) {
			if (tag.getRaw().startsWith("fav")) {
			    try {
				if (Integer.parseInt(tag.getRaw().substring(3)) > 0) {
				    logger.info("Removing tag " + tag.toString() + " from photo " + p.getId());
				    PhotoHelper.getInstance().removeTag(tag.getId());
				    this.count++;
				    LogWindow.addLogMessage("Removed tag " + tag.getText() + " from photo " + p.getId());
				}
			    } catch (Exception e) {
				// ignore
			    }
			}
		    }
		}

		processed++;

		if (processed % 100 == 0) {
		    blocker.setTitle("Fav Tag Delete Processed " + processed + "/" + total);
		}
	    }

	} catch (Exception e) {
	    logger.info("ERROR RUNNING FAV TAG DELETE.", e);
	}

	return null;



    }


    /**
     * Finished, so unblock the GUI and tell the user how many photos were
     * updated by the operation. That way they know the wait was worth it.
     */
    @Override
    protected void done() {
	StringBuilder message = new StringBuilder("Removed fav tags from ");
	message.append(this.count);
	if (this.count == 1) {
	    message.append(" photo.");
	} else {
	    message.append(" photos.");
	}

	blocker.unBlock();
	JOptionPane.showMessageDialog(MainWindow.getMainWindow(),
		message.toString(),
		"Fav Tag Delete Finished",
		JOptionPane.INFORMATION_MESSAGE);
	LogWindow.addLogMessage(message.toString());
    }


    
}
