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

package net.jeremybrooks.suprsetr.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.OutputStream;
import java.io.Writer;

/**
 * @author Jeremy Brooks
 */
public class IOUtil {
  private static Logger logger = LogManager.getLogger(IOUtil.class);

  public IOUtil() {
  }

  public static void close(Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (Exception e) {
      logger.warn("ERROR CLOSING CLOSABLE.", e);
    }
  }

  public static void flush(OutputStream out) {
    try {
      if (out != null)
        out.flush();
    } catch (Exception e) {
      logger.warn("ERROR FLUSHING OUTPUT STREAM.", e);
    }
  }

  public static void flush(Writer out) {
    try {
      if (out != null)
        out.flush();
    } catch (Exception e) {
      logger.warn("ERROR FLUSHING OUTPUT STREAM.", e);
    }
  }
}
