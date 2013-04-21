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

// JAVA I/O
import java.io.File;

// SWING STUFF
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

// SUPRSETR CLASSES
import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.MainWindow;
import net.jeremybrooks.suprsetr.dao.DAOHelper;

// LOGGING
import org.apache.log4j.Logger;


/**
 * This class backs up the database to the specified directory.
 *
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 *
 * @author jeremyb
 */
public class DatabaseBackupWorker extends SwingWorker<Void, Void> {

    /** Logging. */
    private Logger logger = Logger.getLogger(DatabaseBackupWorker.class);

    /** The blocker instance used to provide user with feedback. */
    private BlockerPanel blocker;

    /** The directory that will hold the database backup. */
    private File backupDirectory;



    /**
     * Create an instance of DatabaseBackupWorker.
     *
     * @param blocker the blocker instance.
     * @param backupDirectory
     */
    public DatabaseBackupWorker(BlockerPanel blocker, File backupDirectory) {
	this.blocker = blocker;
	this.backupDirectory = backupDirectory;
    }


    /**
     * Perform the database backup on a background thread.
     *
     * @return this method does not return any data.
     */
    @Override
    protected Void doInBackground() {
	try {
	    blocker.updateMessage("Starting backup....");

	    DAOHelper.performBackup(backupDirectory);

	} catch (Exception e) {
	    logger.error("THERE WAS AN ERROR DURING DATABASE BACKUP.", e);

	    JOptionPane.showMessageDialog(MainWindow.getMainWindow(),
		    "There was an error during the database backup.\n" +
		    "You can continue to use SuprSetr, but the backup\n" +
		    "was not performed. Please send the logs to the developer.",
		    "Backup Error",
		    JOptionPane.ERROR_MESSAGE);
	}

	return null;
    }


    /**
     * Unblock the main window when finished.
     */
    @Override
    protected void done() {
	blocker.unBlock();
    }

}
