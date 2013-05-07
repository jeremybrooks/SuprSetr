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

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ResourceBundle;


/**
 * This is the List cell used to represent set data to the user.
 *
 * @author jeremyb
 */
public class SetOrdererCell extends javax.swing.JPanel {


	private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.setorderercell");


	/**
	 * Creates new form SetCell
	 */
	public SetOrdererCell() {
		initComponents();
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		ResourceBundle bundle = this.resourceBundle;
		lblPic = new JLabel();
		lblTitle = new JLabel();
		lblCount = new JLabel();

		//======== this ========
		setBorder(new BevelBorder(BevelBorder.RAISED));
		setMinimumSize(new Dimension(494, 81));
		setPreferredSize(new Dimension(0, 81));
		setLayout(null);

		//---- lblPic ----
		lblPic.setIcon(new ImageIcon(getClass().getResource("/images/empty_set_icon.png")));
		add(lblPic);
		lblPic.setBounds(2, 3, 75, 75);

		//---- lblTitle ----
		lblTitle.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lblTitle.setText(bundle.getString("SetOrdererCell.lblTitle.text"));
		add(lblTitle);
		lblTitle.setBounds(80, 10, 210, 23);

		//---- lblCount ----
		lblCount.setText(bundle.getString("SetOrdererCell.lblCount.text"));
		add(lblCount);
		lblCount.setBounds(80, 50, 215, lblCount.getPreferredSize().height);

		{ // compute preferred size
			Dimension preferredSize = new Dimension();
			for(int i = 0; i < getComponentCount(); i++) {
				Rectangle bounds = getComponent(i).getBounds();
				preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
				preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
			}
			Insets insets = getInsets();
			preferredSize.width += insets.right;
			preferredSize.height += insets.bottom;
			setMinimumSize(preferredSize);
			setPreferredSize(preferredSize);
		}
	}// </editor-fold>//GEN-END:initComponents


	/**
	 * Set the title for the cell.
	 *
	 * @param title text to be used for the title.
	 */
	public void setTitle(String title) {
		this.lblTitle.setText(title);
	}


	/**
	 * Set the image of the cell.
	 *
	 * @param image the image to display.
	 */
	public void setImage(ImageIcon image) {
		this.lblPic.setIcon(image);
	}


	/**
	 * Set the description of the cell, as the tooltip.
	 *
	 * @param description description to be used as the tooltip.
	 */
	public void setDescription(String description) {
		this.setToolTipText(description);
	}


	/**
	 * Set the background to indicate that this cell is selected.
	 *
	 * @param isSelected true if the cell is selected.
	 */
	public void setSelected(boolean isSelected) {
		if (isSelected) {
			setBackground(java.awt.SystemColor.controlHighlight);
		} else {
			setBackground(null);
		}
	}


	/**
	 * Set the photo count label.
	 *
	 * @param count number of photos in the set.
	 */
	public void setPhotoCount(int count) {
		if (count == 1) {
			this.lblCount.setText(count + " " + resourceBundle.getString("SetOrdererCell.photo"));
		} else {
			this.lblCount.setText(count + " " + resourceBundle.getString("SetOrdererCell.photos"));
		}
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private JLabel lblPic;
	private JLabel lblTitle;
	private JLabel lblCount;
	// End of variables declaration//GEN-END:variables
}
