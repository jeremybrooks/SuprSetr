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

package net.jeremybrooks.suprsetr;

import net.jeremybrooks.suprsetr.utils.SSUtils;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

/**
 * Constants for SuprSetr.
 *
 * <p>These should be public static final.</p>
 *
 * @author Jeremy Brooks
 */
public class SSConstants {
  public static final String TAG_MATCH_MODE_NONE = "NONE";
  public static final String TAG_MATCH_MODE_ANY = "ANY";
  public static final String TAG_MATCH_MODE_ALL = "ALL";

  public static final String[] SORT_ORDER = {
      "Interestingness Descending",
      "Interestingness Ascending",
      "Date Taken Descending",
      "Date Taken Ascending",
      "Date Posted Descending",
      "Date Posted Ascending",
      "No Particular Order",
      "Photo Title Descending",
      "Photo Title Ascending",
      "Random",
      "Views High To Low",
      "Views Low To High"
  };

  public static final String LIST_SORT_ATOZ = "0";
  public static final String LIST_SORT_ZTOA = "1";
  public static final String LIST_SORT_VIEW_HIGHLOW = "2";
  public static final String LIST_SORT_VIEW_LOWHIGH = "3";

  public static final String LOOKUP_KEY_DATABASE_VERSION = "DATABASE_VERSION";
  public static final String LOOKUP_KEY_TWITTER_USERNAME = "TWITTER_USER_NAME";
  public static final String LOOKUP_KEY_TWITTER_USERID = "TWITTER_USER_ID";
  public static final String LOOKUP_KEY_TWITTER_TOKEN = "TWITTER_TOKEN";
  public static final String LOOKUP_KEY_TWITTER_TOKEN_SECRET = "TWITTER_TOKEN_SECRET";
  public static final String LOOKUP_KEY_ADD_VIA = "ADD_VIA";
  public static final String LOOKUP_KEY_ADD_MANAGED = "ADD_MANAGED";
  public static final String LOOKUP_KEY_REFRESH_WAIT = "REFRESH_WAIT";
  public static final String LOOKUP_KEY_CHECK_FOR_UPDATE = "CHECK_FOR_UPDATES";
  public static final String LOOKUP_KEY_X = "WINDOW_X";
  public static final String LOOKUP_KEY_Y = "WINDOW_Y";
  public static final String LOOKUP_KEY_WIDTH = "WINDOW_WIDTH";
  public static final String LOOKUP_KEY_HEIGHT = "WINDOW_HEIGHT";
  public static final String LOOKUP_KEY_FAVRTAGR_INTERVAL = "FAVRTAGR_INTERVAL";
  public static final String LOOKUP_KEY_CASE_SENSITIVE = "CASE_SENSITIVE";
  public static final String LOOKUP_KEY_LIST_SORT_ORDER = "SORT_ORDER";
  public static final String LOOKUP_KEY_TAG_TYPE = "TAG_TYPE";

  // Proxy
  public static final String LOOKUP_KEY_USE_PROXY = "USE_PROXY";
  public static final String LOOKUP_KEY_PROXY_HOST = "PROXY_HOST";
  public static final String LOOKUP_KEY_PROXY_PORT = "PROXY_PORT";
  public static final String LOOKUP_KEY_PROXY_USER = "PROXY_USERNAME";
  public static final String LOOKUP_KEY_PROXY_PASS = "PROXY_PASSWORD";
  public static final String LOOKUP_KEY_PROXY_USE_SYSTEM = "PROXY_USE_SYSTEM";

  public static final String LOOKUP_KEY_HIDE_UNMANAGED = "HIDE_UNMANAGED_SETS";
  public static final String LOOKUP_KEY_HIDE_MANAGED = "HIDE_MANAGED_SETS";

  public static final String LOOKUP_KEY_TUTORIAL_DISPLAYED = "TUTORIAL_DISPLAYED";

  public static final String LOOKUP_KEY_DETAIL_LOG = "DETAIL_LOG";

  public static final String LOOKUP_KEY_LOG_WINDOW_BOUNDS = "LOG_WINDOW_BOUNDS";

  public static final String LOOKUP_KEY_AUTO_REFRESH = "AUTO_REFRESH";
  public static final String LOOKUP_KEY_AUTO_REFRESH_TIME = "AUTO_REFRESH_TIME";
  public static final String LOOKUP_KEY_AUTO_REFRESH_EXIT_AFTER = "AUTO_REFRESH_EXIT_AFTER";

  public static final String LOOKUP_KEY_BACKUP_AT_EXIT = "BACKUP_AT_EXIT";
  public static final String LOOKUP_KEY_BACKUP_COUNT = "BACKUP_COUNT";
  public static final String LOOKUP_KEY_BACKUP_DIRECTORY = "BACKUP_DIRECTORY";

  public static final String LOOKUP_KEY_SHOW_EDIT_TOOLBAR = "SHOW_EDIT_TOOLBAR";
  public static final String LOOKUP_KEY_SHOW_TOOLS_TOOLBAR = "SHOW_TOOLS_TOOLBAR";

  public static final String DEFAULT_TWEET_TEMPLATE =
      "There are %c new photos in my Flickr photoset %u (%t).";

  public static final String DEFAULT_TWEET_CREATE_TEMPLATE =
      "Check out my new photoset '%t' on Flickr: %u.";

  public static final String VIA_TWEET = " (via @suprsetr)";

