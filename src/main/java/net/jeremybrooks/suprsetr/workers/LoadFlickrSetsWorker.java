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

package net.jeremybrooks.suprsetr.workers;

import net.jeremybrooks.jinx.response.photosets.Photoset;
import net.jeremybrooks.jinx.response.photosets.PhotosetList;
import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.MainWindow;
import net.jeremybrooks.suprsetr.SSConstants;
import net.jeremybrooks.suprsetr.SSPhotoset;
import net.jeremybrooks.suprsetr.dao.PhotosetDAO;
import net.jeremybrooks.suprsetr.flickr.FlickrHelper;
import net.jeremybrooks.suprsetr.flickr.PhotosetHelper;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.List;
import java.util.ResourceBundle;


/**
 * This class loads photosets from Flickr, adding them to the database as
 * needed.
 * <p/>
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 * @author jeremyb
 */
public class LoadFlickrSetsWorker extends SwingWorker<Void, SSPhotoset> {

  /**
   * Logging.
   */
  private Logger logger = Logger.getLogger(LoadFlickrSetsWorker.class);

  /**
   * The blocker used for feedback.
   */
  private BlockerPanel blocker;

  private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.workers");


  /**
   * Create a new instance of LoadFlickrSets.
   *
   * @param blocker the blocker.
   */
  public LoadFlickrSetsWorker(BlockerPanel blocker) {
    this.blocker = blocker;
  }


  /**
   * Execute the Flickr operation and database operations on a background
   * thread.
   *
   * @return this method does not return any data.
   */
  @Override
  protected Void doInBackground() {
    blocker.updateMessage(resourceBundle.getString("LoadFlickrSetsWorker.blocker.gettingphotosets"));
    String nsid = FlickrHelper.getInstance().getNSID();
    PhotosetList photosetList;

    long sync = System.currentTimeMillis();

    try {
      photosetList = PhotosetHelper.getInstance().getPhotosets(nsid);
      for (Photoset p : photosetList.getPhotosetList()) {
        blocker.updateMessage(resourceBundle.getString("LoadFlickrSetsWorker.blocker.processing") +
            " \"" + p.getTitle() + "\"");

        SSPhotoset ssp = PhotosetDAO.getPhotosetForId(p.getPhotosetId());

        if (ssp == null) {
          // NEW SET, ADD TO DATABASE
          ssp = new SSPhotoset();
          // set fields inherited from Photoset
          ssp.setDescription(p.getDescription());
          ssp.setFarm(Integer.parseInt(p.getFarm()));
          ssp.setPhotosetId(p.getPhotosetId());
          ssp.setPhotos(p.getPhotos());
          ssp.setVideos(p.getVideos());
          ssp.setPrimary(p.getPrimary());
          ssp.setSecret(p.getSecret());
          ssp.setServer(p.getServer());
          ssp.setTitle(p.getTitle());

          // build the URL -- it is not correct in the getUrl() call
          StringBuilder sb = new StringBuilder();
          sb.append("https://www.flickr.com/photos/");
          sb.append(nsid);
          sb.append("/sets/");
          sb.append(p.getPhotosetId());
          sb.append("/");
          ssp.setUrl(sb.toString());

          // set custom fields
          ssp.setManaged(false);
          ssp.setMatchTakenDates(false);
          ssp.setMatchUploadDates(false);
          ssp.setPrimaryPhotoIcon(PhotosetHelper.getInstance().getIconForPhotoset(p));
          ssp.setSyncTimestamp(sync);
          ssp.setTagMatchMode("NONE");
          ssp.setTags("");
          ssp.setSendTweet(false);
          ssp.setTweetTemplate(SSConstants.DEFAULT_TWEET_TEMPLATE);

          PhotosetDAO.insertPhotoset(ssp);


        } else {
          ssp.setFarm(Integer.parseInt(p.getFarm()));
          ssp.setPhotos(p.getPhotos());
          ssp.setVideos(p.getVideos());

          if (ssp.getPrimaryPhotoIcon() == null) {
            logger.info("Retrieving missing icon for set " + ssp.getTitle());
            ssp.setPrimaryPhotoIcon(PhotosetHelper.getInstance().getIconForPhotoset(p));
          }
          if (!p.getPrimary().equals(ssp.getPrimary())) {
            ssp.setPrimary(p.getPrimary());
            ssp.setPrimaryPhotoIcon(PhotosetHelper.getInstance().getIconForPhotoset(p));
          }
          if (!p.getTitle().equals(ssp.getTitle())) {
            ssp.setTitle(p.getTitle());
          }
          if (!p.getDescription().equals(ssp.getDescription())) {
            ssp.setDescription(p.getDescription());
          }


          ssp.setSecret(p.getSecret());
          ssp.setServer(p.getServer());
          ssp.setSyncTimestamp(sync);

          // build the URL -- it is not correct in the getUrl() call
          StringBuilder sb = new StringBuilder();
          sb.append("https://www.flickr.com/photos/");
          sb.append(nsid);
          sb.append("/sets/");
          sb.append(p.getPhotosetId());
          sb.append("/");
          ssp.setUrl(sb.toString());

          // SAVE THE UPDATED SET TO THE DATABASE
          PhotosetDAO.updatePhotoset(ssp);
        }

      }

      // NOW, DELETE RECORDS THAT NO LONGER EXIST ON FLICKR
      List<SSPhotoset> allData = PhotosetDAO.getPhotosetListOrderByTitle();
      for (SSPhotoset ssp : allData) {
        if (ssp.getSyncTimestamp() != sync) {
          PhotosetDAO.delete(ssp);
        }
      }
    } catch (Exception e) {
      logger.error("ERROR GETTING PHOTOSET LIST.", e);
      JOptionPane.showMessageDialog(null,
          resourceBundle.getString("LoadFlickrSetsWorker.dialog.error.message") + " " + e.getMessage(),
          resourceBundle.getString("LoadFlickrSetsWorker.dialog.error.title"),
          JOptionPane.ERROR_MESSAGE);
    }
    return null;
  }


  /**
   * Finished, so update the GUI and unblock.
   */
  @Override
  protected void done() {
    // UPDATE THE LIST MODEL
    try {
      MainWindow.getMainWindow().updateMasterList(null);
    } catch (Exception e) {
      logger.error("ERROR WHILE TRYING TO UPDATE LIST MODEL.", e);
      JOptionPane.showMessageDialog(null,
          resourceBundle.getString("dialog.guierror.message"),
          resourceBundle.getString("dialog.guierror.title"),
          JOptionPane.WARNING_MESSAGE);
    }
    blocker.unBlock();
  }
}
