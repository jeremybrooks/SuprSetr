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

package net.jeremybrooks.suprsetr;

import java.awt.Component;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * Custom renderer for the List of sets on the main window.
 *
 * @author jeremyb
 */
public class SetListRenderer implements ListCellRenderer {



    /**
     * Return a custom Component that represents the photoset.
     *
     * @param list the list.
     * @param value this is an instance of SSPhotoset.
     * @param index the index
     * @param isSelected true if the list item is selected.
     * @param cellHasFocus true if the cell has focus.
     * @return instance of SetListCell representing the photoset.
     */
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
	SetListCell cell = new SetListCell();
	StringBuilder sb = new StringBuilder();

	if (value instanceof SSPhotoset) {
	    SSPhotoset def = (SSPhotoset) value;

	    sb.append(def.getTitle());
	    sb.append("   [");
	    sb.append(def.getPhotos());
	    sb.append(" photo");
	    if (def.getPhotos() != 1) {
		sb.append('s');
	    }
	    sb.append(']');
	    cell.setTitle(sb.toString());

	    if (def.getDescription() == null || def.getDescription().isEmpty()) {
		cell.setDescription("This photoset does not have a description.");
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

	}
	cell.setSelected(isSelected);

	return cell;
    }

}
