/*
 * SuprSetr is Copyright 2010 by Jeremy Brooks
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
package net.jeremybrooks.suprsetr.workers;


import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import net.jeremybrooks.jinx.dto.Photo;
import net.jeremybrooks.jinx.dto.Photos;
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
 * This class refreshes a photoset on Flickr, or a list of photosets.
 *
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 *
 * @author jeremyb
 */
public class RefreshPhotosetWorker extends SwingWorker<Void, Void> {

    /** Logging. */
    private Logger logger = Logger.getLogger(RefreshPhotosetWorker.class);

    /** The blocker used for feedback. */
    private BlockerPanel blocker;


    /** The list of photosets. */
    private List<SSPhotoset> photosetList = null;



    /**
     * Create an instance of RefreshPhotoset.
     *
     * <p>All photosets in the list will be refreshed. The caller should make
     * sure that the sets in the list are eligible for refresh, or that the user
     * wants to refresh the sets early.</p>
     *
     * @param blocker the blocker.
     * @param photosetList list of photosets to refresh.
     */
    public RefreshPhotosetWorker(BlockerPanel blocker, List<SSPhotoset> photosetList) {
	this.blocker = blocker;
	this.photosetList = photosetList;
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
		    MainWindow.getMainWindow().scrollToPhotoset(set.getId());
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
	int oldCount = ssPhotoset.getPhotos();
	int matches = 0;
	Photos searchResults = null;
	SearchParameters params = null;
	String newPrimaryPhotoId = null;
	String currentPrimaryId = null;

	blocker.setTitle("Refreshing '" + ssPhotoset.getTitle() + "'");

	long time = System.currentTimeMillis();

	// assume things will be OK
	ssPhotoset.setErrorFlag(false);

	if (ssPhotoset.isManaged()) {
	    blocker.updateMessage("Searching for matching photos....");
	    try {

		// get the search results
		logger.info(ssPhotoset.toString());

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
			    blocker.updateMessage("Searching for matching photos on " +
				+ ssPhotoset.getOnThisDayMonth() + "/"
				+ ssPhotoset.getOnThisDayDay() + "/"
				+ year + "....");
			    tempResults = PhotoHelper.getInstance().getPhotos(params);
			    logger.info("Got " + tempResults.getTotal() + " results.");
			    if (searchResults == null) {
				searchResults = tempResults;
			    } else {
				searchResults.setTotal(searchResults.getTotal() + tempResults.getTotal());
				List<Photo> list = searchResults.getPhotos();
				list.addAll(tempResults.getPhotos());
				searchResults.setPhotos(list);
			    }
			}
		    } else {
			for (int year = ssPhotoset.getOnThisDayYearStart(); year <= endYear; year++) {
			    params = SearchHelper.getInstance().getSearchParametersForOnThisDay(ssPhotoset, year);
			    blocker.updateMessage("Searching for matching photos on " +
				+ ssPhotoset.getOnThisDayMonth() + "/"
				+ ssPhotoset.getOnThisDayDay() + "/"
				+ year + "....");
			    tempResults = PhotoHelper.getInstance().getPhotos(params);
			    logger.info("Got " + tempResults.getTotal() + " results.");
			    if (searchResults == null) {
				searchResults = tempResults;
			    } else {
				searchResults.setTotal(searchResults.getTotal() + tempResults.getTotal());
				List<Photo> list = searchResults.getPhotos();
				list.addAll(tempResults.getPhotos());
				searchResults.setPhotos(list);
			    }
			}
		    }
		} else {
		    params = SearchHelper.getInstance().getSearchParameters(ssPhotoset);
		    if (ssPhotoset.isLimitSize()) {
			searchResults = PhotoHelper.getInstance().getPhotos(params, ssPhotoset.getSizeLimit());
		    } else {
			searchResults = PhotoHelper.getInstance().getPhotos(params);
		    }
		}

		if (searchResults == null) {
		    matches = 0;
		} else {
		    matches = searchResults.getPhotos().size();
		}

		logger.info("Got " + matches + " search results.");

