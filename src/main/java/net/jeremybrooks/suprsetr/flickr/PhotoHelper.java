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

package net.jeremybrooks.suprsetr.flickr;

import net.jeremybrooks.jinx.JinxConstants;
import net.jeremybrooks.jinx.PhotoUtils;
import net.jeremybrooks.jinx.response.Response;
import net.jeremybrooks.jinx.response.photos.AddTags;
import net.jeremybrooks.jinx.response.photos.Favorites;
import net.jeremybrooks.jinx.response.photos.Photo;
import net.jeremybrooks.jinx.response.photos.PhotoInfo;
import net.jeremybrooks.jinx.response.photos.Photos;
import net.jeremybrooks.jinx.response.photos.SearchParameters;
import org.apache.log4j.Logger;

import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;


/**
 * This is a wrapper around the Flickr API library.
 * <p/>
 * <p>This wrapper provides access to the photo methods.</p>
 * <p/>
 * <p>This class is implemented as a Singleton. Calling <code>PhotoHelper.getInstance()</code>
 * will return a reference to the instance of this class.</p>
 *
 * @author jeremyb
 */
public class PhotoHelper {

	/**
	 * Logging.
	 */
	private static Logger logger = Logger.getLogger(PhotoHelper.class);

	/**
	 * Reference to the only instance of this class.
	 */
	private static PhotoHelper instance = null;


	/**
	 * Private constructor. This class is a Singleton.
	 */
	private PhotoHelper() {
	}


	/**
	 * Gets a reference to the only instance of this class.
	 *
	 * @return reference to the only instance of PhotoHelper.
	 */
	public static PhotoHelper getInstance() {
		if (PhotoHelper.instance == null) {
			PhotoHelper.instance = new PhotoHelper();
		}

		return PhotoHelper.instance;
	}


	/**
	 * Get a list of photos matching the search parameters.
	 * <p/>
	 * <p>The search will be executed, and all results will be returned. Flickr
	 * returns results in pages, so this method will continue requesting the
	 * next page until all results have been processed. There is no need for the
	 * caller to worry about paging.</p>
	 * <p/>
	 * <p>This method can take a long time to execute if the search results
	 * return a large number of photos.</p>
	 *
	 * @param params search parameters.
	 * @return list of photo objects matching the search, or an empty list if
	 * there were no matches.
	 * @throws Exception if there are any errors.
	 */
	public List<Photo> getPhotos(SearchParameters params) throws
			Exception {
		return getPhotos(params, Integer.MAX_VALUE);
	}


	/**
	 * Get a list of photos matching the search parameters, limiting the
	 * number of returned results.
	 * <p/>
	 * <p>The search will be executed, and all results will be returned. Flickr
	 * returns results in pages, so this method will continue requesting the
	 * next page until all results have been processed. There is no need for the
	 * caller to worry about paging.</p>
	 * <p/>
	 * <p>This method can take a long time to execute if the search results
	 * return a large number of photos.</p>
	 *
	 * @param params search parameters.
	 * @param max    the maximum number of results that will e returned.
	 * @return list of photo objects matching the search, or an empty list if
	 * there were no matches.
	 * @throws Exception if there are any errors.
	 */
	public List<Photo> getPhotos(SearchParameters params, int max) throws Exception {
		int page = 1;
		int count = 0;
		int perPage = 500;

		if (max < 500) {
			perPage = max;
		}

		if (params == null) {
			throw new Exception("Cannot get photos for a null search parameter.");
		}

		List<Photo> photoList = new ArrayList<Photo>();

//	Photos photos = null;

		// return max allowed results
		params.setPerPage(perPage);

		Photos results;

		do {

			results = JinxFactory.getInstance().getPhotosApi().search(params);
//	    results = PhotosApi.getInstance().search(params);

			// Remove photos that have tags matching the "-" matches
//			removeNegativeMatches(results, params);

			// The first search results object becomes the object that is
			// returned. The "photos" parameter of the returned object will be
			// updated with the list of all found photos before the object is
			// returned to the caller. We use the first returned search results
			// because it will have valid parameters, such as total, that can
			// change in the case where the exact number of search results
			// falls on a page boundary, and the last call returns no results.
//	    if (photos == null) {
//		photos = results;
//	    }

            count += results.getPhotoList().size();
			logger.info("Found " + count + " on " + page + " page(s)");
			photoList.addAll(results.getPhotoList());
			page++;

			params.setPage(page);

		} while (page <= results.getPages() && count < max);

		// if we have more results than requested, trim the list
		if (count > max) {
			photoList.subList(max, photoList.size()).clear();
			logger.debug(count + " search results trimmed to " + photoList.size());
		}


//	photos.setPhotos(list);
//	photos.setTotal(list.size());

		return photoList;
	}

//	private void removeNegativeMatches(Photos results, SearchParameters params) {
//		List<Photo> removeMe = new ArrayList<Photo>();
//
//		if (params instanceof SSSearch) {
//			List<String> negativeTagMatch = ((SSSearch) params).getNegativeTagMatch();
//			for (Photo p : results.getPhotos()) {
//				StringTokenizer tok = new StringTokenizer(p.getTags());
//				while (tok.hasMoreTokens()) {
//					String tag = tok.nextToken();
//					if (negativeTagMatch.contains(tag)) {
//						removeMe.add(p);
//						break;
//					}
//				}
//
//			}
//		}
//
//		for (Photo p : removeMe) {
//			results.getPhotos().remove(p);
//			logger.info("Removed '" + p.getTitle() + "' from search results.");
//		}
//	}


