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

package net.jeremybrooks.suprsetr.twitter;

import com.rosaloves.bitlyj.Url;
import net.jeremybrooks.suprsetr.Main;
import net.jeremybrooks.suprsetr.SSConstants;
import net.jeremybrooks.suprsetr.dao.DAOHelper;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import org.apache.log4j.Logger;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import javax.swing.JOptionPane;
import java.util.ResourceBundle;

import static com.rosaloves.bitlyj.Bitly.as;
import static com.rosaloves.bitlyj.Bitly.shorten;


/**
 * This is a wrapper around the Twitter API library.
 *
 * @author jeremyb
 */
public class TwitterHelper {

	/**
	 * Logging.
	 */
	private static Logger logger = Logger.getLogger(TwitterHelper.class);

	/**
	 * Authenticates the user.
	 * <p/>
	 * <p>This method will use the OAuth method of authentication. The user's
	 * browser will be opened, and they will be prompted for the PIN. The
	 * authentication token data will then be saved in the database.</p>
	 *
	 * @throws Exception if there are any errors.
	 */
	public static void authenticate() throws Exception {
		ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.misc");
		// The factory instance is re-useable and thread safe.
		Twitter twitter = TwitterFactory.getSingleton();
		twitter.setOAuthConsumer(Main.getPrivateProperty("TWITTER_CONSUMER_KEY"), Main.getPrivateProperty("TWITTER_CONSUMER_SECRET"));
		RequestToken requestToken = twitter.getOAuthRequestToken();
		AccessToken accessToken;
//		    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//		    while (null == accessToken) {
		String url = requestToken.getAuthenticationURL();
		java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
//		BrowserLauncher.openURL(url);
//				BrowserLauncher.openURL(requestToken.getAuthorizationURL());

		String pin = JOptionPane.showInputDialog(null,
				resourceBundle.getString("TwitterHelper.dialog.pin.message"),
				resourceBundle.getString("TwitterHelper.dialog.pin.title"),
				JOptionPane.INFORMATION_MESSAGE);
		accessToken = twitter.getOAuthAccessToken(requestToken, pin);


//		      System.out.println("Open the following URL and grant access to your account:");
//		      System.out.println(requestToken.getAuthorizationURL());
//		      System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
//		      String pin = br.readLine();
//		      try{
//		         if(pin.length() > 0){
//		           accessToken = twitter.getOAuthAccessToken(requestToken, pin);
//		         }else{
//		           accessToken = twitter.getOAuthAccessToken();
//		         }
//		      } catch (TwitterException te) {
//		        if(401 == te.getStatusCode()){
//		          System.out.println("Unable to get the access token.");
//		        }else{
//		          te.printStackTrace();
//		        }
//		      }
//		    }
		//persist to the accessToken for future reference.
//		    storeAccessToken(twitter.verifyCredentials().getId() , accessToken);
//		    Status status = twitter.updateStatus(args[0]);
//		    System.out.println("Successfully updated the status to [" + status.getText() + "].");
//	Twitter twitter = new Twitter();
//	twitter.setOAuthConsumer(Main.getPrivateProperty("TWITTER_CONSUMER_KEY"), Main.getPrivateProperty("TWITTER_CONSUMER_SECRET"));
//	RequestToken requestToken = twitter.getOAuthRequestToken();
//	AccessToken accessToken = null;
//
//
//	BrowserLauncher.openURL(requestToken.getAuthorizationURL());
//
//	// Open input dialog for user to enter PIN from Twitter
//	String pin = JOptionPane.showInputDialog(null,
//		"After authorizing Twitter, enter the PIN here:",
//		"PIN",
//		JOptionPane.INFORMATION_MESSAGE);
//
//	accessToken = twitter.getOAuthAccessToken(requestToken, pin);


		// Save the access token
		LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_TWITTER_USERID, Long.toString(twitter.verifyCredentials().getId()));
		LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_TWITTER_USERNAME, twitter.verifyCredentials().getName());
		LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_TWITTER_TOKEN, accessToken.getToken());
		LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_TWITTER_TOKEN_SECRET, accessToken.getTokenSecret());

	}


	/**
	 * Update the user status.
	 * <p/>
	 * <p>The user must be authenticated.</p>
	 *
	 * @param tweet the status.
	 * @throws Exception if there are any errors.
	 */
	public static void updateStatus(String tweet) throws Exception {
		if (TwitterHelper.isAuthorized()) {
			Twitter twitter = TwitterFactory.getSingleton();
			Status status = twitter.updateStatus(tweet);
//		    System.out.println("Successfully updated the status to [" + status.getText() + "].");
//	    String token = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TWITTER_TOKEN);
//	    String tokenSecret = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TWITTER_TOKEN_SECRET);

// TODO
//	    Twitter twitter = new Twitter();
//	    twitter.setOAuthConsumer(Main.getPrivateProperty("TWITTER_CONSUMER_KEY"), Main.getPrivateProperty("TWITTER_CONSUMER_SECRET"));
//	    twitter.setOAuthAccessToken(token, tokenSecret);

//	    twitter.updateStatus(tweet);
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
	}


	/**
	 * Determine if the user has been authorized.
	 *
	 * @return true if the user is authorized, false otherwise.
	 */
	public static boolean isAuthorized() {
		boolean authorized = true;

		String token = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TWITTER_TOKEN);

		if (token == null || token.isEmpty()) {
			authorized = false;
		}

		return authorized;
	}


	/**
	 * Build a tweet based on the template and photoset data.
	 * <p/>
	 * <p>The user can define a template for their tweets. This method takes
	 * that template, performs token substitution on it, adds the vanity
	 * "via @SuprSetr" to the end (if there is room), and trims the tweet to
	 * 140 characters.</p>
	 * <p/>
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
	 *         trimmed to 140 characters.
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

		// Now shorten the URL
		if (url != null) {
			try {

				Url bitlyUrl = as(Main.getPrivateProperty("BITLY_USERNAME"),
						Main.getPrivateProperty("BITLY_API_KEY")).call(shorten(url));

				template = template.replace("%u", bitlyUrl.getShortUrl());
			} catch (Exception e) {
				logger.warn("ERROR WHILE SHORTENING URL.", e);
			}
		}

		// get rid of any whitespace at ends, since we now are going to do some
		// operations that care about the length of the string
		template = template.trim();

		// Add "via suprsetr" if user allows it and tweet is short enough
		if (DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_ADD_VIA))) {
			if (template.length() + SSConstants.VIA_TWEET.length() <= 140) {
				template += SSConstants.VIA_TWEET;
			}
		}

		if (template.length() > 140) {
			logger.info("Tweet is " + template.length() +
					" characters, truncating to 140. (" + template + ")");
			template = template.substring(0, 140);
		}

		return template;
	}
}
