/*
 * SuprSetr is Copyright 2010-2017 by Jeremy Brooks
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

package net.jeremybrooks.suprsetr.utils;

import net.jeremybrooks.jinx.response.photos.Photo;
import net.jeremybrooks.jinx.response.photos.SearchParameters;
import net.jeremybrooks.suprsetr.SSConstants;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * Various utility methods used by SuprSetr.
 *
 * <p>These methods should be public static.</p>
 *
 * @author jeremyb
 */
public class SSUtils {

  /**
   * Logging.
   */
  private static Logger logger = Logger.getLogger(SSUtils.class);

  /**
   * Date formatter in a nice full format.
   */
  private static SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' HH:mm:ss");

  /**
   * Date formatter for a yyyy-MM-dd format.
   */
  private static SimpleDateFormat yyyyMMddFormatter = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * Date formatter for a yyyy-MM-dd HH:mm:ss format.
   */
  private static SimpleDateFormat yyyyMMddHHmmssFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  /**
   * This is not the constructor you are looking for.
   */
  private SSUtils() {
  }


  /**
   * Format the date with the long format.
   *
   * @param date date to be formatted.
   * @return formatted date.
   */
  public static String formatDate(Date date) {
    if (date == null) {
      return "";
    } else {
      return formatter.format(date);
    }
  }


  /**
   * Determine if the date is "stale" based on the value the user has
   * provided for refresh times.
   *
   * @param date the date to check.
   * @return true if it has been longer than the refresh time.
   */
  public static boolean readyForUpdate(Date date) {
    long hours;
    if (date == null) {
      return true;
    } else {
      // calculate millis in refresh time
      try {
        hours = Long.parseLong(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_REFRESH_WAIT));
      } catch (Exception e) {
        logger.warn("Error parsing value, default to 24.", e);
        hours = 24;
      }
      long millis = hours * 60 * 60 * 1000;

      return (date.getTime() < (System.currentTimeMillis() - millis));
    }
  }


  /**
   * Format a date as yyyy-MM-dd.
   *
   * @param date the date to format.
   * @return formatted date.
   */
  public static String formatDateAsYYYYMMDD(Date date) {
    if (date == null) {
      return "";
    } else {
      return yyyyMMddFormatter.format(date);
    }
  }


  /**
   * Parse a String in yyyy-MM-dd format to a date.
   *
   * @param yyyymmdd the String to parse.
   * @return the date represented by the string, or null if the string could
   * not be parsed.
   */
  public static Date parseYYYYMMDD(String yyyymmdd) {
    Date retDate = null;

    try {
      retDate = yyyyMMddFormatter.parse(yyyymmdd);
    } catch (Exception e) {
      // no biggie, will return null
    }

    return retDate;
  }

  /**
   * Parse a String in yyyy-MM-dd HH:mm:ss format to a date.
   *
   * @param yyyymmdd the String to parse.
   * @return the date represented by the string, or null if the string could
   * not be parsed.
   */
  public static Date parseYYYYMMDDHHmmss(String yyyymmdd) {
    Date retDate = null;

    try {
      retDate = yyyyMMddHHmmssFormatter.parse(yyyymmdd);
    } catch (Exception e) {
      // no biggie, will return null
    }

    return retDate;
  }


  /**
   * Turn a SearchParameters object into a nice string.
   *
   * @param params the search parameters object.
   * @return nicely formatted string representing the search parameters.
   */
  public static String searchParamsToString(SearchParameters params) {
    StringBuilder sb = new StringBuilder("Search parameters: [");
    sb.append("User Id:").append(params.getUserId());
    sb.append(" | Tag Mode:").append(params.getTagMode());
    sb.append(" | Tags:").append(params.getTags());
    sb.append(" | Min Taken Date:").append(params.getMinTakenDate());
    sb.append(" | Max Taken Date:").append(params.getMaxTakenDate());
    sb.append(" | Min Upload Date:").append(params.getMinUploadDate());
    sb.append(" | Max Upload Date:").append(params.getMaxUploadDate());
    sb.append(" ]");
    return sb.toString();
  }


  /**
   * Sort a list of photos by title in descending order.
   *
   * <p>The sort is not case sensitive.</p>
   *
   * @param list the list of photos to sort.
   */
  public static void sortPhotoListByTitleDescending(List<Photo> list) {
    list.sort(new PhotoTitleComparatorDescending());
  }


  /**
   * Sort a list of photos by title in ascending order.
   *
   * <p>The sort is not case sensitive.</p>
   *
   * @param list the list of photos to sort.
   */
  public static void sortPhotoListByTitleAscending(List<Photo> list) {
    list.sort(new PhotoTitleComparatorAscending());
  }


  /**
   * Comparator that sorts by photo title, but not sensitive to case.
   */
  static class PhotoTitleComparatorAscending implements Comparator<Photo>, Serializable {
    private static final long serialVersionUID = 1387948757971388077L;

    @Override
    public int compare(Photo photoA, Photo photoB) {
      return photoA.getTitle().compareToIgnoreCase(photoB.getTitle());
    }
  }

  static class PhotoTitleComparatorDescending implements Comparator<Photo>, Serializable {
    private static final long serialVersionUID = -3766242945003682431L;

    @Override
    public int compare(Photo photoA, Photo photoB) {
      return -(photoA.getTitle().compareToIgnoreCase(photoB.getTitle()));
    }
  }

  public static int getCurrentYear() {
    return new GregorianCalendar().get(Calendar.YEAR);
  }
}
