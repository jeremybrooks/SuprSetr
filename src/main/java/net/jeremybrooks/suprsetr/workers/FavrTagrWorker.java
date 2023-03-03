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
import net.jeremybrooks.jinx.JinxException;
import net.jeremybrooks.jinx.response.photos.Photo;
import net.jeremybrooks.jinx.response.photos.SearchParameters;
import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.LogWindow;
import net.jeremybrooks.suprsetr.MainWindow;
import net.jeremybrooks.suprsetr.SSConstants;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import net.jeremybrooks.suprsetr.flickr.FlickrHelper;
import net.jeremybrooks.suprsetr.flickr.JinxFactory;
import net.jeremybrooks.suprsetr.flickr.PhotoHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;


/**
 * This class scans a user's photos, adding favxx tags depending on how many
 * times a photo has been faved.
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
public class FavrTagrWorker extends SwingWorker<Void, Void> {

  private Logger logger = LogManager.getLogger(FavrTagrWorker.class);

  private BlockerPanel blocker;

  /* Count of how many photos had tags added. */
  private int count = 0;

  /* The fave interval. */
  private int interval = -1;

  /* For custom intervals, a List of the intervals used. */
  private List<Integer> customIntervals;

  /* Flag to indicate if there were errors. */
  private boolean hasErrors = false;

  private int maxFaves;
  private Photo favoritePhoto;

  private String tagType = "fav";

  private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.workers");

  /**
   * Create a new instance of FavrTagr.
   *
   * @param blocker the blocker instance.
   */
  public FavrTagrWorker(BlockerPanel blocker) {
    this.blocker = blocker;
    try {
      String intervals = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_FAVRTAGR_INTERVAL);
      if (intervals.startsWith("c")) {
        customIntervals = new ArrayList<>();
        for (String s : intervals.substring(2).split(",")) {
          this.customIntervals.add(Integer.valueOf(s));
        }
      } else {
        this.interval = Integer.parseInt(intervals);
      }
    } catch (Exception e) {
      this.customIntervals = null;
      this.interval = -1;
      logger.warn("Error parsing the intervals.", e);
    }

    switch (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TAG_TYPE)) {
      case "0":
        this.tagType = "fav";
        break;
      case "1":
        this.tagType = "favrtagr:count=";
        break;
      default:
        this.tagType = "fav";
    }

    logger.info("Fave tag interval is " + this.interval + "; tag type is " + this.tagType);
  }


  /**
   * Execute the Flickr operations on a background thread.
   *
   * <p>This method searches all the users photos, then gets the fave count
   * for each photo. It then checks to see if the fave count is greater than
   * the fave interval, and computes the tags needed for each fave interval.
   * Any tags that are not already on the photo will be added.</p>
   *
   * @return this method does not return any data.
   */
  @Override
  protected Void doInBackground() {
    // if configuration was not parsed, give up
    if (this.customIntervals == null && this.interval == -1) {
      this.blocker.unBlock();
      return null;
    }

    List<Photo> photoList;
    List<String> newFaves;
    int processed = 0;
    int total;

    try {
      blocker.setTitle(resourceBundle.getString("FavrTagrWorker.blocker.title"));
      blocker.updateMessage(resourceBundle.getString("FavrTagrWorker.blocker.list"));

      // Search for:
      //    All media types
      //    Uploaded from the beginning of time until tomorrow
      //    Return tags as well
      SearchParameters params = new SearchParameters();
      params.setUserId(FlickrHelper.getInstance().getNSID());
      params.setMediaType(JinxConstants.MediaType.all);
      params.setMinUploadDate(new Date(0));
      params.setMaxUploadDate(new Date(System.currentTimeMillis() + 86400000));
      params.setExtras(EnumSet.of(JinxConstants.PhotoExtras.tags));
      photoList = PhotoHelper.getInstance().getPhotos(params);

      total = photoList.size();

      logger.info("Got " + total + " photos.");

      blocker.updateMessage(resourceBundle.getString("FavrTagrWorker.blocker.looking"));

      blocker.setTitle(resourceBundle.getString("FavrTagrWorker.blocker.title.status") + " " + processed + "/" + total);

      // iterate through all photos
      for (Photo p : photoList) {
        int faves = PhotoHelper.getInstance().getFavoriteCount(p);
        if (faves > this.maxFaves) {
          maxFaves = faves;
          this.favoritePhoto = p;
        }

        // tags look like this:
        // street usa abstract bike bicycle boston lights bokeh commuter massachusets

        // get the new fave tags that should be added to this photo, if any
        List<String> existingTags = Arrays.asList(p.getTags().split(" "));
        newFaves = this.getFavTags(existingTags, faves);
        if (newFaves != null && newFaves.size() > 0) {
          // Try to stay within the limit of 75 tags per photo
          // count existing tags
          // if existing + new > 75, trim the new tag list
          if (existingTags.size() >= 75) {
            this.hasErrors = true;
            StringBuilder sb = new StringBuilder(resourceBundle.getString("FavrTagrWorker.message.toomanytags1"));
            sb.append(" ").append(p.getPhotoId()).append(" <").append(JinxFactory.getInstance().buildUrlForPhoto(p)).append("> ");
            sb.append(resourceBundle.getString("FavrTagrWorker.message.toomanytags2"));
            LogWindow.addLogMessage(sb.toString());
          } else if (existingTags.size() + newFaves.size() > 75) {
            // remove elements from the beginning of the list as needed
            int del = (existingTags.size() + newFaves.size()) - 75;
            newFaves.subList(0, del).clear();
            LogWindow.addLogMessage(resourceBundle.getString("message.photo") + " " + p.getPhotoId() +
                " " + resourceBundle.getString("FavrTagrWorker.message.toomanytags3"));
          }

          // Don't even try if there are too many tags.
          if (existingTags.size() < 75) {
            try {
              PhotoHelper.getInstance().addTags(p, newFaves.toArray(new String[newFaves.size()]));
              LogWindow.addLogMessage(resourceBundle.getString("message.Photo") +
                  " " + p.getPhotoId() + "  " + resourceBundle.getString("FavrTagrWorker.message.taggedwith") + " " + newFaves);
              blocker.updateMessage(resourceBundle.getString("FavrTagrWorker.blocker.tagged") +
                  " '" + p.getTitle() +
                  resourceBundle.getString("FavrTagrWorker.blocker.lookingmore"));
              this.count++;
            } catch (JinxException je) {
              if (je.getFlickrErrorCode() == 2) {
                // Too many tags, so display a message in the log window,
                // and set a flag
                // This should not happen unless Flickr changes
                // the limit, since we already check for the number
                // of tags before attempting to add more.
                this.hasErrors = true;
                StringBuilder sb = new StringBuilder(resourceBundle.getString("FavrTagrWorker.message.toomanytags1"));
                sb.append(" ").append(p.getPhotoId()).append(" <").append(JinxFactory.getInstance().buildUrlForPhoto(p)).append("> ");
                sb.append(resourceBundle.getString("FavrTagrWorker.message.toomanytags2"));
                LogWindow.addLogMessage(sb.toString());
              }
            } catch (Exception e) {

              logger.warn("ERROR ADDING TAGS.", e);
            }
          }
        }

        processed++;
        if (processed % 100 == 0) {
          blocker.setTitle(resourceBundle.getString("FavrTagrWorker.blocker.title.status") +
              " " + processed + "/" + total);
        }
      }

    } catch (Exception e) {
      logger.info("ERROR RUNNING FAVRTAGR.", e);
    }

    return null;
  }


  /**
   * Finished, so unblock the GUI and tell the user how many photos were
   * updated by the operation. That way they know the wait was worth it.
   */
  @Override
  protected void done() {
    if (this.customIntervals == null && this.interval == -1) {
      // error message if configuration was bad
      JOptionPane.showMessageDialog(MainWindow.getMainWindow(),
          resourceBundle.getString("FavrTagrWorker.parseerror.message"),
          resourceBundle.getString("FavrTagrWorker.parseerror.title"),
          JOptionPane.ERROR_MESSAGE);
    } else {
      StringBuilder message = new StringBuilder(resourceBundle.getString("FavrTagrWorker.message.addednew"));
      message.append(" ").append(this.count).append(" ");
      if (this.count == 1) {
        message.append(resourceBundle.getString("message.photo"));
      } else {
        message.append(resourceBundle.getString("message.photos"));
      }
      message.append(".\n\n");

      message.append(String.format(resourceBundle.getString("FavrTagrWorker.message.mostfaves"),
          this.favoritePhoto.getTitle(), this.maxFaves));

      blocker.unBlock();
      LogWindow.addLogMessage(resourceBundle.getString("FavrTagrWorker.message.finished") + ".\n" + message.toString());
      Object[] options = {
          resourceBundle.getString("FavrTagrWorker.done.ok"),
          resourceBundle.getString("FavrTagrWorker.done.go")
      };
      int selection = JOptionPane.showOptionDialog(MainWindow.getMainWindow(),
          message.toString(),
          resourceBundle.getString("FavrTagrWorker.message.finished"),
          JOptionPane.DEFAULT_OPTION,
          JOptionPane.INFORMATION_MESSAGE,
          null,
          options,
          options[1]);
      if (selection == 1) {
          String photoUrl = "https://flickr.com/photos/" + FlickrHelper.getInstance().getNSID() + "/" +
              this.favoritePhoto.getPhotoId();
        try {
          Desktop.getDesktop().browse(new URI(photoUrl));
        } catch (Exception e) {
          logger.error("Could not browse to photo '" + photoUrl, e);
          JOptionPane.showMessageDialog(MainWindow.getMainWindow(),
              String.format(resourceBundle.getString("FavrTagrWorker.browseerror.message"), photoUrl),
              resourceBundle.getString("FavrTagrWorker.browseerror.title"),
              JOptionPane.ERROR_MESSAGE);
        }
      }
      if (this.hasErrors) {
        JOptionPane.showMessageDialog(MainWindow.getMainWindow(),
            resourceBundle.getString("FavrTagrWorker.dialog.error.message"),
            resourceBundle.getString("FavrTagrWorker.dialog.error.title"),
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }


  /**
   * Gets a list of the tags needed for a given photo.
   *
   * @param existingTags list of tags that the photo already has.
   * @param faves        the number of faves on the photo.
   * @return list of fav tags needed for the photo.
   */
  private List<String> getFavTags(List<String> existingTags, int faves) {
    List<String> list = new ArrayList<>();

    if (this.customIntervals == null) {
      // If user selected special interval "10 up to 100, then 100", or "The Hawk:...."
      // set 10 as the initial value
      int myInterval = this.interval;
      if (this.interval == 0 || this.interval == 4) {
        myInterval = 10;
      }

      // Do work if faves >= interval
      if (faves >= myInterval) {
        int faveCheck = myInterval;

        // Check to see if the fave count is greater than each fave interval
        // add a tag to the list if needed each time the count is greater
        // than the fave interval
        while (faves >= faveCheck) {
          String tag = this.tagType + faveCheck;
          if (!existingTags.contains(tag)) {
            list.add(tag);
          }

          if (this.interval == 0) {
            if (faveCheck >= 100) {
              myInterval = 100;
            }
          }
          if (this.interval != 4) {
            faveCheck += myInterval;
          }

          // for special mode "The Hawk....", only check for 10, 25, 50, and 100 faves
          if (this.interval == 4) {
            if (faveCheck == 10) {
              faveCheck = 25;
            } else if (faveCheck == 25) {
              faveCheck = 50;
            } else if (faveCheck == 50) {
              faveCheck = 100;
            } else if (faveCheck == 100) {
              faveCheck = Integer.MAX_VALUE;
            }
          }
        }
      }
    } else {
      // handle custom intervals
      for (Integer count : this.customIntervals) {
        String tag = this.tagType + count;
        if (faves >= count) {
          if (!existingTags.contains(tag)) {
            list.add(tag);
          }
        }
      }
    }

    return list;
  }
}
