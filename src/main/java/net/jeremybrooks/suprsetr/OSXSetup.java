/*
 *  SuprSetr is Copyright 2010-2020 by Jeremy Brooks
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

package net.jeremybrooks.suprsetr;

import javax.swing.JOptionPane;
import java.awt.Desktop;
import java.util.ResourceBundle;

/**
 * Handle Mac-specific events.
 *
 * @author Jeremy Brooks
 */
public class OSXSetup {

	private String quitMessage;
	private String quitTitle;

	public OSXSetup() {
		ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.misc");
		quitMessage = resourceBundle.getString("busy.quit.message");
		quitTitle = resourceBundle.getString("busy.quit.title");

		Desktop.getDesktop().setAboutHandler(ae -> new AboutDialog(null, true).setVisible(true));

		Desktop.getDesktop().setQuitHandler((qe, qr) -> {
      int confirm;
      // make the user confirm if busy
      if (MainWindow.isBlocked()) {
        confirm = JOptionPane.showConfirmDialog(MainWindow.getMainWindow(),
            quitMessage,
            quitTitle,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
          MainWindow.getMainWindow().backupAndExit();
        }

      } else {
        MainWindow.getMainWindow().backupAndExit();
      }
    });
		Desktop.getDesktop().setPreferencesHandler(pe -> new Preferences(MainWindow.getMainWindow(), true).setVisible(true));
	}

}
