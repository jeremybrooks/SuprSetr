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
import net.jeremybrooks.suprsetr.utils.SimpleCache;
import org.apache.log4j.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;


/**
 * This class refreshes a photoset on Flickr, or a list of photosets.
 * <p/>
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 * @author jeremyb
 */
public class RefreshPhotosetWorker extends SwingWorker<Void, Void> {

	/**
	 * Logging.
	 */
	private Logger logger = Logger.getLogger(RefreshPhotosetWorker.class);

	/**
	 * The blocker used for feedback.
	 */
	private BlockerPanel blocker;

	private boolean exitWhenDone = false;

	/**
	 * The list of photosets.
	 */
	private List<SSPhotoset> photosetList = null;

	private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.workers");


	/**
	 * Create an instance of RefreshPhotoset.
	 * <p/>
	 * <p>All photosets in the list will be refreshed. The caller should make
	 * sure that the sets in the list are eligible for refresh, or that the user
	 * wants to refresh the sets early.</p>
	 *
	 * @param blocker      the blocker.
	 * @param photosetList list of photosets to refresh.
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
		int matches;
		Photos searchResults = null;
		SearchParameters params;
		String newPrimaryPhotoId = null;
		String currentPrimaryId;

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
					Photos tempResults;
					int endYear = ssPhotoset.getOnThisDayYearEnd();
					if (endYear == 0) {
						endYear = SSUtils.getCurrentYear();
					}
					if (ssPhotoset.getSortOrder() == 2) {
						// Sorted by date taken descending
						for (int year = endYear; year >= ssPhotoset.getOnThisDayYearStart(); year--) {
							params = SearchHelper.getInstance().getSearchParametersForOnThisDay(ssPhotoset, year);
							blocker.updateMessage(resourceBundle.getString("RefreshPhotosetWorker.blocker.searchingon") + " " +
									+ssPhotoset.getOnThisDayMonth() + "/"
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
							blocker.updateMessage(resourceBundle.getString("RefreshPhotosetWorker.blocker.searchingon") + " " +
									+ssPhotoset.getOnThisDayMonth() + "/"
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

					// sort by title or random, if necessary
					if (ssPhotoset.getSortOrder() == 7) {
						SSUtils.sortPhotoListByTitleDescending(searchResults.getPhotos());
					} else if (ssPhotoset.getSortOrder() == 8) {
						SSUtils.sortPhotoListByTitleAscending(searchResults.getPhotos());
					} else if (ssPhotoset.getSortOrder() == 9) {
						Collections.shuffle(searchResults.getPhotos());
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


					blocker.updateMessage(resourceBundle.getString("RefreshPhotosetWorker.blocker.applying"));
					// ADD PHOTOS TO THE SET
					PhotosetHelper.getInstance().editPhotos(ssPhotoset.getId(), newPrimaryPhotoId, searchResults.getPhotos());

					time = System.currentTimeMillis() - time;

					StringBuilder sb = new StringBuilder(resourceBundle.getString("RefreshPhotosetWorker.log.refresh1"));
					sb.append(" '").append(ssPhotoset.getTitle());
					sb.append("'. ").append(resourceBundle.getString("RefreshPhotosetWorker.log.refresh2")).append(" ").append(oldCount);
					sb.append(" ").append(resourceBundle.getString("RefreshPhotosetWorker.log.refresh3")).append(" ").append(searchResults.getTotal());
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
			if (ssPhotoset.isManaged() && (searchResults != null)) {	// null search results means something failed above, so don't bother with saving to db
				blocker.updateMessage(resourceBundle.getString("RefreshPhotosetWorker.blocker.saving"));

				// UPDATE OUR DATA STRUCTURE TO REFLECT THE NEW PHOTOSET ON FLICKR
				ssPhotoset.setLastRefreshDate(new Date());
				ssPhotoset.setPhotos(searchResults.getPhotos().size());

				ssPhotoset.setSyncTimestamp(System.currentTimeMillis());
			} //- end if is managed

			// mark the list cell as invalid, so anything that has changed
			// will get updated when the list is repainted
			SimpleCache.getInstance().invalidate(ssPhotoset.getId());

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
			String id;
			id = this.photosetList.get(0).getId();
			MainWindow.getMainWindow().doFilter(id);
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
			System.exit(0);
		}
	}
}