		if (matches > 0) {

		    // sort by title, if necessary
		    if (ssPhotoset.getSortOrder() == 7) {
			SSUtils.sortPhotoListByTitleDescending(searchResults.getPhotos());
		    } else if (ssPhotoset.getSortOrder() == 8) {
			SSUtils.sortPhotoListByTitleAscending(searchResults.getPhotos());
		    }
		    

		    // determine which photo should be the primary photo
		    if (ssPhotoset.isLockPrimaryPhoto()) {
			currentPrimaryId = ssPhotoset.getPrimary();

			// if the current primary photo is in the search results,
			// use it
			for (Photo p : searchResults.getPhotos()) {
			    if (p.getId().equals(currentPrimaryId)) {
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
			Photo p = searchResults.getPhotos().get(0);
			logger.info("Using photo " + p.getTitle() + " as new primary photo.");
			newPrimaryPhotoId = p.getId();
			ssPhotoset.setPrimary(newPrimaryPhotoId);
			ssPhotoset.setPrimaryPhotoIcon(PhotoHelper.getInstance().getIconForPhoto(newPrimaryPhotoId));
		    }


		    blocker.updateMessage("Applying changes to photoset....");
		    // ADD PHOTOS TO THE SET
		    PhotosetHelper.getInstance().editPhotos(ssPhotoset.getId(), newPrimaryPhotoId, searchResults.getPhotos());

		    time = System.currentTimeMillis() - time;

		    StringBuilder sb = new StringBuilder("Refreshed photoset '");
		    sb.append(ssPhotoset.getTitle());
		    sb.append("'. Set had ").append(oldCount);
		    sb.append(" photos, now has ").append(searchResults.getTotal());
		    sb.append(" photos. Elapsed time ");
		    sb.append(time).append("ms");

		    logger.info(sb.toString());
		    LogWindow.addLogMessage(sb.toString());


		} else {
		    logger.warn("No results found for set " + ssPhotoset.getTitle()
			    + ". This is probably temporary.");

		    LogWindow.addLogMessage(" WARN: No search results found for set '"
			    + ssPhotoset.getTitle() + "'");
		    ssPhotoset.setErrorFlag(true);

		}

	    } catch (Exception e) {
		logger.error("ERROR REFRESHING SET " + ssPhotoset, e);

		LogWindow.addLogMessage("ERROR: While refreshing set '"
			+ ssPhotoset.getTitle() + "', error was " + e.getMessage()
			+ ". See log for details.");
		ssPhotoset.setErrorFlag(true);

		//throw e;
	    }
	} //- end if is managed

	try {
	    if (ssPhotoset.isManaged()) {
		blocker.updateMessage("Saving set information to database....");

		// UPDATE OUR DATA STRUCTURE TO REFLECT THE NEW PHOTOSET ON FLICKR
		ssPhotoset.setLastRefreshDate(new Date());
		ssPhotoset.setPhotos(searchResults.getPhotos().size());

		ssPhotoset.setSyncTimestamp(System.currentTimeMillis());
	    } //- end if is managed


	    PhotosetDAO.updatePhotoset(ssPhotoset);


	    // Send tweet if user has requested it AND there are new photos in the set
	    if (ssPhotoset.isSendTweet() && oldCount != ssPhotoset.getPhotos()) {
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
	    if (this.photosetList.size() == 1) {
		MainWindow.getMainWindow().scrollToPhotoset(this.photosetList.get(0).getId());
	    } else {
		MainWindow.getMainWindow().makeIndexVisibleAndSelected(0);
	    }


	} catch (Exception e) {
	    logger.error("ERROR WHILE TRYING TO UPDATE LIST MODEL.", e);
	    JOptionPane.showMessageDialog(null,
		    "There was an error while trying to update the list.\n"
		    + "However, the new set has been refreshed successfully,\n"
		    + "and should appear in the list next time you start SuprSetr.",
		    "Error Updating GUI", JOptionPane.WARNING_MESSAGE);
	}
	blocker.unBlock();
    }

}
