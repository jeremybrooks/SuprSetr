/*
 *  SuprSetr is Copyright 2010-2021 by Jeremy Brooks
 *
 *  This file is part of SuprSetr.
 *
 *   SuprSetr is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SuprSetr is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SuprSetr.  If not, see <http://www.gnu.org/licenses/>.
 */


package net.jeremybrooks.suprsetr;

import net.jeremybrooks.suprsetr.utils.SSUtils;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * This is the List cell used to represent set data to the user.
 *
 * @author Jeremy Brooks
 */
public class SetListCell extends javax.swing.JPanel {

	private static final long serialVersionUID = -1125238132858767565L;
	private boolean cacheValid = false;

	// Some static references to often-used icons. No need to have more than
	// one instance of these
	private static final ImageIcon ICON_REFRESH;
	private static final ImageIcon ICON_UNMANAGED;
	private static final ImageIcon ICON_ACCEPT;
	private static final ImageIcon ICON_WARN;

	private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.setlistcell");

	static {
    ICON_REFRESH = new ImageIcon(SetListCell.class.getResource("/images/759-refresh-2-toolbar.png"));
		ICON_ACCEPT = new ImageIcon(SetListCell.class.getResource("/images/1040-checkmark-toolbar-selected-22x22.png"));
		ICON_UNMANAGED = new ImageIcon(SetListCell.class.getResource("/images/20-no-symbol.png"));
		ICON_WARN = new ImageIcon(SetListCell.class.getResource("/images/791-warning-toolbar-22x22.png"));
	}


	/**
	 * Creates new form SetCell
	 */
	public SetListCell() {
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
    lblTwitter = new JLabel();
    lblManaged = new JLabel();
    lblLastUpdate = new JLabel();

    //======== this ========
    setBorder(new BevelBorder(BevelBorder.RAISED));
    setMinimumSize(new Dimension(494, 81));
    setPreferredSize(new Dimension(0, 81));
    setLayout(new GridBagLayout());
    ((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0, 0};
    ((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0};
    ((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
    ((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

    //---- lblPic ----
    lblPic.setIcon(new ImageIcon(getClass().getResource("/images/empty_set_icon.png")));
    add(lblPic, new GridBagConstraints(0, 0, 1, 3, 0.0, 0.0,
      GridBagConstraints.CENTER, GridBagConstraints.BOTH,
      new Insets(0, 0, 0, 5), 0, 0));

    //---- lblTitle ----
    lblTitle.setFont(new Font("Lucida Grande", Font.BOLD, 13));
    lblTitle.setText(bundle.getString("SetListCell.lblTitle.text"));
    add(lblTitle, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
      GridBagConstraints.CENTER, GridBagConstraints.BOTH,
      new Insets(0, 0, 5, 5), 0, 0));

    //---- lblTwitter ----
    lblTwitter.setIcon(new ImageIcon(getClass().getResource("/images/1282-twitter-toolbar.png")));
    add(lblTwitter, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
      GridBagConstraints.CENTER, GridBagConstraints.BOTH,
      new Insets(0, 0, 5, 0), 0, 0));

    //---- lblManaged ----
    lblManaged.setIcon(new ImageIcon(getClass().getResource("/images/1040-checkmark-toolbar-selected-22x22.png")));
    lblManaged.setText(bundle.getString("SetListCell.lblManaged.text"));
    add(lblManaged, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0,
      GridBagConstraints.CENTER, GridBagConstraints.BOTH,
      new Insets(0, 0, 5, 0), 0, 0));

    //---- lblLastUpdate ----
    lblLastUpdate.setIcon(new ImageIcon(getClass().getResource("/images/1040-checkmark-toolbar-selected-22x22.png")));
    lblLastUpdate.setText(bundle.getString("SetListCell.lblLastUpdate.text"));
    add(lblLastUpdate, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0,
      GridBagConstraints.CENTER, GridBagConstraints.BOTH,
      new Insets(0, 0, 0, 0), 0, 0));
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
	 * Get the value of the title text.
	 *
	 * @return title text.
	 */
	public String getTitle() {
		return this.lblTitle.getText();
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
	 * Get the description text.
	 *
	 * @return description text.
	 */
	public String getDescription() {
		return this.getToolTipText();
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
	 * Set the text and icon to indicate if the set is managed.
	 *
	 * @param isManaged true if SuprSetr manages this set.
	 */
	public void setManaged(boolean isManaged) {
		if (isManaged) {
			this.lblManaged.setText(resourceBundle.getString("SetListCell.lblManaged.true"));
			this.lblManaged.setIcon(ICON_ACCEPT);
		} else {
			this.lblManaged.setText(resourceBundle.getString("SetListCell.lblManaged.false"));
			this.lblManaged.setIcon(ICON_UNMANAGED);
		}
	}


	/**
	 * Set the text and icon indicating that this set is ready to be refreshed.
	 *
	 * <p>The elapsed time before a set is "stale" is configured by the user,
	 * so this must be taken into account.</p>
	 *
	 * @param lastUpdate date of the last set refresh.
	 */
	public void setLastUpdate(Date lastUpdate) {
		this.lblLastUpdate.setVisible(true);
		if (lastUpdate == null) {
			this.lblLastUpdate.setText(resourceBundle.getString("SetListCell.lblLastUpdate.noupdates"));
			this.lblLastUpdate.setIcon(ICON_REFRESH);
		} else {

			this.lblLastUpdate.setText(resourceBundle.getString("SetListCell.lblLastUpdate.updated") + " " + SSUtils.formatDate(lastUpdate));

			if (SSUtils.readyForUpdate(lastUpdate)) {
				this.lblLastUpdate.setIcon(ICON_REFRESH);
			} else {
				this.lblLastUpdate.setIcon(ICON_ACCEPT);
			}
		}
	}


	/**
	 * Hides the "last update" text and icon.
	 */
	public void hideLastUpdate() {
		this.lblLastUpdate.setVisible(false);
	}


	/**
	 * Sets the warning icon.
	 *
	 * @param warn true if the warning icon should be used.
	 */
	public void setWarnIcon(boolean warn) {
		if (warn) {
			this.lblLastUpdate.setIcon(ICON_WARN);
		}
	}


	/**
	 * Flag indicating if this set is tweeted when refreshed.
	 * <p>If true, a bird icon will show up on the cell.</p>
	 *
	 * @param twitter true if tweeting is enabled for the set.
	 */
	public void setTwitter(boolean twitter) {
		this.lblTwitter.setVisible(twitter);
	}


	// Variables declaration - do not modify//GEN-BEGIN:variables
  private JLabel lblPic;
  private JLabel lblTitle;
  private JLabel lblTwitter;
  private JLabel lblManaged;
  private JLabel lblLastUpdate;
	// End of variables declaration//GEN-END:variables


	/**
	 * @return the valid
	 */
	public boolean isCacheValid() {
		return cacheValid;
	}


	/**
	 * @param cacheValid the valid to set
	 */
	public void setCacheValid(boolean cacheValid) {
		this.cacheValid = cacheValid;
	}

}
