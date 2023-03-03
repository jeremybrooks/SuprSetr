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
import net.jeremybrooks.jinx.response.photos.PhotoInfo;
import net.jeremybrooks.jinx.response.photos.SearchParameters;
import net.jeremybrooks.jinx.response.photos.Tag;
import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.LogWindow;
import net.jeremybrooks.suprsetr.MainWindow;
import net.jeremybrooks.suprsetr.SSConstants;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import net.jeremybrooks.suprsetr.flickr.FlickrHelper;
import net.jeremybrooks.suprsetr.flickr.PhotoHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;


/**
 * This class scans a user's photos, deleting favxx tags.
 *
 * <p>This operation can take a very long time!</p>
 *
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 * @author Jeremy Brooks
 */
public class FavDeleteWorker extends SwingWorker<Void, Void> {
  private static final Logger logger = LogManager.getLogger(FavDeleteWorker.class);

  /* The blocker to provide user with feedback. */
  private final BlockerPanel blocker;

  /* Count of how many photos had tags deleted. */
  private int count = 0;

  private final ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.workers");

  private final String tagType;

  /**
   * Create a new instance of FavrTagr.
   *
   * @param blocker the blocker instance.
   */
  public FavDeleteWorker(BlockerPanel blocker) {
    this.blocker = blocker;
    if (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TAG_TYPE).equals("1")) {
      this.tagType = "favrtagr:count=";
    } else {
      this.tagType = "fav";
    }
  }


  /**
   * Execute the Flickr operations on a background thread.
   *
   * <p>This method searches all the users photos, then looks at the tags. If
   * a tag is found that matches the "favxx" pattern, it is deleted.</p>
   *
   * @return this method does not return any data.
   */
  @Override
  protected Void doInBackground() {
    List<Photo> photos;
    int processed = 0;
    int total;

    try {
      blocker.setTitle(resourceBundle.getString("FavDeleteWorker.blocker.title.running"));
      blocker.updateMessage(resourceBundle.getString("FavDeleteWorker.blocker.list"));

      // Search for:
      //    All media types
      //    Uploaded from the beginning of time until tomorrow
      //    Return tags as well
      SearchParameters params = new SearchParameters();
      params.setUserId(FlickrHelper.getInstance().getNSID());
      params.setMediaType(JinxConstants.MediaType.all);
      params.setMinUploadDate(new Date(0));
      params.setMaxUploadDate(new Date(System.currentTimeMillis() + 86400000));
      if (this.tagType.equals("fav")) {
        params.setExtras(EnumSet.of(JinxConstants.PhotoExtras.tags));
      } else {
        params.setExtras(EnumSet.of(JinxConstants.PhotoExtras.machine_tags));
      }
      photos = PhotoHelper.getInstance().getPhotos(params);

      total = photos.size();

      logger.info("Got " + total + " photos.");

      blocker.updateMessage(resourceBundle.getString("FavDeleteWorker.blocker.looking"));

      blocker.setTitle(resourceBundle.getString("FavDeleteWorker.blocker.title.status") + " " + processed + "/" + total);

      // iterate through all photos
      for (Photo p : photos) {
        // if it looks like we might have some fav tags, get the photo info
        boolean containsTag = false;
        if (this.tagType.equals("fav")) {
          String tags = p.getMachineTags();
          if (tags != null) {
            containsTag = tags.contains(this.tagType);
          }
        } else {
          String machineTags = p.getMachineTags();
          if (machineTags != null) {
            containsTag = machineTags.contains(this.tagType);
          }
        }
        if (containsTag) {
          PhotoInfo pi = PhotoHelper.getInstance().getPhotoInfo(p);
          // Look for this.tagType tags, and delete them.
          for (Tag tag : pi.getTags()) {
            if (tag.getRaw().startsWith(this.tagType)) {
              try {
                if (Integer.parseInt(tag.getRaw().substring(this.tagType.length())) > 0) {
                  logger.info("Removing tag {} from photo {}", tag, p.getPhotoId());
                  PhotoHelper.getInstance().removeTag(tag.getTagId());
                  this.count++;
                  LogWindow.addLogMessage(resourceBundle.getString("FavDeleteWorker.log.removed") +
                      " " + tag.getTag() + " " +
                      resourceBundle.getString("FavDeleteWorker.log.fromphoto") +
                      " " + p.getPhotoId());
                }
              } catch (Exception e) {
                logger.warn("Error removing tag " + tag.getRaw() + " from photo + " + p.getPhotoId(), e);
              }
            }
          }
        }
        processed++;
        if (processed % 100 == 0) {
          blocker.setTitle(resourceBundle.getString("FavDeleteWorker.blocker.title.finished") + " " + processed + "/" + total);
        }
      }
    } catch (Exception e) {
      logger.info("ERROR RUNNING FAV TAG DELETE.", e);
    }
    return null;
  }


  /**
   * Finished, so unblock the GUI and tell the user how many photos were
   * updated by the operation. That way they know the wait was worth it.
   */
  @Override
  protected void done() {
    StringBuilder message = new StringBuilder(resourceBundle.getString("FavDeleteWorker.message.removed"));
    message.append(" ").append(this.count).append(" ");
    if (this.count == 1) {
      message.append(resourceBundle.getString("message.photo"));
    } else {
      message.append(resourceBundle.getString("message.photos"));
    }
    message.append(".");

    blocker.unBlock();
    JOptionPane.showMessageDialog(MainWindow.getMainWindow(),
        message.toString(),
        resourceBundle.getString("FavDeleteWorker.dialog.finished.title"),
        JOptionPane.INFORMATION_MESSAGE);
    LogWindow.addLogMessage(message.toString());
  }
}
