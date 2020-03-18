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

package net.jeremybrooks.suprsetr.twitter;

import net.jeremybrooks.suprsetr.Main;
import net.jeremybrooks.suprsetr.SSConstants;
import net.jeremybrooks.suprsetr.dao.DAOHelper;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import javax.swing.JOptionPane;
import java.util.ResourceBundle;


/**
 * This is a wrapper around the Twitter API library.
 *
 * @author Jeremy Brooks
 */
public class TwitterHelper {

  /**
   * Logging.
   */
  private static Logger logger = LogManager.getLogger(TwitterHelper.class);

  private static TwitterFactory twitterFactory = new TwitterFactory();
  private static Twitter twitter;

  /**
   * Authenticates the user.
   *
   * <p>This method will use the OAuth method of authentication. The user's
   * browser will be opened, and they will be prompted for the PIN. The
   * authentication token data will then be saved in the database.</p>
   *
   * @throws Exception if there are any errors.
   */
  public static void authenticate() throws Exception {
    ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.misc");
    twitter = TwitterFactory.getSingleton();
    twitter.setOAuthConsumer(Main.getPrivateProperty("TWITTER_CONSUMER_KEY"), Main.getPrivateProperty("TWITTER_CONSUMER_SECRET"));
    try {
      RequestToken requestToken = twitter.getOAuthRequestToken();
      AccessToken accessToken;
      String url = requestToken.getAuthenticationURL();
      java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));

      String pin = JOptionPane.showInputDialog(null,
          resourceBundle.getString("TwitterHelper.dialog.pin.message"),
          resourceBundle.getString("TwitterHelper.dialog.pin.title"),
          JOptionPane.INFORMATION_MESSAGE);
      accessToken = twitter.getOAuthAccessToken(requestToken, pin);
      // Save the access token
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_TWITTER_USERID, Long.toString(accessToken.getUserId()));
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_TWITTER_USERNAME, twitter.verifyCredentials().getName());
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_TWITTER_TOKEN, accessToken.getToken());
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_TWITTER_TOKEN_SECRET, accessToken.getTokenSecret());
    } catch (Exception e) {
      twitter = null;
      throw e;
    }
  }


  /**
   * Update the user status.
   *
   * <p>The user must be authenticated.</p>
   *
   * @param tweet the status.
   * @throws Exception if there are any errors.
   */
  public static void updateStatus(String tweet) throws Exception {
    if (TwitterHelper.isAuthorized()) {
      Status status = twitter.updateStatus(tweet);
      logger.info("Updated status to [" + status.getText() + "]");
    }
  }


  /**
   * Delete authentication token data from the database.
   */
  public static void logout() {
    LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_TWITTER_USERID, null);
    LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_TWITTER_USERNAME, null);
    LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_TWITTER_TOKEN, null);
    LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_TWITTER_TOKEN_SECRET, null);
    twitter = null;
  }


  /**
   * Determine if the user has been authorized.
   * If the user is authorized, ensure that the Twitter object exists and has the required OAuth token.
   *
   * @return true if the user is authorized, false otherwise.
   */
  public static boolean isAuthorized() {
    boolean authorized = true;

    String token = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TWITTER_TOKEN);

    if (token == null || token.isEmpty()) {
      authorized = false;
    } else {
      if (twitter == null) {
        AccessToken accessToken = new AccessToken(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TWITTER_TOKEN),
            LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TWITTER_TOKEN_SECRET));
        twitter = twitterFactory.getInstance();
        twitter.setOAuthConsumer(Main.getPrivateProperty("TWITTER_CONSUMER_KEY"), Main.getPrivateProperty("TWITTER_CONSUMER_SECRET"));
        twitter.setOAuthAccessToken(accessToken);
      }
    }

    return authorized;
  }


  /**
   * Build a tweet based on the template and photoset data.
   *
   * <p>The user can define a template for their tweets. This method takes
   * that template, performs token substitution on it, adds the vanity
   * "via @SuprSetr" to the end (if there is room), and trims the tweet to
   * 140 characters.</p>
   *
   * <p>In addition, if the URL is not null, it will be shortened using
   * bit.ly and the shortened URL will be substituted for the %u token in the
   * template.</p>
   *
   * @param template the tweet template defined by the user.
   * @param title    the title to substitute for the %t token.
   * @param url      the url to substitute for the %u token.
   * @param count    the count to substitute for the %c token.
   * @param total    the total count to substitute for the %C token.
   * @return the tweet text with all substitution and URL shortening done, and
   * trimmed to 140 characters.
   * @throws Exception if there are any errors.
   */
  public static String buildTweet(String template, String title, String url, int count, int total) throws Exception {
    if (template == null || template.isEmpty()) {
      throw new Exception("Cannot build tweet from empty template.");
    }
    if (title == null) {
      title = "";
    }


    // First, replace the title and counts in the template
    template = template.replace("%t", title);
    template = template.replace("%c", Integer.toString(count));
    template = template.replace("%C", Integer.toString(total));

    // calculate length. URL's are wrapped with t.co URL's which are 22 characters
    int tweetLength = template.trim().length();
    if (template.contains("%u")) {
      tweetLength += 20;
    }
    // Add "via suprsetr" if user allows it and tweet is short enough
    if (DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_ADD_VIA))) {
      if (tweetLength + SSConstants.VIA_TWEET.length() <= 140) {
        template += SSConstants.VIA_TWEET;
      }
    }

    // Replace URL; we let Twitter do the shortening for us
    if (url != null) {
      template = template.replace("%u", url);
    }

    if (tweetLength > 140) {
      logger.info("Tweet is " + template.length() +
          " characters, truncating to 140. (" + template + ")");
      template = template.substring(0, 140);
    }

    return template;
  }

  public static int calculateTweetLength(String template, String title, int count, int total) {
    if (template == null || template.isEmpty()) {
      return 0;
    }
    if (title == null) {
      title = "";
    }

    // First, replace the title and counts in the template
    template = template.replace("%t", title);
    template = template.replace("%c", Integer.toString(count));
    template = template.replace("%C", Integer.toString(total));

    // calculate length. URL's are wrapped with t.co URL's which are 22 characters
    int tweetLength = template.trim().length();
    if (template.contains("%u")) {
      tweetLength += 20;
    }
    // Add "via suprsetr" if user allows it and tweet is short enough
    if (DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_ADD_VIA))) {
      if (tweetLength + SSConstants.VIA_TWEET.length() <= 140) {
        tweetLength += SSConstants.VIA_TWEET.length();
      }
    }
    return tweetLength;
  }
}
