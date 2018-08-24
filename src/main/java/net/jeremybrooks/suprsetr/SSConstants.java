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

package net.jeremybrooks.suprsetr;

/**
 * Constants for SuprSetr.
 *
 * <p>These should be public static final.</p>
 *
 * @author Jeremy Brooks
 */
public class SSConstants {


  public static final String KEY_TOKEN = "token";
  public static final String KEY_NSID = "nsid";
  public static final String KEY_REALNAME = "realname";
  public static final String KEY_USERNAME = "username";
  public static final String KEY_ICONFARM = "iconfarm";
  public static final String KEY_ICONSERVER = "iconserver";
  public static final String KEY_PERMISSION = "permission";

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

  public static final String DEFAULT_TWEET_TEMPLATE =
      "There are %c new photos in my Flickr photoset %u (%t).";

  public static final String DEFAULT_TWEET_CREATE_TEMPLATE =
      "Check out my new photoset '%t' on Flickr: %u.";

  public static final String VIA_TWEET = " (via @suprsetr)";

  public static final String DEFAULT_REFRESH_WAIT = "24";
  public static final String DEFAULT_FAVRTAGR_INTERVAL = "10";

  public static final String VERSION_URL = "http://www.jeremybrooks.net/suprsetr/VERSION";
  public static final String DOWNLOAD_URL = "http://www.jeremybrooks.net/suprsetr/download.html";

  public static final String ADD_MANAGED = "This set is managed by <a href=\"http://www.jeremybrooks.net/suprsetr\">SuprSetr</a>";

  public static final int DATABASE_SCHEMA_CURRENT_VERSION = 9;
}
