/*
 * SuprSetr is Copyright 2010-2014 by Jeremy Brooks
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

import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.MainWindow;
import net.jeremybrooks.suprsetr.dao.DAOHelper;
import org.apache.log4j.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.io.File;
import java.util.ResourceBundle;

/**
 * This class backs up the database to the specified directory.
 * <p/>
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 * @author jeremyb
 */
public class DatabaseBackupWorker extends SwingWorker<Void, Void> {

	/**
	 * Logging.
	 */
	private Logger logger = Logger.getLogger(DatabaseBackupWorker.class);

	/**
	 * The blocker instance used to provide user with feedback.
	 */
	private BlockerPanel blocker;

	/**
	 * The directory that will hold the database backup.
	 */
	private File backupDirectory;

	private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.workers");


	/**
	 * Create an instance of DatabaseBackupWorker.
	 *
	 * @param blocker         the blocker instance.
	 * @param backupDirectory the backup directory.
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
			blocker.updateMessage(resourceBundle.getString("DatabaseBackupWorker.blocker.starting"));
			DAOHelper.performBackup(backupDirectory);
		} catch (Exception e) {
			logger.error("THERE WAS AN ERROR DURING DATABASE BACKUP.", e);
			JOptionPane.showMessageDialog(MainWindow.getMainWindow(),
					resourceBundle.getString("DatabaseBackupWorker.dialog.error.message"),
					resourceBundle.getString("DatabaseBackupWorker.dialog.error.title"),
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
