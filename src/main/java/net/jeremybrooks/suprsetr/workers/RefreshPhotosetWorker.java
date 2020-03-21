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

package net.jeremybrooks.suprsetr.workers;


import net.jeremybrooks.jinx.response.photos.Photo;
import net.jeremybrooks.jinx.response.photos.SearchParameters;
import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.LogWindow;
import net.jeremybrooks.suprsetr.MainWindow;
import net.jeremybrooks.suprsetr.SSPhotoset;
import net.jeremybrooks.suprsetr.dao.PhotosetDAO;
import net.jeremybrooks.suprsetr.flickr.PhotoHelper;
import net.jeremybrooks.suprsetr.flickr.PhotosetHelper;
import net.jeremybrooks.suprsetr.flickr.SearchHelper;
import net.jeremybrooks.suprsetr.twitter.TwitterHelper;
import net.jeremybrooks.suprsetr.utils.SSUtils;
import net.jeremybrooks.suprsetr.utils.SimpleCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;


/**
 * This class refreshes a photoset on Flickr, or a list of photosets.
 *
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 * @author Jeremy Brooks
 */
public class RefreshPhotosetWorker extends SwingWorker<Void, Void> {

  private Logger logger = LogManager.getLogger(RefreshPhotosetWorker.class);
  private BlockerPanel blocker;
  private boolean exitWhenDone = false;
  private List<SSPhotoset> photosetList = null;
  private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.workers");

  /**
   * Create an instance of RefreshPhotoset.
   *
   * <p>All photosets in the list will be refreshed. The caller should make
   * sure that the sets in the list are eligible for refresh, or that the user
   * wants to refresh the sets early.</p>
   *
   * @param blocker      the blocker.
   * @param photosetList list of photosets to refresh.
   * @param exitWhenDone if true, SuprSetr will exit when this process finishes.
   */
  public RefreshPhotosetWorker(BlockerPanel blocker, List<SSPhotoset> photosetList, boolean exitWhenDone) {
    this.blocker = blocker;
    this.photosetList = photosetList;
    this.exitWhenDone = exitWhenDone;
  }


  /**
   * Execute the Flickr operation and database operations on a background
   * thread.
   *
   * @return this method does not return any data.
   */
  @Override
  protected Void doInBackground() {
    if (this.photosetList != null) {
      for (SSPhotoset set : this.photosetList) {
        try {
          MainWindow.getMainWindow().scrollToPhotoset(set.getPhotosetId());
          this.updatePhotoset(set);
          MainWindow.getMainWindow().updatePhotosetInList(set);
        } catch (Exception e) {
          // error was already logged in the updatePhotoset method
          // continue with the next set
        }
      }
    }

    return null;
  }


