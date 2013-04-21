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

import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.SwingWorker;
import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.MainWindow;
import net.jeremybrooks.suprsetr.SSPhotoset;
import org.apache.log4j.Logger;


/**
 * This class updates the main window list on a background thread.
 *
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 *
 * @author jeremyb
 */
public class FilterSetListWorker extends SwingWorker<Void, Void> {

    /** Logging. */
    private Logger logger = Logger.getLogger(FilterSetListWorker.class);

    /** The blocker used for feedback. */
    private BlockerPanel blocker;

    /** The photoset list. */
    private List<SSPhotoset> list;

    /** Filter used to determine which sets to add to the list model. */
    private String filter;

    /** The list model. */
    private DefaultListModel listModel;

    /** Flag indicating if unmanaged sets should be hidden. */
    private boolean hide;

    /** Photoset to make visible after list is updated. */
    private String visiblePhotosetId;

    /**
     * Create a new instance of MasterListWorker.
     *
     * @param blocker the blocker instance.
     * @param list the list of photosets.
     * @param filter filter used to determine which sets to add.
     * @param listModel the list model.
     */
    public FilterSetListWorker(BlockerPanel blocker, List<SSPhotoset> list, String filter, DefaultListModel listModel, boolean hide, String visiblePhotosetId) {
	this.blocker = blocker;
	this.list = list;
	this.filter = filter;
	this.listModel = listModel;
	this.hide = hide;
	this.visiblePhotosetId = visiblePhotosetId;
    }


    /**
     * Updates the list model, adding photosets that match the filter.
     *
     * <p>The filter is NOT case sensitive.</p>
     *
     * @return this method does not return any data.
     */
    @Override
    protected Void doInBackground() {
	blocker.block("working...");

	int i = 1;
	for (SSPhotoset set : this.list) {
	    if (filter == null || set.getTitle().toLowerCase().contains(filter)) {
		if ( (!hide) || (set.isManaged())) {
		    this.listModel.addElement(set);
		}
	    }
	    if (i % 10 == 0) {
		blocker.updateMessage("working... (" + i + "/" + list.size() + ")");
	    }
	    i++;
	}
	
	return null;
    }



    /**
     * Finished, so unblock.
     */
    @Override
    protected void done() {
	// if updating a specific photoset, scroll to it
	// otherwise, scroll to the top
	if (this.visiblePhotosetId == null) {
	    MainWindow.getMainWindow().makeIndexVisibleAndSelected(0);
	} else {
	    MainWindow.getMainWindow().scrollToPhotoset(this.visiblePhotosetId);
	}

	MainWindow.getMainWindow().updateTitle();
	
	blocker.unBlock();
	
    }

}