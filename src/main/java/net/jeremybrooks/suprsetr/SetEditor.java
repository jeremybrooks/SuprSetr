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

import java.awt.Color;
import java.util.Date;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import net.jeremybrooks.jinx.api.PhotosetsApi;
import net.jeremybrooks.suprsetr.dao.DAOHelper;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import net.jeremybrooks.suprsetr.dao.PhotosetDAO;
import net.jeremybrooks.suprsetr.twitter.TwitterHelper;
import net.jeremybrooks.suprsetr.utils.SSUtils;
import net.jeremybrooks.suprsetr.utils.SimpleCache;
import org.apache.log4j.Logger;


/**
 *
 * @author jeremyb
 */
public class SetEditor extends javax.swing.JDialog {




    /**
     * Mode we are being called in.
     */
    public static enum EditorMode {

	CREATE, EDIT

    };

    /** The combo box model for 29 days. */
    private static ComboBoxModel MODEL_29_DAYS = new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29" });

    /** The combo box model for 30 days. */
    private static ComboBoxModel MODEL_30_DAYS = new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30" });

    /** The combo box model for 31 days. */
    private static ComboBoxModel MODEL_31_DAYS = new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" });


    /** The mode. */
    private EditorMode editorMode = EditorMode.CREATE;

    /** The photoset we are working on. */
    private SSPhotoset ssPhotoset;

    /** Logging. */
    private Logger logger = Logger.getLogger(SetEditor.class);

    /** Cancelable properties. */
    // These can be changed from the PhotoPicker, so we need to remember this
    // state when the window is first displayed in order to revert any changes
    // if the user clicks Cancel
    private ImageIcon originalPrimaryIcon;
    private String originalPrimaryId;
    private boolean originalLockSelected;


