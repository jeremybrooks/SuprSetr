/*
 *  SuprSetr is Copyright 2010-2023 by Jeremy Brooks
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

import net.jeremybrooks.jinx.logger.LogInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Allow logging of detailed information from Jinx.
 * 
 * @author Jeremy Brooks
 */
public class MyJinxLogger implements LogInterface {

    /** Logger. */
    private static final Logger logger = LogManager.getLogger(MyJinxLogger.class);

    @Override
    public void log(String message) {
	this.logger.debug(message);
    }


    @Override
    public void log(String message, Throwable t) {
	this.logger.debug(message, t);
    }

}
