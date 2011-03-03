/*
 * SuprSetr is Copyright 2010-2011 by Jeremy Brooks
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

import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import net.jeremybrooks.jinx.JinxConstants;
import net.jeremybrooks.jinx.JinxException;
import net.jeremybrooks.jinx.api.PhotosetsApi;
import net.jeremybrooks.jinx.dto.Photo;
import net.jeremybrooks.jinx.dto.Photos;
import net.jeremybrooks.jinx.dto.Photoset;
import net.jeremybrooks.jinx.dto.PhotosetInfo;
import net.jeremybrooks.suprsetr.SSPhotoset;

import org.apache.log4j.Logger;


/**
 * This is a wrapper around the Flickr API library.
 *
 * <p>This wrapper provides access to the photoset methods.</p>
 *
 * <p>This class is implemented as a Singleton. Calling <code>PhotosetHelper.getInstance()</code>
 * will return a reference to the instance of this class.</p>
 *
 * @author jeremyb
 */
public class PhotosetHelper {

    /** Logging. */
    private Logger logger = Logger.getLogger(PhotosetHelper.class);

    /** Reference to the only instance of this class. */
    private static PhotosetHelper instance = null;


    /**
     * Private constructor. This class is a singleton.
     */
    private PhotosetHelper() {
    }


    /**
     * Gets a reference to the only instance of PhotosetHelper.
     *
     * @return reference to the only instance of PhotosetHelper.
     */
    public static PhotosetHelper getInstance() {
	if (PhotosetHelper.instance == null) {
	    PhotosetHelper.instance = new PhotosetHelper();
	}

	return PhotosetHelper.instance;
    }


    /**
     * Get a list of all photosets for the specified user.
     *
     * @param nsid the user ID.
     * @return list of all photosets for the user, or an empty list if the user
     *         does not have any photosets.
     * @throws Exception if there are any errors.
     */
//    public List<Photoset> getPhotosetList(String nsid) throws Exception {
//
//	net.jeremybrooks.jinx.dto.Photosets ps = PhotosetsApi.getInstance().getList(nsid);
//	ps.getPhotosetList();
//	List<Photoset> list = new ArrayList<Photoset>();
//
//	logger.info("Getting photosets for user " + nsid);
//	PhotosetsInterface psi = FlickrHelper.getInstance().getPhotosetsInterface();
//	Photosets psets = psi.getList(nsid);
//
//	for (Object o : psets.getPhotosets()) {
//	    list.add((Photoset) o);
//	}
//
//	return list;
//    }


    /**
     * Get the icon for the specified photoset.
     *
     * <p>This method will get the thumbnail icon from Flickr for the specified
     * photoset, and return it as an ImageIcon instance.</p>
     *
     * @param p the photoset to get the image for.
     * @return thumbnail icon for the photoset.
     * @throws Exception if there are any errors, or if the photoset is null.
     */
    public ImageIcon getIconForPhotoset(Photoset p) throws Exception {
	if (p == null) {
	    throw new Exception("PHOTOSET CANNOT BE NULL.");
	}

	return PhotoHelper.getInstance().getIconForPhoto(p.getPrimary());
    }


    /**
     * Create a new photoset.
     *
     * @param title the title for the new photoset.
     * @param description the description for the new photoset.
     * @param primaryPhotoId the primary photo for the new photoset.
     * @return the new photoset.
     * @throws Exception if there are any errors.
     */
    public Photoset createPhotoset(String title, String description, String primaryPhotoId) throws Exception {
	if (title == null || title.isEmpty()) {
	    throw new Exception("TITLE CANNOT BE NULL OR EMPTY.");
	}
	if (primaryPhotoId == null || primaryPhotoId.isEmpty()) {
	    throw new Exception("PRIMARY PHOTO ID CANNOT BE NULL OR EMPTY.");
	}

//	RequestContext rc = FlickrHelper.getInstance().getRequestContext();
//	PhotosetsInterface pi = FlickrHelper.getInstance().getPhotosetsInterface();
//
//	return pi.create(title, description, primaryPhotoId);

	return PhotosetsApi.getInstance().create(title, description, primaryPhotoId);
    }