	/**
	 * Get the icon for the specified photo.
	 * <p/>
	 * <p>This method will get the thumbnail icon from Flickr for the specified
	 * photo, and return it as an ImageIcon instance.</p>
	 *
	 * @param photoId photo to get the icon for.
	 * @return the thumbnail image for the photo, or the default empty set icon
	 * if the photo cannot be processed for some reason.
	 * @throws Exception if the parameter is null or empty, or if there are other errors.
	 */
	public ImageIcon getIconForPhoto(String photoId) throws Exception {
		PhotoInfo info = JinxFactory.getInstance().getPhotosApi().getInfo(photoId, null);
		if (info.getCode() > 0) {
			throw new Exception("Error getting info for photo " + photoId + ". Code " + info.getCode() + ":" + info.getMessage());
		}
//	PhotoInfo info = PhotosApi.getInstance().getInfo(id, null, true);
		ImageIcon icon;
		try {
			icon = new ImageIcon(PhotoUtils.getImageForSize(JinxConstants.PhotoSize.SIZE_SMALL_SQUARE, info));
//	    icon = new ImageIcon(info.getImageForSize(JinxConstants.SIZE_SMALL_SQUARE));
		} catch (Exception e) {
			logger.warn("ERROR GETTING ICON FOR PHOTO " + photoId, e);
			icon = new ImageIcon(this.getClass().getClassLoader().getResource("images/empty_set_icon.png"));
		}

		return icon;
	}


	/**
	 * Get the number of people who have favorited a given photo.
	 * <p/>
	 * <p>The maximum number of people returned by the Flickr getFavorites API
	 * is 50, so we ask for 50 per page here. This method will contine requesting
	 * the next page from flickr until all results are returned. There is no
	 * need for the caller to worry about paging.</p>
	 *
	 * @param p the photo to get a favorite count for.
	 * @return number of faves on a photo.
	 * @throws Exception if there are any errors or if the photo is null.
	 */
	public int getFavoriteCount(Photo p) throws Exception {
		if (p == null) {
			throw new Exception("Photo parameter cannot be null.");
		}
		Favorites favorites = JinxFactory.getInstance().getPhotosApi().getFavorites(p.getPhotoId(), 0, 0);
		return favorites.getTotal() == null ? 0 : favorites.getTotal();
//	return PhotosApi.getInstance().getFavorites(p.getId(), 0, 0, true).getTotal();
	}


	/**
	 * Add the specified tags to the specified photo.
	 *
	 * @param p    the photo to add tags to.
	 * @param tags array of tags to add.
	 * @throws Exception if there are any errors.
	 */
	public void addTags(Photo p, String[] tags) throws Exception {

//	StringBuilder sb = new StringBuilder();
//	for (String tag : tags) {
//	    sb.append(tag).append(',');
//	}
//	if (sb.length() > 0) {
//	    if (sb.charAt(sb.length() - 1) == ',') {
//		sb.deleteCharAt(sb.length() - 1);
//	    }
//	}
		List<String> tagList = new ArrayList<String>();
		for (String tag : tags) {
			tagList.add(tag);
		}
//	PhotosApi.getInstance().addTags(p.getId(), sb.toString());
		AddTags addTags = JinxFactory.getInstance().getPhotosApi().addTags(p.getPhotoId(), tagList);
		if (addTags.getCode() != 0) {
			throw new Exception("Error adding tags. Code " + addTags.getCode() + ":" + addTags.getMessage());
		}
	}


	public void removeTag(String tagId) throws Exception {
//		PhotosApi.getInstance().removeTag(tagId);
		Response response = JinxFactory.getInstance().getPhotosApi().removeTag(tagId);
		if (response.getCode() != 0) {
			throw new Exception("Unable to remove tagId " + tagId + ". Code " + response.getCode() + ":" + response.getMessage());
		}
	}


	public PhotoInfo getPhotoInfo(Photo p) throws Exception {
//		return PhotosApi.getInstance().getInfo(p.getId(), p.getSecret(), true);
		return JinxFactory.getInstance().getPhotosApi().getInfo(p.getPhotoId(), p.getSecret());
	}

}
