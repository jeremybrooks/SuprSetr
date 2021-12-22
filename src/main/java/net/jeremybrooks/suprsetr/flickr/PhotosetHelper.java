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

package net.jeremybrooks.suprsetr.flickr;

import net.jeremybrooks.jinx.JinxConstants;
import net.jeremybrooks.jinx.JinxException;
import net.jeremybrooks.jinx.response.Response;
import net.jeremybrooks.jinx.response.photos.Photo;
import net.jeremybrooks.jinx.response.photosets.Photoset;
import net.jeremybrooks.jinx.response.photosets.PhotosetInfo;
import net.jeremybrooks.jinx.response.photosets.PhotosetList;
import net.jeremybrooks.jinx.response.photosets.PhotosetPhotos;
import net.jeremybrooks.suprsetr.SSPhotoset;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.List;


/**
 * This is a wrapper around the Flickr API library.
 *
 * <p>This wrapper provides access to the photoset methods.</p>
 *
 * <p>This class is implemented as a Singleton. Calling {@code PhotosetHelper.getInstance()}
 * will return a reference to the instance of this class.</p>
 *
 * @author Jeremy Brooks
 */
public class PhotosetHelper {

  /**
   * Logging.
   */
  private Logger logger = LogManager.getLogger(PhotosetHelper.class);

  /**
   * Reference to the only instance of this class.
   */
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
   * does not have any photosets.
   * @throws Exception if there are any errors.
   */
  public List<Photoset> getPhotosets(String nsid) throws Exception {
    // get the first batch of results
    PhotosetList photosetList = JinxFactory.getInstance().getPhotosetsApi().getList(nsid, 0, 500, null);
    List<Photoset> allPhotosets = new ArrayList<>(photosetList.getPhotosetList());
    int page = 0;
    int pages = photosetList.getPages();

    // then loop through the rest of the results
    while (page < pages) {
      page++;
      logger.info("Loading page {} of sets...", page);
      photosetList = JinxFactory.getInstance().getPhotosetsApi().getList(nsid, page, 500, null);
      allPhotosets.addAll(photosetList.getPhotosetList());
    }
    return allPhotosets;
  }

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
   * @param title          the title for the new photoset.
   * @param description    the description for the new photoset.
   * @param primaryPhotoId the primary photo for the new photoset.
   * @return the new photoset.
   * @throws Exception if there are any errors.
   */
  public PhotosetInfo createPhotoset(String title, String description, String primaryPhotoId) throws Exception {
    if (title == null || title.isEmpty()) {
      throw new Exception("TITLE CANNOT BE NULL OR EMPTY.");
    }
    if (primaryPhotoId == null || primaryPhotoId.isEmpty()) {
      throw new Exception("PRIMARY PHOTO ID CANNOT BE NULL OR EMPTY.");
    }

    return JinxFactory.getInstance().getPhotosetsApi().create(title, description, primaryPhotoId);
  }


  /**
   * Adds the specified photo to the specified photoset.
   *
   * @param photosetId the id of the photoset to add the photo to.
   * @param photoId    the id of the photo to add to the photoset.
   * @throws Exception if there are any errors.
   */
  public void addPhoto(String photosetId, String photoId) throws Exception {
    Response response = JinxFactory.getInstance().getPhotosetsApi().addPhoto(photosetId, photoId);
    if (response.getCode() != 0) {
      throw new Exception("Unable to add photo. Code " + response.getCode() + ":" + response.getMessage());
    }
  }


  /**
   * Get a photoset by ID.
   *
   * @param photosetId the ID of the requested photoset.
   * @return the requested photoset.
   * @throws Exception if there are any errors.
   */
  public PhotosetInfo getPhotosetById(String photosetId) throws Exception {
    return JinxFactory.getInstance().getPhotosetsApi().getInfo(photosetId);
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
    Response response = JinxFactory.getInstance().getPhotosetsApi().delete(ssPhotoset.getPhotosetId());
    if (response.getCode() != 0) {
      throw new Exception("Error deleting photoset. Code " + response.getCode() + ":" + response.getMessage());
    }
  }


