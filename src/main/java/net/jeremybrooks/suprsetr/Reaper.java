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
package net.jeremybrooks.suprsetr;

import java.awt.Rectangle;
import net.jeremybrooks.suprsetr.dao.DAOHelper;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import org.apache.log4j.Logger;


/**
 * Shutdown hook.
 *
 * <p>Everything in the run method will be executed just before the JVM
 * shuts down.</p>
 *
 * @author jeremyb
 */
public class Reaper implements Runnable {

    /** Logging. */
    private Logger logger = Logger.getLogger(Reaper.class);


    /**
     * Executed when the JVM is ready to exit.
     */
    @Override
    public void run() {

	// save window position
	Rectangle rect = MainWindow.getMainWindow().getBounds();

	LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_X, Integer.toString(rect.x));
	LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_Y, Integer.toString(rect.y));
	LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_WIDTH, Integer.toString(rect.width));
	LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_HEIGHT, Integer.toString(rect.height));

	logger.info("Compressing database tables...");
	try {
	    DAOHelper.compressTables();
	} catch (Exception e) {
	    logger.error("ERROR COMPRESSING DATABASE TABLES.", e);
	} finally {
	    try {
		DAOHelper.shutdown();
	    } catch (Exception e) {
		// ignore; this is expected
	    }
	}
	logger.info("Database has been shut down");

	logger.info("SuprSetr exiting. Goodbye.");
    }

}
