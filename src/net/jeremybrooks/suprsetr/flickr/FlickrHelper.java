/*
 * SuprSetr is Copyright 2010 by Jeremy Brooks
 *
 * This file is part of SuprSetr.
 *
 *  SuprSetr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  SuprSetr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with SuprSetr.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.jeremybrooks.suprsetr.flickr;

//import com.aetrion.flickr.photos.PhotosInterface;
//import com.aetrion.flickr.photosets.PhotosetsInterface;
import java.io.File;
import java.net.URL;
import net.jeremybrooks.jinx.Jinx;
import net.jeremybrooks.jinx.JinxConstants;
import net.jeremybrooks.jinx.api.AuthApi;
import net.jeremybrooks.jinx.dto.Frob;
import net.jeremybrooks.jinx.dto.Token;
import net.jeremybrooks.suprsetr.Main;
import org.apache.log4j.Logger;


/**
 * This is a wrapper around the Flickr API library.
 *
 * <p>This wrapper provides access to the authentication methods, the API
 * interfaces, and metadata about the user.</p>
 *
 * <p>This class is implemented as a Singleton. Calling <code>FlickrHelper.getInstance()</code>
 * will return a reference to the instance of this class. The initialize
 * method must be called once before other methods are called.</p>
 *
 * @author jeremyb
 */
public class FlickrHelper {

    
    /** Logging. */
    private static Logger logger = Logger.getLogger(FlickrHelper.class);

    /** Reference to the only instance of this class. */
    private static FlickrHelper instance = null;

    /** File that holds auth token info. */
    private File tokenFile = null;

    /** The frob. */
    private Frob frob = null;

    /** The auth token object. */
    private Token token = null;


    /**
     * Private constructor. This class is a Singleton.
     */
    private FlickrHelper() {
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

	// first, try loading the jinx auth token
	if (this.tokenFile.exists()) {
	    this.token = new Token();
	    try {
		this.token.load(this.tokenFile);
		Jinx.getInstance().setToken(this.token);

	    } catch (Exception ex) {
		logger.warn("Unable to load auth token from file.", ex);
	    }
	    success = true;
	}

	return success;
    }


    /**
     * Get the username of the currently authorized user.
     *
     * @return username of the currently authorized user.
     */
    public String getUsername() {
	String username = null;
	try {
	    username = this.token.getUsername();
	} catch (Exception e) {
	    // ignore; will return null
	}
	return username;
    }


    /**
     * Get the NSID of the currently authorized user.
     *
     * @return NSID of the currently authorized user.
     */
    public String getNSID() {
	String nsid = null;
	try {
	    nsid = this.token.getNsid();
	} catch (Exception e) {
	    // ignore; will return null
	}
	return nsid;
    }


    /**
     * Get the authentication URL.
     *
     * @return authentication URL.
     * @throws Exception if there are any errors.
     */
    public URL getAuthenticationURL() throws Exception {
	this.frob = AuthApi.getInstance().getFrob(JinxConstants.PERMS_WRITE);
	return new URL(this.frob.getLoginUrl());
    }


    /**
     * Complete authentication.
     *
     * <p>This method will use the frob to get an authorization token, and then
     * store that authorization token in the auth store.</p>
     *
     * @throws Exception if there are any errors.
     */
    public void completeAuthentication() throws Exception {
	this.token = AuthApi.getInstance().getToken(this.frob);
	Jinx.getInstance().setToken(this.token);
	this.token.store(this.tokenFile);
    }


    /**
     * Delete all stored authorization data.
     */
    public void deauthorize() {
	if (this.tokenFile.exists()) {
	    if (this.tokenFile.delete()) {
		logger.info("Authorization token deleted.");
	    } else {
		logger.warn("Could not delete the authorization token.");
	    }
	}
    }
}
