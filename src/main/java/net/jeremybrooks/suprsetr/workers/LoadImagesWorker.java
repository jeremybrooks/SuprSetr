/*
 *  SuprSetr is Copyright 2010-2023 by Jeremy Brooks
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

package net.jeremybrooks.suprsetr.workers;

import net.jeremybrooks.jinx.JinxConstants;
import net.jeremybrooks.jinx.response.photos.Photo;
import net.jeremybrooks.jinx.response.photosets.PhotosetPhotos;
import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.flickr.JinxFactory;
import net.jeremybrooks.suprsetr.flickr.PhotoHelper;
import net.jeremybrooks.suprsetr.utils.ObjectCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.SwingWorker;
import java.util.List;
import java.util.ResourceBundle;


/**
 * This class reordered the photosets on flickr.
 *
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 * @author Jeremy Brooks
 */
public class LoadImagesWorker extends SwingWorker<Void, Void> {

  /* Logging. */
  private static final Logger logger = LogManager.getLogger(LoadImagesWorker.class);

  /* The blocker instance used to provide user with feedback. */
  private final BlockerPanel blocker;

  /* The photoset to get photos from. */
  private final String photosetId;

  /* The page of photos to get. */
  private final int page;

  /* The object cache to use. */
  private final ObjectCache cache;

  /* The photos object used by the picker. */
  private final List<Photo> thePhotos;

  /* The parent frame. */
  private final JDialog parent;

  private final ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.workers");


  /**
   * Create an instance of OrderSetsWorker.
   *
   * @param blocker    the blocker instance.
   * @param photosetId string array of photoset id's.
   * @param page       page of photos.
   * @param cache      the object cache.
   * @param thePhotos  list of photos.
   * @param parent     the parent component.
   */
  public LoadImagesWorker(BlockerPanel blocker, String photosetId, int page,
                          ObjectCache cache, List<Photo> thePhotos, JDialog parent) {
    this.blocker = blocker;
    this.photosetId = photosetId;
    this.page = page;
    this.cache = cache;
    this.thePhotos = thePhotos;
    this.parent = parent;
  }


  /**
   * Execute the Flickr operation on a background thread.
   *
   * @return this method does not return any data.
   */
  @Override
  protected Void doInBackground() {
    PhotosetPhotos p;
    int count = 1;
    int total;
    this.thePhotos.clear();
    try {
      // Now get the first 25 photos and populate the model
      p = JinxFactory.getInstance().getPhotosetsApi().getPhotos(photosetId, null, null, 25, page, JinxConstants.MediaType.all);
      List<Photo> photoList = p.getPhotoList();
      if (photoList != null) {
        total = photoList.size();
        for (Photo photo : photoList) {
          if (this.cache.get(photo.getPhotoId()) == null) {
            blocker.updateMessage(resourceBundle.getString("LoadImagesWorker.blocker.loading") +
                " " + (count++) + "/" + total + " ("
                + photo.getTitle() + ")");
            ImageIcon image = PhotoHelper.getInstance().getIconForPhoto(photo.getPhotoId());
            this.cache.put(photo.getPhotoId(), image);
          }
          this.thePhotos.add(photo);
          this.parent.repaint();
        }
      }
    } catch (Exception e) {
      logger.error("ERROR LOADING IMAGE(S).", e);
    }
    return null;
  }

  /**
   * Finished, so update the GUI, making the first photoset the
   * currently selected photoset. Then remove the blocker.
   */
  @Override
  protected void done() {
    blocker.unBlock();
  }
}
