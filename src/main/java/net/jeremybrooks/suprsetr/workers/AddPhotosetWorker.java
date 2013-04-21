/*
 * SuprSetr is Copyright 2010-2013 by Jeremy Brooks
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

import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import net.jeremybrooks.jinx.dto.Photo;
import net.jeremybrooks.jinx.dto.Photos;
import net.jeremybrooks.jinx.dto.Photoset;
import net.jeremybrooks.jinx.dto.SearchParameters;
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
import org.apache.log4j.Logger;


/**
 * This class adds a photoset to Flickr and to the database.
 *
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 * 
 * @author jeremyb
 */
public class AddPhotosetWorker extends SwingWorker<Void, Void> {

    /** Logging. */
    private Logger logger = Logger.getLogger(AddPhotosetWorker.class);

    /** The blocker instance for user feedback. */
    private BlockerPanel blocker;

    /** The photoset to add. */
    private SSPhotoset ssPhotoset;


    /**
     * Create an instance of AddPhotoset.
     *
     * @param blocker the blocker instance.
     * @param ssPhotoset the photoset to add.
     */
    public AddPhotosetWorker(BlockerPanel blocker, SSPhotoset ssPhotoset) {
	this.blocker = blocker;
	this.ssPhotoset = ssPhotoset;
    }


