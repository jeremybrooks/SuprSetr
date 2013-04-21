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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import net.jeremybrooks.jinx.dto.Photo;
import net.jeremybrooks.suprsetr.workers.LoadImagesWorker;
import net.whirljack.common.ObjectCache;
import org.apache.log4j.Logger;


/**
 * Display a dialog showing the photos in the photoset.
 *
 * When the user selects a photo and clicks Save, the selected photo will become
 * the primary photo on the SetEditor window.
 *
 * This class uses an Object Cache to save square images that have already
 * been downloaded. This will reduce traffic to Flickr and improve performance.
 * Once the cache is created, it will persist for the lifetime of the
 * application.
 * 
 * @author jeremyb
 */
public class PhotoPickerDialog extends javax.swing.JDialog {

    /** Logging. */
    private Logger logger = Logger.getLogger(PhotoPickerDialog.class);

    /** The photoset that we are displaying photos from. */
    private SSPhotoset photoset;

    /** Cache image icons. */
    private ObjectCache cache = null;
    
    /** The dataset used by the table model. */
    private List<Photo> photos;

    /** The page of results we are on. There are 25 photos per page. */
    private int page = 1;

    /** The total number of pages we will need for all photos in the set. */
    private int pages = 1;

    /** The parent frame (should be SetEditor). */
    private JDialog parent;


