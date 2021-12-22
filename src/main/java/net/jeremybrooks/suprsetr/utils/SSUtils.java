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

package net.jeremybrooks.suprsetr.utils;

import net.jeremybrooks.jinx.JinxConstants;
import net.jeremybrooks.jinx.response.photos.Photo;
import net.jeremybrooks.jinx.response.photos.SearchParameters;
import net.jeremybrooks.suprsetr.SSConstants;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * Various utility methods used by SuprSetr.
 *
 * <p>These methods should be public static.</p>
 *
 * @author Jeremy Brooks
 */
public class SSUtils {

  private static Logger logger = LogManager.getLogger(SSUtils.class);

  /* Date formatter in a nice full format. */
  private static SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d, yyyy 'at' HH:mm:ss");

  /* Date formatter for a yyyy-MM-dd format. */
  private static SimpleDateFormat yyyyMMddFormatter = new SimpleDateFormat("yyyy-MM-dd");

  /* Date formatter for a yyyy-MM-dd HH:mm:ss format. */
  private static SimpleDateFormat yyyyMMddHHmmssFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  /* This is not the constructor you are looking for. */
  private SSUtils() {
  }

  /**
   * Convert a list of ColorCode to a String that can be stored in the database.
   * @param colorCodes list of color codes.
   * @return comma delimited string of color codes.
   */
  public static String colorCodeListToString(List<JinxConstants.ColorCode> colorCodes) {
    StringBuilder builder = new StringBuilder();
    if (colorCodes != null) {
      for (JinxConstants.ColorCode colorCode : colorCodes) {
        builder.append(colorCode.getColorName()).append(",");
      }
    }
    if (builder.length() > 0) {
      builder.deleteCharAt(builder.length() - 1);
    }
    return builder.toString();
  }

  /**
   * Convert a String of color codes to a List of ColorCode.
   * @param colors string to convert.
   * @return list of ColorCode.
   */
  public static List<JinxConstants.ColorCode> stringToColorCodeList(String colors) {
    List<JinxConstants.ColorCode> colorCodes = new ArrayList<>();

    if (colors != null) {
      for (String color : colors.split(",")) {
        for (JinxConstants.ColorCode code : JinxConstants.ColorCode.values()) {
          if (color.equals(code.getColorName())) {
            colorCodes.add(code);
            break;
          }
        }
      }
    }
    return colorCodes;
  }

  /**
   * Convert a List of PictureStyle to a comma delimited String that can be stored in the database.
   * @param pictureStyles list of PictureStyle.
   * @return comma delimited String.
   */
  public static String pictureStyleListToString(List<JinxConstants.PictureStyle> pictureStyles) {
    StringBuilder builder = new StringBuilder();
    if (pictureStyles != null) {
      for (JinxConstants.PictureStyle style : pictureStyles) {
        builder.append(style.getStyleName()).append(',');
      }
    }
    if (builder.length() > 0) {
      builder.deleteCharAt(builder.length() - 1);
    }
    return builder.toString();
  }

  /**
   * Convert a String to a list of PictureStyle.
   * @param styles string of picture styles.
   * @return list of PictureStyle.
   */
  public static List<JinxConstants.PictureStyle> stringToPictureStyleList(String styles) {
    List<JinxConstants.PictureStyle> pictureStyles = new ArrayList<>();
    if (styles != null) {
      for (String styleName : styles.split(",")) {
        for (JinxConstants.PictureStyle style : JinxConstants.PictureStyle.values()) {
          if (styleName.equals(style.getStyleName())) {
            pictureStyles.add(style);
            break;
          }
        }
      }
    }
    return pictureStyles;
  }

  /**
   * Convert a list of Orientation to a comma delimited String that can be stored in the database.
   * @param orientations list of orientation.
   * @return comma delimited string of orientation.
   */
  public static String orientationListToString(List<JinxConstants.Orientation> orientations) {
    StringBuilder builder = new StringBuilder();
    if (orientations != null) {
      for (JinxConstants.Orientation orientation : orientations) {
        builder.append(orientation.toString()).append(',');
      }
    }
    if (builder.length() > 0) {
      builder.deleteCharAt(builder.length() -1);
    }
    return builder.toString();
  }

