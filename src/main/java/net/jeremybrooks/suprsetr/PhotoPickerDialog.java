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

import net.jeremybrooks.jinx.dto.Photo;
import net.jeremybrooks.suprsetr.utils.ObjectCache;
import net.jeremybrooks.suprsetr.workers.LoadImagesWorker;
import org.apache.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Display a dialog showing the photos in the photoset.
 * <p/>
 * When the user selects a photo and clicks Save, the selected photo will become
 * the primary photo on the SetEditor window.
 * <p/>
 * This class uses an Object Cache to save square images that have already
 * been downloaded. This will reduce traffic to Flickr and improve performance.
 * Once the cache is created, it will persist for the lifetime of the
 * application.
 *
 * @author jeremyb
 */
public class PhotoPickerDialog extends javax.swing.JDialog {

	private static final long serialVersionUID = 3224665899617494633L;

	/**
	 * Logging.
	 */
	private Logger logger = Logger.getLogger(PhotoPickerDialog.class);

	/**
	 * The photoset that we are displaying photos from.
	 */
	private SSPhotoset photoset;

	/**
	 * Cache image icons.
	 */
	private ObjectCache cache = null;

	/**
	 * The dataset used by the table model.
	 */
	private List<Photo> photos;

	/**
	 * The page of results we are on. There are 25 photos per page.
	 */
	private int page = 1;

	/**
	 * The total number of pages we will need for all photos in the set.
	 */
	private int pages = 1;

	/**
	 * The parent frame (should be SetEditor).
	 */
	private JDialog parent;

	private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.photopicker");


