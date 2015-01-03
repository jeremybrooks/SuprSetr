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

import net.jeremybrooks.jinx.response.photos.Photo;
import net.jeremybrooks.jinx.response.photos.SearchParameters;
import net.jeremybrooks.jinx.response.photosets.Photoset;
import net.jeremybrooks.jinx.response.photosets.PhotosetInfo;
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

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;


/**
 * This class adds a photoset to Flickr and to the database.
 * <p/>
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 * @author jeremyb
 */
public class AddPhotosetWorker extends SwingWorker<Void, Void> {

	/**
	 * Logging.
	 */
	private Logger logger = Logger.getLogger(AddPhotosetWorker.class);

	/**
	 * The blocker instance for user feedback.
	 */
	private BlockerPanel blocker;

	/**
	 * The photoset to add.
	 */
	private SSPhotoset ssPhotoset;

	private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.workers");

	/**
	 * Create an instance of AddPhotoset.
	 *
	 * @param blocker    the blocker instance.
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
		int matches = 0;
		Photoset newSet = null;
		Photo firstPhoto = null;
		List<Photo> photos = null;
//		int total = 0;

		// TO CREATE A SET, WE NEED PHOTOS
		// SO GET SEARCH RESULTS FOR THE PARAMETERS
		try {
			logger.info(this.ssPhotoset.toString());
			blocker.updateMessage(resourceBundle.getString("AddPhotosetWorker.blocker.searching"));
			SearchParameters params;

			if (ssPhotoset.isOnThisDay()) {
				List<Photo> tempResults;
				int endYear = ssPhotoset.getOnThisDayYearEnd();
				if (endYear == 0) {
					endYear = SSUtils.getCurrentYear();
				}
				if (ssPhotoset.getSortOrder() == 2) {
					// Sorted by date taken descending
					for (int year = endYear; year >= ssPhotoset.getOnThisDayYearStart(); year--) {
						params = SearchHelper.getInstance().getSearchParametersForOnThisDay(ssPhotoset, year);
						blocker.updateMessage(resourceBundle.getString("AddPhotosetWorker.blocker.searchingon") + " "
								+ ssPhotoset.getOnThisDayMonth() + "/"
								+ ssPhotoset.getOnThisDayDay() + "/"
								+ year + "....");
						tempResults = PhotoHelper.getInstance().getPhotos(params);
						logger.info("Got " + tempResults.size() + " results.");

						if (photos == null) {
							photos = tempResults;
						} else {
							photos.addAll(tempResults);
						}
					}
				} else {
					// Sorted by date taken descending, or no particular order
					for (int year = ssPhotoset.getOnThisDayYearStart(); year <= endYear; year++) {
						params = SearchHelper.getInstance().getSearchParametersForOnThisDay(ssPhotoset, year);
						blocker.updateMessage(resourceBundle.getString("AddPhotosetWorker.blocker.searchingon") + " "
								+ +ssPhotoset.getOnThisDayMonth() + "/"
								+ ssPhotoset.getOnThisDayDay() + "/"
								+ year + "....");
						tempResults = PhotoHelper.getInstance().getPhotos(params);
						logger.info("Got " + tempResults.size() + " results.");
						if (photos == null) {
							photos = tempResults;
						} else {
							photos.addAll(tempResults);
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
				matches = photos.size();
			}

			if (matches > 0) {
				logger.info("Got " + matches + " search results.");

				// sort by title or random, if necessary
				if (ssPhotoset.getSortOrder() == 7) {
					SSUtils.sortPhotoListByTitleDescending(photos);
				} else if (ssPhotoset.getSortOrder() == 8) {
					SSUtils.sortPhotoListByTitleAscending(photos);
				} else if (ssPhotoset.getSortOrder() == 9) {
					Collections.shuffle(photos);
				}

				firstPhoto = photos.get(0);


				blocker.updateMessage(resourceBundle.getString("AddPhotosetWorker.blocker.creating"));
				// CREATE THE SET
				PhotosetInfo info = PhotosetHelper.getInstance().createPhotoset(this.ssPhotoset.getTitle(), this.ssPhotoset.getDescription(), firstPhoto.getPhotoId());
				newSet = info.getPhotoset();

				blocker.updateMessage(resourceBundle.getString("AddPhotosetWorker.blocker.applying"));
				// ADD PHOTOS TO THE SET
				PhotosetHelper.getInstance().editPhotos(newSet.getPhotosetId(), firstPhoto.getPhotoId(), photos);

				LogWindow.addLogMessage(resourceBundle.getString("AddPhotosetWorker.logmessage.added") +
						" '" +
						this.ssPhotoset.getTitle() +
						"' " +
						resourceBundle.getString("AddPhotosetWorker.logmessage.with") +
						" " +
						photos.size() +
						" " +
						resourceBundle.getString("AddPhotosetWorker.logmessage.photos"));
			} else {
				JOptionPane.showMessageDialog(null,
						resourceBundle.getString("AddPhotosetWorker.dialog.nophotos.message"),
						resourceBundle.getString("AddPhotosetWorker.dialog.nophotos.title"),
						JOptionPane.INFORMATION_MESSAGE);

				// null out the photoset, telling the done() method that no
				// set was added
				this.ssPhotoset = null;
			}
		} catch (Exception e) {
			logger.error("ERROR ADDING SET.", e);
			JOptionPane.showMessageDialog(null,
					resourceBundle.getString("AddPhotosetWorker.dialog.addseterror.message") +
							" " + e.getMessage(),
					resourceBundle.getString("AddPhotosetWorker.dialog.addseterror.title"),
					JOptionPane.ERROR_MESSAGE);
		}

		if (matches > 0) {
			try {
                if (newSet == null) {
                    throw new Exception("New set was null; cannot add.");
                }
                // count photos and videos
                // counting media type avoids a call to get photoset info
                int videoCount = 0;
                int photoCount = 0;
                for (Photo p : photos) {
                    if (p.getMedia().equalsIgnoreCase("video")) {
                        videoCount++;
                    } else {
                        photoCount++;
                    }
                }
				blocker.updateMessage(resourceBundle.getString("AddPhotosetWorker.blocker.saving"));
                this.ssPhotoset.setFarm(newSet.getFarm() == null ? 0 : Integer.parseInt(newSet.getFarm()));
                this.ssPhotoset.setPhotosetId(newSet.getPhotosetId());
				this.ssPhotoset.setLastRefreshDate(new Date());
				this.ssPhotoset.setPhotos(photoCount);
                this.ssPhotoset.setVideos(videoCount);
				this.ssPhotoset.setPrimaryPhotoIcon(PhotoHelper.getInstance().getIconForPhoto(firstPhoto.getPhotoId()));
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
						resourceBundle.getString("AddPhotosetWorker.dialog.setsaveerror.message") +
								" " + e.getMessage(),
						resourceBundle.getString("AddPhotosetWorker.dialog.setsaveerror.title"),
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
					resourceBundle.getString("dialog.guierror.message"),
					resourceBundle.getString("dialog.guierror.title"),
					JOptionPane.WARNING_MESSAGE);
		}
		blocker.unBlock();
	}
}
