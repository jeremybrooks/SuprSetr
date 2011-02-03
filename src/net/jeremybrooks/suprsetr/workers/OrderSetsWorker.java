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

import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.flickr.PhotosetHelper;
import org.apache.log4j.Logger;


/**
 * This class reordered the photosets on flickr.
 *
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 *
 * @author jeremyb
 */
public class OrderSetsWorker extends SwingWorker<Void, Void> {

    /** Logging. */
    private Logger logger = Logger.getLogger(OrderSetsWorker.class);

    /** The blocker instance used to provide user with feedback. */
    private BlockerPanel blocker;

    /** Array of photoset Id's indicating the order of photosets. */
    private List<String> photosetIds;


    /**
     * Create an instance of OrderSetsWorker.
     *
     * @param blocker the blocker instance.
     * @param photosetIds string array of photoset id's.
     */
    public OrderSetsWorker(BlockerPanel blocker, List<String> photosetIds) {
	this.blocker = blocker;
	this.photosetIds = photosetIds;
    }


    /**
     * Execute the Flickr operation on a background thread.
     *
     * @return this method does not return any data.
     */
    @Override
    protected Void doInBackground() {

	blocker.block("Ordering " + this.photosetIds.size() + " sets.");

	try {
	    PhotosetHelper.getInstance().orderSets(photosetIds);
	} catch (Exception e) {
	    logger.error("ERROR ORDERING SETS ON FLICKR.", e);
	}

	return null;
    }


    /**
     * Finished, so update the GUI, making the first photoset the
     * currently selected photoset. Then remove the blocker.
     */
    @Override
    protected void done() {

	JOptionPane.showMessageDialog(null,
		"Your photosets have been re-ordered.",
		"Done",
		JOptionPane.INFORMATION_MESSAGE);
	blocker.unBlock();
    }

}