	/**
	 * Creates a new PhotoPickerDialog.
	 * <p/>
	 * The first page of photos will be loaded here.
	 *
	 * @param parent   parent window.
	 * @param modal    should this dialog be modal.
	 * @param photoset the photoset we will display photos from.
	 * @throws Exception if there are any errors.
	 */
	public PhotoPickerDialog(javax.swing.JDialog parent, boolean modal, SSPhotoset photoset) throws Exception {
		super(parent, modal);
		this.parent = parent;
		this.photoset = photoset;
		this.photos = new ArrayList<>();
		this.cache = new ObjectCache(new File(Main.configDir, "image_cache"));
		initComponents();
		setIconImage(new ImageIcon(getClass().getResource("/images/s16.png")).getImage());

		this.jTable1.setModel(new PhotoPickerTableModel());
		// center on the parent
		setBounds((parent.getWidth() - 422) / 2 + parent.getX(), (parent.getHeight() - 490) / 2 + parent.getY(), 422, 490);
		this.setTitle(resourceBundle.getString("PhotoPickerDialog.title") + photoset.getTitle() + "'");
		int total = photoset.getPhotos() + photoset.getVideos();
		pages = total / 25;
		if (pages * 25 != total) {
			pages++;
		}
		setButtonStates();
		loadPageOfPhotos();
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
		jScrollPane1 = new JScrollPane();
		jTable1 = new JTable();
		panel1 = new JPanel();
		panel2 = new JPanel();
		btnPrev = new JButton();
		lblPage = new JLabel();
		btnNext = new JButton();
		panel3 = new JPanel();
		btnCancel = new JButton();
		btnSave = new JButton();

		//======== this ========
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== jScrollPane1 ========
		{

			//---- jTable1 ----
			jTable1.setCellSelectionEnabled(true);
			jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jTable1.setShowGrid(false);
			jTable1.setMinimumSize(new Dimension(75, 75));
			jTable1.setRowHeight(75);
			jTable1.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent e) {
					jTable1MouseReleased(e);
				}
			});
			jScrollPane1.setViewportView(jTable1);
		}
		contentPane.add(jScrollPane1, BorderLayout.CENTER);

		//======== panel1 ========
		{
			panel1.setLayout(new BorderLayout());

			//======== panel2 ========
			{
				panel2.setLayout(new FlowLayout(FlowLayout.LEFT));

				//---- btnPrev ----
				btnPrev.setText("<");
				btnPrev.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						btnPrevActionPerformed(e);
					}
				});
				panel2.add(btnPrev);

				//---- lblPage ----
				lblPage.setText("0/0");
				panel2.add(lblPage);

				//---- btnNext ----
				btnNext.setText(">");
				btnNext.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						btnNextActionPerformed(e);
					}
				});
				panel2.add(btnNext);
			}
			panel1.add(panel2, BorderLayout.WEST);

			//======== panel3 ========
			{
				panel3.setLayout(new FlowLayout(FlowLayout.RIGHT));

				//---- btnCancel ----
				btnCancel.setText(bundle.getString("PhotoPickerDialog.btnCancel.text"));
				btnCancel.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						btnCancelActionPerformed(e);
					}
				});
				panel3.add(btnCancel);

				//---- btnSave ----
				btnSave.setText(bundle.getString("PhotoPickerDialog.btnSave.text"));
				btnSave.setEnabled(false);
				btnSave.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						btnSaveActionPerformed(e);
					}
				});
				panel3.add(btnSave);
			}
			panel1.add(panel3, BorderLayout.CENTER);
		}
		contentPane.add(panel1, BorderLayout.SOUTH);
		setLocationRelativeTo(getOwner());
	}// </editor-fold>//GEN-END:initComponents


	/**
	 * Close the window if user cancels the operation.
	 *
	 * @param evt
	 */
	private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
		this.dispose();
		this.setVisible(false);
	}


	/**
	 * Respond to "Previous" button presses.
	 * <p/>
	 * <p>The button states are managed carefully so that the user cannot press
	 * the button if there is no page to go back to.</p>
	 *
	 * @param evt
	 */
	private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {
		this.page--;
		this.btnSave.setEnabled(false);
		setButtonStates();
		loadPageOfPhotos();
	}


	/**
	 * Respond to "Next" button presses.
	 * <p/>
	 * <p>The button states are managed carefully so that the user cannot press
	 * the button if there is no page to go forward to.</p>
	 *
	 * @param evt
	 */
	private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {
		this.page++;
		this.btnSave.setEnabled(false);
		setButtonStates();
		loadPageOfPhotos();
	}


	/**
	 * Save the selected photo as the primary image.
	 * <p/>
	 * <p>If the user has selected an empty space, or if they have not selected
	 * any photo, this method does nothing.</p>
	 *
	 * @param evt
	 */
	private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {
		int index = (5 * this.jTable1.getSelectedRow()) + this.jTable1.getSelectedColumn();

		if (index < this.photos.size()) {
			String id = this.photos.get(index).getId();
			try {
				if (this.parent instanceof SetEditor) {
					((SetEditor) this.parent).setPrimaryPhotoImage(id, (ImageIcon) this.cache.get(id));
					((SetEditor) this.parent).setMessage(resourceBundle.getString("PhotoPickerDialog.changeMessage"));
				}
			} catch (Exception e) {
				logger.error("Could not set image.", e);
				JOptionPane.showMessageDialog(this,
						resourceBundle.getString("PhotoPickerDialog.error.message") + e.getMessage(),
						resourceBundle.getString("PhotoPickerDialog.error.title"),
						JOptionPane.ERROR_MESSAGE);
			} finally {
				this.dispose();
				this.setVisible(false);
			}
		}
	}

	private void jTable1MouseReleased(java.awt.event.MouseEvent evt) {
		int index = (jTable1.getSelectedRow() * 5) + jTable1.getSelectedColumn();
		this.btnSave.setEnabled(index < photos.size());
	}

	/**
	 * Set the enable state of previous and next buttons based on the page
	 * we are on, and the number of pages.
	 * <p/>
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
		BlockerPanel blocker = new BlockerPanel(this, resourceBundle.getString("PhotoPickerDialog.blocker.title"));
		setGlassPane(blocker);
		blocker.block(resourceBundle.getString("PhotoPickerDialog.blocker.loading") + photoset.getTitle() + "'");
		new LoadImagesWorker(blocker, photoset.getId(), this.page, this.cache, this.photos, this).execute();
	}


	// Variables declaration - do not modify//GEN-BEGIN:variables
	private JScrollPane jScrollPane1;
	private JTable jTable1;
	private JPanel panel1;
	private JPanel panel2;
	private JButton btnPrev;
	private JLabel lblPage;
	private JButton btnNext;
	private JPanel panel3;
	private JButton btnCancel;
	private JButton btnSave;
	// End of variables declaration//GEN-END:variables


	/**
	 * Table model for the table that displays the photos.
	 */
	private class PhotoPickerTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -579332801874840308L;

		/**
		 * There will always be five columns.
		 *
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
			ImageIcon result = null;
			if (photos != null) {
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
