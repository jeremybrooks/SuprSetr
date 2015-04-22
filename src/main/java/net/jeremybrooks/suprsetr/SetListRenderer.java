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

package net.jeremybrooks.suprsetr;

import net.jeremybrooks.suprsetr.utils.SimpleCache;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Component;
import java.util.ResourceBundle;


/**
 * Custom renderer for the List of sets on the main window.
 *
 * @author jeremyb
 */
public class SetListRenderer implements ListCellRenderer {

	/**
	 * Cache the set list instances.
	 */
	private SimpleCache cache;

	private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.misc");

	public SetListRenderer() {
		super();
		this.cache = SimpleCache.getInstance();
	}


	/**
	 * Return a custom Component that represents the photoset.
	 *
	 * @param list         the list.
	 * @param value        this is an instance of SSPhotoset.
	 * @param index        the index
	 * @param isSelected   true if the list item is selected.
	 * @param cellHasFocus true if the cell has focus.
	 * @return instance of SetListCell representing the photoset.
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		SSPhotoset def;
		SetListCell cell;

		if (!(value instanceof SSPhotoset)) {
			cell = new SetListCell();
			cell.setTitle(resourceBundle.getString("SetListRenderer.error.wrongclass"));

		} else {
			def = (SSPhotoset) value;

			cell = this.cache.getFromCache(def.getPhotosetId());
			if (cell == null) {
				cell = new SetListCell();
				cell.setCacheValid(false);
			}

			if (!cell.isCacheValid()) {
				StringBuilder sb = new StringBuilder();

				sb.append(def.getTitle());
				sb.append("  [");
				sb.append(def.getPhotos()).append(' ');
				if (def.getPhotos() == 1) {
					sb.append(resourceBundle.getString("SetListRenderer.text.photo"));
				} else {
					sb.append(resourceBundle.getString("SetListRenderer.text.photos"));
				}
				if (def.getVideos() > 0) {
					sb.append(", ").append(def.getVideos()).append(" ");
					if (def.getVideos() == 1) {
						sb.append(resourceBundle.getString("SetListRenderer.text.video"));
					} else {
						sb.append(resourceBundle.getString("SetListRenderer.text.videos"));
					}
				}

				if (def.getViewCount() != -1) {
					sb.append(", ").append(def.getViewCount()).append(' ');
					if (def.getViewCount() == 1) {
						sb.append(resourceBundle.getString("SetListRenderer.text.count"));
					} else {
						sb.append(resourceBundle.getString("SetListRenderer.text.counts"));
					}
				}

				sb.append(']');

				cell.setTitle(sb.toString());

				if (def.getDescription() == null || def.getDescription().isEmpty()) {
					cell.setDescription(resourceBundle.getString("SetListRenderer.text.nodescription"));
				} else {
					cell.setDescription(def.getDescription());
				}

				if (def.isManaged()) {
					cell.setLastUpdate(def.getLastRefreshDate());
				} else {
					cell.hideLastUpdate();
				}

				cell.setManaged(def.isManaged());

				if (def.getPrimaryPhotoIcon() != null) {
					cell.setImage(def.getPrimaryPhotoIcon());
				}

				cell.setTwitter(def.isSendTweet());

				cell.setWarnIcon(def.isErrorFlag());

				cell.setCacheValid(true);
				this.cache.putInCache(def.getPhotosetId(), cell);
			}
		}
		cell.setSelected(isSelected);
		return cell;
	}

}