  /**
   * Update the specified photoset.
   *
   * @param ssPhotoset the photoset to refresh.
   * @throws Exception if there are any errors.
   */
  private void updatePhotoset(SSPhotoset ssPhotoset) throws Exception {
    int oldCount = ssPhotoset.getPhotos() + ssPhotoset.getVideos();
    int matches;
    List<Photo> searchResults = null;
    SearchParameters params;
    String newPrimaryPhotoId = null;
    String currentPrimaryId;
    int photoCount = 0;
    int videoCount = 0;

    blocker.setTitle(resourceBundle.getString("RefreshPhotosetWorker.blocker.title") + " '" + ssPhotoset.getTitle() + "'");

    long time = System.currentTimeMillis();

    // assume things will be OK
    ssPhotoset.setErrorFlag(false);

    if (ssPhotoset.isManaged()) {
      blocker.updateMessage(resourceBundle.getString("RefreshPhotosetWorker.blocker.searching"));
      try {
        // get the search results
        logger.info(ssPhotoset.toString());

        if (ssPhotoset.isOnThisDay()) {
          searchResults = new ArrayList<>();
          int endYear = ssPhotoset.getOnThisDayYearEnd();
          if (endYear == 0) {
            endYear = SSUtils.getCurrentYear();
          }
          if (ssPhotoset.getSortOrder() == 2) {
            // Sorted by date taken descending
            for (int year = endYear; year >= ssPhotoset.getOnThisDayYearStart(); year--) {
              searchResults.addAll(doSearchForYear(ssPhotoset, year));
            }
          } else {
            for (int year = ssPhotoset.getOnThisDayYearStart(); year <= endYear; year++) {
              searchResults.addAll(doSearchForYear(ssPhotoset, year));
            }
          }
        } else {
          params = SearchHelper.getInstance().getSearchParameters(ssPhotoset);
          if (ssPhotoset.isLimitSize() && ssPhotoset.getSortOrder() != 9) {
            // handle limited size sets that are not sorted by random order
            // limited size sets sorted by random order are sized AFTER randomizing
            searchResults = PhotoHelper.getInstance().getPhotos(params, ssPhotoset.getSizeLimit());
          } else {
            searchResults = PhotoHelper.getInstance().getPhotos(params);
          }
        }

        matches = searchResults == null ? 0 : searchResults.size();

        logger.info("Got " + matches + " search results.");

        if (matches > 0) {
          SSUtils.sortPhotoList(searchResults, ssPhotoset.getSortOrder());

          // if random sort AND limit size, do the sizing here
          if (ssPhotoset.isLimitSize() && ssPhotoset.getSortOrder() == 9) {
            if (searchResults.size() > ssPhotoset.getSizeLimit()) {
              while (searchResults.size() > ssPhotoset.getSizeLimit()) {
                searchResults.remove(searchResults.size() - 1);
              }
            }
          }

          // determine which photo should be the primary photo
          if (ssPhotoset.isLockPrimaryPhoto()) {
            currentPrimaryId = ssPhotoset.getPrimary();

            // if the current primary photo is in the search results,
            // use it
            for (Photo p : searchResults) {
              if (p.getPhotoId().equals(currentPrimaryId)) {
                newPrimaryPhotoId = currentPrimaryId;
                logger.info("Search results contain the current primary photo, so not changing it.");
                break;
              }
            }
          }

          // if the newPrimaryPhotoId is still null, use the first
          // photo in the search results, and set the data structure
          // to reflect the change
          if (newPrimaryPhotoId == null) {
            Photo p = searchResults.get(0);
            logger.info("Using photo " + p.getTitle() + " as new primary photo.");
            newPrimaryPhotoId = p.getPhotoId();
            ssPhotoset.setPrimary(newPrimaryPhotoId);
            ssPhotoset.setPrimaryPhotoIcon(PhotoHelper.getInstance().getIconForPhoto(newPrimaryPhotoId));
          }


          blocker.updateMessage(resourceBundle.getString("RefreshPhotosetWorker.blocker.applying"));
          // ADD PHOTOS TO THE SET
          PhotosetHelper.getInstance().editPhotos(ssPhotoset.getPhotosetId(), newPrimaryPhotoId, searchResults);

          time = System.currentTimeMillis() - time;

          // count photos and videos
          // counting media type avoids a call to get photoset info
          for (Photo p : searchResults) {
            if (p.getMedia().equalsIgnoreCase("video")) {
              videoCount++;
            } else {
              photoCount++;
            }
          }

          StringBuilder sb = new StringBuilder(resourceBundle.getString("RefreshPhotosetWorker.log.refresh1"));
          sb.append(" '").append(ssPhotoset.getTitle());
          sb.append("'. ").append(resourceBundle.getString("RefreshPhotosetWorker.log.refresh2")).append(" ").append(oldCount);
          sb.append(" ").append(resourceBundle.getString("RefreshPhotosetWorker.log.refresh3")).append(" ").append(searchResults.size());
          sb.append(" ").append(resourceBundle.getString("RefreshPhotosetWorker.log.refresh4")).append(" ");
          sb.append(time).append("ms");

          logger.info(sb.toString());
          LogWindow.addLogMessage(sb.toString());
        } else {
          logger.warn("No results found for set " + ssPhotoset.getTitle() + ". This is probably temporary.");

          LogWindow.addLogMessage(" " + resourceBundle.getString("RefreshPhotosetWorker.log.noresults") +
              " '" + ssPhotoset.getTitle() + "'");
          ssPhotoset.setErrorFlag(true);
        }

      } catch (Exception e) {
        logger.error("ERROR REFRESHING SET " + ssPhotoset, e);

        LogWindow.addLogMessage(resourceBundle.getString("RefreshPhotosetWorker.log.error1") +
            " '" + ssPhotoset.getTitle() + "', " +
            resourceBundle.getString("RefreshPhotosetWorker.log.error2") +
            " '" + e.getMessage() + "'. " +
            resourceBundle.getString("RefreshPhotosetWorker.log.error3") +
            ".");
        ssPhotoset.setErrorFlag(true);
      }
    }

    try {
      if (ssPhotoset.isManaged() && (searchResults != null)) {  // null search results means something failed above, so don't bother with saving to db

        blocker.updateMessage(resourceBundle.getString("RefreshPhotosetWorker.blocker.saving"));

        // UPDATE OUR DATA STRUCTURE TO REFLECT THE NEW PHOTOSET ON FLICKR
        ssPhotoset.setLastRefreshDate(new Date());
        ssPhotoset.setPhotos(photoCount);
        ssPhotoset.setVideos(videoCount);
        ssPhotoset.setSyncTimestamp(System.currentTimeMillis());
      }

      // mark the list cell as invalid, so anything that has changed
      // will get updated when the list is repainted
      SimpleCache.getInstance().invalidate(ssPhotoset.getPhotosetId());

      PhotosetDAO.updatePhotoset(ssPhotoset);

      // Send tweet if user has requested it AND there are new photos in the set
      if (ssPhotoset.isSendTweet() && (oldCount != (ssPhotoset.getPhotos() + ssPhotoset.getVideos()))) {
        try {
          String tweet = TwitterHelper.buildTweet(
              ssPhotoset.getTweetTemplate(),
              ssPhotoset.getTitle(),
              ssPhotoset.getUrl(),
              Math.abs(oldCount - ssPhotoset.getPhotos()),
              ssPhotoset.getPhotos());

          logger.info("Sending tweet (" + tweet.length() + " chars): '"
              + tweet + "'");

          TwitterHelper.updateStatus(tweet);
        } catch (Exception e) {
          logger.warn("ERROR SENDING TWEET - IGNORING.", e);
        }
      }
    } catch (Exception e) {
      logger.error("ERROR SAVING SET TO DATABASE.", e);
      LogWindow.addLogMessage(" WARN: Error saving set info to database.");
      ssPhotoset.setErrorFlag(true);
    }
  }


