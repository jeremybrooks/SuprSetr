/*
 * SuprSetr is Copyright 2010-2014 by Jeremy Brooks
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

package net.jeremybrooks.suprsetr.flickr;

import net.jeremybrooks.jinx.Jinx;
import net.jeremybrooks.jinx.OAuthAccessToken;
import net.jeremybrooks.jinx.api.OAuthApi;
import net.jeremybrooks.jinx.api.PhotosApi;
import net.jeremybrooks.jinx.api.PhotosetsApi;
import net.jeremybrooks.jinx.logger.JinxLogger;
import net.jeremybrooks.jinx.logger.LogInterface;
import net.jeremybrooks.jinx.response.photos.Photo;
import org.apache.log4j.Logger;

/**
 * @author Jeremy Brooks
 */
public class JinxFactory {

	private static Jinx jinx;
	private static JinxFactory instance;
	private PhotosApi photosApi;
	private OAuthApi oAuthApi;
	private PhotosetsApi photosetsApi;

	private Logger logger = Logger.getLogger(JinxFactory.class);

	private JinxFactory() {
	}

	public static JinxFactory getInstance() {
		if (instance == null) {
			instance = new JinxFactory();
		}
		return instance;
	}

	public void init(String flickrKey, String flickrSecret) {
		jinx = new Jinx(flickrKey, flickrSecret);
		logger.info("JinxFactory initiated with key and secret.");
	}

	public void setAccessToken(OAuthAccessToken token) {
		jinx.setoAuthAccessToken(token);
	}


    public void setLogger(LogInterface jinxLogger) {
        JinxLogger.setLogger(jinxLogger);
        jinx.setVerboseLogging(jinxLogger != null);
    }

	public OAuthApi getoAuthApi() {
		if (oAuthApi == null) {
			oAuthApi = new OAuthApi(jinx);
		}
		return oAuthApi;
	}

	public PhotosApi getPhotosApi() {
		if (photosApi == null) {
			photosApi = new PhotosApi(jinx);
		}
		return photosApi;
	}

	public PhotosetsApi getPhotosetsApi() {
		if (photosetsApi == null) {
			photosetsApi = new PhotosetsApi(jinx);
		}
		return photosetsApi;
	}

	/**
	 * Build the photo page URL for this photo.
	 * <p/>
	 * Photo URL's are in the format
	 * http://www.flickr.com/photos/{user-id}/{photo-id}
	 *
	 * @param photo photo to build the URL for.
	 * @return string representation of the photo page URL.
	 */
	public String buildUrlForPhoto(Photo photo) {
		StringBuilder sb = new StringBuilder("http://www.flickr.com/photos/");
		sb.append(jinx.getoAuthAccessToken().getNsid()).append('/').append(photo.getPhotoId());
		return sb.toString();
	}
}
