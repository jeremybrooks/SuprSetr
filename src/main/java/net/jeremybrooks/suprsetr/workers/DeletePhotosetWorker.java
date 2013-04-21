/*
 * SuprSetr is Copyright 2010-2013 by Jeremy Brooks
 *
 * This file is part of SuprSetr.
 *
 * SuprSetr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SuprSetr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SuprSetr.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.jeremybrooks.suprsetr.workers;

import java.io.ObjectOutputStream;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.LogWindow;
import net.jeremybrooks.suprsetr.MainWindow;
import net.jeremybrooks.suprsetr.SSPhotoset;
import net.jeremybrooks.suprsetr.dao.PhotosetDAO;
import net.jeremybrooks.suprsetr.flickr.PhotosetHelper;
import org.apache.log4j.Logger;


/**
 * This class removes a photoset from Flickr and from the database.
 *
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 *
 * @author jeremyb
 */
public class DeletePhotosetWorker extends SwingWorker<Void, Void> {

    /** Logging. */
    private Logger logger = Logger.getLogger(DeletePhotosetWorker.class);

    /** The blocker instance used to provide user with feedback. */
    private BlockerPanel blocker;

    /** The photoset to delete. */
    private SSPhotoset ssPhotoset;


    /**
     * Create an instance of DeletePhotoset.
     *
     * @param blocker the blocker instance.
     * @param ssPhotoset the photoset to add.
     */
    public DeletePhotosetWorker(BlockerPanel blocker, SSPhotoset ssPhotoset) {
	this.blocker = blocker;
	this.ssPhotoset = ssPhotoset;
    }


    /**
     * Execute the Flickr operation and database operations on a background
     * thread.
     *
     * @return this method does not return any data.
     */
    @Override
    protected Void doInBackground() {
	ObjectOutputStream out = null;

	try {
	    PhotosetHelper.getInstance().delete(ssPhotoset);

	    LogWindow.addLogMessage("Deleted set '" + ssPhotoset.getTitle() + "'");

	    // delete from database
	    PhotosetDAO.delete(ssPhotoset);

	} catch (Exception e) {
	    logger.error("ERROR DELETING SET ON FLICKR.", e);
	}

	return null;
    }


    /**
     * Finished, so update the GUI, making the first photoset the
     * currently selected photoset. Then remove the blocker.
     */
    @Override
    protected void done() {

	try {
	    MainWindow.getMainWindow().deletePhotosetFromListModel(ssPhotoset);
	    MainWindow.getMainWindow().makeIndexVisibleAndSelected(0);
	    
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