  /**
   * Refresh has finished, so scroll to correct position and unblock window.
   */
  @Override
  protected void done() {
    try {
      String id;
      id = this.photosetList.get(0).getPhotosetId();
      MainWindow.getMainWindow().updateMasterList(id);
    } catch (Exception e) {
      logger.error("ERROR WHILE TRYING TO UPDATE LIST MODEL.", e);
      JOptionPane.showMessageDialog(null,
          resourceBundle.getString("dialog.guierror.message"),
          resourceBundle.getString("dialog.guierror.title"),
          JOptionPane.WARNING_MESSAGE);
    }
    blocker.unBlock();

    if (this.exitWhenDone) {
      logger.info("Refresh is done, exiting.");
      MainWindow.getMainWindow().backupAndExit();
    }
  }

  private List<Photo> doSearchForYear(SSPhotoset ssPhotoset, int year) throws Exception {
    SearchParameters params = SearchHelper.getInstance().getSearchParametersForOnThisDay(ssPhotoset, year);
    blocker.updateMessage(resourceBundle.getString("RefreshPhotosetWorker.blocker.searchingon") + " " +
        + ssPhotoset.getOnThisDayMonth() + "/"
        + ssPhotoset.getOnThisDayDay() + "/"
        + year + "....");

    List<Photo> tempResults = PhotoHelper.getInstance().getPhotos(params);
    logger.info("Got " + tempResults.size() + " results.");
    return tempResults;
  }
}
