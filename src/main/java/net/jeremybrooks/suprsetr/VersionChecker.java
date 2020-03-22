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

import net.jeremybrooks.suprsetr.utils.NetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Check for a new version of the program.
 *
 * @author Jeremy Brooks
 */
public class VersionChecker implements Runnable {

  private Logger logger = LogManager.getLogger(VersionChecker.class);
  private boolean showNoUpdateMessage;
  private boolean delayCheck;

  VersionChecker() {
    this(false, true);
  }

  VersionChecker(boolean showNoUpdateMessage, boolean delayCheck) {
    this.showNoUpdateMessage = showNoUpdateMessage;
    this.delayCheck = delayCheck;
  }

  /**
   * Run loop for the Runnable.
   *
   * <p>This method will check to see if there is a new version available.
   * It runs as a separate Thread so that it will not block the GUI if the
   * network connection is slow or missing.</p>
   */
  @Override
  public void run() {
    HttpURLConnection conn = null;
    BufferedReader in = null;
    String latestVersion;
    try {
      if (delayCheck) {
        // WAIT A LITTLE BIT TO MAKE SURE THE MAIN WINDOW IS READY
        Thread.sleep(2000);
      }

      // GET THE VERSION WEB PAGE
      conn = (HttpURLConnection) new URL(SSConstants.VERSION_URL).openConnection(NetUtil.getProxy());
      in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      latestVersion = in.readLine();
      logger.info(latestVersion);

      if (latestVersion.compareTo(Main.VERSION) > 0) {
        logger.info("New version is available.");
        MainWindow.getMainWindow().showUpdateDialog();
      } else {
        logger.info("No new version is available.");
        if (this.showNoUpdateMessage) {
          MainWindow.getMainWindow().showNoUpdateDialog();
        }
      }

    } catch (Exception e) {
      logger.warn("ERROR WHILE CHECKING FOR A NEW VERSION.", e);
      if (this.showNoUpdateMessage) {
        JOptionPane.showMessageDialog(MainWindow.getMainWindow(),
            "There was an error while checking for a new version.\nTry again later.",
            "Error", JOptionPane.ERROR_MESSAGE);
      }
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (Exception e) {
        // ignore
      } finally {
        if (conn != null) {
          conn.disconnect();
        }
      }
    }
  }
}
