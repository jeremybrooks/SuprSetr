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

import net.jeremybrooks.jinx.api.PhotosetsApi;
import net.jeremybrooks.suprsetr.dao.DAOHelper;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import net.jeremybrooks.suprsetr.dao.PhotosetDAO;
import net.jeremybrooks.suprsetr.twitter.TwitterHelper;
import net.jeremybrooks.suprsetr.utils.SSUtils;
import net.jeremybrooks.suprsetr.utils.SimpleCache;
import org.apache.log4j.Logger;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormatSymbols;
import java.util.Date;
import java.util.ResourceBundle;


/**
 * @author jeremyb
 */
public class SetEditor extends javax.swing.JDialog {


	private static final long serialVersionUID = -5144092069221017344L;

	/**
	 * Mode we are being called in.
	 */
	public static enum EditorMode {
		CREATE, EDIT
	};

	/**
	 * The combo box model for 29 days.
	 */
	private static ComboBoxModel MODEL_29_DAYS = new javax.swing.DefaultComboBoxModel(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29"});

	/**
	 * The combo box model for 30 days.
	 */
	private static ComboBoxModel MODEL_30_DAYS = new javax.swing.DefaultComboBoxModel(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"});

	/**
	 * The combo box model for 31 days.
	 */
	private static ComboBoxModel MODEL_31_DAYS = new javax.swing.DefaultComboBoxModel(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"});


	/**
	 * The mode.
	 */
	private EditorMode editorMode = EditorMode.CREATE;

	/**
	 * The photoset we are working on.
	 */
	private SSPhotoset ssPhotoset;

	/**
	 * Logging.
	 */
	private Logger logger = Logger.getLogger(SetEditor.class);

	private String[] sortModelArray = new String[9];

	/**
	 * Cancelable properties.
	 */
	// These can be changed from the PhotoPicker, so we need to remember this
	// state when the window is first displayed in order to revert any changes
	// if the user clicks Cancel
	private ImageIcon originalPrimaryIcon;
	private String originalPrimaryId;
	private boolean originalLockSelected;

	private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.seteditor");


	/**
	 * Create a new instance of the SetEditor.
	 *
	 * @param parent     the parent frame.
	 * @param editorMode mode to edit in (create or edit).
	 * @param ssPhotoset the photoset we are editing or creating.
	 */
	public SetEditor(JFrame parent, EditorMode editorMode, SSPhotoset ssPhotoset) {
		super(parent, true);

		this.ssPhotoset = ssPhotoset;
		this.editorMode = editorMode;
		for (int i = 0; i < 9; i++) {
			sortModelArray[i] = resourceBundle.getString("SetEditor.sortModel." + i);
		}

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
		jTabbedPane1 = new JTabbedPane();
		basicPanel = new JPanel();
		pnlTitle = new JPanel();
		lblIcon = new JLabel();
		cbxManage = new JCheckBox();
		cbxLock = new JCheckBox();
		txtTitle = new JTextField();
		jScrollPane1 = new JScrollPane();
		txtDescription = new JTextArea();
		lblMessage = new JLabel();
		pnlTags = new JPanel();
		txtTags = new JTextField();
		cmbTags = new JComboBox<>();
		pnlDates = new JPanel();
		cbxDateTaken = new JCheckBox();
		txtDateTakenAfter = new JTextField();
		jLabel3 = new JLabel();
		txtDateTakenBefore = new JTextField();
		cbxDateUploaded = new JCheckBox();
		txtDateUploadedAfter = new JTextField();
		jLabel4 = new JLabel();
		txtDateUploadedBefore = new JTextField();
		cbxOnThisDay = new JCheckBox();
		cmbOTDMonth = new JComboBox();
		cmbOTDDay = new JComboBox<>();
		jLabel9 = new JLabel();
		txtOTDYearStart = new JTextField();
		jLabel13 = new JLabel();
		txtOTDYearEnd = new JTextField();
		cbxCurrentYear = new JCheckBox();
		pnlOther = new JPanel();
		jLabel1 = new JLabel();
		cmbSortBy = new JComboBox();
		jScrollPane3 = new JScrollPane();
		txtTweet = new JEditorPane();
		jLabel2 = new JLabel();
		jLabel5 = new JLabel();
		jLabel6 = new JLabel();
		jLabel7 = new JLabel();
		radioTweetNone = new JRadioButton();
		radioTweetUpdated = new JRadioButton();
		radioTweetCreated = new JRadioButton();
		advancedPanel = new JPanel();
		jPanel4 = new JPanel();
		cmbPrivacy = new JComboBox<>();
		jLabel8 = new JLabel();
		lblSafeSearch = new JLabel();
		cmbSafeSearch = new JComboBox<>();
		jPanel5 = new JPanel();
		jLabel10 = new JLabel();
		cmbContentType = new JComboBox<>();
		jLabel11 = new JLabel();
		cmbMediaType = new JComboBox<>();
		jPanel6 = new JPanel();
		jLabel12 = new JLabel();
		cmbGeotag = new JComboBox<>();
		cbxInGallery = new JCheckBox();
		cbxInCommons = new JCheckBox();
		cbxInGetty = new JCheckBox();
		cbxLimitSize = new JCheckBox();
		txtSetSize = new JTextField();
		buttonPanel = new JPanel();
		btnSave = new JButton();
		btnCancel = new JButton();
		btnSaveAndRefresh = new JButton();

		//======== this ========
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== jTabbedPane1 ========
		{

			//======== basicPanel ========
			{

				//======== pnlTitle ========
				{
					pnlTitle.setBorder(new TitledBorder(bundle.getString("SetEditor.pnlTitle.border")));

					//---- lblIcon ----
					lblIcon.setIcon(new ImageIcon(getClass().getResource("/images/empty_set_icon.png")));
					lblIcon.setToolTipText(bundle.getString("SetEditor.lblIcon.toolTipText"));
					lblIcon.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							lblIconMouseClicked(e);
						}
					});

					//---- cbxManage ----
					cbxManage.setText(bundle.getString("SetEditor.cbxManage.text"));
					cbxManage.setToolTipText(bundle.getString("SetEditor.cbxManage.toolTipText"));
					cbxManage.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							cbxManageActionPerformed(e);
						}
					});

					//---- cbxLock ----
					cbxLock.setText(bundle.getString("SetEditor.cbxLock.text"));
					cbxLock.setToolTipText(bundle.getString("SetEditor.cbxLock.toolTipText"));

					//---- txtTitle ----
					txtTitle.setToolTipText(bundle.getString("SetEditor.txtTitle.toolTipText"));
					txtTitle.addKeyListener(new KeyAdapter() {
						@Override
						public void keyReleased(KeyEvent e) {
							txtTitleKeyReleased(e);
						}
					});

					//======== jScrollPane1 ========
					{

						//---- txtDescription ----
						txtDescription.setColumns(20);
						txtDescription.setLineWrap(true);
						txtDescription.setRows(5);
						txtDescription.setToolTipText(bundle.getString("SetEditor.txtDescription.toolTipText"));
						txtDescription.setWrapStyleWord(true);
						jScrollPane1.setViewportView(txtDescription);
					}

					//---- lblMessage ----
					lblMessage.setFont(new Font("Lucida Grande", Font.BOLD, 13));
					lblMessage.setForeground(Color.red);

					GroupLayout pnlTitleLayout = new GroupLayout(pnlTitle);
					pnlTitle.setLayout(pnlTitleLayout);
					pnlTitleLayout.setHorizontalGroup(
						pnlTitleLayout.createParallelGroup()
							.addGroup(pnlTitleLayout.createSequentialGroup()
								.addContainerGap()
								.addGroup(pnlTitleLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
									.addGroup(GroupLayout.Alignment.LEADING, pnlTitleLayout.createSequentialGroup()
										.addComponent(lblIcon, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(pnlTitleLayout.createParallelGroup()
											.addComponent(txtTitle, GroupLayout.DEFAULT_SIZE, 655, Short.MAX_VALUE)
											.addGroup(pnlTitleLayout.createSequentialGroup()
												.addGroup(pnlTitleLayout.createParallelGroup()
													.addComponent(cbxLock)
													.addComponent(cbxManage))
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(lblMessage))))
									.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 736, Short.MAX_VALUE))
								.addContainerGap())
					);
					pnlTitleLayout.setVerticalGroup(
						pnlTitleLayout.createParallelGroup()
							.addGroup(pnlTitleLayout.createSequentialGroup()
								.addGroup(pnlTitleLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
									.addGroup(pnlTitleLayout.createSequentialGroup()
										.addGroup(pnlTitleLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
											.addGroup(pnlTitleLayout.createSequentialGroup()
												.addComponent(cbxManage)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(cbxLock)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
											.addGroup(pnlTitleLayout.createSequentialGroup()
												.addContainerGap()
												.addComponent(lblMessage)
												.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)))
										.addComponent(txtTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
									.addComponent(lblIcon, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE)
								.addContainerGap())
					);
				}

				//======== pnlTags ========
				{
					pnlTags.setBorder(new TitledBorder(bundle.getString("SetEditor.pnlTags.border")));

					//---- txtTags ----
					txtTags.setToolTipText(bundle.getString("SetEditor.txtTags.toolTipText"));

					//---- cmbTags ----
					cmbTags.setModel(new DefaultComboBoxModel<>(new String[] {
						"All",
						"Any"
					}));
					cmbTags.setToolTipText(bundle.getString("SetEditor.cmbTags.toolTipText"));

					GroupLayout pnlTagsLayout = new GroupLayout(pnlTags);
					pnlTags.setLayout(pnlTagsLayout);
					pnlTagsLayout.setHorizontalGroup(
						pnlTagsLayout.createParallelGroup()
							.addGroup(pnlTagsLayout.createSequentialGroup()
								.addComponent(cmbTags, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(txtTags, GroupLayout.DEFAULT_SIZE, 662, Short.MAX_VALUE))
					);
					pnlTagsLayout.setVerticalGroup(
						pnlTagsLayout.createParallelGroup()
							.addGroup(pnlTagsLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(cmbTags, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
								.addComponent(txtTags, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					);
				}

				//======== pnlDates ========
				{
					pnlDates.setBorder(new TitledBorder(bundle.getString("SetEditor.pnlDates.border")));

					//---- cbxDateTaken ----
					cbxDateTaken.setText(bundle.getString("SetEditor.cbxDateTaken.text"));
					cbxDateTaken.setToolTipText(bundle.getString("SetEditor.cbxDateTaken.toolTipText"));
					cbxDateTaken.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							cbxDateTakenActionPerformed(e);
						}
					});

					//---- txtDateTakenAfter ----
					txtDateTakenAfter.setToolTipText(bundle.getString("SetEditor.txtDateTakenAfter.toolTipText"));
					txtDateTakenAfter.setEnabled(false);

					//---- jLabel3 ----
					jLabel3.setText(bundle.getString("SetEditor.jLabel3.text"));

					//---- txtDateTakenBefore ----
					txtDateTakenBefore.setToolTipText(bundle.getString("SetEditor.txtDateTakenBefore.toolTipText"));
					txtDateTakenBefore.setEnabled(false);

					//---- cbxDateUploaded ----
					cbxDateUploaded.setText(bundle.getString("SetEditor.cbxDateUploaded.text"));
					cbxDateUploaded.setToolTipText(bundle.getString("SetEditor.cbxDateUploaded.toolTipText"));
					cbxDateUploaded.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							cbxDateUploadedActionPerformed(e);
						}
					});

					//---- txtDateUploadedAfter ----
					txtDateUploadedAfter.setToolTipText(bundle.getString("SetEditor.txtDateUploadedAfter.toolTipText"));
					txtDateUploadedAfter.setEnabled(false);

					//---- jLabel4 ----
					jLabel4.setText(bundle.getString("SetEditor.jLabel4.text"));

					//---- txtDateUploadedBefore ----
					txtDateUploadedBefore.setToolTipText(bundle.getString("SetEditor.txtDateUploadedBefore.toolTipText"));
					txtDateUploadedBefore.setEnabled(false);

					//---- cbxOnThisDay ----
					cbxOnThisDay.setText(bundle.getString("SetEditor.cbxOnThisDay.text"));
					cbxOnThisDay.setToolTipText(bundle.getString("SetEditor.cbxOnThisDay.toolTipText"));
					cbxOnThisDay.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							cbxOnThisDayActionPerformed(e);
						}
					});

					//---- cmbOTDMonth ----
					cmbOTDMonth.setToolTipText(bundle.getString("SetEditor.cmbOTDMonth.toolTipText"));
					cmbOTDMonth.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							cmbOTDMonthActionPerformed(e);
						}
					});
					cmbOTDMonth.setModel(new DefaultComboBoxModel(DateFormatSymbols.getInstance().getMonths()));

					//---- cmbOTDDay ----
					cmbOTDDay.setModel(new DefaultComboBoxModel<>(new String[] {
						"1",
						"2",
						"3",
						"4",
						"5",
						"6",
						"7",
						"8",
						"9",
						"10",
						"11",
						"12",
						"13",
						"14",
						"15",
						"16",
						"17",
						"18",
						"19",
						"20",
						"21",
						"22",
						"23",
						"24",
						"25",
						"26",
						"27",
						"28",
						"29",
						"30",
						"31"
					}));
					cmbOTDDay.setToolTipText(bundle.getString("SetEditor.cmbOTDDay.toolTipText"));

					//---- jLabel9 ----
					jLabel9.setText(bundle.getString("SetEditor.jLabel9.text"));

					//---- txtOTDYearStart ----
					txtOTDYearStart.setToolTipText(bundle.getString("SetEditor.txtOTDYearStart.toolTipText"));

					//---- jLabel13 ----
					jLabel13.setText(bundle.getString("SetEditor.jLabel13.text"));

					//---- txtOTDYearEnd ----
					txtOTDYearEnd.setToolTipText(bundle.getString("SetEditor.txtOTDYearEnd.toolTipText"));

					//---- cbxCurrentYear ----
					cbxCurrentYear.setText(bundle.getString("SetEditor.cbxCurrentYear.text"));
					cbxCurrentYear.setToolTipText(bundle.getString("SetEditor.cbxCurrentYear.toolTipText"));
					cbxCurrentYear.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							cbxCurrentYearActionPerformed(e);
						}
					});

					GroupLayout pnlDatesLayout = new GroupLayout(pnlDates);
					pnlDates.setLayout(pnlDatesLayout);
					pnlDatesLayout.setHorizontalGroup(
						pnlDatesLayout.createParallelGroup()
							.addGroup(pnlDatesLayout.createSequentialGroup()
								.addContainerGap()
								.addGroup(pnlDatesLayout.createParallelGroup()
									.addGroup(pnlDatesLayout.createSequentialGroup()
										.addGroup(pnlDatesLayout.createParallelGroup()
											.addComponent(cbxDateUploaded)
											.addComponent(cbxDateTaken))
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(pnlDatesLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
											.addComponent(txtDateTakenAfter, GroupLayout.Alignment.TRAILING)
											.addComponent(txtDateUploadedAfter, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
										.addGroup(pnlDatesLayout.createParallelGroup()
											.addGroup(pnlDatesLayout.createSequentialGroup()
												.addGap(2, 2, 2)
												.addComponent(jLabel4)
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED))
											.addGroup(GroupLayout.Alignment.TRAILING, pnlDatesLayout.createSequentialGroup()
												.addGap(4, 4, 4)
												.addComponent(jLabel3)
												.addGap(8, 8, 8)))
										.addGroup(pnlDatesLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
											.addComponent(txtDateUploadedBefore)
											.addComponent(txtDateTakenBefore, GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)))
									.addGroup(pnlDatesLayout.createSequentialGroup()
										.addComponent(cbxOnThisDay)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(cmbOTDMonth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(cmbOTDDay, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jLabel9)
										.addGap(1, 1, 1)
										.addComponent(txtOTDYearStart, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
										.addGap(6, 6, 6)
										.addComponent(jLabel13)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(pnlDatesLayout.createParallelGroup()
											.addComponent(cbxCurrentYear)
											.addComponent(txtOTDYearEnd, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE))))
								.addContainerGap(59, Short.MAX_VALUE))
					);
					pnlDatesLayout.setVerticalGroup(
						pnlDatesLayout.createParallelGroup()
							.addGroup(pnlDatesLayout.createSequentialGroup()
								.addGroup(pnlDatesLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(cbxDateTaken)
									.addComponent(jLabel3)
									.addComponent(txtDateTakenAfter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(txtDateTakenBefore, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(pnlDatesLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(cbxDateUploaded)
									.addComponent(txtDateUploadedAfter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(txtDateUploadedBefore, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabel4))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(pnlDatesLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(cbxOnThisDay)
									.addComponent(cmbOTDMonth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(cmbOTDDay, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabel9)
									.addComponent(txtOTDYearStart, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabel13)
									.addComponent(txtOTDYearEnd, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(cbxCurrentYear))
					);
				}

				//======== pnlOther ========
				{
					pnlOther.setBorder(new TitledBorder(bundle.getString("SetEditor.pnlOther.border")));

					//---- jLabel1 ----
					jLabel1.setText(bundle.getString("SetEditor.jLabel1.text"));

					//---- cmbSortBy ----
					cmbSortBy.setToolTipText(bundle.getString("SetEditor.cmbSortBy.toolTipText"));
					cmbSortBy.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							cmbSortByActionPerformed(e);
						}
					});
					cmbSortBy.setModel(new DefaultComboBoxModel<>(this.sortModelArray));

					//======== jScrollPane3 ========
					{

						//---- txtTweet ----
						txtTweet.setToolTipText(bundle.getString("SetEditor.txtTweet.toolTipText"));
						jScrollPane3.setViewportView(txtTweet);
					}

					//---- jLabel2 ----
					jLabel2.setText(bundle.getString("SetEditor.jLabel2.text"));

					//---- jLabel5 ----
					jLabel5.setText(bundle.getString("SetEditor.jLabel5.text"));

					//---- jLabel6 ----
					jLabel6.setText(bundle.getString("SetEditor.jLabel6.text"));

					//---- jLabel7 ----
					jLabel7.setText(bundle.getString("SetEditor.jLabel7.text"));

					//---- radioTweetNone ----
					radioTweetNone.setSelected(true);
					radioTweetNone.setText(bundle.getString("SetEditor.radioTweetNone.text"));
					radioTweetNone.setToolTipText(bundle.getString("SetEditor.radioTweetNone.toolTipText"));
					radioTweetNone.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							radioTweetNoneActionPerformed(e);
						}
					});

					//---- radioTweetUpdated ----
					radioTweetUpdated.setText(bundle.getString("SetEditor.radioTweetUpdated.text"));
					radioTweetUpdated.setToolTipText(bundle.getString("SetEditor.radioTweetUpdated.toolTipText"));
					radioTweetUpdated.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							radioTweetCreatedOrUpdatedActionPerformed(e);
						}
					});

					//---- radioTweetCreated ----
					radioTweetCreated.setText(bundle.getString("SetEditor.radioTweetCreated.text"));
					radioTweetCreated.setToolTipText(bundle.getString("SetEditor.radioTweetCreated.toolTipText"));
					radioTweetCreated.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							radioTweetCreatedOrUpdatedActionPerformed(e);
						}
					});

					GroupLayout pnlOtherLayout = new GroupLayout(pnlOther);
					pnlOther.setLayout(pnlOtherLayout);
					pnlOtherLayout.setHorizontalGroup(
						pnlOtherLayout.createParallelGroup()
							.addGroup(pnlOtherLayout.createSequentialGroup()
								.addGroup(pnlOtherLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
									.addGroup(pnlOtherLayout.createSequentialGroup()
										.addContainerGap()
										.addGroup(pnlOtherLayout.createParallelGroup()
											.addComponent(radioTweetNone)
											.addComponent(radioTweetUpdated)
											.addComponent(radioTweetCreated))
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(jScrollPane3))
									.addGroup(pnlOtherLayout.createSequentialGroup()
										.addGap(78, 78, 78)
										.addComponent(jLabel1)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(cmbSortBy, GroupLayout.PREFERRED_SIZE, 277, GroupLayout.PREFERRED_SIZE)))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(pnlOtherLayout.createParallelGroup()
									.addComponent(jLabel2)
									.addComponent(jLabel5)
									.addComponent(jLabel6)
									.addComponent(jLabel7))
								.addContainerGap(43, Short.MAX_VALUE))
					);
					pnlOtherLayout.setVerticalGroup(
						pnlOtherLayout.createParallelGroup()
							.addGroup(pnlOtherLayout.createSequentialGroup()
								.addGroup(pnlOtherLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(cmbSortBy, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabel1))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(pnlOtherLayout.createParallelGroup()
									.addGroup(pnlOtherLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
										.addGroup(GroupLayout.Alignment.LEADING, pnlOtherLayout.createSequentialGroup()
											.addComponent(jLabel2)
											.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
											.addComponent(jLabel5)
											.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
											.addComponent(jLabel6)
											.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
											.addComponent(jLabel7))
										.addComponent(jScrollPane3, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE))
									.addGroup(pnlOtherLayout.createSequentialGroup()
										.addComponent(radioTweetNone)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(radioTweetUpdated)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(radioTweetCreated)))
								.addContainerGap())
					);
				}

				GroupLayout basicPanelLayout = new GroupLayout(basicPanel);
				basicPanel.setLayout(basicPanelLayout);
				basicPanelLayout.setHorizontalGroup(
					basicPanelLayout.createParallelGroup()
						.addGroup(basicPanelLayout.createSequentialGroup()
							.addComponent(pnlOther, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addContainerGap())
						.addGroup(GroupLayout.Alignment.TRAILING, basicPanelLayout.createSequentialGroup()
							.addGroup(basicPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
								.addComponent(pnlDates, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(pnlTitle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(pnlTags, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addGap(12, 12, 12))
				);
				basicPanelLayout.setVerticalGroup(
					basicPanelLayout.createParallelGroup()
						.addGroup(basicPanelLayout.createSequentialGroup()
							.addComponent(pnlTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(6, 6, 6)
							.addComponent(pnlTags, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(pnlDates, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addComponent(pnlOther, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addContainerGap())
				);
			}
			jTabbedPane1.addTab(bundle.getString("SetEditor.basicPanel.tab.title"), basicPanel);


			//======== advancedPanel ========
			{
				advancedPanel.setVerifyInputWhenFocusTarget(false);

				//======== jPanel4 ========
				{
					jPanel4.setBorder(new TitledBorder(bundle.getString("SetEditor.jPanel4.border")));

					//---- cmbPrivacy ----
					cmbPrivacy.setModel(new DefaultComboBoxModel<>(new String[] {
						"No Restriction",
						"Public",
						"Friends",
						"Family",
						"Friends & Family",
						"Private"
					}));
					cmbPrivacy.setToolTipText(bundle.getString("SetEditor.cmbPrivacy.toolTipText"));

					//---- jLabel8 ----
					jLabel8.setText(bundle.getString("SetEditor.jLabel8.text"));

					//---- lblSafeSearch ----
					lblSafeSearch.setText(bundle.getString("SetEditor.lblSafeSearch.text"));

					//---- cmbSafeSearch ----
					cmbSafeSearch.setModel(new DefaultComboBoxModel<>(new String[] {
						"Safe",
						"Moderate",
						"Restricted"
					}));
					cmbSafeSearch.setToolTipText(bundle.getString("SetEditor.cmbSafeSearch.toolTipText"));

					GroupLayout jPanel4Layout = new GroupLayout(jPanel4);
					jPanel4.setLayout(jPanel4Layout);
					jPanel4Layout.setHorizontalGroup(
						jPanel4Layout.createParallelGroup()
							.addGroup(jPanel4Layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
									.addComponent(lblSafeSearch)
									.addComponent(jLabel8))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
									.addComponent(cmbSafeSearch)
									.addComponent(cmbPrivacy, GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE))
								.addContainerGap(357, Short.MAX_VALUE))
					);
					jPanel4Layout.setVerticalGroup(
						jPanel4Layout.createParallelGroup()
							.addGroup(jPanel4Layout.createSequentialGroup()
								.addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(cmbPrivacy, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabel8))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(lblSafeSearch)
									.addComponent(cmbSafeSearch, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					);
				}

				//======== jPanel5 ========
				{
					jPanel5.setBorder(new TitledBorder(bundle.getString("SetEditor.jPanel5.border")));

					//---- jLabel10 ----
					jLabel10.setText(bundle.getString("SetEditor.jLabel10.text"));

					//---- cmbContentType ----
					cmbContentType.setModel(new DefaultComboBoxModel<>(new String[] {
						"Photos Only",
						"Screenshots Only",
						"Other Only",
						"Photos & Screenshots",
						"Screenshots & Other",
						"Photos & Other",
						"All"
					}));
					cmbContentType.setToolTipText(bundle.getString("SetEditor.cmbContentType.toolTipText"));

					//---- jLabel11 ----
					jLabel11.setText(bundle.getString("SetEditor.jLabel11.text"));

					//---- cmbMediaType ----
					cmbMediaType.setModel(new DefaultComboBoxModel<>(new String[] {
						"All",
						"Photos",
						"Video"
					}));
					cmbMediaType.setToolTipText(bundle.getString("SetEditor.cmbMediaType.toolTipText"));

					GroupLayout jPanel5Layout = new GroupLayout(jPanel5);
					jPanel5.setLayout(jPanel5Layout);
					jPanel5Layout.setHorizontalGroup(
						jPanel5Layout.createParallelGroup()
							.addGroup(jPanel5Layout.createSequentialGroup()
								.addGap(44, 44, 44)
								.addGroup(jPanel5Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
									.addComponent(jLabel11)
									.addComponent(jLabel10))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(jPanel5Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
									.addComponent(cmbMediaType)
									.addComponent(cmbContentType))
								.addContainerGap(372, Short.MAX_VALUE))
					);
					jPanel5Layout.setVerticalGroup(
						jPanel5Layout.createParallelGroup()
							.addGroup(jPanel5Layout.createSequentialGroup()
								.addGroup(jPanel5Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(jLabel10)
									.addComponent(cmbContentType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(jPanel5Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(jLabel11)
									.addComponent(cmbMediaType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					);
				}

				//======== jPanel6 ========
				{
					jPanel6.setBorder(new TitledBorder(bundle.getString("SetEditor.jPanel6.border")));

					//---- jLabel12 ----
					jLabel12.setText(bundle.getString("SetEditor.jLabel12.text"));

					//---- cmbGeotag ----
					cmbGeotag.setModel(new DefaultComboBoxModel<>(new String[] {
						"Ignore geotag data",
						"Include only photos with geotag data",
						"Include only photos without geotag data"
					}));
					cmbGeotag.setToolTipText(bundle.getString("SetEditor.cmbGeotag.toolTipText"));

					//---- cbxInGallery ----
					cbxInGallery.setText(bundle.getString("SetEditor.cbxInGallery.text"));
					cbxInGallery.setToolTipText(bundle.getString("SetEditor.cbxInGallery.toolTipText"));

					//---- cbxInCommons ----
					cbxInCommons.setText(bundle.getString("SetEditor.cbxInCommons.text"));
					cbxInCommons.setToolTipText(bundle.getString("SetEditor.cbxInCommons.toolTipText"));

					//---- cbxInGetty ----
					cbxInGetty.setText(bundle.getString("SetEditor.cbxInGetty.text"));
					cbxInGetty.setToolTipText(bundle.getString("SetEditor.cbxInGetty.toolTipText"));

					//---- cbxLimitSize ----
					cbxLimitSize.setText(bundle.getString("SetEditor.cbxLimitSize.text"));
					cbxLimitSize.setToolTipText(bundle.getString("SetEditor.cbxLimitSize.toolTipText"));
					cbxLimitSize.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							cbxLimitSizeActionPerformed(e);
						}
					});

					//---- txtSetSize ----
					txtSetSize.setToolTipText(bundle.getString("SetEditor.txtSetSize.toolTipText"));

					GroupLayout jPanel6Layout = new GroupLayout(jPanel6);
					jPanel6.setLayout(jPanel6Layout);
					jPanel6Layout.setHorizontalGroup(
						jPanel6Layout.createParallelGroup()
							.addGroup(jPanel6Layout.createSequentialGroup()
								.addGap(53, 53, 53)
								.addComponent(jLabel12)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel6Layout.createParallelGroup()
									.addGroup(jPanel6Layout.createSequentialGroup()
										.addComponent(cbxLimitSize)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(txtSetSize, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE))
									.addComponent(cbxInCommons)
									.addComponent(cbxInGallery)
									.addComponent(cmbGeotag, GroupLayout.PREFERRED_SIZE, 353, GroupLayout.PREFERRED_SIZE)
									.addComponent(cbxInGetty))
								.addContainerGap(117, Short.MAX_VALUE))
					);
					jPanel6Layout.setVerticalGroup(
						jPanel6Layout.createParallelGroup()
							.addGroup(jPanel6Layout.createSequentialGroup()
								.addGroup(jPanel6Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(cmbGeotag, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabel12))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(cbxInGallery)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(cbxInCommons)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(cbxInGetty)
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel6Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(cbxLimitSize)
									.addComponent(txtSetSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(20, 20, 20))
					);
				}

				GroupLayout advancedPanelLayout = new GroupLayout(advancedPanel);
				advancedPanel.setLayout(advancedPanelLayout);
				advancedPanelLayout.setHorizontalGroup(
					advancedPanelLayout.createParallelGroup()
						.addComponent(jPanel4, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(jPanel5, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(jPanel6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				);
				advancedPanelLayout.setVerticalGroup(
					advancedPanelLayout.createParallelGroup()
						.addGroup(advancedPanelLayout.createSequentialGroup()
							.addComponent(jPanel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
							.addComponent(jPanel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
							.addComponent(jPanel6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addContainerGap(303, Short.MAX_VALUE))
				);
			}
			jTabbedPane1.addTab(bundle.getString("SetEditor.advancedPanel.tab.title"), advancedPanel);

		}
		contentPane.add(jTabbedPane1, BorderLayout.CENTER);

		//======== buttonPanel ========
		{
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

			//---- btnSave ----
			btnSave.setText(bundle.getString("SetEditor.btnSave.text"));
			btnSave.setToolTipText(bundle.getString("SetEditor.btnSave.toolTipText"));
			btnSave.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					btnSaveActionPerformed(e);
				}
			});
			buttonPanel.add(btnSave);

			//---- btnCancel ----
			btnCancel.setText(bundle.getString("SetEditor.btnCancel.text"));
			btnCancel.setToolTipText(bundle.getString("SetEditor.btnCancel.toolTipText"));
			btnCancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					btnCancelActionPerformed(e);
				}
			});
			buttonPanel.add(btnCancel);

			//---- btnSaveAndRefresh ----
			btnSaveAndRefresh.setText(bundle.getString("SetEditor.btnSaveAndRefresh.text"));
			btnSaveAndRefresh.setToolTipText(bundle.getString("SetEditor.btnSaveAndRefresh.toolTipText"));
			btnSaveAndRefresh.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					btnSaveAndRefreshActionPerformed(e);
				}
			});
			buttonPanel.add(btnSaveAndRefresh);
		}
		contentPane.add(buttonPanel, BorderLayout.PAGE_END);
		setSize(795, 819);
		setLocationRelativeTo(null);

		//---- btnGrpTweet ----
		ButtonGroup btnGrpTweet = new ButtonGroup();
		btnGrpTweet.add(radioTweetNone);
		btnGrpTweet.add(radioTweetUpdated);
		btnGrpTweet.add(radioTweetCreated);
	}// </editor-fold>//GEN-END:initComponents


	/**
	 * Responds to clicks on the "date taken" checkbox.
	 * <p>The text input boxes for date taken min/max should be enabled and disabled
	 * as the user checks and unchecks the box.</p>
	 *
	 * @param evt the event
	 */
	private void cbxDateTakenActionPerformed(java.awt.event.ActionEvent evt) {
		if (this.cbxDateTaken.isSelected()) {
			this.cbxOnThisDay.setSelected(false);
		}
		this.setEnableStates();
	}


	/**
	 * Responds to clicks on the "date uploaded" checkbox.
	 * <p>The text input boxes for date uploaded min/max should be enabled and disabled
	 * as the user checks and unchecks the box.</p>
	 *
	 * @param evt the event
	 */
	private void cbxDateUploadedActionPerformed(java.awt.event.ActionEvent evt) {
		if (this.cbxDateUploaded.isSelected()) {
			this.cbxOnThisDay.setSelected(false);
		}
		this.setEnableStates();
	}


	/**
	 * Respond to clicks on the cancel button.
	 * <p/>
	 * <p>The window will be closed.</p>
	 *
	 * @param evt
	 */
	private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
		// make sure any icon changes are undone
		this.lblIcon.setIcon(this.originalPrimaryIcon);
		this.ssPhotoset.setPrimary(this.originalPrimaryId);
		this.ssPhotoset.setPrimaryPhotoIcon(this.originalPrimaryIcon);
		this.cbxLock.setSelected(this.originalLockSelected);
		this.ssPhotoset.setLockPrimaryPhoto(this.originalLockSelected);

		this.dispose();
	}


	/**
	 * Respond to clicks on the save button.
	 * <p>Perform validation on the user entries. If everything is OK, save
	 * the record. Otherwise, tell the user what is wrong and let them fix it.</p>
	 *
	 * @param evt
	 */
	private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {
		if (this.doValidation()) {
			try {
				PhotosetDAO.updatePhotoset(this.ssPhotoset);
				SimpleCache.getInstance().invalidate(this.ssPhotoset.getId());
				this.setVisible(false);
				this.dispose();
			} catch (Exception e) {
				logger.error("Error saving set parameters to database.", e);
				JOptionPane.showMessageDialog(this,
						resourceBundle.getString("SetEditor.saveError.message") + e.getMessage(),
						resourceBundle.getString("SetEditor.saveError.title"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}


	/**
	 * Verify entry of title text.
	 * <p>If the user tries to enter a title that is already taken by another
	 * set, make the text red.</p>
	 *
	 * @param evt
	 */
	private void txtTitleKeyReleased(java.awt.event.KeyEvent evt) {
		if (editorMode == EditorMode.CREATE) {
			if (MainWindow.getMainWindow().setTitleExists(this.txtTitle.getText())) {
				this.btnSave.setEnabled(false);
				this.txtTitle.setForeground(Color.red);
			} else {
				this.btnSave.setEnabled(true);
				this.txtTitle.setForeground(Color.black);
			}
		}
	}


	/**
	 * Respond to clicks on the "Manage" checkbox.
	 * <p/>
	 * <p>This calls a method that enables and disables components as needed.</p>
	 *
	 * @param evt
	 */
	private void cbxManageActionPerformed(java.awt.event.ActionEvent evt) {
		this.addManagedByTextToDescription();
		this.setEnableStates();
	}


	/**
	 * Respond to clicks on the "Tweet" checkbox.
	 * <p/>
	 * <p>If the user selects the checkbox, verify that they are authorized to
	 * use Twitter. If not, offer to take them to the Preferences where they
	 * can authorize.</p>
	 *
	 * @param evt
	 */
	private void btnSaveAndRefreshActionPerformed(java.awt.event.ActionEvent evt) {
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
	}

	private void lblIconMouseClicked(java.awt.event.MouseEvent evt) {
		if (evt.getClickCount() == 2) {
			if (this.editorMode == EditorMode.EDIT) {
				//JOptionPane.showMessageDialog(this, "Edit primary photo", "edit", JOptionPane.INFORMATION_MESSAGE);

				try {
					PhotoPickerDialog picker = new PhotoPickerDialog(this, true, this.ssPhotoset);
					picker.setVisible(true);
				} catch (Exception e) {
					logger.error("Could not display photo picker dialog.", e);
					JOptionPane.showMessageDialog(this,
							resourceBundle.getString("SetEditor.pickerError.message"),
							resourceBundle.getString("SetEditor.saveError.title"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private void radioTweetCreatedOrUpdatedActionPerformed(java.awt.event.ActionEvent evt) {
		this.txtTweet.setEnabled(true);
		this.txtTweet.setText(SSConstants.DEFAULT_TWEET_TEMPLATE);
		if (!TwitterHelper.isAuthorized()) {
			int confirm = JOptionPane.showConfirmDialog(this,
					resourceBundle.getString("SetEditor.authTwitter.message"),
					resourceBundle.getString("SetEditor.authTwitter.title"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);

			if (confirm == JOptionPane.YES_OPTION) {
				Preferences prefs = new Preferences(MainWindow.getMainWindow(), true);
				prefs.setTabIndex(Preferences.TWITTER_PANEL);
				prefs.setVisible(true);
			}
		}
	}


	private void radioTweetNoneActionPerformed(java.awt.event.ActionEvent evt) {
		this.txtTweet.setEnabled(false);
	}

	private void cbxLimitSizeActionPerformed(java.awt.event.ActionEvent evt) {
		this.txtSetSize.setEnabled(cbxLimitSize.isSelected());
	}

	private void cbxOnThisDayActionPerformed(java.awt.event.ActionEvent evt) {
		if (this.cbxOnThisDay.isSelected()) {
			this.cbxDateTaken.setSelected(false);
			this.cbxDateUploaded.setSelected(false);
			int sort = this.cmbSortBy.getSelectedIndex();
			if (sort != 2 && sort != 3 && sort != 6) {
				this.cmbSortBy.setSelectedIndex(3);    // sort ascending by default
			}
		}
		this.setEnableStates();
	}

	private void cmbOTDMonthActionPerformed(java.awt.event.ActionEvent evt) {
		this.updateDayOfMonthComboBox();
	}

	private void cbxCurrentYearActionPerformed(java.awt.event.ActionEvent evt) {
		if (this.cbxCurrentYear.isSelected()) {
			this.txtOTDYearEnd.setEnabled(false);
			this.txtOTDYearEnd.setText(Integer.toString(SSUtils.getCurrentYear()));
		} else {
			this.txtOTDYearEnd.setEnabled(true);
		}
	}


	private void cmbSortByActionPerformed(java.awt.event.ActionEvent evt) {
		// check for sort mode used when an On This Day set is created.
		// warn the user about sort mode selection
		if (this.cbxOnThisDay.isSelected()) {
			switch (this.cmbSortBy.getSelectedIndex()) {
				case 2:                    // Date Taken Descending
				case 3:                    // Date Taken Ascending
				case 6:                    // No Particular Order
					// These sorting modes are handled correctly.
					break;

				default:
					JOptionPane.showMessageDialog(this,
							resourceBundle.getString("SetEditor.sortOrder.message"),
							resourceBundle.getString("SetEditor.sortOrder.title"),
							JOptionPane.WARNING_MESSAGE);
					break;
			}
		}
	}


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
			sb.append(resourceBundle.getString("SetEditor.validation.title"));
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
				sb.append(resourceBundle.getString("SetEditor.validation.dateTaken"));
				ok = false;
			} else if (takenMax.before(takenMin)) {
				sb.append(resourceBundle.getString("SetEditor.validation.dateTakenValues"));
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
				sb.append(resourceBundle.getString("SetEditor.validation.dateUploaded"));
				ok = false;
			} else if (uploadedMax.before(uploadedMin)) {
				sb.append(resourceBundle.getString("SetEditor.validation.dateUploadedValues"));
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
				sb.append(resourceBundle.getString("SetEditor.validation.setSize"));
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
				sb.append(resourceBundle.getString("SetEditor.validation.onThisDayStartYear"));
				ok = false;
			}
			try {
				if (this.cbxCurrentYear.isSelected()) {
					this.ssPhotoset.setOnThisDayYearEnd(0);
					yearEnd = SSUtils.getCurrentYear();    // to make the start > end check valid
				} else {
					yearEnd = Integer.parseInt(this.txtOTDYearEnd.getText());
					if (yearEnd > SSUtils.getCurrentYear()) {
						throw new Exception();
					}
					this.ssPhotoset.setOnThisDayYearEnd(yearEnd);
				}
			} catch (Exception e) {
				sb.append(resourceBundle.getString("SetEditor.validation.onThisDayEndYear"));
				ok = false;
			}

			if (yearStart > yearEnd) {
				sb.append(resourceBundle.getString("SetEditor.validation.onThisDayValues"));
				ok = false;
			}
		}


		if (ok) {
			// do we have to change the title/description on Flickr?
			String t = this.txtTitle.getText().trim();
			String d = this.txtDescription.getText().trim();

			if (
					(!t.equals(this.ssPhotoset.getTitle()) ||
							!d.equals(this.ssPhotoset.getDescription())) &&
							this.editorMode == EditorMode.EDIT) {
				try {
					PhotosetsApi.getInstance().editMeta(this.ssPhotoset.getId(), t, d);
					LogWindow.addLogMessage(resourceBundle.getString("SetEditor.validation.titleUpdated") +
							this.ssPhotoset.getTitle() + "'.");
				} catch (Exception e) {
					logger.error("Error setting metadata.", e);
					JOptionPane.showMessageDialog(this,
							resourceBundle.getString("SetEditor.validation.titleUpdateError.message"),
							resourceBundle.getString("SetEditor.validation.titleUpdateError.title"),
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
					resourceBundle.getString("SetEditor.validation.error.message") + sb.toString(),
					resourceBundle.getString("SetEditor.validation.error.title"),
					JOptionPane.ERROR_MESSAGE);
		}

		return ok;
	}


	/**
	 * Enable and disable components as needed.
	 * <p/>
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
		this.txtOTDYearEnd.setEnabled(this.cbxManage.isSelected() && this.cbxOnThisDay.isSelected() && (!this.cbxCurrentYear.isSelected()));
		this.txtOTDYearStart.setEnabled(this.cbxManage.isSelected() && this.cbxOnThisDay.isSelected());
		this.cbxCurrentYear.setEnabled(this.cbxManage.isSelected() && this.cbxOnThisDay.isSelected());
	}


	/**
	 * Set the message label text.
	 * <p/>
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
	 * <p/>
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
	private JTabbedPane jTabbedPane1;
	private JPanel basicPanel;
	private JPanel pnlTitle;
	private JLabel lblIcon;
	private JCheckBox cbxManage;
	private JCheckBox cbxLock;
	private JTextField txtTitle;
	private JScrollPane jScrollPane1;
	private JTextArea txtDescription;
	private JLabel lblMessage;
	private JPanel pnlTags;
	private JTextField txtTags;
	private JComboBox<String> cmbTags;
	private JPanel pnlDates;
	private JCheckBox cbxDateTaken;
	private JTextField txtDateTakenAfter;
	private JLabel jLabel3;
	private JTextField txtDateTakenBefore;
	private JCheckBox cbxDateUploaded;
	private JTextField txtDateUploadedAfter;
	private JLabel jLabel4;
	private JTextField txtDateUploadedBefore;
	private JCheckBox cbxOnThisDay;
	private JComboBox cmbOTDMonth;
	private JComboBox<String> cmbOTDDay;
	private JLabel jLabel9;
	private JTextField txtOTDYearStart;
	private JLabel jLabel13;
	private JTextField txtOTDYearEnd;
	private JCheckBox cbxCurrentYear;
	private JPanel pnlOther;
	private JLabel jLabel1;
	private JComboBox cmbSortBy;
	private JScrollPane jScrollPane3;
	private JEditorPane txtTweet;
	private JLabel jLabel2;
	private JLabel jLabel5;
	private JLabel jLabel6;
	private JLabel jLabel7;
	private JRadioButton radioTweetNone;
	private JRadioButton radioTweetUpdated;
	private JRadioButton radioTweetCreated;
	private JPanel advancedPanel;
	private JPanel jPanel4;
	private JComboBox<String> cmbPrivacy;
	private JLabel jLabel8;
	private JLabel lblSafeSearch;
	private JComboBox<String> cmbSafeSearch;
	private JPanel jPanel5;
	private JLabel jLabel10;
	private JComboBox<String> cmbContentType;
	private JLabel jLabel11;
	private JComboBox<String> cmbMediaType;
	private JPanel jPanel6;
	private JLabel jLabel12;
	private JComboBox<String> cmbGeotag;
	private JCheckBox cbxInGallery;
	private JCheckBox cbxInCommons;
	private JCheckBox cbxInGetty;
	private JCheckBox cbxLimitSize;
	private JTextField txtSetSize;
	private JPanel buttonPanel;
	private JButton btnSave;
	private JButton btnCancel;
	private JButton btnSaveAndRefresh;
	// End of variables declaration//GEN-END:variables


	/**
	 * Document that is limited to a specific number of characters.
	 * <p/>
	 * <p>This document is used by the Tweet Profile text entry field to limit
	 * user entry to 140 characters.</p>
	 */
	class LimitedPlainDocument extends PlainDocument {

		/**
		 * Maximum length of the text field.
		 */
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

			// 31 days in any other month
			default:
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
