/*
 * SuprSetr is Copyright 2010-2011 by Jeremy Brooks
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

import java.util.Date;
import javax.swing.ImageIcon;
import net.jeremybrooks.suprsetr.utils.SSUtils;

/**
 * This is the List cell used to represent set data to the user.
 *
 * @author jeremyb
 */
public class SetListCell extends javax.swing.JPanel {

    private boolean cacheValid = false;

    // Some static references to often-used icons. No need to have more than
    // one instance of these
    private static final ImageIcon ICON_REFRESH;
    private static final ImageIcon ICON_UNMANAGED;
    private static final ImageIcon ICON_ACCEPT;
    private static final ImageIcon ICON_WARN;
    
    /** 24 hours in milliseconds. */
    private static final long MILLIS_24_HOURS = 86400000L;

    static {
	ICON_REFRESH = new ImageIcon(SetListCell.class.getResource("/images/refresh16.png"));
	ICON_ACCEPT = new ImageIcon(SetListCell.class.getResource("/images/accept.png"));
	ICON_UNMANAGED = new ImageIcon(SetListCell.class.getResource("/images/unmanaged.png"));
	ICON_WARN = new ImageIcon(SetListCell.class.getResource("/images/warning.png"));
    }

    
    /** Creates new form SetCell */
    public SetListCell() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTwitter = new javax.swing.JLabel();
        lblPic = new javax.swing.JLabel();
        lblTitle = new javax.swing.JLabel();
        lblLastUpdate = new javax.swing.JLabel();
        lblManaged = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setMinimumSize(new java.awt.Dimension(494, 81));
        setPreferredSize(new java.awt.Dimension(494, 81));
        setSize(new java.awt.Dimension(0, 81));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTwitter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/twitter24.png"))); // NOI18N
        add(lblTwitter, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 0, -1, -1));

        lblPic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/empty_set_icon.png"))); // NOI18N
        add(lblPic, new org.netbeans.lib.awtextra.AbsoluteConstraints(2, 3, 75, 75));

        lblTitle.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        lblTitle.setText("Set Title");
        add(lblTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 0, -1, 23));

        lblLastUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/accept.png"))); // NOI18N
        lblLastUpdate.setText("Last Update:");
        add(lblLastUpdate, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 60, -1, 23));

        lblManaged.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/accept.png"))); // NOI18N
        lblManaged.setText("Managed/Unmanaged");
        add(lblManaged, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 30, -1, 23));
    }// </editor-fold>//GEN-END:initComponents


    /**
     * Set the title for the cell.
     * @param title text to be used for the title.
     */
    public void setTitle(String title) {
        this.lblTitle.setText(title);
    }

    /**
     * Get the value of the title text.
     * @return title text.
     */
    public String getTitle() {
	return this.lblTitle.getText();
    }


    /**
     * Set the image of the cell.
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
	    this.lblManaged.setText("This set is managed by SuprSetr.");
	    this.lblManaged.setIcon(ICON_ACCEPT);
	} else {
	    this.lblManaged.setText("This set is not managed by SuprSetr.");
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
	    this.lblLastUpdate.setText("This set has not been updated.");
	    this.lblLastUpdate.setIcon(ICON_REFRESH);
	} else {

	    this.lblLastUpdate.setText("Last updated " + SSUtils.formatDate(lastUpdate));

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
    private javax.swing.JLabel lblLastUpdate;
    private javax.swing.JLabel lblManaged;
    private javax.swing.JLabel lblPic;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTwitter;
    // End of variables declaration//GEN-END:variables



    /**
     * @return the valid
     */
    public boolean isCacheValid() {
	return cacheValid;
    }


    /**
     * @param valid the valid to set
     */
    public void setCacheValid(boolean cacheValid) {
	this.cacheValid = cacheValid;
    }

}
