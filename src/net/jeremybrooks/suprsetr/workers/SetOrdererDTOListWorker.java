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

//import com.aetrion.flickr.photosets.Photoset;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import net.jeremybrooks.jinx.api.PhotosetsApi;
import net.jeremybrooks.jinx.dto.Photoset;
import net.jeremybrooks.jinx.dto.Photosets;
import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.SSPhotoset;
import net.jeremybrooks.suprsetr.SetOrderer;
import net.jeremybrooks.suprsetr.SetOrdererDTO;
import net.jeremybrooks.suprsetr.dao.PhotosetDAO;
import net.jeremybrooks.suprsetr.flickr.FlickrHelper;
import net.jeremybrooks.suprsetr.flickr.PhotosetHelper;
import org.apache.log4j.Logger;


/**
 * This class loads photosets from Flickr, then updates the SetOrderer list
 * model with the results.
 *
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 *
 * @author jeremyb
 */
public class SetOrdererDTOListWorker extends SwingWorker<List<SetOrdererDTO>, SSPhotoset> {

    /** Logging. */
    private Logger logger = Logger.getLogger(SetOrdererDTOListWorker.class);

    /** The blocker used for feedback. */
    private BlockerPanel blocker;

    /** The list model to update. */
    private DefaultListModel listModel;


    /** The dialog. */
    private SetOrderer dialog;

    
    /**
     * Create a new instance of LoadFlickrSets.
     *
     * @param blocker the blocker.
     */
    public SetOrdererDTOListWorker(BlockerPanel blocker, DefaultListModel listModel, SetOrderer dialog) {
	this.blocker = blocker;
	this.listModel = listModel;
	this.dialog = dialog;
    }


    /**
     * Execute the Flickr operation and database operations on a background
     * thread.
     *
     * @return this method does not return any data.
     */
    @Override
    protected List<SetOrdererDTO> doInBackground() {
	blocker.updateMessage("Getting photosets from Flickr...");
	String nsid = FlickrHelper.getInstance().getNSID();
	Photosets photosets = null;
	List<SetOrdererDTO> dtoList = new ArrayList<SetOrdererDTO>();

	try {
	    photosets = PhotosetsApi.getInstance().getList(nsid, true);
	    for (Photoset p : photosets.getPhotosetList()) {
		blocker.updateMessage("Processing \"" + p.getTitle() + "\"");

		// populate a DTO
		SetOrdererDTO sod = new SetOrdererDTO();
		sod.setDescription(p.getDescription());
		sod.setPhotoCount(p.getPhotos());
		sod.setId(p.getId());
		sod.setTitle(p.getTitle());

		// get the icon from the database if possible
		SSPhotoset ssp = PhotosetDAO.getPhotosetForId(p.getId());

		if (ssp == null) {
		    // This means the set was created manually on Flickr after
		    // SuprSetr was launched. So load the icon from Flickr
		    // This is slower than loading from the database, but at
		    // least we get the icon
		    sod.setIcon(PhotosetHelper.getInstance().getIconForPhotoset(p));

		} else {
		    sod.setIcon(ssp.getPrimaryPhotoIcon());
		}

		// add to the list
		dtoList.add(sod);

	    }

	} catch (Exception e) {
	    logger.error("ERROR GETTING PHOTOSET LIST FOR SET ORDERER.", e);

	    JOptionPane.showMessageDialog(null,
		    "There was an error while getting photosets.\n"
		    + "The error was " + e.getMessage() + "\n\n"
		    + "See the log for details.",
		    "Error",
		    JOptionPane.ERROR_MESSAGE);
	}

	return dtoList;
    }


    /**
     * Finished, so update the GUI and unblock.
     */
    @Override
    protected void done() {
	// UPDATE THE LIST MODEL
	try {
	    List<SetOrdererDTO> list = get();
	    this.dialog.setDtoList(list);
	    
	    for (SetOrdererDTO sod : list) {
		this.listModel.addElement(sod);
	    }
	} catch (Exception e) {
	    logger.warn("ERROR DURING UPDATE.", e);
	}
	blocker.unBlock();
    }

}
