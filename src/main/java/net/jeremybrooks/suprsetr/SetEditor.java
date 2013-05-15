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

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JYearChooser;
import net.jeremybrooks.jinx.api.PhotosetsApi;
import net.jeremybrooks.suprsetr.dao.DAOHelper;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import net.jeremybrooks.suprsetr.dao.PhotosetDAO;
import net.jeremybrooks.suprsetr.twitter.TwitterHelper;
import net.jeremybrooks.suprsetr.utils.SSUtils;
import net.jeremybrooks.suprsetr.utils.SimpleCache;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

	private String[] sortModelArray = new String[10];
	private String[] privacyModelArray = new String[6];
	private String[] safeModelArray = new String[3];
	private String[] contentModelArray = new String[7];
	private String[] typeModelArray = new String[3];
	private String[] geotagModelArray = new String[3];

	private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.seteditor");

	// Cancelable properties.
	// These can be changed from the PhotoPicker, so we need to remember this
	// state when the window is first displayed in order to revert any changes
	// if the user clicks Cancel
	private ImageIcon originalPrimaryIcon;
	private String originalPrimaryId;
	private boolean originalLockSelected;


	public SetEditor(JFrame parent, EditorMode editorMode, SSPhotoset ssPhotoset) {
		super(parent, true);

		this.ssPhotoset = ssPhotoset;
		this.editorMode = editorMode;
		for (int i = 0; i < 10; i++) {
			sortModelArray[i] = resourceBundle.getString("SetEditor.sortModel." + i);
		}
		for (int i = 0; i < 6; i++) {
			privacyModelArray[i] = resourceBundle.getString("SetEditor.privacyModel." + i);
		}
		for (int i = 0; i < 3; i++) {
			safeModelArray[i] = resourceBundle.getString("SetEditor.safeModel." + i);
		}
		for (int i = 0; i < 7; i++) {
			contentModelArray[i] = resourceBundle.getString("SetEditor.contentModel." + i);
		}
		for (int i = 0; i < 3; i++) {
			typeModelArray[i] = resourceBundle.getString("SetEditor.typeModel." + i);
		}
		for (int i = 0; i < 3; i++) {
			geotagModelArray[i] = resourceBundle.getString("SetEditor.geotagModel." + i);
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
		this.dateTakenAfter.setDate(ssPhotoset.getMinTakenDate());
		this.dateTakenBefore.setDate(ssPhotoset.getMaxTakenDate());
		this.dateTakenBefore.setMinSelectableDate(this.dateTakenAfter.getDate());

		this.cbxDateUploaded.setSelected(ssPhotoset.isMatchUploadDates());
		this.dateUploadedAfter.setDate(ssPhotoset.getMinUploadDate());
		this.dateUploadedBefore.setDate(ssPhotoset.getMaxUploadDate());
		this.dateUploadedBefore.setMinSelectableDate(this.dateUploadedAfter.getDate());

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
		this.yearOTDStart.setYear(ssPhotoset.getOnThisDayYearStart());
		this.yearOTDStart.setMaximum(SSUtils.getCurrentYear());
		this.yearOTDEnd.setMaximum(SSUtils.getCurrentYear());
		if (ssPhotoset.getOnThisDayYearEnd() == 0) {
			this.cbxCurrentYear.setSelected(true);
			this.yearOTDEnd.setYear(SSUtils.getCurrentYear());
		} else {
			this.yearOTDEnd.setYear(ssPhotoset.getOnThisDayYearEnd());
		}
		this.yearOTDEnd.setMinimum(this.yearOTDStart.getYear());

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
		lblMessage = new JLabel();
		cbxManage = new JCheckBox();
		cbxLock = new JCheckBox();
		label1 = new JLabel();
		txtTitle = new JTextField();
		label2 = new JLabel();
		jScrollPane1 = new JScrollPane();
		txtDescription = new JTextArea();
		pnlTags = new JPanel();
		txtTags = new JTextField();
		cmbTags = new JComboBox<>();
		pnlDates = new JPanel();
		panel1 = new JPanel();
		cbxDateTaken = new JCheckBox();
		dateTakenAfter = new JDateChooser();
		jLabel3 = new JLabel();
		dateTakenBefore = new JDateChooser();
		cbxDateUploaded = new JCheckBox();
		dateUploadedAfter = new JDateChooser();
		jLabel4 = new JLabel();
		dateUploadedBefore = new JDateChooser();
		panel2 = new JPanel();
		cbxOnThisDay = new JCheckBox();
		cmbOTDMonth = new JComboBox();
		cmbOTDDay = new JComboBox<>();
		jLabel9 = new JLabel();
		yearOTDStart = new JYearChooser();
		jLabel13 = new JLabel();
		yearOTDEnd = new JYearChooser();
		cbxCurrentYear = new JCheckBox();
		pnlOther = new JPanel();
		jLabel1 = new JLabel();
		cmbSortBy = new JComboBox();
		jScrollPane3 = new JScrollPane();
		txtTweet = new JTextArea();
		jLabel2 = new JLabel();
		jLabel5 = new JLabel();
		jLabel6 = new JLabel();
		jLabel7 = new JLabel();
		radioTweetNone = new JRadioButton();
		radioTweetUpdated = new JRadioButton();
		radioTweetCreated = new JRadioButton();
		advancedPanel = new JPanel();
		jPanel4 = new JPanel();
		jLabel8 = new JLabel();
		cmbPrivacy = new JComboBox();
		lblSafeSearch = new JLabel();
		cmbSafeSearch = new JComboBox();
		jPanel5 = new JPanel();
		jLabel10 = new JLabel();
		cmbContentType = new JComboBox();
		jLabel11 = new JLabel();
		cmbMediaType = new JComboBox();
		jPanel6 = new JPanel();
		jLabel12 = new JLabel();
		cmbGeotag = new JComboBox();
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
				basicPanel.setLayout(new VerticalLayout(5));

				//======== pnlTitle ========
				{
					pnlTitle.setBorder(new TitledBorder(bundle.getString("SetEditor.pnlTitle.border")));
					pnlTitle.setLayout(new GridBagLayout());
					((GridBagLayout)pnlTitle.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
					((GridBagLayout)pnlTitle.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
					((GridBagLayout)pnlTitle.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)pnlTitle.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

					//---- lblIcon ----
					lblIcon.setIcon(new ImageIcon(getClass().getResource("/images/empty_set_icon.png")));
					lblIcon.setToolTipText(bundle.getString("SetEditor.lblIcon.toolTipText"));
					lblIcon.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							lblIconMouseClicked(e);
						}
					});
					pnlTitle.add(lblIcon, new GridBagConstraints(0, 0, 1, 3, 0.0, 0.0,
						GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- lblMessage ----
					lblMessage.setFont(new Font("Lucida Grande", Font.BOLD, 13));
					lblMessage.setForeground(Color.red);
					pnlTitle.add(lblMessage, new GridBagConstraints(1, 0, 2, 1, 0.0, 1.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- cbxManage ----
					cbxManage.setText(bundle.getString("SetEditor.cbxManage.text"));
					cbxManage.setToolTipText(bundle.getString("SetEditor.cbxManage.toolTipText"));
					cbxManage.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							cbxManageActionPerformed();
						}
					});
					pnlTitle.add(cbxManage, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0,
						GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- cbxLock ----
					cbxLock.setText(bundle.getString("SetEditor.cbxLock.text"));
					cbxLock.setToolTipText(bundle.getString("SetEditor.cbxLock.toolTipText"));
					pnlTitle.add(cbxLock, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label1 ----
					label1.setText(bundle.getString("SetEditor.label1.text"));
					pnlTitle.add(label1, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- txtTitle ----
					txtTitle.setToolTipText(bundle.getString("SetEditor.txtTitle.toolTipText"));
					txtTitle.addKeyListener(new KeyAdapter() {
						@Override
						public void keyReleased(KeyEvent e) {
							txtTitleKeyReleased();
						}
					});
					pnlTitle.add(txtTitle, new GridBagConstraints(2, 3, 1, 1, 1.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label2 ----
					label2.setText(bundle.getString("SetEditor.label2.text"));
					pnlTitle.add(label2, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0,
						GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 5), 0, 0));

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
					pnlTitle.add(jScrollPane1, new GridBagConstraints(2, 4, 1, 1, 1.0, 1.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 3, 0, 8), 0, 0));
				}
				basicPanel.add(pnlTitle);

				//======== pnlTags ========
				{
					pnlTags.setBorder(new TitledBorder(bundle.getString("SetEditor.pnlTags.border")));
					pnlTags.setLayout(new GridBagLayout());
					((GridBagLayout)pnlTags.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)pnlTags.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)pnlTags.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)pnlTags.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- txtTags ----
					txtTags.setToolTipText(bundle.getString("SetEditor.txtTags.toolTipText"));
					pnlTags.add(txtTags, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));

					//---- cmbTags ----
					cmbTags.setModel(new DefaultComboBoxModel<>(new String[] {
						"All",
						"Any"
					}));
					cmbTags.setToolTipText(bundle.getString("SetEditor.cmbTags.toolTipText"));
					pnlTags.add(cmbTags, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				basicPanel.add(pnlTags);

				//======== pnlDates ========
				{
					pnlDates.setBorder(new TitledBorder(bundle.getString("SetEditor.pnlDates.border")));
					pnlDates.setLayout(new VerticalLayout());

					//======== panel1 ========
					{
						panel1.setLayout(new GridBagLayout());
						((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
						((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
						((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
						((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

						//---- cbxDateTaken ----
						cbxDateTaken.setText(bundle.getString("SetEditor.cbxDateTaken.text"));
						cbxDateTaken.setToolTipText(bundle.getString("SetEditor.cbxDateTaken.toolTipText"));
						cbxDateTaken.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								cbxDateTakenActionPerformed();
							}
						});
						panel1.add(cbxDateTaken, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- dateTakenAfter ----
						dateTakenAfter.setToolTipText(bundle.getString("SetEditor.dateTakenAfter.toolTipText"));
						dateTakenAfter.addPropertyChangeListener(new PropertyChangeListener() {
							@Override
							public void propertyChange(PropertyChangeEvent e) {
								dateTakenAfterPropertyChange();
							}
						});
						panel1.add(dateTakenAfter, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- jLabel3 ----
						jLabel3.setText(bundle.getString("SetEditor.jLabel3.text"));
						panel1.add(jLabel3, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- dateTakenBefore ----
						dateTakenBefore.setToolTipText(bundle.getString("SetEditor.dateTakenBefore.toolTipText"));
						panel1.add(dateTakenBefore, new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//---- cbxDateUploaded ----
						cbxDateUploaded.setText(bundle.getString("SetEditor.cbxDateUploaded.text"));
						cbxDateUploaded.setToolTipText(bundle.getString("SetEditor.cbxDateUploaded.toolTipText"));
						cbxDateUploaded.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								cbxDateUploadedActionPerformed();
							}
						});
						panel1.add(cbxDateUploaded, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- dateUploadedAfter ----
						dateUploadedAfter.setToolTipText(bundle.getString("SetEditor.dateUploadedAfter.toolTipText"));
						dateUploadedAfter.addPropertyChangeListener(new PropertyChangeListener() {
							@Override
							public void propertyChange(PropertyChangeEvent e) {
								dateUploadedAfterPropertyChange();
							}
						});
						panel1.add(dateUploadedAfter, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- jLabel4 ----
						jLabel4.setText(bundle.getString("SetEditor.jLabel4.text"));
						panel1.add(jLabel4, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- dateUploadedBefore ----
						dateUploadedBefore.setToolTipText(bundle.getString("SetEditor.dateUploadedBefore.toolTipText"));
						panel1.add(dateUploadedBefore, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));
					}
					pnlDates.add(panel1);

					//======== panel2 ========
					{
						panel2.setLayout(new GridBagLayout());
						((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
						((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0};
						((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
						((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

						//---- cbxOnThisDay ----
						cbxOnThisDay.setText(bundle.getString("SetEditor.cbxOnThisDay.text"));
						cbxOnThisDay.setToolTipText(bundle.getString("SetEditor.cbxOnThisDay.toolTipText"));
						cbxOnThisDay.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								cbxOnThisDayActionPerformed();
							}
						});
						panel2.add(cbxOnThisDay, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- cmbOTDMonth ----
						cmbOTDMonth.setToolTipText(bundle.getString("SetEditor.cmbOTDMonth.toolTipText"));
						cmbOTDMonth.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								cmbOTDMonthActionPerformed();
							}
						});
						cmbOTDMonth.setModel(new DefaultComboBoxModel(DateFormatSymbols.getInstance().getMonths()));
						panel2.add(cmbOTDMonth, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

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
						panel2.add(cmbOTDDay, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- jLabel9 ----
						jLabel9.setText(bundle.getString("SetEditor.jLabel9.text"));
						panel2.add(jLabel9, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- yearOTDStart ----
						yearOTDStart.setToolTipText(bundle.getString("SetEditor.yearOTDStart.toolTipText"));
						yearOTDStart.setMinimum(1900);
						yearOTDStart.setStartYear(1900);
						yearOTDStart.addPropertyChangeListener(new PropertyChangeListener() {
							@Override
							public void propertyChange(PropertyChangeEvent e) {
								yearFromPropertyChange();
							}
						});
						panel2.add(yearOTDStart, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- jLabel13 ----
						jLabel13.setText(bundle.getString("SetEditor.jLabel13.text"));
						panel2.add(jLabel13, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- yearOTDEnd ----
						yearOTDEnd.setToolTipText(bundle.getString("SetEditor.yearOTDEnd.toolTipText"));
						yearOTDEnd.setMinimum(1900);
						panel2.add(yearOTDEnd, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
							new Insets(0, 0, 5, 5), 0, 0));

						//---- cbxCurrentYear ----
						cbxCurrentYear.setText(bundle.getString("SetEditor.cbxCurrentYear.text"));
						cbxCurrentYear.setToolTipText(bundle.getString("SetEditor.cbxCurrentYear.toolTipText"));
						cbxCurrentYear.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								cbxCurrentYearActionPerformed();
							}
						});
						panel2.add(cbxCurrentYear, new GridBagConstraints(6, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
							new Insets(0, 0, 0, 5), 0, 0));
					}
					pnlDates.add(panel2);
				}
				basicPanel.add(pnlDates);

				//======== pnlOther ========
				{
					pnlOther.setBorder(new TitledBorder(bundle.getString("SetEditor.pnlOther.border")));
					pnlOther.setLayout(new GridBagLayout());
					((GridBagLayout)pnlOther.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
					((GridBagLayout)pnlOther.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
					((GridBagLayout)pnlOther.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)pnlOther.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

					//---- jLabel1 ----
					jLabel1.setText(bundle.getString("SetEditor.jLabel1.text"));
					pnlOther.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- cmbSortBy ----
					cmbSortBy.setToolTipText(bundle.getString("SetEditor.cmbSortBy.toolTipText"));
					cmbSortBy.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							cmbSortByActionPerformed();
						}
					});
					cmbSortBy.setModel(new DefaultComboBoxModel<>(this.sortModelArray));
					pnlOther.add(cmbSortBy, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 5), 0, 0));

					//======== jScrollPane3 ========
					{

						//---- txtTweet ----
						txtTweet.setToolTipText(bundle.getString("SetEditor.txtTweet.toolTipText"));
						txtTweet.setLineWrap(true);
						txtTweet.setWrapStyleWord(true);
						Document styledDoc = txtTweet.getDocument();
						if (styledDoc instanceof AbstractDocument) {
						    AbstractDocument doc = (AbstractDocument)styledDoc;
						    doc.setDocumentFilter(new DocumentSizeFilter(140));
						}
						jScrollPane3.setViewportView(txtTweet);
					}
					pnlOther.add(jScrollPane3, new GridBagConstraints(1, 1, 1, 4, 1.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- jLabel2 ----
					jLabel2.setText(bundle.getString("SetEditor.jLabel2.text"));
					pnlOther.add(jLabel2, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- jLabel5 ----
					jLabel5.setText(bundle.getString("SetEditor.jLabel5.text"));
					pnlOther.add(jLabel5, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- jLabel6 ----
					jLabel6.setText(bundle.getString("SetEditor.jLabel6.text"));
					pnlOther.add(jLabel6, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- jLabel7 ----
					jLabel7.setText(bundle.getString("SetEditor.jLabel7.text"));
					pnlOther.add(jLabel7, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- radioTweetNone ----
					radioTweetNone.setSelected(true);
					radioTweetNone.setText(bundle.getString("SetEditor.radioTweetNone.text"));
					radioTweetNone.setToolTipText(bundle.getString("SetEditor.radioTweetNone.toolTipText"));
					radioTweetNone.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							radioTweetNoneActionPerformed();
						}
					});
					pnlOther.add(radioTweetNone, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- radioTweetUpdated ----
					radioTweetUpdated.setText(bundle.getString("SetEditor.radioTweetUpdated.text"));
					radioTweetUpdated.setToolTipText(bundle.getString("SetEditor.radioTweetUpdated.toolTipText"));
					radioTweetUpdated.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							radioTweetCreatedOrUpdatedActionPerformed();
						}
					});
					pnlOther.add(radioTweetUpdated, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- radioTweetCreated ----
					radioTweetCreated.setText(bundle.getString("SetEditor.radioTweetCreated.text"));
					radioTweetCreated.setToolTipText(bundle.getString("SetEditor.radioTweetCreated.toolTipText"));
					radioTweetCreated.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							radioTweetCreatedOrUpdatedActionPerformed();
						}
					});
					pnlOther.add(radioTweetCreated, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));
				}
				basicPanel.add(pnlOther);
			}
			jTabbedPane1.addTab(bundle.getString("SetEditor.basicPanel.tab.title"), basicPanel);


			//======== advancedPanel ========
			{
				advancedPanel.setVerifyInputWhenFocusTarget(false);
				advancedPanel.setLayout(new VerticalLayout(5));

				//======== jPanel4 ========
				{
					jPanel4.setBorder(new TitledBorder(bundle.getString("SetEditor.jPanel4.border")));
					jPanel4.setLayout(new GridBagLayout());
					((GridBagLayout)jPanel4.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)jPanel4.getLayout()).rowHeights = new int[] {0, 0, 0};
					((GridBagLayout)jPanel4.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)jPanel4.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

					//---- jLabel8 ----
					jLabel8.setText(bundle.getString("SetEditor.jLabel8.text"));
					jPanel4.add(jLabel8, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- cmbPrivacy ----
					cmbPrivacy.setToolTipText(bundle.getString("SetEditor.cmbPrivacy.toolTipText"));
					cmbPrivacy.setModel(new DefaultComboBoxModel<>(this.privacyModelArray));
					jPanel4.add(cmbPrivacy, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- lblSafeSearch ----
					lblSafeSearch.setText(bundle.getString("SetEditor.lblSafeSearch.text"));
					jPanel4.add(lblSafeSearch, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- cmbSafeSearch ----
					cmbSafeSearch.setToolTipText(bundle.getString("SetEditor.cmbSafeSearch.toolTipText"));
					cmbSafeSearch.setModel(new DefaultComboBoxModel<>(this.safeModelArray));
					jPanel4.add(cmbSafeSearch, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				advancedPanel.add(jPanel4);

				//======== jPanel5 ========
				{
					jPanel5.setBorder(new TitledBorder(bundle.getString("SetEditor.jPanel5.border")));
					jPanel5.setLayout(new GridBagLayout());
					((GridBagLayout)jPanel5.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)jPanel5.getLayout()).rowHeights = new int[] {0, 0, 0};
					((GridBagLayout)jPanel5.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)jPanel5.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

					//---- jLabel10 ----
					jLabel10.setText(bundle.getString("SetEditor.jLabel10.text"));
					jPanel5.add(jLabel10, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- cmbContentType ----
					cmbContentType.setToolTipText(bundle.getString("SetEditor.cmbContentType.toolTipText"));
					cmbContentType.setModel(new DefaultComboBoxModel<>(this.contentModelArray));
					jPanel5.add(cmbContentType, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- jLabel11 ----
					jLabel11.setText(bundle.getString("SetEditor.jLabel11.text"));
					jPanel5.add(jLabel11, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- cmbMediaType ----
					cmbMediaType.setToolTipText(bundle.getString("SetEditor.cmbMediaType.toolTipText"));
					cmbMediaType.setModel(new DefaultComboBoxModel<>(this.typeModelArray));
					jPanel5.add(cmbMediaType, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				advancedPanel.add(jPanel5);

				//======== jPanel6 ========
				{
					jPanel6.setBorder(new TitledBorder(bundle.getString("SetEditor.jPanel6.border")));
					jPanel6.setLayout(new GridBagLayout());
					((GridBagLayout)jPanel6.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
					((GridBagLayout)jPanel6.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0};
					((GridBagLayout)jPanel6.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)jPanel6.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

					//---- jLabel12 ----
					jLabel12.setText(bundle.getString("SetEditor.jLabel12.text"));
					jPanel6.add(jLabel12, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- cmbGeotag ----
					cmbGeotag.setToolTipText(bundle.getString("SetEditor.cmbGeotag.toolTipText"));
					cmbGeotag.setModel(new DefaultComboBoxModel<>(this.geotagModelArray));
					jPanel6.add(cmbGeotag, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- cbxInGallery ----
					cbxInGallery.setText(bundle.getString("SetEditor.cbxInGallery.text"));
					cbxInGallery.setToolTipText(bundle.getString("SetEditor.cbxInGallery.toolTipText"));
					jPanel6.add(cbxInGallery, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- cbxInCommons ----
					cbxInCommons.setText(bundle.getString("SetEditor.cbxInCommons.text"));
					cbxInCommons.setToolTipText(bundle.getString("SetEditor.cbxInCommons.toolTipText"));
					jPanel6.add(cbxInCommons, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- cbxInGetty ----
					cbxInGetty.setText(bundle.getString("SetEditor.cbxInGetty.text"));
					cbxInGetty.setToolTipText(bundle.getString("SetEditor.cbxInGetty.toolTipText"));
					jPanel6.add(cbxInGetty, new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- cbxLimitSize ----
					cbxLimitSize.setText(bundle.getString("SetEditor.cbxLimitSize.text"));
					cbxLimitSize.setToolTipText(bundle.getString("SetEditor.cbxLimitSize.toolTipText"));
					cbxLimitSize.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							cbxLimitSizeActionPerformed();
						}
					});
					jPanel6.add(cbxLimitSize, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- txtSetSize ----
					txtSetSize.setToolTipText(bundle.getString("SetEditor.txtSetSize.toolTipText"));
					jPanel6.add(txtSetSize, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				advancedPanel.add(jPanel6);
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
					btnSaveActionPerformed();
				}
			});
			buttonPanel.add(btnSave);

			//---- btnCancel ----
			btnCancel.setText(bundle.getString("SetEditor.btnCancel.text"));
			btnCancel.setToolTipText(bundle.getString("SetEditor.btnCancel.toolTipText"));
			btnCancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					btnCancelActionPerformed();
				}
			});
			buttonPanel.add(btnCancel);

			//---- btnSaveAndRefresh ----
			btnSaveAndRefresh.setText(bundle.getString("SetEditor.btnSaveAndRefresh.text"));
			btnSaveAndRefresh.setToolTipText(bundle.getString("SetEditor.btnSaveAndRefresh.toolTipText"));
			btnSaveAndRefresh.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					btnSaveAndRefreshActionPerformed();
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


	private void dateTakenAfterPropertyChange() {
		dateTakenBefore.setMinSelectableDate(dateTakenAfter.getDate());
	}

	private void dateUploadedAfterPropertyChange() {
		dateUploadedBefore.setMinSelectableDate(dateTakenAfter.getDate());
	}

	private void yearFromPropertyChange() {
		if (this.yearOTDStart.getYear() < this.yearOTDEnd.getYear()) {
			this.yearOTDStart.setYear(this.yearOTDEnd.getYear());
		}
		this.yearOTDStart.setMinimum(this.yearOTDEnd.getYear());
	}


	/*
	 * Responds to clicks on the "date taken" checkbox.
	 * <p>The text input boxes for date taken min/max should be enabled and disabled
	 * as the user checks and unchecks the box.</p>
	 */
	private void cbxDateTakenActionPerformed() {
		if (this.cbxDateTaken.isSelected()) {
			this.cbxOnThisDay.setSelected(false);
		}
		this.setEnableStates();
	}


	/*
	 * Responds to clicks on the "date uploaded" checkbox.
	 * <p>The text input boxes for date uploaded min/max should be enabled and disabled
	 * as the user checks and unchecks the box.</p>
	 */
	private void cbxDateUploadedActionPerformed() {
		if (this.cbxDateUploaded.isSelected()) {
			this.cbxOnThisDay.setSelected(false);
		}
		this.setEnableStates();
	}


	/*
	 * Respond to clicks on the cancel button.
	 * <p/>
	 * <p>The window will be closed.</p>
	 */
	private void btnCancelActionPerformed() {
		// make sure any icon changes are undone
		this.lblIcon.setIcon(this.originalPrimaryIcon);
		this.ssPhotoset.setPrimary(this.originalPrimaryId);
		this.ssPhotoset.setPrimaryPhotoIcon(this.originalPrimaryIcon);
		this.cbxLock.setSelected(this.originalLockSelected);
		this.ssPhotoset.setLockPrimaryPhoto(this.originalLockSelected);

		this.dispose();
	}


	/*
	 * Respond to clicks on the save button.
	 * <p>Perform validation on the user entries. If everything is OK, save
	 * the record. Otherwise, tell the user what is wrong and let them fix it.</p>
	 */
	private void btnSaveActionPerformed() {
		if (this.doValidation()) {
			try {
				PhotosetDAO.updatePhotoset(this.ssPhotoset);
				SimpleCache.getInstance().invalidate(this.ssPhotoset.getId());
				MainWindow.getMainWindow().setMasterList(PhotosetDAO.getPhotosetListOrderByManagedAndTitle(), this.ssPhotoset.getId());
				this.setVisible(false);
				this.dispose();
			} catch (Exception e) {
				logger.error("Error saving set parameters to database.", e);
				JOptionPane.showMessageDialog(this,
						resourceBundle.getString("SetEditor.saveError.message") + "\n" + e.getMessage(),
						resourceBundle.getString("SetEditor.saveError.title"),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}


	private void btnSaveAndRefreshActionPerformed() {
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


	/*
	 * Verify entry of title text.
	 * <p>If the user tries to enter a title that is already taken by another
	 * set, make the text red.</p>
	 */
	private void txtTitleKeyReleased() {
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


	private void cbxManageActionPerformed() {
		this.addManagedByTextToDescription();
		this.setEnableStates();
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

	private void radioTweetCreatedOrUpdatedActionPerformed() {
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
				prefs.setTabIndex(Preferences.AUTH_PANEL);
				prefs.setVisible(true);
			}
		}
	}


	private void radioTweetNoneActionPerformed() {
		this.txtTweet.setEnabled(false);
	}

	private void cbxLimitSizeActionPerformed() {
		this.txtSetSize.setEnabled(cbxLimitSize.isSelected());
	}

	private void cbxOnThisDayActionPerformed() {
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

	private void cmbOTDMonthActionPerformed() {
		this.updateDayOfMonthComboBox();
	}

	private void cbxCurrentYearActionPerformed() {
		if (this.cbxCurrentYear.isSelected()) {
			this.yearOTDEnd.setYear(SSUtils.getCurrentYear());
			this.yearOTDEnd.setEnabled(false);
		} else {
			this.yearOTDEnd.setEnabled(true);
		}
	}


	private void cmbSortByActionPerformed() {
		// check for sort mode used when an On This Day set is created.
		// warn the user about sort mode selection
		if (this.cbxOnThisDay.isSelected()) {
			switch (this.cmbSortBy.getSelectedIndex()) {
				case 2:		// Date Taken Descending
				case 3:		// Date Taken Ascending
				case 6:		// No Particular Order
				case 9:		// Random
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
			sb.append(resourceBundle.getString("SetEditor.validation.title")).append('\n');
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
			takenMin = this.dateTakenAfter.getDate();
			takenMax = this.dateTakenBefore.getDate();
			if (takenMin == null || takenMax == null) {
				sb.append(resourceBundle.getString("SetEditor.validation.dateTaken")).append('\n');
				ok = false;
			} else if (takenMax.before(takenMin)) {
				sb.append(resourceBundle.getString("SetEditor.validation.dateTakenValues")).append('\n');
				ok = false;
			}
		}

		// DATE UPLOADED:
		// IF THE DATE UPLOADED CHECKBOX IS SELECTED, DATES MUST BE VALID
		// YYYY-MM-DD FORMAT
		if (this.cbxDateUploaded.isSelected()) {
			uploadedMin = this.dateUploadedAfter.getDate();
			uploadedMax = this.dateUploadedBefore.getDate();
			if (uploadedMin == null || uploadedMax == null) {
				sb.append(resourceBundle.getString("SetEditor.validation.dateUploaded")).append('\n');
				ok = false;
			} else if (uploadedMax.before(uploadedMin)) {
				sb.append(resourceBundle.getString("SetEditor.validation.dateUploadedValues")).append('\n');
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
				sb.append(resourceBundle.getString("SetEditor.validation.setSize")).append('\n');
				ok = false;
			}
		}

		if (this.cbxOnThisDay.isSelected()) {
			int yearStart = 0;
			int yearEnd = 0;
			try {
				yearStart = this.yearOTDStart.getYear();
				if (yearStart < 1900) {
					throw new Exception();
				}
				this.ssPhotoset.setOnThisDayYearStart(yearStart);
			} catch (Exception e) {
				sb.append(resourceBundle.getString("SetEditor.validation.onThisDayStartYear")).append('\n');
				ok = false;
			}
			try {
				if (this.cbxCurrentYear.isSelected()) {
					this.ssPhotoset.setOnThisDayYearEnd(0);
					yearEnd = SSUtils.getCurrentYear();    // to make the start > end check valid
				} else {
					yearEnd = this.yearOTDEnd.getYear();
					if (yearEnd > SSUtils.getCurrentYear()) {
						throw new Exception();
					}
					this.ssPhotoset.setOnThisDayYearEnd(yearEnd);
				}
			} catch (Exception e) {
				sb.append(resourceBundle.getString("SetEditor.validation.onThisDayEndYear")).append('\n');
				ok = false;
			}

			if (yearStart > yearEnd) {
				sb.append(resourceBundle.getString("SetEditor.validation.onThisDayValues")).append('\n');
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
					resourceBundle.getString("SetEditor.validation.error.message") + "\n" +  sb.toString(),
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
		this.dateTakenAfter.setEnabled(this.cbxManage.isSelected());
		this.dateTakenBefore.setEnabled(this.cbxManage.isSelected());
		this.cbxDateUploaded.setEnabled(this.cbxManage.isSelected());
		this.dateUploadedAfter.setEnabled(this.cbxManage.isSelected());
		this.dateUploadedBefore.setEnabled(this.cbxManage.isSelected());
		this.cmbSortBy.setEnabled(this.cbxManage.isSelected());
		this.radioTweetCreated.setEnabled(this.cbxManage.isSelected());
		this.radioTweetNone.setEnabled(this.cbxManage.isSelected());
		this.radioTweetUpdated.setEnabled(this.cbxManage.isSelected());
		this.txtTweet.setEnabled(this.cbxManage.isSelected());
		this.cbxLock.setEnabled(this.cbxManage.isSelected());

		// now set things that may change if is managed
		if (this.cbxManage.isSelected()) {
			this.dateTakenAfter.setEnabled(this.cbxDateTaken.isSelected());
			this.dateTakenBefore.setEnabled(this.cbxDateTaken.isSelected());

			this.dateUploadedAfter.setEnabled(this.cbxDateUploaded.isSelected());
			this.dateUploadedBefore.setEnabled(this.cbxDateUploaded.isSelected());

			this.txtTweet.setEnabled(this.radioTweetCreated.isSelected() ||
					this.radioTweetUpdated.isSelected());

		}

		// set the On This Day section
		this.cbxOnThisDay.setEnabled(this.cbxManage.isSelected());
		this.cmbOTDDay.setEnabled(this.cbxManage.isSelected() && this.cbxOnThisDay.isSelected());
		this.cmbOTDMonth.setEnabled(this.cbxManage.isSelected() && this.cbxOnThisDay.isSelected());
		this.yearOTDEnd.setEnabled(this.cbxManage.isSelected() && this.cbxOnThisDay.isSelected() && (!this.cbxCurrentYear.isSelected()));
		this.yearOTDStart.setEnabled(this.cbxManage.isSelected() && this.cbxOnThisDay.isSelected());
		this.cbxCurrentYear.setEnabled(this.cbxManage.isSelected() && this.cbxOnThisDay.isSelected());

		// ADVANCED TAB
		this.cmbPrivacy.setEnabled(this.cbxManage.isSelected());
		this.cmbSafeSearch.setEnabled(this.cbxManage.isSelected());
		this.cmbContentType.setEnabled(this.cbxManage.isSelected());
		this.cmbMediaType.setEnabled(this.cbxManage.isSelected());
		this.cmbGeotag.setEnabled(this.cbxManage.isSelected());
		this.cbxInGallery.setEnabled(this.cbxManage.isSelected());
		this.cbxInGetty.setEnabled(this.cbxManage.isSelected());
		this.cbxInCommons.setEnabled(this.cbxManage.isSelected());
		this.cbxLimitSize.setEnabled(this.cbxManage.isSelected());
		this.txtSetSize.setEnabled(this.cbxManage.isSelected());
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
	private JLabel lblMessage;
	private JCheckBox cbxManage;
	private JCheckBox cbxLock;
	private JLabel label1;
	private JTextField txtTitle;
	private JLabel label2;
	private JScrollPane jScrollPane1;
	private JTextArea txtDescription;
	private JPanel pnlTags;
	private JTextField txtTags;
	private JComboBox<String> cmbTags;
	private JPanel pnlDates;
	private JPanel panel1;
	private JCheckBox cbxDateTaken;
	private JDateChooser dateTakenAfter;
	private JLabel jLabel3;
	private JDateChooser dateTakenBefore;
	private JCheckBox cbxDateUploaded;
	private JDateChooser dateUploadedAfter;
	private JLabel jLabel4;
	private JDateChooser dateUploadedBefore;
	private JPanel panel2;
	private JCheckBox cbxOnThisDay;
	private JComboBox cmbOTDMonth;
	private JComboBox<String> cmbOTDDay;
	private JLabel jLabel9;
	private JYearChooser yearOTDStart;
	private JLabel jLabel13;
	private JYearChooser yearOTDEnd;
	private JCheckBox cbxCurrentYear;
	private JPanel pnlOther;
	private JLabel jLabel1;
	private JComboBox cmbSortBy;
	private JScrollPane jScrollPane3;
	private JTextArea txtTweet;
	private JLabel jLabel2;
	private JLabel jLabel5;
	private JLabel jLabel6;
	private JLabel jLabel7;
	private JRadioButton radioTweetNone;
	private JRadioButton radioTweetUpdated;
	private JRadioButton radioTweetCreated;
	private JPanel advancedPanel;
	private JPanel jPanel4;
	private JLabel jLabel8;
	private JComboBox cmbPrivacy;
	private JLabel lblSafeSearch;
	private JComboBox cmbSafeSearch;
	private JPanel jPanel5;
	private JLabel jLabel10;
	private JComboBox cmbContentType;
	private JLabel jLabel11;
	private JComboBox cmbMediaType;
	private JPanel jPanel6;
	private JLabel jLabel12;
	private JComboBox cmbGeotag;
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
