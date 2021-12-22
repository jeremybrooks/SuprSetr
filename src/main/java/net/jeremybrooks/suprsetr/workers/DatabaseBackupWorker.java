/*
 *  SuprSetr is Copyright 2010-2021 by Jeremy Brooks
 *
 *  This file is part of SuprSetr.
 *
 *   SuprSetr is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SuprSetr is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SuprSetr.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.jeremybrooks.suprsetr.workers;

import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.MainWindow;
import net.jeremybrooks.suprsetr.SSConstants;
import net.jeremybrooks.suprsetr.dao.DAOHelper;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import net.jeremybrooks.suprsetr.utils.FileIsBackupDirectoryFilter;
import net.jeremybrooks.suprsetr.utils.FilenameComparator;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * This class backs up the database to the specified directory.
 *
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 * @author Jeremy Brooks
 */
public class DatabaseBackupWorker extends SwingWorker<Void, Void> {

	private Logger logger = LogManager.getLogger(DatabaseBackupWorker.class);
	private BlockerPanel blocker;
	private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.workers");
  boolean exitWhenFinished;

	/**
	 * Create an instance of DatabaseBackupWorker.
	 *
	 * @param blocker         the blocker instance.
	 */
	public DatabaseBackupWorker(BlockerPanel blocker, boolean exitWhenFinished) {
		this.blocker = blocker;
		this.exitWhenFinished = exitWhenFinished;
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
      File backupDirectory = new File(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_BACKUP_DIRECTORY));

      logger.info("Checking for old backups.");
      // list all the current backups, and delete the oldest if there are more than
      File[] files = backupDirectory.listFiles(new FileIsBackupDirectoryFilter());
      if (null != files && files.length >= Integer.parseInt(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_BACKUP_COUNT))) {
        Arrays.sort(files, new FilenameComparator());
        try {
          logger.info("Deleting old backup {}", files[0].getAbsolutePath());
          FileUtils.deleteDirectory(files[0]);
        } catch (IOException ioe) {
          logger.warn("Could not delete old backup {}", files[0].getAbsolutePath(), ioe);
        }
      }
      File newBackup = new File(backupDirectory, Long.toString(System.currentTimeMillis()));
      DAOHelper.performBackup(newBackup);
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
		if (exitWhenFinished) {
		  MainWindow.getMainWindow().exitSuprSetr();
    }
	}
}