    /**
     * Creates a new PhotoPickerDialog.
     *
     * The first page of photos will be loaded here.
     *
     * @param parent parent window.
     * @param modal should this dialog be modal.
     * @param photoset the photoset we will display photos from.
     * @throws Exception if there are any errors.
     */
    public PhotoPickerDialog(javax.swing.JDialog parent, boolean modal, SSPhotoset photoset) throws Exception {
        super(parent, modal);
	this.parent = parent;
	this.photoset = photoset;
	this.photos = new ArrayList<Photo>();
	this.cache = new ObjectCache(new File(Main.configDir, "image_cache"));
	
        initComponents();
	
	// center on the parent
	setBounds((parent.getWidth()-422)/2 + parent.getX(), (parent.getHeight()-490)/2 + parent.getY(), 422, 490);

	this.setTitle("Select primary photo for '" + photoset.getTitle() + "'");
	
	int total = photoset.getPhotos() + photoset.getVideos();
	pages = total/25;
	if (pages * 25 != total) {
	    pages++;
	}
	
	setButtonStates();
	loadPageOfPhotos();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        btnPrev = new javax.swing.JButton();
        lblPage = new javax.swing.JLabel();
        btnNext = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jTable1.setModel(new PhotoPickerTableModel());
        jTable1.setCellSelectionEnabled(true);
        jTable1.setRowHeight(75);
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable1.setShowGrid(false);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jTable1MouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        btnSave.setText("Save");
        btnSave.setEnabled(false);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        btnPrev.setText("<");
        btnPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevActionPerformed(evt);
            }
        });

        lblPage.setText("0/0");

        btnNext.setText(">");
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(btnPrev)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblPage)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnNext)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 30, Short.MAX_VALUE)
                .add(btnCancel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnSave)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnSave)
                    .add(btnCancel)
                    .add(btnPrev)
                    .add(lblPage)
                    .add(btnNext))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    /**
     * Close the window if user cancels the operation.
     *
     * @param evt
     */
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
	this.dispose();
	this.setVisible(false);
    }//GEN-LAST:event_btnCancelActionPerformed


    /**
     * Respond to "Previous" button presses.
     *
     * <p>The button states are managed carefully so that the user cannot press
     * the button if there is no page to go back to.</p>
     *
     * @param evt
     */
    private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
	this.page--;
	this.btnSave.setEnabled(false);
	setButtonStates();
	loadPageOfPhotos();
    }//GEN-LAST:event_btnPrevActionPerformed


    /**
     * Respond to "Next" button presses.
     *
     * <p>The button states are managed carefully so that the user cannot press
     * the button if there is no page to go forward to.</p>
     *
     * @param evt
     */
    private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
	this.page++;
	this.btnSave.setEnabled(false);
	setButtonStates();
	loadPageOfPhotos();
    }//GEN-LAST:event_btnNextActionPerformed


    /**
     * Save the selected photo as the primary image.
     *
     * <p>If the user has selected an empty space, or if they have not selected
     * any photo, this method does nothing.</p>
     * 
     * @param evt
     */
    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
	int index = (5 * this.jTable1.getSelectedRow()) + this.jTable1.getSelectedColumn();
	
	if (index < this.photos.size()) {
	    String id = this.photos.get(index).getId();
	    try {
		if (this.parent instanceof SetEditor) {
		    ((SetEditor)this.parent).setPrimaryPhotoImage(id, (ImageIcon)this.cache.get(id));
		    ((SetEditor)this.parent).setMessage("This change will not be visible on Flickr until you refresh the set.");
		}
	    } catch (Exception e) {
		logger.error("Could not set image.", e);
		JOptionPane.showMessageDialog(this, "Something went wrong while setting the image.\n" +
			"The error was: \n" + e.getMessage() +
			"\nCheck the log for details.", "Could not set image",
			JOptionPane.ERROR_MESSAGE);
	    } finally {
		this.dispose();
		this.setVisible(false);
	    }
	}
    }//GEN-LAST:event_btnSaveActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
	
    }//GEN-LAST:event_jTable1MouseClicked

    private void jTable1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseReleased
	int index = (jTable1.getSelectedRow() * 5) + jTable1.getSelectedColumn();

	this.btnSave.setEnabled(index < photos.size());
    }//GEN-LAST:event_jTable1MouseReleased


    /**
     * Set the enable state of previous and next buttons based on the page
     * we are on, and the number of pages.
     *
     * This also sets the "Page x/y" label.
     */
    private void setButtonStates() {
	this.lblPage.setText(page + "/" + pages);
	this.btnPrev.setEnabled(this.page > 1);
	this.btnNext.setEnabled(pages > page);
    }


    /**
     * Load a page of photos.
     */
    private void loadPageOfPhotos() {
	BlockerPanel blocker = new BlockerPanel(this, "Loading photos");
	setGlassPane(blocker);
	blocker.block("Loading images from set '" + photoset.getTitle() + "'");

	new LoadImagesWorker(blocker, photoset.getId(), this.page, this.cache, this.photos, this).execute();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JButton btnSave;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblPage;
    // End of variables declaration//GEN-END:variables



    /**
     * Table model for the table that displays the photos.
     */
    private class PhotoPickerTableModel extends AbstractTableModel {

	/**
	 * There will always be five columns.
	 * @return number of columns.
	 */
	@Override
	public int getColumnCount() {
	    return 5;
	}


	/**
	 * No column names are needed.
	 *
	 * @param column
	 * @return column name.
	 */
	@Override
	public String getColumnName(int column) {
	    return "";
	}


	/**
	 * There will always be five rows.
	 *
	 * @return number of rows.
	 */
	@Override
	public int getRowCount() {
	    return 5;
	}


	/**
	 * Get the object for the specified row/column.
	 *
	 * @param rowIndex
	 * @param columnIndex
	 * @return
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
	    // check to see if photos are available yet
	    if (photos == null) {
		return null;
	    }
	    
	    ImageIcon result = null;
	    int index = (rowIndex * 5) + columnIndex;

	    if (index < photos.size()) {
		try {

		    Photo thePhoto = photos.get(index);
		    
		    result = (ImageIcon) cache.get(thePhoto.getId());
		    if (result == null) {
			logger.warn("Photo should have been in cache, but was not. Something is wrong somewhere.");
		    }
		    
		} catch (Exception e) {
		    logger.warn("Could not get icon for photo.", e);
		    
		}
	    }
	    
	    return result;
	}


	/**
	 * Tell the table that it will be using ImageIcons to display data.
	 * 
	 * @param c
	 * @return
	 */
	@Override
	public Class getColumnClass(int c) {
	    return ImageIcon.class;
	}


	/**
	 * Cells are never editable.
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
	    return false;
	}
    }

}
