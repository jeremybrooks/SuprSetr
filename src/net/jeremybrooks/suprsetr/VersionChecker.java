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

package net.jeremybrooks.suprsetr;

// JAVA I/O
import java.io.BufferedReader;
import java.io.InputStreamReader;

// JAVA NETWORKING
import java.net.URL;
import java.net.HttpURLConnection;

// LOGGING
import net.whirljack.common.util.IOUtil;
import org.apache.log4j.Logger;



/**
 * Check for a new version of the program.
 *
 * @author jeremyb
 */
public class VersionChecker implements Runnable {

    /** Logging. */
    private Logger logger = Logger.getLogger(VersionChecker.class);



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
        String latestVersion = null;


        try {
            // WAIT A LITTLE BIT TO MAKE SURE THE MAIN WINDOW IS INSTANSIATED
            Thread.sleep(2000);

            // GET THE VERSION WEB PAGE
            conn = (HttpURLConnection) new URL(SSConstants.VERSION_URL).openConnection();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            latestVersion = in.readLine();

	    
            if (latestVersion.compareTo(Main.VERSION) > 0) {
		logger.info("New version is available.");
		MainWindow.getMainWindow().setUpdateAvailable(true);
            } else {
		logger.info("No new version is available.");
	    }

        } catch (Exception e) {
            logger.warn("ERROR WHILE CHECKING FOR A NEW VERSION.", e);
        } finally {
            IOUtil.close(in);
            conn.disconnect();
        }

    }
}