    /**
     * Create a new instance of the SetEditor.
     *
     * @param parent the parent frame.
     * @param editorMode mode to edit in (create or edit).
     * @param ssPhotoset the photoset we are editing or creating.
     */
    public SetEditor(JFrame parent, EditorMode editorMode, SSPhotoset ssPhotoset) {
	super(parent, true);

	this.ssPhotoset = ssPhotoset;

	this.editorMode = editorMode;

	initComponents();

	// TODO
	// These search parameters do not work correctly
	// Enable when the API is fixed
	this.cbxInCommons.setVisible(false);
	this.cbxInGetty.setVisible(false);
	this.lblSafeSearch.setVisible(false);
	this.cmbSafeSearch.setVisible(false);



	
	if (this.editorMode == EditorMode.CREATE) {
	    this.btnSave.setVisible(false);
	}

	
	// set the values based on the setDefinition
	if (ssPhotoset.getPrimaryPhotoIcon() != null) {
	    this.lblIcon.setIcon(ssPhotoset.getPrimaryPhotoIcon());
	}
	this.cbxLock.setSelected(ssPhotoset.isLockPrimaryPhoto());

	if (editorMode == EditorMode.CREATE) {
	    this.cbxManage.setSelected(true);
	    this.cbxManage.setEnabled(false);
	} else {
	    this.cbxManage.setSelected(ssPhotoset.isManaged());
	}

	this.txtTitle.setText(ssPhotoset.getTitle());
	this.txtDescription.setText(ssPhotoset.getDescription());
	this.addManagedByTextToDescription();
	
	String tagMode = ssPhotoset.getTagMatchMode();
	if (tagMode.equals(SSConstants.TAG_MATCH_MODE_ALL)) {
	    this.cmbTags.setSelectedIndex(0);
	} else {
	    this.cmbTags.setSelectedIndex(1);
	}
	this.txtTags.setText(ssPhotoset.getTagsAsString());

	this.cbxDateTaken.setSelected(ssPhotoset.isMatchTakenDates());
	this.txtDateTakenAfter.setText(SSUtils.formatDateAsYYYYMMDD(ssPhotoset.getMinTakenDate()));
	this.txtDateTakenBefore.setText(SSUtils.formatDateAsYYYYMMDD(ssPhotoset.getMaxTakenDate()));

	this.cbxDateUploaded.setSelected(ssPhotoset.isMatchUploadDates());
	this.txtDateUploadedAfter.setText(SSUtils.formatDateAsYYYYMMDD(ssPhotoset.getMinUploadDate()));
	this.txtDateUploadedBefore.setText(SSUtils.formatDateAsYYYYMMDD(ssPhotoset.getMaxUploadDate()));

	this.cmbSortBy.setSelectedIndex(ssPhotoset.getSortOrder());

	this.radioTweetUpdated.setSelected(ssPhotoset.isSendTweet());
	this.txtTweet.setText(ssPhotoset.getTweetTemplate());

	this.cmbPrivacy.setSelectedIndex(ssPhotoset.getPrivacy());
	this.cmbSafeSearch.setSelectedIndex(ssPhotoset.getSafeSearch());
	this.cmbContentType.setSelectedIndex(ssPhotoset.getContentType());
	this.cmbMediaType.setSelectedIndex(ssPhotoset.getMediaType());
	this.cmbGeotag.setSelectedIndex(ssPhotoset.getGeotagged());

	this.cbxInCommons.setSelected(ssPhotoset.isInCommons());
	this.cbxInGallery.setSelected(ssPhotoset.isInGallery());
	this.cbxInGetty.setSelected(ssPhotoset.isInGetty());

	this.cbxLimitSize.setSelected(ssPhotoset.isLimitSize());
	this.txtSetSize.setEnabled(ssPhotoset.isLimitSize());
	this.txtSetSize.setText(Integer.toString(ssPhotoset.getSizeLimit()));

	this.cbxOnThisDay.setSelected(ssPhotoset.isOnThisDay());
	this.cmbOTDMonth.setSelectedIndex(ssPhotoset.getOnThisDayMonth() - 1);
	this.updateDayOfMonthComboBox();
	this.cmbOTDDay.setSelectedIndex(ssPhotoset.getOnThisDayDay() - 1);
	this.txtOTDYearStart.setText(Integer.toString(ssPhotoset.getOnThisDayYearStart()));
	if (ssPhotoset.getOnThisDayYearEnd() == 0) {
	    this.cbxCurrentYear.setSelected(true);
	    this.txtOTDYearEnd.setText(Integer.toString(SSUtils.getCurrentYear()));
	} else {
	    this.txtOTDYearEnd.setText(Integer.toString(ssPhotoset.getOnThisDayYearEnd()));
	}

	// remember these
	this.originalPrimaryIcon = this.ssPhotoset.getPrimaryPhotoIcon();
	this.originalPrimaryId = this.ssPhotoset.getPrimary();
	this.originalLockSelected = this.ssPhotoset.isLockPrimaryPhoto();
	

	this.setEnableStates();
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnGrpTweet = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        basicPanel = new javax.swing.JPanel();
        pnlTitle = new javax.swing.JPanel();
        lblIcon = new javax.swing.JLabel();
        cbxManage = new javax.swing.JCheckBox();
        cbxLock = new javax.swing.JCheckBox();
        txtTitle = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtDescription = new javax.swing.JTextArea();
        lblMessage = new javax.swing.JLabel();
        pnlTags = new javax.swing.JPanel();
        txtTags = new javax.swing.JTextField();
        cmbTags = new javax.swing.JComboBox();
        pnlDates = new javax.swing.JPanel();
        cbxDateTaken = new javax.swing.JCheckBox();
        txtDateTakenAfter = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtDateTakenBefore = new javax.swing.JTextField();
        cbxDateUploaded = new javax.swing.JCheckBox();
        txtDateUploadedAfter = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtDateUploadedBefore = new javax.swing.JTextField();
        cbxOnThisDay = new javax.swing.JCheckBox();
        cmbOTDMonth = new javax.swing.JComboBox();
        cmbOTDDay = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        txtOTDYearStart = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txtOTDYearEnd = new javax.swing.JTextField();
        cbxCurrentYear = new javax.swing.JCheckBox();
        pnlOther = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        cmbSortBy = new javax.swing.JComboBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtTweet = new javax.swing.JEditorPane();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        radioTweetNone = new javax.swing.JRadioButton();
        radioTweetUpdated = new javax.swing.JRadioButton();
        radioTweetCreated = new javax.swing.JRadioButton();
        advancedPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        cmbPrivacy = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        lblSafeSearch = new javax.swing.JLabel();
        cmbSafeSearch = new javax.swing.JComboBox();
        jPanel5 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        cmbContentType = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        cmbMediaType = new javax.swing.JComboBox();
        jPanel6 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        cmbGeotag = new javax.swing.JComboBox();
        cbxInGallery = new javax.swing.JCheckBox();
        cbxInCommons = new javax.swing.JCheckBox();
        cbxInGetty = new javax.swing.JCheckBox();
        cbxLimitSize = new javax.swing.JCheckBox();
        txtSetSize = new javax.swing.JTextField();
        buttonPanel = new javax.swing.JPanel();
        btnSave = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        btnSaveAndRefresh = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pnlTitle.setBorder(javax.swing.BorderFactory.createTitledBorder("Title and Description"));

        lblIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/empty_set_icon.png"))); // NOI18N
        lblIcon.setToolTipText("The primary image for the set. This is the image that Flickr will display to represent the set.");
        lblIcon.setSize(new java.awt.Dimension(75, 75));
        lblIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblIconMouseClicked(evt);
            }
        });

        cbxManage.setText("Manage this set with SuprSetr");
        cbxManage.setToolTipText("Select this to allow SuprSetr to manage and update this set.");
        cbxManage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxManageActionPerformed(evt);
            }
        });

        cbxLock.setText("Lock Primary Photo");
        cbxLock.setToolTipText("If this box is checked, the primary photo for this set will not be changed when the set is refreshed.");

        txtTitle.setToolTipText("The title for the set.");
        txtTitle.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtTitleKeyReleased(evt);
            }
        });

        txtDescription.setColumns(20);
        txtDescription.setLineWrap(true);
        txtDescription.setRows(5);
        txtDescription.setToolTipText("The description for the set. HTML is allowed here.");
        txtDescription.setWrapStyleWord(true);
        jScrollPane1.setViewportView(txtDescription);

        lblMessage.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        lblMessage.setForeground(java.awt.Color.red);

        javax.swing.GroupLayout pnlTitleLayout = new javax.swing.GroupLayout(pnlTitle);
        pnlTitle.setLayout(pnlTitleLayout);
        pnlTitleLayout.setHorizontalGroup(
            pnlTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTitleLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlTitleLayout.createSequentialGroup()
                        .addComponent(lblIcon)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 625, Short.MAX_VALUE)
                            .addGroup(pnlTitleLayout.createSequentialGroup()
                                .addGroup(pnlTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbxLock)
                                    .addComponent(cbxManage))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblMessage))))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 710, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlTitleLayout.setVerticalGroup(
            pnlTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTitleLayout.createSequentialGroup()
                .addGroup(pnlTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(pnlTitleLayout.createSequentialGroup()
                        .addGroup(pnlTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(pnlTitleLayout.createSequentialGroup()
                                .addComponent(cbxManage)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbxLock)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(pnlTitleLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblMessage)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                        .addComponent(txtTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblIcon))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
                .addContainerGap())
        );

        pnlTags.setBorder(javax.swing.BorderFactory.createTitledBorder("Tags - Comma Separated"));

        txtTags.setToolTipText("The list of tags to match. Separate tags with commas: California, San Francisco, Neon");

        cmbTags.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All", "Any" }));
        cmbTags.setToolTipText("Match All tags (logical AND) or Any tags (logical OR).");

        javax.swing.GroupLayout pnlTagsLayout = new javax.swing.GroupLayout(pnlTags);
        pnlTags.setLayout(pnlTagsLayout);
        pnlTagsLayout.setHorizontalGroup(
            pnlTagsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTagsLayout.createSequentialGroup()
                .addComponent(cmbTags, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtTags, javax.swing.GroupLayout.DEFAULT_SIZE, 663, Short.MAX_VALUE))
        );
        pnlTagsLayout.setVerticalGroup(
            pnlTagsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTagsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(cmbTags, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(txtTags, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pnlDates.setBorder(javax.swing.BorderFactory.createTitledBorder("Dates (YYYY-MM-DD format)"));

        cbxDateTaken.setText("Include photos taken on or after");
        cbxDateTaken.setToolTipText("If selected, the matches will be filtered by the date photos were taken.");
        cbxDateTaken.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxDateTakenActionPerformed(evt);
            }
        });

        txtDateTakenAfter.setToolTipText("Find photos taken on or after this date.");
        txtDateTakenAfter.setEnabled(false);

        jLabel3.setText("and on or before");

        txtDateTakenBefore.setToolTipText("Find photos taken on or before this date.");
        txtDateTakenBefore.setEnabled(false);

        cbxDateUploaded.setText("Include photos uploaded on or after");
        cbxDateUploaded.setToolTipText("If selected, the matches will be filtered by the date photos were uploaded to Flickr.");
        cbxDateUploaded.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxDateUploadedActionPerformed(evt);
            }
        });

        txtDateUploadedAfter.setToolTipText("Find photos uploaded on or after this date.");
        txtDateUploadedAfter.setEnabled(false);

        jLabel4.setText("and on or before");

        txtDateUploadedBefore.setToolTipText("Find photos uploaded on or before this date.");
        txtDateUploadedBefore.setEnabled(false);

        cbxOnThisDay.setText("On this day");
        cbxOnThisDay.setToolTipText("Find photos on a specific day over several years.");
        cbxOnThisDay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxOnThisDayActionPerformed(evt);
            }
        });

        cmbOTDMonth.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }));
        cmbOTDMonth.setToolTipText("The month of the photos to find.");
        cmbOTDMonth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbOTDMonthActionPerformed(evt);
            }
        });

        cmbOTDDay.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" }));
        cmbOTDDay.setToolTipText("The day of the photos to find.");

        jLabel9.setText("for years");

        txtOTDYearStart.setToolTipText("The year to begin searches from.");

        jLabel13.setText("through");

        txtOTDYearEnd.setToolTipText("The year to end searches.");

        cbxCurrentYear.setText("Always use current year");
        cbxCurrentYear.setToolTipText("If selected, the ending year for searches will always be the current year.");
        cbxCurrentYear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxCurrentYearActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlDatesLayout = new javax.swing.GroupLayout(pnlDates);
        pnlDates.setLayout(pnlDatesLayout);
        pnlDatesLayout.setHorizontalGroup(
            pnlDatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDatesLayout.createSequentialGroup()
                        .addGroup(pnlDatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbxDateUploaded)
                            .addComponent(cbxDateTaken))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlDatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtDateTakenAfter, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtDateUploadedAfter, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
                        .addGroup(pnlDatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlDatesLayout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDatesLayout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(jLabel3)
                                .addGap(8, 8, 8)))
                        .addGroup(pnlDatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtDateUploadedBefore)
                            .addComponent(txtDateTakenBefore, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)))
                    .addGroup(pnlDatesLayout.createSequentialGroup()
                        .addComponent(cbxOnThisDay)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbOTDMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbOTDDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9)
                        .addGap(1, 1, 1)
                        .addComponent(txtOTDYearStart, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlDatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbxCurrentYear)
                            .addComponent(txtOTDYearEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(49, Short.MAX_VALUE))
        );
        pnlDatesLayout.setVerticalGroup(
            pnlDatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDatesLayout.createSequentialGroup()
                .addGroup(pnlDatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxDateTaken)
                    .addComponent(jLabel3)
                    .addComponent(txtDateTakenAfter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDateTakenBefore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxDateUploaded)
                    .addComponent(txtDateUploadedAfter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDateUploadedBefore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxOnThisDay)
                    .addComponent(cmbOTDMonth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbOTDDay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(txtOTDYearStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(txtOTDYearEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cbxCurrentYear))
        );

        pnlOther.setBorder(javax.swing.BorderFactory.createTitledBorder("Other Options"));

        jLabel1.setText("Sort photos by");

        cmbSortBy.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Interestingness Descending", "Interestingness Ascending", "Date Taken Descending", "Date Taken Ascending", "Date Posted Descending", "Date Posted Ascending", "No Particular Order", "Photo Title Descending", "Photo Title Ascending" }));
        cmbSortBy.setToolTipText("This defines how to sort the photos in the set.");
        cmbSortBy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSortByActionPerformed(evt);
            }
        });

        txtTweet.setDocument(new LimitedPlainDocument(140));
        txtTweet.setText(SSConstants.DEFAULT_TWEET_TEMPLATE);
        txtTweet.setToolTipText("The text to send via Twitter. Use %t, %u, %c, and %C to insert interesting data in the message.");
        jScrollPane3.setViewportView(txtTweet);

        jLabel2.setText("%t - Photoset Title");

        jLabel5.setText("%u - Photoset URL");

        jLabel6.setText("%c - Count of photos just added");

        jLabel7.setText("%C - Count of total photos in photoset");

        btnGrpTweet.add(radioTweetNone);
        radioTweetNone.setSelected(true);
        radioTweetNone.setText("No Tweets");
        radioTweetNone.setToolTipText("Do not tweet about this set.");
        radioTweetNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioTweetNoneActionPerformed(evt);
            }
        });

        btnGrpTweet.add(radioTweetUpdated);
        radioTweetUpdated.setText("Tweet when updated");
        radioTweetUpdated.setToolTipText("Tweet every time this set is updated and there are changes.");
        radioTweetUpdated.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioTweetUpdatedActionPerformed(evt);
            }
        });

        btnGrpTweet.add(radioTweetCreated);
        radioTweetCreated.setText("Tweet when created");
        radioTweetCreated.setToolTipText("Tweet when this set is created, but not when updated.");
        radioTweetCreated.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioTweetCreatedActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlOtherLayout = new javax.swing.GroupLayout(pnlOther);
        pnlOther.setLayout(pnlOtherLayout);
        pnlOtherLayout.setHorizontalGroup(
            pnlOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOtherLayout.createSequentialGroup()
                .addGroup(pnlOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlOtherLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(radioTweetNone)
                            .addComponent(radioTweetUpdated)
                            .addComponent(radioTweetCreated))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3))
                    .addGroup(pnlOtherLayout.createSequentialGroup()
                        .addGap(78, 78, 78)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbSortBy, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        pnlOtherLayout.setVerticalGroup(
            pnlOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOtherLayout.createSequentialGroup()
                .addGroup(pnlOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbSortBy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlOtherLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlOtherLayout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel5)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel6)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7))
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlOtherLayout.createSequentialGroup()
                        .addComponent(radioTweetNone)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioTweetUpdated)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioTweetCreated)))
                .addContainerGap())
        );

        javax.swing.GroupLayout basicPanelLayout = new javax.swing.GroupLayout(basicPanel);
        basicPanel.setLayout(basicPanelLayout);
        basicPanelLayout.setHorizontalGroup(
            basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basicPanelLayout.createSequentialGroup()
                .addComponent(pnlOther, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, basicPanelLayout.createSequentialGroup()
                .addGroup(basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlDates, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlTags, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(12, 12, 12))
        );
        basicPanelLayout.setVerticalGroup(
            basicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basicPanelLayout.createSequentialGroup()
                .addComponent(pnlTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(pnlTags, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDates, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlOther, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Basic", basicPanel);

        advancedPanel.setVerifyInputWhenFocusTarget(false);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Privacy"));

        cmbPrivacy.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Restriction", "Public", "Friends", "Family", "Friends & Family", "Private" }));
        cmbPrivacy.setToolTipText("Filters the returned results based on privacy level of the photos.");

        jLabel8.setText("Restrict results to privacy level:");

        lblSafeSearch.setText("Include photos flagged as:");

        cmbSafeSearch.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Safe", "Moderate", "Restricted" }));
        cmbSafeSearch.setToolTipText("Filter the returned photos based on the viewing safety level.");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblSafeSearch)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbSafeSearch, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbPrivacy, 0, 193, Short.MAX_VALUE))
                .addContainerGap(346, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbPrivacy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSafeSearch)
                    .addComponent(cmbSafeSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Content and Media"));

        jLabel10.setText("Include content types:");

        cmbContentType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Photos Only", "Screenshots Only", "Other Only", "Photos & Screenshots", "Screenshots & Other", "Photos & Other", "All" }));
        cmbContentType.setToolTipText("Limit the returned results to certain content type.");

        jLabel11.setText("Filter by media:");

        cmbMediaType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All", "Photos", "Video" }));
        cmbMediaType.setToolTipText("Limit the returned results by media type.");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel11)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbMediaType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbContentType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(375, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(cmbContentType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(cmbMediaType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Other Options"));

        jLabel12.setText("Filter by geotag data:");

        cmbGeotag.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ignore geotag data", "Include only photos with geotag data", "Include only photos without geotag data" }));
        cmbGeotag.setToolTipText("Filter returned results based on geotag data.");

        cbxInGallery.setText("Limit search to photos that are included in a gallery");
        cbxInGallery.setToolTipText("If this is selected, only photos that appear in a gallery will be included in the search results.");

        cbxInCommons.setText("Limit search to photos that are part of the Flickr Commons project");
        cbxInCommons.setToolTipText("If this is selected, only photos in the Flickr Commons will be in the search results.");

        cbxInGetty.setText("Limit search to photos that are for sale on Getty");
        cbxInGetty.setToolTipText("If this is selected, only photos that are for sale on Getty Images will be returned.");

        cbxLimitSize.setText("Limit number of photos in set to");
        cbxLimitSize.setToolTipText("If this is selected, the set will contain only the number of results specified.");
        cbxLimitSize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbxLimitSizeActionPerformed(evt);
            }
        });

        txtSetSize.setToolTipText("The number of items to include in the set.");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(cbxLimitSize)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSetSize, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cbxInCommons)
                    .addComponent(cbxInGallery)
                    .addComponent(cmbGeotag, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxInGetty))
                .addContainerGap(120, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbGeotag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbxInGallery)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxInCommons)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbxInGetty)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxLimitSize)
                    .addComponent(txtSetSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout advancedPanelLayout = new javax.swing.GroupLayout(advancedPanel);
        advancedPanel.setLayout(advancedPanelLayout);
        advancedPanelLayout.setHorizontalGroup(
            advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        advancedPanelLayout.setVerticalGroup(
            advancedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(advancedPanelLayout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(325, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Advanced", advancedPanel);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnSave.setText("Save");
        btnSave.setToolTipText("Save changes, but do not update the set on Flickr immediately.");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        buttonPanel.add(btnSave);

        btnCancel.setText("Cancel");
        btnCancel.setToolTipText("Close the window, discarding changes.");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        buttonPanel.add(btnCancel);

        btnSaveAndRefresh.setText("Save & Refresh");
        btnSaveAndRefresh.setToolTipText("Save the changes and refresh the set on Flickr immediately.");
        btnSaveAndRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveAndRefreshActionPerformed(evt);
            }
        });
        buttonPanel.add(btnSaveAndRefresh);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.PAGE_END);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-795)/2, (screenSize.height-819)/2, 795, 819);
    }// </editor-fold>//GEN-END:initComponents


    /**
     * Responds to clicks on the "date taken" checkbox.
     * <p>The text input boxes for date taken min/max should be enabled and disabled
     * as the user checks and unchecks the box.</p>
     *
     * @param evt the event
     */
    private void cbxDateTakenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxDateTakenActionPerformed
	if (this.cbxDateTaken.isSelected()) {
	    this.cbxOnThisDay.setSelected(false);
	}
	this.setEnableStates();
    }//GEN-LAST:event_cbxDateTakenActionPerformed


    /**
     * Responds to clicks on the "date uploaded" checkbox.
     * <p>The text input boxes for date uploaded min/max should be enabled and disabled
     * as the user checks and unchecks the box.</p>
     *
     * @param evt the event
     */
    private void cbxDateUploadedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxDateUploadedActionPerformed
	if (this.cbxDateUploaded.isSelected()) {
	    this.cbxOnThisDay.setSelected(false);
	}
	this.setEnableStates();
    }//GEN-LAST:event_cbxDateUploadedActionPerformed


    /**
     * Respond to clicks on the cancel button.
     *
     * <p>The window will be closed.</p>
     * @param evt
     */
    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
	// make sure any icon changes are undone
	this.lblIcon.setIcon(this.originalPrimaryIcon);
	this.ssPhotoset.setPrimary(this.originalPrimaryId);
	this.ssPhotoset.setPrimaryPhotoIcon(this.originalPrimaryIcon);
	this.cbxLock.setSelected(this.originalLockSelected);
	this.ssPhotoset.setLockPrimaryPhoto(this.originalLockSelected);

	this.dispose();
    }//GEN-LAST:event_btnCancelActionPerformed


    /**
     * Respond to clicks on the save button.
     * <p>Perform validation on the user entries. If everything is OK, save
     * the record. Otherwise, tell the user what is wrong and let them fix it.</p>
     *
     * @param evt
     */
    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
	if (this.doValidation()) {
	    try {
		PhotosetDAO.updatePhotoset(this.ssPhotoset);
		SimpleCache.getInstance().invalidate(this.ssPhotoset.getId());
		this.setVisible(false);
		this.dispose();
	    } catch (Exception e) {
		logger.error("Error saving set parameters to database.", e);
		JOptionPane.showMessageDialog(this,
			"There was an error while trying to save set parameters.\n" +
			"The error message was:\n" +
			e.getMessage(),
			"Error Saving",
			JOptionPane.ERROR_MESSAGE);
	    }
	}
    }//GEN-LAST:event_btnSaveActionPerformed


    /**
     * Verify entry of title text.
     * <p>If the user tries to enter a title that is already taken by another
     * set, make the text red.</p>
     *
     * @param evt
     */
    private void txtTitleKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTitleKeyReleased
	if (editorMode == EditorMode.CREATE) {
	    if (MainWindow.getMainWindow().setTitleExists(this.txtTitle.getText())) {
		this.btnSave.setEnabled(false);
		this.txtTitle.setForeground(Color.red);
	    } else {
		this.btnSave.setEnabled(true);
		this.txtTitle.setForeground(Color.black);
	    }
	}

    }//GEN-LAST:event_txtTitleKeyReleased


    /**
     * Respond to clicks on the "Manage" checkbox.
     *
     * <p>This calls a method that enables and disables components as needed.</p>
     *
     * @param evt
     */
    private void cbxManageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxManageActionPerformed
	this.addManagedByTextToDescription();

	this.setEnableStates();
    }//GEN-LAST:event_cbxManageActionPerformed


    /**
     * Respond to clicks on the "Tweet" checkbox.
     *
     * <p>If the user selects the checkbox, verify that they are authorized to
     * use Twitter. If not, offer to take them to the Preferences where they
     * can authorize.</p>
     *
     * @param evt
     */
    private void btnSaveAndRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveAndRefreshActionPerformed

	if (this.doValidation()) {
	    if (this.editorMode == EditorMode.CREATE) {
		this.ssPhotoset.setPrimaryPhotoIcon(new ImageIcon(this.getClass().getClassLoader().getResource("images/empty_set_icon.png")));
		MainWindow.getMainWindow().executeAddSetWorker(this.ssPhotoset);
	    } else {
		MainWindow.getMainWindow().executeRefreshSetWorker(this.ssPhotoset);
	    }

	    this.setVisible(false);
	    this.dispose();
	}
    }//GEN-LAST:event_btnSaveAndRefreshActionPerformed

    private void lblIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblIconMouseClicked
	if (evt.getClickCount() == 2) {
	    if (this.editorMode == EditorMode.EDIT) {
		//JOptionPane.showMessageDialog(this, "Edit primary photo", "edit", JOptionPane.INFORMATION_MESSAGE);

		try {
		    PhotoPickerDialog picker = new PhotoPickerDialog(this, true, this.ssPhotoset);
		    picker.setVisible(true);
		} catch (Exception e) {
		    logger.error("Could not display photo picker dialog.", e);
		    JOptionPane.showMessageDialog(this,
			    "Something went wrong while trying to display the Photo Picker.\n"
			    + "If this error persists, you should probably send the logs\n"
			    + "to the developers for assistance.",
			    "Could not open Photo Picker",
			    JOptionPane.ERROR_MESSAGE);
		}
	    }
	}
    }//GEN-LAST:event_lblIconMouseClicked

    private void radioTweetUpdatedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioTweetUpdatedActionPerformed
	this.txtTweet.setEnabled(true);
	this.txtTweet.setText(SSConstants.DEFAULT_TWEET_TEMPLATE);
	if (!TwitterHelper.isAuthorized()) {
	    int confirm = JOptionPane.showConfirmDialog(this,
		    "SuprSetr is not yet authorized to send tweets.\n"
		    + "You can authorize SuprSetr from the Preferences dialog.\n"
		    + "Would you like to do this now?",
		    "Authorize Twitter?",
		    JOptionPane.YES_NO_OPTION,
		    JOptionPane.QUESTION_MESSAGE);

	    if (confirm == JOptionPane.YES_OPTION) {
		Preferences prefs = new Preferences(MainWindow.getMainWindow(), true);
		prefs.setTabIndex(Preferences.TWITTER_PANEL);
		prefs.setVisible(true);
	    }
	}

    }//GEN-LAST:event_radioTweetUpdatedActionPerformed

    private void radioTweetCreatedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioTweetCreatedActionPerformed
	this.txtTweet.setEnabled(true);
	this.txtTweet.setText(SSConstants.DEFAULT_TWEET_CREATE_TEMPLATE);
	if (!TwitterHelper.isAuthorized()) {
	    int confirm = JOptionPane.showConfirmDialog(this,
		    "SuprSetr is not yet authorized to send tweets.\n"
		    + "You can authorize SuprSetr from the Preferences dialog.\n"
		    + "Would you like to do this now?",
		    "Authorize Twitter?",
		    JOptionPane.YES_NO_OPTION,
		    JOptionPane.QUESTION_MESSAGE);

	    if (confirm == JOptionPane.YES_OPTION) {
		Preferences prefs = new Preferences(MainWindow.getMainWindow(), true);
		prefs.setTabIndex(Preferences.TWITTER_PANEL);
		prefs.setVisible(true);
	    }
	}
    }//GEN-LAST:event_radioTweetCreatedActionPerformed

    private void radioTweetNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioTweetNoneActionPerformed
	this.txtTweet.setEnabled(false);
    }//GEN-LAST:event_radioTweetNoneActionPerformed

    private void cbxLimitSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxLimitSizeActionPerformed
	this.txtSetSize.setEnabled(cbxLimitSize.isSelected());
    }//GEN-LAST:event_cbxLimitSizeActionPerformed

    private void cbxOnThisDayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxOnThisDayActionPerformed
	if (this.cbxOnThisDay.isSelected()) {
	    this.cbxDateTaken.setSelected(false);
	    this.cbxDateUploaded.setSelected(false);
	    int sort = this.cmbSortBy.getSelectedIndex();
	    if (sort != 2 && sort != 3 && sort != 6) {
		this.cmbSortBy.setSelectedIndex(3);	// sort ascending by default
	    }
	}
	this.setEnableStates();
    }//GEN-LAST:event_cbxOnThisDayActionPerformed

    private void cmbOTDMonthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbOTDMonthActionPerformed
	this.updateDayOfMonthComboBox();
    }//GEN-LAST:event_cmbOTDMonthActionPerformed

    private void cbxCurrentYearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxCurrentYearActionPerformed
	if (this.cbxCurrentYear.isSelected()) {
	    this.txtOTDYearEnd.setEnabled(false);
	    this.txtOTDYearEnd.setText(Integer.toString(SSUtils.getCurrentYear()));
	} else {
	    this.txtOTDYearEnd.setEnabled(true);
	}
    }//GEN-LAST:event_cbxCurrentYearActionPerformed


    private void cmbSortByActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSortByActionPerformed
	// check for sort mode used when an On This Day set is created.
	// warn the user about sort mode selection
	if (this.cbxOnThisDay.isSelected()) {
	    switch (this.cmbSortBy.getSelectedIndex()) {
		case 2:					// Date Taken Descending
		case 3:					// Date Taken Ascending
		case 6:					// No Particular Order
		    // These sorting modes are handled correctly.
		    break;

		default:
		    JOptionPane.showMessageDialog(this,
			    "The selected sorting mode will produce unexpected \n" +
			    "results when used with an 'On This Day' set.\n" +
			    "It is recommended that you use either \n" +
			    "'Date Taken Descending', 'Date Taken Ascending', \n" +
			    "or 'No Particular Order' sort modes with this type of set.",
			    "Incompatible Sort Mode",
			    JOptionPane.WARNING_MESSAGE);
		    break;
	    }
	}
    }//GEN-LAST:event_cmbSortByActionPerformed


    private boolean doValidation() {
	boolean ok = true;
	String title;
	String description;
	String tagMode;
	String tags;
	Date takenMin = null;
	Date takenMax = null;
	Date uploadedMin = null;
	Date uploadedMax = null;
	StringBuilder sb = new StringBuilder();

	// Do a bunch of validation. Don't set the fields until all validation
	// is successful!

	// TITLE:
	// MUST HAVE A TITLE. DESCRIPTION IS OPTIONAL BUT IF NULL, WE WILL MAKE
	// IT AN EMPTY STRING
	title = this.txtTitle.getText();
	if (title == null || title.trim().isEmpty()) {
	    sb.append("You must provide a title.\n");
	    ok = false;
	}

	description = this.txtDescription.getText();
	if (description == null) {
	    description = "";
	}
	this.addManagedByTextToDescription();

	// TAGS:
	// IF ANY TAGS ENTERED, SET THE MATCH MODE, OTHERWISE SET MATCH MODE TO NONE
	tags = this.txtTags.getText();
	if (tags == null || tags.trim().isEmpty()) {
	    tagMode = SSConstants.TAG_MATCH_MODE_NONE;
	} else {
	    if (cmbTags.getSelectedIndex() == 0) {
		tagMode = SSConstants.TAG_MATCH_MODE_ALL;
	    } else {
		tagMode = SSConstants.TAG_MATCH_MODE_ANY;
	    }
	}

	// DATE TAKEN:
	// IF THE DATE TAKEN CHECKBOX IS SELECTED, DATES MUST BE VALID
	// YYYY-MM-DD FORMAT, AND BEFORE MUST BE EARLIER THAN AFTER
	if (this.cbxDateTaken.isSelected()) {
	    takenMin = SSUtils.parseYYYYMMDDHHmmss(this.txtDateTakenAfter.getText() + " 00:00:00");
	    takenMax = SSUtils.parseYYYYMMDDHHmmss(this.txtDateTakenBefore.getText() + " 23:59:59");
	    if (takenMin == null || takenMax == null) {
		sb.append("You must provide a date range to match 'Date Taken'\n");
		ok = false;
	    } else if (takenMax.before(takenMin)) {
		sb.append("Date Taken 'before' must be earler than 'after'\n");
		ok = false;
	    }
	}

	// DATE UPLOADED:
	// IF THE DATE UPLOADED CHECKBOX IS SELECTED, DATES MUST BE VALID
	// YYYY-MM-DD FORMAT
	if (this.cbxDateUploaded.isSelected()) {
	    uploadedMin = SSUtils.parseYYYYMMDDHHmmss(this.txtDateUploadedAfter.getText() + " 00:00:00");
	    uploadedMax = SSUtils.parseYYYYMMDDHHmmss(this.txtDateUploadedBefore.getText() + " 23:59:59");
	    if (uploadedMin == null || uploadedMax == null) {
		sb.append("You must provide a date range to match 'Date Uploaded'\n");
		ok = false;
	    } else if (uploadedMax.before(uploadedMin)) {
		sb.append("Date Uploaded 'before' must be earlier than 'after'\n");
		ok = false;
	    }
	}

	// LIMIT SET SIZE:
	// IF CHECKBOX IS CHECKED, VALUE MUST BE > 0
	if (this.cbxLimitSize.isSelected()) {
	    try {
		int size = Integer.parseInt(this.txtSetSize.getText());
		if (size < 1) {
		    throw new Exception();
		}
	    } catch (Exception e) {
		sb.append("Set size limit must be a number greater than zero.\n");
		ok = false;
	    }
	}

	if (this.cbxOnThisDay.isSelected()) {
	    int yearStart = 0;
	    int yearEnd = 0;
	    try {
		yearStart = Integer.parseInt(this.txtOTDYearStart.getText());
		if (yearStart < 1900) {
		    throw new Exception();
		}
		this.ssPhotoset.setOnThisDayYearStart(yearStart);
	    } catch (Exception e) {
		sb.append("On this day start year must be a number greater than 1900.\n");
		ok = false;
	    }
	    try {
		if (this.cbxCurrentYear.isSelected()) {
		    this.ssPhotoset.setOnThisDayYearEnd(0);
		    yearEnd = SSUtils.getCurrentYear();	// to make the start > end check valid
		} else {
		    yearEnd = Integer.parseInt(this.txtOTDYearEnd.getText());
		    if (yearEnd  > SSUtils.getCurrentYear()) {
			throw new Exception();
		    }
		    this.ssPhotoset.setOnThisDayYearEnd(yearEnd);
		}
	    } catch (Exception e) {
		sb.append("On this day end year must be a number less than the current year.\n");
		ok = false;
	    }

	    if (yearStart > yearEnd) {
		sb.append("On this day start year cannot be larger than the end year.\n");
		ok = false;
	    }
	}


	if (ok) {
	    // do we have to change the title/description on Flickr?
	    String t = this.txtTitle.getText().trim();
	    String d = this.txtDescription.getText().trim();
	    
	    if (
		    ( !t.equals(this.ssPhotoset.getTitle()) ||
		    !d.equals(this.ssPhotoset.getDescription()) ) &&
		    this.editorMode == EditorMode.EDIT) {
		try {
		    PhotosetsApi.getInstance().editMeta(this.ssPhotoset.getId(), t, d);
		    LogWindow.addLogMessage("Title and/or Description updated on Flickr for set '" +
			    this.ssPhotoset.getTitle() + "'.");
		} catch (Exception e) {
		    logger.error("Error setting metadata.", e);
		    JOptionPane.showMessageDialog(this, "There was an error while changing the title and/or\n"
			    + "description for this set on Flickr. This is probably temporary.\n"
			    + "Just wait a few minutes and try refreshing the set.",
			    "Error setting metadata",
			    JOptionPane.ERROR_MESSAGE);
		}
	    }
	    this.ssPhotoset.setManaged(this.cbxManage.isSelected());
	    this.ssPhotoset.setTitle(title.trim());
	    this.ssPhotoset.setDescription(description.trim());
	    this.ssPhotoset.setTagMatchMode(tagMode);
	    this.ssPhotoset.setTags(this.txtTags.getText());

	    this.ssPhotoset.setMatchTakenDates(this.cbxDateTaken.isSelected());
	    this.ssPhotoset.setMinTakenDate(takenMin);
	    this.ssPhotoset.setMaxTakenDate(takenMax);

	    this.ssPhotoset.setMatchUploadDates(this.cbxDateUploaded.isSelected());
	    this.ssPhotoset.setMinUploadDate(uploadedMin);
	    this.ssPhotoset.setMaxUploadDate(uploadedMax);

	    this.ssPhotoset.setSortOrder(this.cmbSortBy.getSelectedIndex());

	    this.ssPhotoset.setSendTweet(this.radioTweetUpdated.isSelected());
	    this.ssPhotoset.setTweetWhenCreated(this.radioTweetCreated.isSelected());
	    if (this.txtTweet.getText().trim().isEmpty()) {
		this.ssPhotoset.setTweetTemplate(SSConstants.DEFAULT_TWEET_TEMPLATE);
	    } else {
		this.ssPhotoset.setTweetTemplate(this.txtTweet.getText().trim());
	    }

	    this.ssPhotoset.setLockPrimaryPhoto(this.cbxLock.isSelected());

	    this.ssPhotoset.setPrivacy(this.cmbPrivacy.getSelectedIndex());
	    this.ssPhotoset.setSafeSearch(this.cmbSafeSearch.getSelectedIndex());
	    this.ssPhotoset.setContentType(this.cmbContentType.getSelectedIndex());
	    this.ssPhotoset.setMediaType(this.cmbMediaType.getSelectedIndex());
	    this.ssPhotoset.setGeotagged(this.cmbGeotag.getSelectedIndex());
	    
	    this.ssPhotoset.setInCommons(this.cbxInCommons.isSelected());
	    this.ssPhotoset.setInGallery(this.cbxInGallery.isSelected());
	    this.ssPhotoset.setInGetty(this.cbxInGetty.isSelected());

	    this.ssPhotoset.setLimitSize(this.cbxLimitSize.isSelected());
	    this.ssPhotoset.setSizeLimit(Integer.parseInt(this.txtSetSize.getText()));

	    this.ssPhotoset.setOnThisDay(this.cbxOnThisDay.isSelected());
	    this.ssPhotoset.setOnThisDayMonth(this.cmbOTDMonth.getSelectedIndex() + 1);
	    this.ssPhotoset.setOnThisDayDay(this.cmbOTDDay.getSelectedIndex() + 1);
	    
	} else {
	    JOptionPane.showMessageDialog(this,
		    "Please correct these errors and try again:\n"
		    + sb.toString(),
		    "Missing Information",
		    JOptionPane.ERROR_MESSAGE);
	}

	return ok;
    }


    /**
     * Enable and disable components as needed.
     *
     * <p>The enable/disable state of entry boxes, etc depends on if the set is
     * managed by SuprSetr. This method will set all components as needed.</p>
     */
    private void setEnableStates() {
	// first, set all enable/disable based on manage state
	this.txtTitle.setEnabled(this.cbxManage.isSelected());
	this.txtDescription.setEnabled(this.cbxManage.isSelected());
	this.cmbTags.setEnabled(this.cbxManage.isSelected());
	this.txtTags.setEnabled(this.cbxManage.isSelected());
	this.cbxDateTaken.setEnabled(this.cbxManage.isSelected());
	this.txtDateTakenAfter.setEnabled(this.cbxManage.isSelected());
	this.txtDateTakenBefore.setEnabled(this.cbxManage.isSelected());
	this.cbxDateUploaded.setEnabled(this.cbxManage.isSelected());
	this.txtDateUploadedAfter.setEnabled(this.cbxManage.isSelected());
	this.txtDateUploadedBefore.setEnabled(this.cbxManage.isSelected());
	this.cmbSortBy.setEnabled(this.cbxManage.isSelected());
	this.radioTweetCreated.setEnabled(this.cbxManage.isSelected());
	this.radioTweetNone.setEnabled(this.cbxManage.isSelected());
	this.radioTweetUpdated.setEnabled(this.cbxManage.isSelected());
	this.txtTweet.setEnabled(this.cbxManage.isSelected());
	this.cbxLock.setEnabled(this.cbxManage.isSelected());

	// now set things that may change if is managed
	if (this.cbxManage.isSelected()) {
	    this.txtDateTakenAfter.setEnabled(this.cbxDateTaken.isSelected());
	    this.txtDateTakenBefore.setEnabled(this.cbxDateTaken.isSelected());

	    this.txtDateUploadedAfter.setEnabled(this.cbxDateUploaded.isSelected());
	    this.txtDateUploadedBefore.setEnabled(this.cbxDateUploaded.isSelected());

	    this.txtTweet.setEnabled(this.radioTweetCreated.isSelected() ||
		    this.radioTweetUpdated.isSelected());
	    
	}

	// set the On This Day section
	this.cbxOnThisDay.setEnabled(this.cbxManage.isSelected());
	this.cmbOTDDay.setEnabled(this.cbxManage.isSelected() && this.cbxOnThisDay.isSelected());
	this.cmbOTDMonth.setEnabled(this.cbxManage.isSelected() && this.cbxOnThisDay.isSelected());
	this.txtOTDYearEnd.setEnabled(this.cbxManage.isSelected() && this.cbxOnThisDay.isSelected() && (! this.cbxCurrentYear.isSelected()));
	this.txtOTDYearStart.setEnabled(this.cbxManage.isSelected() && this.cbxOnThisDay.isSelected());
	this.cbxCurrentYear.setEnabled(this.cbxManage.isSelected() && this.cbxOnThisDay.isSelected());

    }


    /**
     * Set the message label text.
     *
     * <p>Used by the PhotoPickerDialog to display a message when the user
     * changes the primary photo.</p>
     *
     * @param message
     */
    void setMessage(String message) {
	this.lblMessage.setText(message);
    }

    /**
     * Set the primary photo image.
     *
     * <p>Used by the PhotoPickerDialog when the user changes the primary photo.</p>
     * 
     * @param id
     * @param image
     */
    void setPrimaryPhotoImage(String id, ImageIcon image) {
	this.lblIcon.setIcon(image);
	this.ssPhotoset.setPrimary(id);
	this.ssPhotoset.setPrimaryPhotoIcon(image);
	this.cbxLock.setSelected(true);
	this.ssPhotoset.setLockPrimaryPhoto(true);
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel advancedPanel;
    private javax.swing.JPanel basicPanel;
    private javax.swing.JButton btnCancel;
    private javax.swing.ButtonGroup btnGrpTweet;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSaveAndRefresh;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JCheckBox cbxCurrentYear;
    private javax.swing.JCheckBox cbxDateTaken;
    private javax.swing.JCheckBox cbxDateUploaded;
    private javax.swing.JCheckBox cbxInCommons;
    private javax.swing.JCheckBox cbxInGallery;
    private javax.swing.JCheckBox cbxInGetty;
    private javax.swing.JCheckBox cbxLimitSize;
    private javax.swing.JCheckBox cbxLock;
    private javax.swing.JCheckBox cbxManage;
    private javax.swing.JCheckBox cbxOnThisDay;
    private javax.swing.JComboBox cmbContentType;
    private javax.swing.JComboBox cmbGeotag;
    private javax.swing.JComboBox cmbMediaType;
    private javax.swing.JComboBox cmbOTDDay;
    private javax.swing.JComboBox cmbOTDMonth;
    private javax.swing.JComboBox cmbPrivacy;
    private javax.swing.JComboBox cmbSafeSearch;
    private javax.swing.JComboBox cmbSortBy;
    private javax.swing.JComboBox cmbTags;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JLabel lblSafeSearch;
    private javax.swing.JPanel pnlDates;
    private javax.swing.JPanel pnlOther;
    private javax.swing.JPanel pnlTags;
    private javax.swing.JPanel pnlTitle;
    private javax.swing.JRadioButton radioTweetCreated;
    private javax.swing.JRadioButton radioTweetNone;
    private javax.swing.JRadioButton radioTweetUpdated;
    private javax.swing.JTextField txtDateTakenAfter;
    private javax.swing.JTextField txtDateTakenBefore;
    private javax.swing.JTextField txtDateUploadedAfter;
    private javax.swing.JTextField txtDateUploadedBefore;
    private javax.swing.JTextArea txtDescription;
    private javax.swing.JTextField txtOTDYearEnd;
    private javax.swing.JTextField txtOTDYearStart;
    private javax.swing.JTextField txtSetSize;
    private javax.swing.JTextField txtTags;
    private javax.swing.JTextField txtTitle;
    private javax.swing.JEditorPane txtTweet;
    // End of variables declaration//GEN-END:variables


    /**
     * Document that is limited to a specific number of characters.
     *
     * <p>This document is used by the Tweet Profile text entry field to limit
     * user entry to 140 characters.</p>
     */
    class LimitedPlainDocument extends PlainDocument {

	/** Maximum length of the text field. */
	private int maxLength;


	/**
	 * Constructor.
	 *
	 * @param maxLength the maximum length of the document.
	 */
	public LimitedPlainDocument(int maxLength) {
	    this.maxLength = maxLength;
	}


	/**
	 * Override the insertString method, limiting length.
	 * 
	 * @param offset
	 * @param str
	 * @param a
	 * @throws BadLocationException
	 */
	@Override
	public void insertString(int offset, String str, AttributeSet a)
		throws BadLocationException {
	    int length = str.length();

	    if (offset + length > maxLength) {
		length = maxLength - offset;
	    }

	    super.insertString(offset, str.substring(0, length), a);
	}

    }


    private void updateDayOfMonthComboBox() {
	// save the current day selection
	int currentDay = this.cmbOTDDay.getSelectedIndex() + 1;

	switch (this.cmbOTDMonth.getSelectedIndex() + 1) {

	    // 29 in February (allow for leap year)
	    case 2:
		this.cmbOTDDay.setModel(MODEL_29_DAYS);
		break;

	    // 30 days in April, June, September, November
	    case 4:
	    case 6:
	    case 9:
	    case 11:
		this.cmbOTDDay.setModel(MODEL_30_DAYS);
		break;
		
	    // 31 days in January, March, May, July, August, October, December
	    case 1:
	    case 3:
	    case 5:
	    case 7:
	    case 8:
	    case 10:
	    case 12:
		this.cmbOTDDay.setModel(MODEL_31_DAYS);
		break;
	}

	// now restore the selected index
	if (currentDay > this.cmbOTDDay.getModel().getSize()) {
	    currentDay = this.cmbOTDDay.getModel().getSize();
	}
	this.cmbOTDDay.setSelectedIndex(currentDay - 1);
    }


    private void addManagedByTextToDescription() {
	if (DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_ADD_MANAGED))) {
	    String desc = this.txtDescription.getText().trim();

	    desc = desc.replace(SSConstants.ADD_MANAGED, "").trim();

	    if (this.cbxManage.isSelected()) {
		if (desc.isEmpty()) {
		    desc = desc + SSConstants.ADD_MANAGED;
		} else {
		    desc = desc + "\n\n" + SSConstants.ADD_MANAGED;
		}
	    }

	    this.txtDescription.setText(desc);
	}
    }
}
