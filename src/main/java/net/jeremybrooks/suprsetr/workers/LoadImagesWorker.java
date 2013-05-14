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
import net.jeremybrooks.jinx.api.PhotosetsApi;
import net.jeremybrooks.jinx.dto.Photo;
import net.jeremybrooks.jinx.dto.Photos;
import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.flickr.PhotoHelper;
import net.jeremybrooks.suprsetr.utils.ObjectCache;
import org.apache.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.SwingWorker;
import java.util.List;
import java.util.ResourceBundle;


/**
 * This class reordered the photosets on flickr.
 * <p/>
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 * @author jeremyb
 */
public class LoadImagesWorker extends SwingWorker<Void, Void> {

	/**
	 * Logging.
	 */
	private Logger logger = Logger.getLogger(LoadImagesWorker.class);

	/**
	 * The blocker instance used to provide user with feedback.
	 */
	private BlockerPanel blocker;

	/**
	 * The photoset to get photos from.
	 */
	private String photosetId;

	/**
	 * The page of photos to get.
	 */
	private int page;

	/**
	 * The object cache to use.
	 */
	private ObjectCache cache;

	/**
	 * The photos object used by the picker.
	 */
	private List<Photo> thePhotos;

	/**
	 * The parent frame.
	 */
	private JDialog parent;

	private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.workers");


	/**
	 * Create an instance of OrderSetsWorker.
	 *
	 * @param blocker     the blocker instance.
	 * @param photosetId string array of photoset id's.
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
		Photos p;
		int count = 1;
		int total;
		this.thePhotos.clear();
		try {
			// Now get the first 25 photos and populate the model
			p = PhotosetsApi.getInstance().getPhotos(photosetId, null, null, 25, page, JinxConstants.MEDIA_ALL, true);
			total = p.getPhotos().size();

			for (Photo photo : p.getPhotos()) {
				if (this.cache.get(photo.getId()) == null) {
					blocker.updateMessage(resourceBundle.getString("LoadImagesWorker.blocker.loading") +
							" " + (count++) + "/" + total + " ("
							+ photo.getTitle() + ")");
					ImageIcon image = PhotoHelper.getInstance().getIconForPhoto(photo.getId());
					this.cache.put(photo.getId(), image);
				}
				this.thePhotos.add(photo);
				this.parent.repaint();
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