    /**
     * Execute the Flickr operation and database operations on a background
     * thread.
     *
     * @return this method does not return any data.
     */
    @Override
    protected Void doInBackground() {
	int count = 0;
	int matches = 0;
	Photoset newSet = null;
	Photo firstPhoto = null;
	Photos photos = null;
	
	// TO CREATE A SET, WE NEED PHOTOS
	// SO GET SEARCH RESULTS FOR THE PARAMETERS
	try {
	    logger.info(this.ssPhotoset.toString());
	    blocker.updateMessage("Searching for matching photos....");
	    SearchParameters params = null;

	    //SearchParameters params = SearchHelper.getInstance().getSearchParameters(this.ssPhotoset);
	    //photos = PhotoHelper.getInstance().getPhotos(params);
	    if (ssPhotoset.isOnThisDay()) {
		Photos tempResults = null;
		int endYear = ssPhotoset.getOnThisDayYearEnd();
		if (endYear == 0) {
		    endYear = SSUtils.getCurrentYear();
		}
		if (ssPhotoset.getSortOrder() == 2) {
		    // Sorted by date taken descending
		    for (int year = endYear; year >= ssPhotoset.getOnThisDayYearStart(); year--) {
			params = SearchHelper.getInstance().getSearchParametersForOnThisDay(ssPhotoset, year);
			blocker.updateMessage("Searching for matching photos on "
				+ +ssPhotoset.getOnThisDayMonth() + "/"
				+ ssPhotoset.getOnThisDayDay() + "/"
				+ year + "....");
			tempResults = PhotoHelper.getInstance().getPhotos(params);
			logger.info("Got " + tempResults.getTotal() + " results.");
			
			if (photos == null) {
			    photos = tempResults;
			} else {
			    photos.setTotal(photos.getTotal() + tempResults.getTotal());
			    List<Photo> list = photos.getPhotos();
			    list.addAll(tempResults.getPhotos());
			    photos.setPhotos(list);
			}
		    }
		} else {
		    // Sorted by date taken descending, or no particular order
		    for (int year = ssPhotoset.getOnThisDayYearStart(); year <= endYear; year++) {
			params = SearchHelper.getInstance().getSearchParametersForOnThisDay(ssPhotoset, year);
			blocker.updateMessage("Searching for matching photos on "
				+ +ssPhotoset.getOnThisDayMonth() + "/"
				+ ssPhotoset.getOnThisDayDay() + "/"
				+ year + "....");
			tempResults = PhotoHelper.getInstance().getPhotos(params);
			logger.info("Got " + tempResults.getTotal() + " results.");
			if (photos == null) {
			    photos = tempResults;
			} else {
			    photos.setTotal(photos.getTotal() + tempResults.getTotal());
			    List<Photo> list = photos.getPhotos();
			    list.addAll(tempResults.getPhotos());
			    photos.setPhotos(list);
			}
		    }
		}
	    } else {
		params = SearchHelper.getInstance().getSearchParameters(this.ssPhotoset);
		if (this.ssPhotoset.isLimitSize()) {
		    photos = PhotoHelper.getInstance().getPhotos(params, this.ssPhotoset.getSizeLimit());
		} else {
		    photos = PhotoHelper.getInstance().getPhotos(params);
		}
	    }

	    if (photos == null) {
		matches = 0;
	    } else {
		matches = photos.getPhotos().size();
	    }

	    if (matches > 0) {
		logger.info("Got " + matches + " search results.");



		firstPhoto = photos.getPhotos().get(0);

		// sort by title, if necessary
		if (this.ssPhotoset.getSortOrder() == 7) {
		    SSUtils.sortPhotoListByTitleDescending(photos.getPhotos());
		} else if (this.ssPhotoset.getSortOrder() == 8) {
		    SSUtils.sortPhotoListByTitleAscending(photos.getPhotos());
		}

		blocker.updateMessage("Creating new set on Flickr....");
		// CREATE THE SET
		newSet = PhotosetHelper.getInstance().createPhotoset(this.ssPhotoset.getTitle(), this.ssPhotoset.getDescription(), firstPhoto.getId());

		blocker.updateMessage("Applying changes to photoset....");
		// ADD PHOTOS TO THE SET
		PhotosetHelper.getInstance().editPhotos(newSet.getId(), firstPhoto.getId(), photos.getPhotos());

		LogWindow.addLogMessage("Added set '" + this.ssPhotoset.getTitle()
			+ "' with " + photos.getPhotos().size() + " photos.");
	    } else {
		JOptionPane.showMessageDialog(null,
			"There were no photos that matched, so the set was not created.",
			"No Results",
			JOptionPane.INFORMATION_MESSAGE);

		// null out the photoset, telling the done() method that no
		// set was added
		this.ssPhotoset = null;
	    }
	} catch (Exception e) {
	    logger.error("ERROR ADDING SET.", e);
	    JOptionPane.showMessageDialog(null,
		    "There was an error creating and populating new set.\n"
		    + "The error message was: " + e.getMessage() + "\n"
		    + "Depending on when the process failed, the set may \n"
		    + "or may not have been created. You should probably\n"
		    + "check Flickr to see if the set is there.\n"
		    + "See the log for more details.",
		    "Error Adding Set", JOptionPane.ERROR_MESSAGE);
	}

	if (matches > 0) {
	    try {
		blocker.updateMessage("Saving new set in database....");

		this.ssPhotoset.setFarm(newSet.getFarm());
		this.ssPhotoset.setId(newSet.getId());
		this.ssPhotoset.setLastRefreshDate(new Date());
		this.ssPhotoset.setPhotos(photos.getPhotos().size());
		this.ssPhotoset.setPrimaryPhotoIcon(PhotoHelper.getInstance().getIconForPhoto(firstPhoto.getId()));
		this.ssPhotoset.setSecret(newSet.getSecret());
		this.ssPhotoset.setServer(newSet.getServer());
		this.ssPhotoset.setSyncTimestamp(System.currentTimeMillis());
		this.ssPhotoset.setUrl(newSet.getUrl());

		PhotosetDAO.insertPhotoset(this.ssPhotoset);

		if (ssPhotoset.isSendTweet()) {

		    try {
			String tweet = TwitterHelper.buildTweet(
				ssPhotoset.getTweetTemplate(),
				ssPhotoset.getTitle(),
				ssPhotoset.getUrl(),
				ssPhotoset.getPhotos(),
				ssPhotoset.getPhotos());

			logger.info("Sending tweet (" + tweet.length() + " chars): '"
				+ tweet + "'");

			TwitterHelper.updateStatus(tweet);
		    } catch (Exception e) {
			logger.warn("ERROR SENDING TWEET - IGNORING.", e);
		    }

		} else if (ssPhotoset.isTweetWhenCreated()) {
		    try {
			String tweet = TwitterHelper.buildTweet(
				ssPhotoset.getTweetTemplate(),
				ssPhotoset.getTitle(),
				ssPhotoset.getUrl(),
				ssPhotoset.getPhotos(),
				ssPhotoset.getPhotos());

			logger.info("Sending creation tweet (" + tweet.length() + " chars): '"
				+ tweet + "'");

			TwitterHelper.updateStatus(tweet);
		    } catch (Exception e) {
			logger.warn("ERROR SENDING TWEET - IGNORING.", e);
		    }
		}

	    } catch (Exception e) {
		logger.error("ERROR ADDING SET.", e);
		JOptionPane.showMessageDialog(null,
			"There was an error while saving the set in the database.\n"
			+ "Error message: " + e.getMessage() + "\n"
			+ "The set was created on Flickr, but will not show up\n"
			+ "in the list until you restart SuprSetr.",
			"Error Saving Set",
			JOptionPane.ERROR_MESSAGE);
	    }
	}

	return null;
    }


    /**
     * Finished, so update the GUI, making the newly added photoset the
     * currently selected photoset. Then remove the blocker.
     */
    @Override
    protected void done() {
	// UPDATE THE LIST MODEL

	try {
	    if (this.ssPhotoset != null) {
		// this will update the list model, but don't update the entire
		// list in the GUI
		List<SSPhotoset> list = PhotosetDAO.getPhotosetListOrderByManagedAndTitle();
		MainWindow.getMainWindow().insertPhotosetInListModel(list, ssPhotoset);

	    }
	} catch (Exception e) {
	    logger.error("ERROR WHILE TRYING TO UPDATE LIST MODEL.", e);
	    JOptionPane.showMessageDialog(null,
		    "There was an error while trying to update the list.\n"
		    + "However, the new set has been created successfully,\n"
		    + "and should appear in the list next time you start SuprSetr.",
		    "Error Updating GUI", JOptionPane.WARNING_MESSAGE);
	}
	blocker.unBlock();
    }

}
