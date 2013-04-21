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

import net.jeremybrooks.jinx.JinxConstants;
import net.jeremybrooks.jinx.JinxException;
import net.jeremybrooks.jinx.dto.Photo;
import net.jeremybrooks.jinx.dto.Photos;
import net.jeremybrooks.jinx.dto.SearchParameters;
import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.LogWindow;
import net.jeremybrooks.suprsetr.MainWindow;
import net.jeremybrooks.suprsetr.SSConstants;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import net.jeremybrooks.suprsetr.flickr.FlickrHelper;
import net.jeremybrooks.suprsetr.flickr.PhotoHelper;
import org.apache.log4j.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * This class scans a user's photos, adding favxx tags depending on how many
 * times a photo has been faved.
 * <p/>
 * <p>This operation can take a very long time!</p>
 * <p/>
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 * @author jeremyb
 */
public class FavrTagrWorker extends SwingWorker<Void, Void> {

	/**
	 * Logging.
	 */
	private Logger logger = Logger.getLogger(FavrTagrWorker.class);

	/**
	 * The blocker to provide user with feedback.
	 */
	private BlockerPanel blocker;

	/**
	 * Count of how many photos had tags added.
	 */
	private int count = 0;

	/**
	 * The fave interval, default to 10.
	 */
	private int interval = 10;

	/**
	 * Flag to indicate if there were errors.
	 */
	private boolean hasErrors = false;

	/**
	 * Create a new instance of FavrTagr.
	 *
	 * @param blocker the blocker instance.
	 */
	public FavrTagrWorker(BlockerPanel blocker) {
		this.blocker = blocker;
		try {
			this.interval = Integer.parseInt(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_FAVRTAGR_INTERVAL));
		} catch (Exception e) {
			// default value of 10 will be used
		}

		logger.info("Fave tag interval is " + this.interval);
	}


	/**
	 * Execute the Flickr operations on a background thread.
	 * <p/>
	 * <p>This method searches all the users photos, then gets the fave count
	 * for each photo. It then checks to see if the fave count is greater than
	 * the fave interval, and computes the tags needed for each fave interval.
	 * Any tags that are not already on the photo will be added.</p>
	 *
	 * @return this method does not return any data.
	 */
	@Override
	protected Void doInBackground() {
		Photos photos = null;
		List<String> newFaves = null;
		int processed = 0;
		int total = 0;

		try {
			blocker.setTitle("FavrTagr Running");
			blocker.updateMessage("Getting a list of your photos....");

			// Search for:
			//    All media types
			//    Uploaded from the beginning of time until tomorrow
			//    Return tags as well
			SearchParameters params = new SearchParameters();
			params.setUserId(FlickrHelper.getInstance().getNSID());
			params.setMedia(JinxConstants.MEDIA_ALL);
			params.setMinUploadDate(new Date(0));
			params.setMaxUploadDate(new Date(System.currentTimeMillis() + 86400000));
			params.setExtras(JinxConstants.EXTRAS_TAGS);
			photos = PhotoHelper.getInstance().getPhotos(params);

			total = photos.getTotal();

			logger.info("Got " + total + " photos.");

			blocker.updateMessage("Looking for photos to tag...");

			blocker.setTitle("FavrTagr Processed " + processed + "/" + total);

			// iterate through all photos
			for (Photo p : photos.getPhotos()) {
				int faves = PhotoHelper.getInstance().getFavoriteCount(p);
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
						StringBuilder sb = new StringBuilder("FavrTagr: Photo ");
						sb.append(p.getId()).append(" <").append(p.getUrl());
						sb.append("> has too many tags! Cannot add any more.");
						LogWindow.addLogMessage(sb.toString());
					} else if (existingTags.size() + newFaves.size() > 75) {
						// remove elements from the beginning of the list as needed

						int del = (existingTags.size() + newFaves.size()) - 75;
						newFaves.subList(0, del).clear();

						LogWindow.addLogMessage("Photo " + p.getId() +
								" had too many tags. Tag list was trimmed.");
					}

					// Don't even try if there are too many tags.
					if (existingTags.size() < 75) {
						try {
							PhotoHelper.getInstance().addTags(p, newFaves.toArray(new String[0]));
							LogWindow.addLogMessage("Photo " + p.getId() + " was tagged with: " + newFaves);
							blocker.updateMessage("Tagged '" + p.getTitle() + "', looking for more photos to tag...");
							this.count++;
						} catch (JinxException je) {
							if (je.getErrorCode() == 2) {
								// Too many tags, so display a message in the log window,
								// and set a flag
								// This should not happen unless Flickr changes
								// the limit, since we already check for the number
								// of tags before attempting to add more.
								this.hasErrors = true;
								StringBuilder sb = new StringBuilder("FavrTagr: Photo ");
								sb.append(p.getId()).append(" <").append(p.getUrl());
								sb.append("> has too many tags! Cannot add any more.");
								LogWindow.addLogMessage(sb.toString());
							}
						} catch (Exception e) {

							logger.warn("ERROR ADDING TAGS.", e);
						}
					}
				}

				processed++;
				if (processed % 100 == 0) {
					blocker.setTitle("FavrTagr Processed " + processed + "/" + total);
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
		StringBuilder message = new StringBuilder("Added new fav tags to ");
		message.append(this.count);
		if (this.count == 1) {
			message.append(" photo.");
		} else {
			message.append(" photos.");
		}

		blocker.unBlock();
		LogWindow.addLogMessage("FavrTagr finished. " + message.toString());
		JOptionPane.showMessageDialog(MainWindow.getMainWindow(),
				message.toString(),
				"FavrTagr Finished",
				JOptionPane.INFORMATION_MESSAGE);


		if (this.hasErrors) {
			JOptionPane.showMessageDialog(MainWindow.getMainWindow(),
					"Some photos could not be tagged because they already\n" +
							"have too many tags. Look in the Activity Log\n" +
							"(View -> Show Activity Log) for details.",
					"Errors",
					JOptionPane.ERROR_MESSAGE);
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
		// If user selected special interval "10 up to 100, then 100", or "The Hawk:...."
		// set 10 as the initial value
		int myInterval = this.interval;
		if (this.interval == 0 || this.interval == 4) {
			myInterval = 10;
		}

		// If the fave count is less than the minimum fave tag interval, return null
		if (faves < myInterval) {
			return null;
		}

		List<String> list = new ArrayList<String>();
		int faveCheck = myInterval;

		// Check to see if the fave count is greater than each fave interval
		// add a tag to the list if needed each time the count is greater
		// than the fave interval
		while (faves >= faveCheck) {
			String tag = "fav" + faveCheck;
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

			// for special mode "The Hawk....", only check for 10, 25, and 100 faves
			if (this.interval == 4) {
				if (faveCheck == 10) {
					faveCheck = 25;
				} else if (faveCheck == 25) {
					faveCheck = 100;
				} else if (faveCheck == 100) {
					faveCheck = Integer.MAX_VALUE;
				}
			}
		}

		return list;
	}


}
