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

package net.jeremybrooks.suprsetr;


import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Component;


/**
 * Custom renderer for the List of sets on the main window.
 *
 * @author jeremyb
 */
public class SetOrdererRenderer implements ListCellRenderer {


	/**
	 * Return a custom Component that represents the photoset.
	 *
	 * @param list         the list.
	 * @param value        this is an instance of Photoset.
	 * @param index        the index
	 * @param isSelected   true if the list item is selected.
	 * @param cellHasFocus true if the cell has focus.
	 * @return instance of SetListCell representing the photoset.
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		SetOrdererCell soc = new SetOrdererCell();
		if (value instanceof SetOrdererDTO) {
			SetOrdererDTO dto = (SetOrdererDTO) value;
			soc.setTitle(dto.getTitle());
			soc.setDescription(dto.getDescription());
			soc.setPhotoAndVideoCount(dto.getPhotoCount(), dto.getVideoCount());
			if (dto.getIcon() != null) {
				soc.setImage(dto.getIcon());
			}
		}
		soc.setSelected(isSelected);
		return soc;
	}
}
