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


/**
 * This is the List cell used to represent set data to the user.
 *
 * @author jeremyb
 */
public class SetOrdererCell extends javax.swing.JPanel {



    
    /** Creates new form SetCell */
    public SetOrdererCell() {
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

        lblPic = new javax.swing.JLabel();
        lblTitle = new javax.swing.JLabel();
        lblCount = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        setMinimumSize(new java.awt.Dimension(494, 81));
        setPreferredSize(new java.awt.Dimension(300, 81));
        setSize(new java.awt.Dimension(0, 81));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblPic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/empty_set_icon.png"))); // NOI18N
        add(lblPic, new org.netbeans.lib.awtextra.AbsoluteConstraints(2, 3, 75, 75));

        lblTitle.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        lblTitle.setText("Set Title");
        add(lblTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 10, -1, 23));

        lblCount.setText("jLabel1");
        add(lblCount, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 50, -1, -1));
    }// </editor-fold>//GEN-END:initComponents


    /**
     * Set the title for the cell.
     * @param title text to be used for the title.
     */
    public void setTitle(String title) {
        this.lblTitle.setText(title);
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
     * 
     * @param count number of photos in the set.
     */
    public void setPhotoCount(int count) {
	if (count == 1) {
	    this.lblCount.setText(count + " photo");
	} else {
	    this.lblCount.setText(count + " photos");
	}
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblCount;
    private javax.swing.JLabel lblPic;
    private javax.swing.JLabel lblTitle;
    // End of variables declaration//GEN-END:variables

}