    /**
     * Adds the specified photo to the specified photoset.
     *
     * @param photosetId the id of the photoset to add the photo to.
     * @param photoId the id of the photo to add to the photoset.
     * @throws Exception if there are any errors.
     */
    public void addPhoto(String photosetId, String photoId) throws Exception {
//	RequestContext rc = FlickrHelper.getInstance().getRequestContext();
//	PhotosetsInterface pi = FlickrHelper.getInstance().getPhotosetsInterface();
//	pi.addPhoto(photosetId, photoId);
	PhotosetsApi.getInstance().addPhoto(photosetId, photoId);
    }


    /**
     * Get a photoset by ID.
     *
     * @param photosetId the ID of the requested photoset.
     * @return the requested photoset.
     * @throws Exception if there are any errors.
     */
    public PhotosetInfo getPhotosetById(String photosetId) throws Exception {
//	RequestContext rc = FlickrHelper.getInstance().getRequestContext();
//	PhotosetsInterface pi = FlickrHelper.getInstance().getPhotosetsInterface();
//	return pi.getInfo(photosetId);

	return PhotosetsApi.getInstance().getInfo(photosetId);
    }


    /**
     * Delete the specified photoset from Flickr.
     *
     * @param ssPhotoset the photoset to delete.
     * @throws Exception if there are any errors.
     */
    public void delete(SSPhotoset ssPhotoset) throws Exception {
	if (ssPhotoset == null) {
	    throw new Exception("delete: PARAMETER CANNOT BE NULL.");
	}

//	RequestContext rc = FlickrHelper.getInstance().getRequestContext();
//	PhotosetsInterface pi = FlickrHelper.getInstance().getPhotosetsInterface();
//	pi.delete(ssPhotoset.getId());
	PhotosetsApi.getInstance().delete(ssPhotoset.getId());
    }


    /**
     * Add a photo to a photoset.
     *
     * @param ssPhotoset the photoset to add the photo to.
     * @param photo the photo to add.
     * @throws Exception if there are any errors, or if either parameter is null.
     */
    public void addPhoto(SSPhotoset ssPhotoset, Photo photo) throws Exception {
	if (ssPhotoset == null) {
	    throw new Exception("addPhoto: PARAMETER CANNOT BE NULL.");
	}
	if (photo == null) {
	    throw new Exception("addPhoto: PHOTO CANNOT BE NULL.");
	}

//	RequestContext rc = FlickrHelper.getInstance().getRequestContext();
//	PhotosetsInterface pi = FlickrHelper.getInstance().getPhotosetsInterface();

	try {
//	    pi.addPhoto(ssPhotoset.getId(), photo.getId());
	    PhotosetsApi.getInstance().addPhoto(ssPhotoset.getId(), photo.getId());
	    logger.info("Photo " + photo.getTitle() + " added to set " + ssPhotoset.getTitle());
	} catch (JinxException fe) {
	    if (fe.getErrorCode() == 3) {
		logger.info("Photo " + photo.getTitle() + " already in set, not added.");
	    } else {
		logger.warn("Unexpected flickr error", fe);
	    }
	} catch (Exception e) {
	    logger.error("Unexpected error.", e);
	}
    }


    /**
     * Remove the specified photo from the photoset.
     *
     * @param ssPhotoset the photoset to remove the photo from.
     * @param photo the photo to be removed
     * @throws Exception if there are any errors, or if either parameter is null.
     */
    public void removePhoto(SSPhotoset ssPhotoset, Photo photo) throws Exception {
	if (ssPhotoset == null) {
	    throw new Exception("removePhoto: PARAMETER CANNOT BE NULL.");
	}
	if (photo == null) {
	    throw new Exception("removePhoto: PHOTO CANNOT BE NULL.");
	}

//	RequestContext rc = FlickrHelper.getInstance().getRequestContext();
//	PhotosetsInterface pi = FlickrHelper.getInstance().getPhotosetsInterface();
//	pi.removePhoto(ssPhotoset.getId(), photo.getId());

	logger.info("Photo " + photo.getTitle() + " removed from set " + ssPhotoset.getId());
    }


