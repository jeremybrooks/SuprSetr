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

package net.jeremybrooks.suprsetr.flickr;


import com.github.scribejava.core.model.OAuth1RequestToken;
import net.jeremybrooks.jinx.OAuthAccessToken;
import net.jeremybrooks.jinx.api.OAuthApi;
import net.jeremybrooks.suprsetr.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;


/**
 * This is a wrapper around the Flickr API library.
 *
 * <p>This wrapper provides access to the authentication methods, the API
 * interfaces, and metadata about the user.</p>
 *
 * <p>This class is implemented as a Singleton. Calling {@code FlickrHelper.getInstance()}
 * will return a reference to the instance of this class. The initialize
 * method must be called once before other methods are called.</p>
 *
 * @author Jeremy Brooks
 */
public class FlickrHelper {
  private static Logger logger = LogManager.getLogger(FlickrHelper.class);
  private static FlickrHelper instance = null;

  /* File that holds auth token info (old style auth). */
  private File tokenFile;

  /* File that holds oauth token info. */
  private File oauthTokenFile;

  private OAuthAccessToken oAuthAccessToken = null;

  private OAuth1RequestToken tempToken = null;

  /* Private constructor. This class is a Singleton. */
  private FlickrHelper() {
    this.oauthTokenFile = new File(Main.configDir, "jinx_oauth.token");
    this.tokenFile = new File(Main.configDir, "jinx.token");
  }


  /**
   * Get a reference to the only instance of this class.
   *
   * @return reference to FlickrHelper instance.
   */
  public static FlickrHelper getInstance() {
    if (instance == null) {
      instance = new FlickrHelper();
    }
    return instance;
  }


  /**
   * Authorize the user.
   *
   * <p>Authorization data is loaded from the token file if available. If the
   * authorization data is not available, this method returns false.</p>
   *
   * @return true if user is authorized.
   */
  public boolean authorize() {
    boolean success = false;
    // try loading oauth token
    if (this.oauthTokenFile.exists()) {
      logger.info("Loading oauth token from " + this.oauthTokenFile.getAbsolutePath());
      oAuthAccessToken = new OAuthAccessToken();
      try (InputStream in = new FileInputStream(oauthTokenFile)) {
        oAuthAccessToken.load(in);
        JinxFactory.getInstance().setAccessToken(oAuthAccessToken);
        success = true;
      } catch (Exception e) {
        logger.warn("Unable to load oauth access token from file.", e);
      }
    } else if (this.tokenFile.exists()) {
      logger.info("Loading legacy auth token from " + this.tokenFile.getAbsolutePath());
      try (InputStream in = new FileInputStream(tokenFile)) {
        logger.info("Converting legacy auth token to oauth token.");
        OAuthApi oauth = JinxFactory.getInstance().getoAuthApi();
        oAuthAccessToken = oauth.getAccessToken(in);
        JinxFactory.getInstance().setAccessToken(oAuthAccessToken);
        try (OutputStream out = new FileOutputStream(oauthTokenFile)) {
          oAuthAccessToken.store(out);
          success = true;
        }
      } catch (Exception e) {
        logger.warn("Unable to load legacy auth token from file.", e);
      } finally {
        if (this.tokenFile.delete()) {
          logger.info("Legacy auth token deleted.");
        } else {
          logger.warn("Unable to delete old legacy auth token file.");
        }
      }
    }

    return success;
  }


  /**
   * Get the username of the currently authorized user.
   *
   * @return username of the currently authorized user.
   */
  public String getUsername() {
    return this.oAuthAccessToken == null ? null : this.oAuthAccessToken.getUsername();
  }


  /**
   * Get the NSID of the currently authorized user.
   *
   * @return NSID of the currently authorized user.
   */
  public String getNSID() {
    return this.oAuthAccessToken == null ? null : this.oAuthAccessToken.getNsid();
  }


  /**
   * Get the authentication URL.
   *
   * @return authentication URL.
   * @throws Exception if there are any errors.
   */
  public URL getAuthenticationURL() throws Exception {
    tempToken = JinxFactory.getInstance().getRequestToken();
    String url = JinxFactory.getInstance().getAuthenticationUrl(tempToken);
    return new URL(url);
  }


  /**
   * Complete authentication.
   *
   * @param verificationCode oauth verification code.
   * @throws Exception if there are any errors.
   */
  public void completeAuthentication(String verificationCode) throws Exception {
    this.oAuthAccessToken = JinxFactory.getInstance().getAccessToken(tempToken, verificationCode);
    JinxFactory.getInstance().setAccessToken(this.oAuthAccessToken);
    try (OutputStream out = new FileOutputStream(oauthTokenFile)) {
      this.oAuthAccessToken.store(out);
    }
  }


  /**
   * Delete all stored authorization data.
   */
  public void deauthorize() {
    if (this.tokenFile.exists()) {
      if (this.tokenFile.delete()) {
        logger.info("Authorization token deleted.");
      } else {
        logger.warn("Could not delete the legacy authorization token.");
      }
    }
    if (this.oauthTokenFile.exists()) {
      if (this.oauthTokenFile.delete()) {
        logger.info("Oauth token deleted.");
      } else {
        logger.warn("Could not delete the oauth token.");
      }
    }
  }
}