  /**
   * Convert a String to a List of Orientation.
   * @param orientations string to convert.
   * @return list of orientation.
   */
  public static List<JinxConstants.Orientation> stringToOrientationList(String orientations) {
    List<JinxConstants.Orientation> orientationList = new ArrayList<>();
    if (orientations != null) {
      for (String orientationName : orientations.split(",")) {
        for (JinxConstants.Orientation orientation : JinxConstants.Orientation.values()) {
          if (orientationName.equals(orientation.toString())) {
            orientationList.add(orientation);
            break;
          }
        }
      }
    }
    return orientationList;
  }


  /**
   * Sort a list of photos.
   *
   * <p>For sort order constants, see {@link SSConstants#SORT_ORDER}.</p>
   *
   * @param photoList the list of photos to sort.
   * @param sortOrder the sort order.
   */
  public static void sortPhotoList(List<Photo> photoList, int sortOrder) {
    if (photoList == null) {
      return;
    }
    if (sortOrder == 7) {
      SSUtils.sortPhotoListByTitleDescending(photoList);
    } else if (sortOrder == 8) {
      SSUtils.sortPhotoListByTitleAscending(photoList);
    } else if (sortOrder == 9) {
      Collections.shuffle(photoList);
    } else if (sortOrder == 10) {
      SSUtils.sortPhotoListByViewsDescending(photoList);
    } else if (sortOrder == 11) {
      SSUtils.sortPhotoListByViewsAscending(photoList);
    }
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
    return "Search parameters: [" + "User Id:" + params.getUserId() +
        " | Tag Mode:" + params.getTagMode() +
        " | Tags:" + params.getTags() +
        " | Min Taken Date:" + params.getMinTakenDate() +
        " | Max Taken Date:" + params.getMaxTakenDate() +
        " | Min Upload Date:" + params.getMinUploadDate() +
        " | Max Upload Date:" + params.getMaxUploadDate() +
        " ]";
  }


  /**
   * Sort a list of photos by title in descending order.
   *
   * <p>The sort is not case sensitive.</p>
   *
   * @param list the list of photos to sort.
   */
  private static void sortPhotoListByTitleDescending(List<Photo> list) {
    list.sort(new PhotoTitleComparatorDescending());
  }


  /**
   * Sort a list of photos by title in ascending order.
   *
   * <p>The sort is not case sensitive.</p>
   *
   * @param list the list of photos to sort.
   */
  private static void sortPhotoListByTitleAscending(List<Photo> list) {
    list.sort(new PhotoTitleComparatorAscending());
  }

  /**
   * Sort a list of photos by number of views in descending order.
   *
   * @param list the list of photos to sort.
   */
  private static void sortPhotoListByViewsDescending(List<Photo> list) {
    list.sort(new PhotoViewsComparatorDescending());
  }

  /**
   * Sort a list of photos by number of views in ascending order.
   *
   * @param list the list of photos to sort.
   */
  private static void sortPhotoListByViewsAscending(List<Photo> list) {
    list.sort(new PhotoViewsComparatorAscending());
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

  /**
   * Comparator that sorts by photo title in descnending order, but is not sensitive to case.
   */
  static class PhotoTitleComparatorDescending implements Comparator<Photo>, Serializable {
    private static final long serialVersionUID = -3766242945003682431L;

    @Override
    public int compare(Photo photoA, Photo photoB) {
      return -(photoA.getTitle().compareToIgnoreCase(photoB.getTitle()));
    }
  }

  /**
   * Comparator that sorts by photo views in ascending order.
   */
  static class PhotoViewsComparatorAscending implements Comparator<Photo>, Serializable {
    private static final long serialVersionUID = 2703785877608384885L;

    @Override
    public int compare(Photo photoA, Photo photoB) {
      return Integer.compare(photoA.getViews(), photoB.getViews());
    }
  }

  /**
   * Comparator that sorts by photo views in descending order.
   */
  static class PhotoViewsComparatorDescending implements Comparator<Photo>, Serializable {
    private static final long serialVersionUID = -8953067632780861868L;

    @Override
    public int compare(Photo photoA, Photo photoB) {
      return -Integer.compare(photoA.getViews(), photoB.getViews());
    }
  }

  /**
   * Get the current calendar year.
   *
   * @return current calendar year.
   */
  public static int getCurrentYear() {
    return new GregorianCalendar().get(Calendar.YEAR);
  }
}