  public static final String DEFAULT_REFRESH_WAIT = "24";
  public static final String DEFAULT_FAVRTAGR_INTERVAL = "10";

  public static final String VERSION_URL = "https://www.jeremybrooks.net/suprsetr/VERSION";
  public static final String DOWNLOAD_URL = "https://www.jeremybrooks.net/suprsetr/download.html";

  public static final String ADD_MANAGED = "This set is managed by <a href=\"https://www.jeremybrooks.net/suprsetr\">SuprSetr</a>";

  public static final int DATABASE_SCHEMA_CURRENT_VERSION = 9;

  /**
   * This map contains the DDL necessary to update the database from version to version.
   * Each entry in the Map contains the patch version, and a list of DDL necessary to
   * bring the database up to that version.
   */
  public static final Map<Integer, List<String>> DB_UPGRADES = Map.ofEntries(
      entry(2, List.of(
          "ALTER TABLE PHOTOSET ADD COLUMN LOCK_PRIMARY_PHOTO VARCHAR(1)",
          "UPDATE PHOTOSET SET LOCK_PRIMARY_PHOTO = 'N'",
          "ALTER TABLE PHOTOSET ALTER COLUMN DESCRIPTION SET DATA TYPE VARCHAR(32000)")),
      entry(3, List.of(
          "ALTER TABLE PHOTOSET ADD COLUMN PRIVACY INTEGER",
          "UPDATE PHOTOSET SET PRIVACY = 0",
          "ALTER TABLE PHOTOSET ADD COLUMN SAFE_SEARCH INTEGER",
          "UPDATE PHOTOSET SET SAFE_SEARCH = 0",
          "ALTER TABLE PHOTOSET ADD COLUMN CONTENT_TYPE INTEGER",
          "UPDATE PHOTOSET SET CONTENT_TYPE = 6",
          "ALTER TABLE PHOTOSET ADD COLUMN MEDIA_TYPE INTEGER",
          "UPDATE PHOTOSET SET MEDIA_TYPE = 0",
          "ALTER TABLE PHOTOSET ADD COLUMN GEOTAGGED INTEGER",
          "UPDATE PHOTOSET SET GEOTAGGED = 0",
          "ALTER TABLE PHOTOSET ADD COLUMN IN_COMMONS VARCHAR(1)",
          "UPDATE PHOTOSET SET IN_COMMONS = 'N'",
          "ALTER TABLE PHOTOSET ADD COLUMN IN_GALLERY VARCHAR(1)",
          "UPDATE PHOTOSET SET IN_GALLERY = 'N'",
          "ALTER TABLE PHOTOSET ADD COLUMN IN_GETTY VARCHAR(1)",
          "UPDATE PHOTOSET SET IN_GETTY = 'N'")),
      entry(4, List.of(
          "ALTER TABLE PHOTOSET ADD COLUMN LIMIT_SIZE VARCHAR(1)",
          "UPDATE PHOTOSET SET LIMIT_SIZE = 'N'",
          "ALTER TABLE PHOTOSET ADD COLUMN SIZE_LIMIT INTEGER",
          "UPDATE PHOTOSET SET SIZE_LIMIT = 0")),
      entry(5, List.of(
          "ALTER TABLE PHOTOSET ADD COLUMN ON_THIS_DAY VARCHAR(1)",
          "UPDATE PHOTOSET SET ON_THIS_DAY = 'N'",
          "ALTER TABLE PHOTOSET ADD COLUMN OTD_MONTH INTEGER",
          "UPDATE PHOTOSET SET OTD_MONTH = 1",
          "ALTER TABLE PHOTOSET ADD COLUMN OTD_DAY INTEGER",
          "UPDATE PHOTOSET SET OTD_DAY = 1",
          "ALTER TABLE PHOTOSET ADD COLUMN OTD_YEAR_START INTEGER",
          "UPDATE PHOTOSET SET OTD_YEAR_START = 1995",
          "ALTER TABLE PHOTOSET ADD COLUMN OTD_YEAR_END INTEGER",
          "UPDATE PHOTOSET SET OTD_YEAR_END = " + SSUtils.getCurrentYear())),
      entry(6, List.of(
          "ALTER TABLE PHOTOSET ADD COLUMN VIDEO_COUNT INTEGER",
          "UPDATE PHOTOSET SET VIDEO_COUNT = 0")),
      entry(7, List.of(
          "ALTER TABLE PHOTOSET ADD COLUMN MACHINE_TAGS VARCHAR(2000)",
          "ALTER TABLE PHOTOSET ADD COLUMN MACHINE_TAG_MATCH_MODE VARCHAR(8)",
          "UPDATE PHOTOSET SET MACHINE_TAG_MATCH_MODE = '" + SSConstants.TAG_MATCH_MODE_NONE + "'")),
      entry(8, List.of(
          "ALTER TABLE PHOTOSET ADD COLUMN TEXT_SEARCH VARCHAR(2000)",
          "ALTER TABLE PHOTOSET ADD COLUMN VIEW_COUNT INTEGER",
          "UPDATE PHOTOSET SET VIEW_COUNT = -1")),
      entry(9, List.of(
          "ALTER TABLE PHOTOSET ADD COLUMN COLOR_CODE VARCHAR(2000)",
          "ALTER TABLE PHOTOSET ADD COLUMN PICTURE_STYLE VARCHAR(2000)",
          "ALTER TABLE PHOTOSET ADD COLUMN ORIENTATION VARCHAR(2000)"))
  );
}