  /**
   * Add a photo to a photoset.
   *
   * @param ssPhotoset the photoset to add the photo to.
   * @param photo      the photo to add.
   * @throws Exception if there are any errors, or if either parameter is null.
   */
  public void addPhoto(SSPhotoset ssPhotoset, Photo photo) throws Exception {
    if (ssPhotoset == null) {
      throw new Exception("addPhoto: PARAMETER CANNOT BE NULL.");
    }
    if (photo == null) {
      throw new Exception("addPhoto: PHOTO CANNOT BE NULL.");
    }

    try {
      Response response = JinxFactory.getInstance().getPhotosetsApi().addPhoto(ssPhotoset.getPhotosetId(), photo.getPhotoId());
      if (response.getCode() == 0) {
        logger.info("Photo " + photo.getTitle() + " added to set " + ssPhotoset.getTitle());
      } else if (response.getCode() == 3) {
        logger.info("Photo " + photo.getTitle() + " already in set, not added.");
      }
    } catch (JinxException fe) {
      logger.warn("Unexpected flickr error", fe);
    } catch (Exception e) {
      logger.error("Unexpected error.", e);
    }
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
    List<String> list = new ArrayList<>();
    int page = 1;
    int count = 0;

    logger.info("Getting photos in set " + ssPhotoset);

    try {
      PhotosetPhotos pp;
      do {
        pp = JinxFactory.getInstance().getPhotosetsApi().getPhotos(ssPhotoset.getPhotosetId(), null, JinxConstants.PrivacyFilter.privacyPrivate, 500, page, JinxConstants.MediaType.photos);
        count += pp.getPhotoList().size();
        logger.info("Found " + count + " on " + page + " page(s)");
        for (Photo p : pp.getPhotoList()) {
          list.add(p.getPhotoId());
        }

        page++;
      } while (pp.getPhotoList().size() == 500);

    } catch (Exception e) {
      // ignore "set not found" errors when we already have found some
      // stuff in the set, since this seems to happen when the end of the
      // set is reached
      if (e instanceof JinxException) {
        if (((JinxException) e).getFlickrErrorCode() == 1) {
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
   * @param photoId    the ID of the photo to be removed.
   * @throws Exception if there are any errors, or if either parameter is null.
   */
  public void removePhoto(SSPhotoset ssPhotoset, String photoId) throws Exception {
    if (ssPhotoset == null) {
      throw new Exception("removePhoto: PARAMETER CANNOT BE NULL.");
    }
    if (photoId == null) {
      throw new Exception("removePhoto: PHOTO CANNOT BE NULL.");
    }
    Response response = JinxFactory.getInstance().getPhotosetsApi().removePhoto(ssPhotoset.getPhotosetId(), photoId);
    if (response.getCode() == 0) {
      logger.info("Photo " + photoId + " removed from set " + ssPhotoset.getPhotosetId());
    } else {
      throw new Exception("Error removing photo. Code " + response.getCode() + ":" + response.getMessage());
    }
  }


  /**
   * Reorder photosets.
   *
   * @param photosetIdList list of photoset id's in the desired order.
   * @throws Exception if there are any errors.
   */
  public void orderSets(List<String> photosetIdList) throws Exception {
    logger.info("Reordering photosets.");
    Response response = JinxFactory.getInstance().getPhotosetsApi().orderSets(photosetIdList);
    if (response.getCode() != 0) {
      throw new Exception("There was an error while ordering sets. Code " + response.getCode() + ":" + response.getMessage());
    }
  }


  /**
   * Edit the title and description of an existing set.
   *
   * @param ssPhotoset the photoset to edit
   * @throws Exception if there are any errors
   */
  public void editMeta(SSPhotoset ssPhotoset) throws Exception {
    Response response = JinxFactory.getInstance().getPhotosetsApi().editMeta(ssPhotoset.getPhotosetId(),
        ssPhotoset.getTitle(), ssPhotoset.getDescription());
    if (response.getCode() != 0) {
      throw new Exception("There was an error while editing set. Code " + response.getCode() + ":" + response.getMessage());
    }
  }


  /**
   * Edit the photos contained in the photoset.
   *
   * <p>This will replace the photos in the photoset with the photos in
   * the array.</p>
   *
   * @param photosetId the photoset to change.
   * @param photoId    the ID of the primary photo.
   * @param photoList  photos that the set should contain.
   * @throws Exception if there are any errors
   */
  public void editPhotos(String photosetId, String photoId, List<Photo> photoList) throws Exception {
    logger.info("Executing editPhotos on photoset " + photosetId
        + ", using primary photo " + photoId
        + ", and " + photoList.size() + " photos.");
    List<String> idList = new ArrayList<>();
    for (Photo p : photoList) {
      idList.add(p.getPhotoId());
    }

    Response response = JinxFactory.getInstance().getPhotosetsApi().editPhotos(photosetId, photoId, idList);
    if (response.getCode() != 0) {
      throw new Exception("Error editing photos. Code " + response.getCode() + ":" + response.getMessage());
    }
  }
}