    /**
     * Get the list of photos in a given photoset.
     *
     * <p>Returns a list of Photo ID's representing all the photos in the
     * specified photoset. Flickr returns results in pages, so this method will
     * continue requesting the next page until all results are returned. There is
     * no need for the caller to worry about paging.</p>
     *
     * @param ssPhotoset the photoset to get a list of photos for.
     * @return list of photo ID's in the set.
     * @throws Exception if there are any errors.
     */
    public List<String> getListOfPhotoIdsInSet(SSPhotoset ssPhotoset) throws Exception {
	List<String> list = new ArrayList<String>();
	Photos pList = null;
	int page = 1;
	int count = 0;

	logger.info("Getting photos in set " + ssPhotoset);

//	RequestContext rc = FlickrHelper.getInstance().getRequestContext();
//	PhotosetsInterface pi = FlickrHelper.getInstance().getPhotosetsInterface();

	try {
	    do {
		pList = PhotosetsApi.getInstance().getPhotos(ssPhotoset.getId(), null, JinxConstants.PRIVACY_PRIVATE, 500, page, JinxConstants.MEDIA_PHOTOS, true);
//		pList = pi.getPhotos(ssPhotoset.getId(), 500, page);
		count += pList.getPhotos().size();
		logger.info("Found " + count + " on " + page + " page(s)");
		for (Photo p : pList.getPhotos()) {
		    list.add(p.getId());
		}

		page++;
	    } while (pList.getPhotos().size() == 500);

	} catch (Exception e) {
	    // ignore "set not found" errors when we already have found some
	    // stuff in the set, since this seems to happen when the end of the
	    // set is reached
	    if (e instanceof JinxException) {
		if (((JinxException) e).getErrorCode() == 1) {
		    if (list.isEmpty()) {
			throw e;
		    }
		}
	    }
	}


	return list;
    }


    /**
     * Remove the specified photo from the photoset.
     *
     * @param ssPhotoset the photoset to remove the photo from.
     * @param photoId the ID of the photo to be removed.
     * @throws Exception if there are any errors, or if either parameter is null.
     */
    public void removePhoto(SSPhotoset ssPhotoset, String photoId) throws Exception {
	if (ssPhotoset == null) {
	    throw new Exception("removePhoto: PARAMETER CANNOT BE NULL.");
	}
	if (photoId == null) {
	    throw new Exception("removePhoto: PHOTO CANNOT BE NULL.");
	}

//	RequestContext rc = FlickrHelper.getInstance().getRequestContext();
//	PhotosetsInterface pi = FlickrHelper.getInstance().getPhotosetsInterface();
//	pi.removePhoto(ssPhotoset.getId(), photoId);
	PhotosetsApi.getInstance().removePhoto(ssPhotoset.getId(), photoId);
	logger.info("Photo " + photoId + " removed from set " + ssPhotoset.getId());
    }


    /**
     * Reorder photosets.
     *
     * @param photosetIds
     * @throws Exception
     */
    public void orderSets(List<String> photosetIdList) throws Exception {
	logger.info("Reordering photosets.");
//	RequestContext rc = FlickrHelper.getInstance().getRequestContext();
//	PhotosetsInterface pi = FlickrHelper.getInstance().getPhotosetsInterface();
//	pi.orderSets(photosetIds);

	PhotosetsApi.getInstance().orderSets(photosetIdList);
    }


    /**
     * Edit the title and description of an existing set.
     *
     * @param ssPhotoset the photoset to edit
     * @throws Exception if there are any errors
     */
    public void editMeta(SSPhotoset ssPhotoset) throws Exception {
//	RequestContext rc = FlickrHelper.getInstance().getRequestContext();
//	PhotosetsInterface pi = FlickrHelper.getInstance().getPhotosetsInterface();
//	pi.editMeta(ssPhotoset.getId(), ssPhotoset.getTitle(), ssPhotoset.getDescription());

	PhotosetsApi.getInstance().editMeta(ssPhotoset.getId(), ssPhotoset.getTitle(), ssPhotoset.getDescription());
    }


    /**
     * Edit the photos contained in the photoset.
     *
     * <p>This will replace the photos in the photoset with the photos in
     * the array.</p>
     *
     * @param photosetId the photoset to change.
     * @param photoId the ID of the primary photo.
     * @param photoIds photos that the set should contain.
     * @throws Exception if there are any errors
     */
    public void editPhotos(String photosetId, String photoId, List<Photo> photoList) throws Exception {
	logger.info("Executing editPhotos on photoset " + photosetId
		+ ", using primary photo " + photoId
		+ ", and " + photoList.size() + " photos.");
//	RequestContext rc = FlickrHelper.getInstance().getRequestContext();
//	PhotosetsInterface pi = FlickrHelper.getInstance().getPhotosetsInterface();
//	pi.editPhotos(photosetId, photoId, photoIds);

	List<String> idList = new ArrayList<String>();
	for (Photo p : photoList) {
	    idList.add(p.getId());
	}

	PhotosetsApi.getInstance().editPhotos(photosetId, photoId, idList);
    }

}
