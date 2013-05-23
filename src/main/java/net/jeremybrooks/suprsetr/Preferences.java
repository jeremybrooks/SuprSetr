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


import net.jeremybrooks.jinx.logger.JinxLogger;
import net.jeremybrooks.suprsetr.dao.DAOHelper;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import net.jeremybrooks.suprsetr.dao.PhotosetDAO;
import net.jeremybrooks.suprsetr.flickr.FlickrHelper;
import net.jeremybrooks.suprsetr.twitter.TwitterHelper;
import net.jeremybrooks.suprsetr.utils.NetUtil;
import net.jeremybrooks.suprsetr.workers.TwitterAuthenticatorWorker;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerDateModel;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;


/**
 * @author jeremyb
 */
public class Preferences extends javax.swing.JDialog {

	private static final long serialVersionUID = 6297020767085159090L;
	/**
	 * Logging.
	 */
	private Logger logger = Logger.getLogger(Preferences.class);

	/**
	 * Constant defining the options tab panel.
	 */
	public static final int OPTIONS_PANEL = 0;

	/**
	 * Constant defining the Authorizations tab panel.
	 */
	public static final int AUTH_PANEL = 1;

	/**
	 * Constant defining the Proxy tab panel.
	 */
	public static final int PROXY_PANEL = 2;

	/**
	 * Flag indicating if something has changed requiring list refresh.
	 */
	private boolean refreshList = false;

	private SimpleDateFormat autoRefreshFormat = new SimpleDateFormat("HH:mm");
	private Date autoRefreshDate = new Date();


	private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.preferences");

	private void btnTwitterActionPerformed(ActionEvent e) {
		this.lblMessage.setText("");

		if (btnTwitter.getText().equals(resourceBundle.getString("Preferences.btnTwitter_LoggedOut.text"))) {
			BlockerPanel blocker = new BlockerPanel(this, resourceBundle.getString("Preferences.message.twitterAuthorization"));
			setGlassPane(blocker);
			new TwitterAuthenticatorWorker(this, blocker).execute();
		} else {
			TwitterHelper.logout();
			updateStatus();
		}
	}


	/**
	 * Update and close the window.
	 * When the user clicks OK, we need to update the list view because the
	 * options affect the state of the photosets.
	 *
	 * @param e
	 */
	private void btnOKActionPerformed(ActionEvent e) {
		if (this.validateProxyInput()) {
			if (this.cbxProxy.isSelected()) {
				// save proxy settings
				String host = this.txtProxyHost.getText().trim();
				if (host != null) {
					if (host.startsWith("http://")) {
						host = host.substring("http://".length());
					}
				}

				String port = this.txtProxyPort.getText().trim();
				String user = this.txtProxyUser.getText().trim();
				String pass = new String(this.txtProxyPass.getPassword());

				LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_USE_PROXY, DAOHelper.booleanToString(true));
				LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_PROXY_HOST, host);
				LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_PROXY_PORT, port);
				LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_PROXY_USER, user);
				LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_PROXY_PASS, pass);
				LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_PROXY_USE_SYSTEM, DAOHelper.booleanToString(rbtnSystem.isSelected()));

				if (rbtnSystem.isSelected()) {
					logger.info("Using system proxy settings");
					NetUtil.enableSystemProxy();
				} else {
					logger.info("Using proxy " + host + ":" + port);
					NetUtil.enableProxy(host, port, user, pass.toCharArray());
				}

			} else {
				// Save proxy setting and clear system properties
				LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_USE_PROXY, DAOHelper.booleanToString(false));
				NetUtil.clearProxy();
			}

			// save logging properties
			Main.getLoggingProperties().setProperty("size", this.cmbLogSize.getSelectedItem().toString());
			Main.getLoggingProperties().setProperty("index", this.cmbLogIndex.getSelectedItem().toString());
			Main.storeLoggingProperties();

			if (this.refreshList) {
				try {
					MainWindow.getMainWindow().setMasterList(PhotosetDAO.getPhotosetListOrderByManagedAndTitle(), null);
				} catch (Exception ex) {
					logger.warn("Could not update the list.", ex);
				}
			}

			LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_AUTO_REFRESH, DAOHelper.booleanToString(this.cbxAutoRefresh.isSelected()));
			LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_AUTO_REFRESH_EXIT_AFTER, DAOHelper.booleanToString(this.cbxExitAfter.isSelected()));
			String time = autoRefreshFormat.format((Date)timeSpinner.getValue());
			LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_AUTO_REFRESH_TIME, time);
			MainWindow.getMainWindow().updateStatusBar();

			this.setVisible(false);
			this.dispose();
		} else {
			JOptionPane.showMessageDialog(this,
					resourceBundle.getString("Preferences.message.proxyErrorMsg"),
					resourceBundle.getString("Preferences.message.proxyErrorTitle"),
					JOptionPane.WARNING_MESSAGE);
		}
	}


	private void cbxAddViaActionPerformed(ActionEvent e) {
		LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_ADD_VIA, DAOHelper.booleanToString(this.cbxAddVia.isSelected()));
	}

	private void cbxAutoRefreshActionPerformed() {
		this.timeSpinner.setEnabled(cbxAutoRefresh.isSelected());
		this.cbxExitAfter.setEnabled(cbxAutoRefresh.isSelected());
	}

	private void rbtnSystemActionPerformed() {
		setProxyComponentStates();
	}

	private void rbtnCustomActionPerformed() {
		setProxyComponentStates();
	}

	private void button1ActionPerformed() {
		logger.info("Testing twitter");
		try {
			TwitterHelper.updateStatus("Test tweet");
		} catch (Exception e) {
			logger.error("error", e);
		}
	}




	public Preferences(java.awt.Frame parent, boolean modal) {
		super(parent, modal);

		String time = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_AUTO_REFRESH_TIME);
		if (time != null) {
			try {
				autoRefreshDate = autoRefreshFormat.parse(time);
			} catch (Exception e) {
				logger.warn("Failed to parse time " + time + "; using current time.");
			}
		}

		initComponents();

		setIconImage(new ImageIcon(getClass().getResource("/images/s16.png")).getImage());

		// After window is init'ed, lookup values in DB and set accordingly
		this.cbxAddVia.setSelected(DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_ADD_VIA)));

		String refresh = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_REFRESH_WAIT);
		if (refresh == null) {
			this.cmbRefresh.setSelectedItem(SSConstants.DEFAULT_REFRESH_WAIT);
		} else {
			this.cmbRefresh.setSelectedItem(refresh);
		}

		this.cbxDetailLog.setSelected(DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_DETAIL_LOG)));
		this.cmbLogSize.setSelectedItem(Main.getLoggingProperties().getProperty("size"));
		this.cmbLogIndex.setSelectedItem(Main.getLoggingProperties().getProperty("index"));

		// The value "0" indicates a special selection for interval, so set accordingly
		String interval = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_FAVRTAGR_INTERVAL);
		switch (Integer.valueOf(interval)) {
			case 10:
				this.cmbFavr.setSelectedIndex(0);
				break;
			case 25:
				this.cmbFavr.setSelectedIndex(1);
				break;
			case 100:
				this.cmbFavr.setSelectedIndex(2);
				break;
			case 0:
				this.cmbFavr.setSelectedIndex(3);
				break;
			case 4:
				this.cmbFavr.setSelectedIndex(4);
				break;
			default:
				this.cmbFavr.setSelectedIndex(0);
				break;
		}

		this.cbxUpdate.setSelected(DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_CHECK_FOR_UPDATE)));
		this.updateStatus();

		this.cbxProxy.setSelected(DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_USE_PROXY)));
		this.rbtnSystem.setSelected(DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_USE_SYSTEM)));
		this.rbtnCustom.setSelected(!DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_USE_SYSTEM)));
		this.txtProxyHost.setText(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_HOST));
		this.txtProxyPort.setText(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_PORT));
		this.txtProxyUser.setText(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_USER));
		this.txtProxyPass.setText(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_PASS));
		this.setProxyComponentStates();

		this.cbxAddManaged.setSelected(DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_ADD_MANAGED)));
		this.refreshList = false;

		this.cbxAutoRefresh.setSelected(DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_AUTO_REFRESH)));
		this.cbxExitAfter.setSelected(DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_AUTO_REFRESH_EXIT_AFTER)));
		this.timeSpinner.setEnabled(this.cbxAutoRefresh.isSelected());
		this.cbxExitAfter.setEnabled(this.cbxAutoRefresh.isSelected());
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
		jPanel1 = new JPanel();
		cbxAddVia = new JCheckBox();
		cbxAddManaged = new JCheckBox();
		lblRefreshPrefix = new JLabel();
		cmbRefresh = new JComboBox<>();
		lblRefreshSuffix = new JLabel();
		cbxUpdate = new JCheckBox();
		lblFavrPrefix = new JLabel();
		cmbFavr = new JComboBox<>();
		cbxDetailLog = new JCheckBox();
		lblLogFile = new JLabel();
		cmbLogSize = new JComboBox<>();
		lblRetain = new JLabel();
		cmbLogIndex = new JComboBox<>();
		lblNote = new JLabel();
		pnlAuthorizations = new JPanel();
		pnlFlickr = new JPanel();
		lblFlickrStatus = new JLabel();
		btnFlickr = new JButton();
		pnlTwitter = new JPanel();
		lblTwitterStatus = new JLabel();
		lblMessage = new JLabel();
		btnTwitter = new JButton();
		pnlProxy = new JPanel();
		cbxProxy = new JCheckBox();
		rbtnSystem = new JRadioButton();
		rbtnCustom = new JRadioButton();
		panel2 = new JPanel();
		lblHost = new JLabel();
		txtProxyHost = new JTextField();
		lblPort = new JLabel();
		txtProxyPort = new JTextField();
		lblUsername = new JLabel();
		txtProxyUser = new JTextField();
		lblPassword = new JLabel();
		txtProxyPass = new JPasswordField();
		pnlAutoRefresh = new JPanel();
		cbxAutoRefresh = new JCheckBox();
		cbxExitAfter = new JCheckBox();
		label1 = new JLabel();
		timeSpinner = new JSpinner();
		panel1 = new JPanel();
		btnOK = new JButton();

		//======== this ========
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(bundle.getString("Preferences.this.title"));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				formWindowClosed(e);
			}
		});
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== jTabbedPane1 ========
		{
			jTabbedPane1.setToolTipText(bundle.getString("Preferences.jTabbedPane1.toolTipText"));

			//======== jPanel1 ========
			{

				//---- cbxAddVia ----
				cbxAddVia.setText(bundle.getString("Preferences.cbxAddVia.text"));
				cbxAddVia.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cbxAddViaActionPerformed(e);
					}
				});

				//---- cbxAddManaged ----
				cbxAddManaged.setText(bundle.getString("Preferences.cbxAddManaged.text"));
				cbxAddManaged.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cbxAddManagedActionPerformed(e);
					}
				});

				//---- lblRefreshPrefix ----
				lblRefreshPrefix.setText(bundle.getString("Preferences.lblRefreshPrefix.text"));

				//---- cmbRefresh ----
				cmbRefresh.setModel(new DefaultComboBoxModel<>(new String[] {
					"6",
					"12",
					"24",
					"48",
					"72"
				}));
				cmbRefresh.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbRefreshActionPerformed(e);
					}
				});

				//---- lblRefreshSuffix ----
				lblRefreshSuffix.setText(bundle.getString("Preferences.lblRefreshSuffix.text"));

				//---- cbxUpdate ----
				cbxUpdate.setText(bundle.getString("Preferences.cbxUpdate.text"));
				cbxUpdate.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cbxUpdateActionPerformed(e);
					}
				});

				//---- lblFavrPrefix ----
				lblFavrPrefix.setText(bundle.getString("Preferences.lblFavrPrefix.text"));

				//---- cmbFavr ----
				cmbFavr.setModel(new DefaultComboBoxModel<>(new String[] {
					"every 10 favorites",
					"every 25 favorites",
					"every 100 favorites",
					"every 10 favorites up to 100, then every 100 favorites",
					"only 10, 25, 50, and 100 favorites"
				}));
				cmbFavr.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cmbFavrActionPerformed(e);
					}
				});

				//---- cbxDetailLog ----
				cbxDetailLog.setText(bundle.getString("Preferences.cbxDetailLog.text"));
				cbxDetailLog.setToolTipText(bundle.getString("Preferences.cbxDetailLog.toolTipText"));
				cbxDetailLog.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cbxDetailLogActionPerformed(e);
					}
				});

				//---- lblLogFile ----
				lblLogFile.setText(bundle.getString("Preferences.lblLogFile.text"));

				//---- cmbLogSize ----
				cmbLogSize.setModel(new DefaultComboBoxModel<>(new String[] {
					"1MB",
					"5MB",
					"10MB"
				}));

				//---- lblRetain ----
				lblRetain.setText(bundle.getString("Preferences.lblRetain.text"));

				//---- cmbLogIndex ----
				cmbLogIndex.setModel(new DefaultComboBoxModel<>(new String[] {
					"2",
					"5",
					"10"
				}));

				//---- lblNote ----
				lblNote.setFont(new Font("Lucida Grande", Font.ITALIC, 13));
				lblNote.setText(bundle.getString("Preferences.lblNote.text"));

				GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
				jPanel1.setLayout(jPanel1Layout);
				jPanel1Layout.setHorizontalGroup(
					jPanel1Layout.createParallelGroup()
						.addGroup(jPanel1Layout.createSequentialGroup()
							.addGroup(jPanel1Layout.createParallelGroup()
								.addComponent(cbxAddVia)
								.addComponent(cbxUpdate)
								.addComponent(cbxAddManaged)
								.addGroup(jPanel1Layout.createSequentialGroup()
									.addGap(9, 9, 9)
									.addGroup(jPanel1Layout.createParallelGroup()
										.addGroup(jPanel1Layout.createSequentialGroup()
											.addComponent(lblFavrPrefix)
											.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
											.addComponent(cmbFavr, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
										.addGroup(jPanel1Layout.createSequentialGroup()
											.addComponent(lblRefreshPrefix)
											.addGap(10, 10, 10)
											.addComponent(cmbRefresh, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
											.addComponent(lblRefreshSuffix))))
								.addComponent(cbxDetailLog)
								.addGroup(jPanel1Layout.createSequentialGroup()
									.addGap(8, 8, 8)
									.addGroup(jPanel1Layout.createParallelGroup()
										.addGroup(jPanel1Layout.createSequentialGroup()
											.addGroup(jPanel1Layout.createParallelGroup()
												.addComponent(lblLogFile)
												.addComponent(lblRetain))
											.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
											.addGroup(jPanel1Layout.createParallelGroup()
												.addComponent(cmbLogSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(cmbLogIndex, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
										.addComponent(lblNote))))
							.addContainerGap(19, Short.MAX_VALUE))
				);
				jPanel1Layout.setVerticalGroup(
					jPanel1Layout.createParallelGroup()
						.addGroup(jPanel1Layout.createSequentialGroup()
							.addComponent(cbxAddVia)
							.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
							.addComponent(cbxAddManaged)
							.addGap(7, 7, 7)
							.addComponent(cbxUpdate)
							.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
							.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(lblRefreshPrefix)
								.addComponent(lblRefreshSuffix)
								.addComponent(cmbRefresh, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(lblFavrPrefix)
								.addComponent(cmbFavr, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(18, 18, 18)
							.addComponent(cbxDetailLog)
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(lblLogFile)
								.addComponent(cmbLogSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
							.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(lblRetain)
								.addComponent(cmbLogIndex, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 53, Short.MAX_VALUE)
							.addComponent(lblNote)
							.addContainerGap())
				);
			}
			jTabbedPane1.addTab(bundle.getString("Preferences.jPanel1.tab.title"), jPanel1);

			//======== pnlAuthorizations ========
			{
				pnlAuthorizations.setLayout(new VerticalLayout(5));

				//======== pnlFlickr ========
				{
					pnlFlickr.setBorder(new TitledBorder(bundle.getString("Preferences.pnlFlickr.border")));
					pnlFlickr.setLayout(new GridBagLayout());
					((GridBagLayout)pnlFlickr.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)pnlFlickr.getLayout()).rowHeights = new int[] {0, 0, 0};
					((GridBagLayout)pnlFlickr.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
					((GridBagLayout)pnlFlickr.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

					//---- lblFlickrStatus ----
					lblFlickrStatus.setText(bundle.getString("Preferences.lblFlickrStatus.text"));
					pnlFlickr.add(lblFlickrStatus, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));

					//---- btnFlickr ----
					btnFlickr.setText(bundle.getString("Preferences.btnFlickr.text"));
					btnFlickr.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							btnFlickrActionPerformed(e);
						}
					});
					pnlFlickr.add(btnFlickr, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				pnlAuthorizations.add(pnlFlickr);

				//======== pnlTwitter ========
				{
					pnlTwitter.setBorder(new TitledBorder(bundle.getString("Preferences.pnlTwitter.border")));
					pnlTwitter.setLayout(new GridBagLayout());
					((GridBagLayout)pnlTwitter.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)pnlTwitter.getLayout()).rowHeights = new int[] {0, 0, 0};
					((GridBagLayout)pnlTwitter.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)pnlTwitter.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

					//---- lblTwitterStatus ----
					lblTwitterStatus.setText(bundle.getString("Preferences.lblTwitterStatus.text"));
					pnlTwitter.add(lblTwitterStatus, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));
					pnlTwitter.add(lblMessage, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- btnTwitter ----
					btnTwitter.setText(bundle.getString("Preferences.btnTwitter.text"));
					btnTwitter.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							btnTwitterActionPerformed(e);
						}
					});
					pnlTwitter.add(btnTwitter, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				pnlAuthorizations.add(pnlTwitter);
			}
			jTabbedPane1.addTab(bundle.getString("Preferences.pnlAuthorizations.tab.title"), pnlAuthorizations);

			//======== pnlProxy ========
			{
				pnlProxy.setBorder(new TitledBorder(bundle.getString("Preferences.pnlProxy.border")));
				pnlProxy.setLayout(new GridBagLayout());
				((GridBagLayout)pnlProxy.getLayout()).columnWidths = new int[] {0, 0, 0};
				((GridBagLayout)pnlProxy.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
				((GridBagLayout)pnlProxy.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
				((GridBagLayout)pnlProxy.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

				//---- cbxProxy ----
				cbxProxy.setText(bundle.getString("Preferences.cbxProxy.text"));
				cbxProxy.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cbxProxyActionPerformed(e);
					}
				});
				pnlProxy.add(cbxProxy, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- rbtnSystem ----
				rbtnSystem.setText(bundle.getString("Preferences.rbtnSystem.text"));
				rbtnSystem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						rbtnSystemActionPerformed();
					}
				});
				pnlProxy.add(rbtnSystem, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- rbtnCustom ----
				rbtnCustom.setText(bundle.getString("Preferences.rbtnCustom.text"));
				rbtnCustom.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						rbtnCustomActionPerformed();
					}
				});
				pnlProxy.add(rbtnCustom, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel2 ========
				{
					panel2.setLayout(new GridBagLayout());
					((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0};
					((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
					((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

					//---- lblHost ----
					lblHost.setText(bundle.getString("Preferences.lblHost.text"));
					panel2.add(lblHost, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 5), 0, 0));
					panel2.add(txtProxyHost, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- lblPort ----
					lblPort.setText(bundle.getString("Preferences.lblPort.text"));
					panel2.add(lblPort, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 5), 0, 0));
					panel2.add(txtProxyPort, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- lblUsername ----
					lblUsername.setText(bundle.getString("Preferences.lblUsername.text"));
					panel2.add(lblUsername, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 5, 5), 0, 0));
					panel2.add(txtProxyUser, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//---- lblPassword ----
					lblPassword.setText(bundle.getString("Preferences.lblPassword.text"));
					panel2.add(lblPassword, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- txtProxyPass ----
					txtProxyPass.setToolTipText(bundle.getString("Preferences.txtProxyPass.toolTipText"));
					panel2.add(txtProxyPass, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				pnlProxy.add(panel2, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			jTabbedPane1.addTab(bundle.getString("Preferences.pnlProxy.tab.title"), pnlProxy);

			//======== pnlAutoRefresh ========
			{
				pnlAutoRefresh.setLayout(new GridBagLayout());
				((GridBagLayout)pnlAutoRefresh.getLayout()).columnWidths = new int[] {0, 0, 0};
				((GridBagLayout)pnlAutoRefresh.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
				((GridBagLayout)pnlAutoRefresh.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
				((GridBagLayout)pnlAutoRefresh.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

				//---- cbxAutoRefresh ----
				cbxAutoRefresh.setText(bundle.getString("Preferences.cbxAutoRefresh.text"));
				cbxAutoRefresh.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cbxAutoRefreshActionPerformed();
					}
				});
				pnlAutoRefresh.add(cbxAutoRefresh, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- cbxExitAfter ----
				cbxExitAfter.setText(bundle.getString("Preferences.cbxExitAfter.text"));
				pnlAutoRefresh.add(cbxExitAfter, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label1 ----
				label1.setText(bundle.getString("Preferences.label1.text"));
				pnlAutoRefresh.add(label1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- timeSpinner ----
				timeSpinner.setModel(new SpinnerDateModel(autoRefreshDate, null, null, Calendar.MINUTE));
				timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));
				pnlAutoRefresh.add(timeSpinner, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			jTabbedPane1.addTab(bundle.getString("Preferences.pnlAutoRefresh.tab.title"), pnlAutoRefresh);
		}
		contentPane.add(jTabbedPane1, BorderLayout.NORTH);

		//======== panel1 ========
		{
			panel1.setLayout(new FlowLayout(FlowLayout.RIGHT));

			//---- btnOK ----
			btnOK.setText(bundle.getString("Preferences.btnOK.text"));
			btnOK.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					btnOKActionPerformed(e);
				}
			});
			panel1.add(btnOK);
		}
		contentPane.add(panel1, BorderLayout.SOUTH);
		setSize(595, 450);
		setLocationRelativeTo(null);

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(rbtnSystem);
		buttonGroup1.add(rbtnCustom);
	}// </editor-fold>//GEN-END:initComponents


	/**
	 * Simple check for valid input in the Proxy tab.
	 *
	 * @return true if the proxy dialog input passes simple sanity tests.
	 */
	private boolean validateProxyInput() {

		// everything is OK if the checkbox is not selected
		if (!this.cbxProxy.isSelected()) {
			return true;
		}

		// Fail if the host is empty
		if (this.txtProxyHost.getText().trim().length() == 0) {
			return false;
		}

		// Fail if the port is not an Integer
		try {
			Integer.parseInt(this.txtProxyPort.getText().trim());
		} catch (Exception e) {
			return false;
		}

		// Looks good
		return true;
	}


	private void cbxUpdateActionPerformed(java.awt.event.ActionEvent evt) {
		LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_CHECK_FOR_UPDATE, DAOHelper.booleanToString(this.cbxUpdate.isSelected()));
	}

	private void cmbFavrActionPerformed(java.awt.event.ActionEvent evt) {
		switch (this.cmbFavr.getSelectedIndex()) {
			case 0:
				LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_FAVRTAGR_INTERVAL, "10");
				break;
			case 1:
				LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_FAVRTAGR_INTERVAL, "25");
				break;
			case 2:
				LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_FAVRTAGR_INTERVAL, "100");
				break;
			case 3:
				LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_FAVRTAGR_INTERVAL, "0");
				break;
			case 4:
				LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_FAVRTAGR_INTERVAL, "4");
				break;
			default:
				break;
		}
	}

	private void cmbRefreshActionPerformed(java.awt.event.ActionEvent evt) {
		LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_REFRESH_WAIT, cmbRefresh.getSelectedItem().toString());
		this.refreshList = true;
	}

	private void btnFlickrActionPerformed(java.awt.event.ActionEvent evt) {
		int confirm = JOptionPane.showConfirmDialog(this,
				resourceBundle.getString("Preferences.message.clearFlickrMsg"),
				resourceBundle.getString("Preferences.message.clearFlickrTitle"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (confirm == JOptionPane.YES_OPTION) {
			FlickrHelper.getInstance().deauthorize();
			System.exit(2);
		}
	}

	private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
		if (this.refreshList) {
			try {
				MainWindow.getMainWindow().setMasterList(PhotosetDAO.getPhotosetListOrderByManagedAndTitle(), null);
			} catch (Exception e) {
				logger.warn("Could not update the list.", e);
			}
		}
	}//GEN-LAST:event_formWindowClosed


	/**
	 * Respond to clicks on the "Use Proxy" checkbox.
	 * <p/>
	 * <p>The text entryfields will be enabled/disabled based on the state of
	 * the checkbox.</p>
	 *
	 * @param evt
	 */
	private void cbxProxyActionPerformed(java.awt.event.ActionEvent evt) {
		setProxyComponentStates();
	}

	private void setProxyComponentStates() {
		this.rbtnSystem.setEnabled(this.cbxProxy.isSelected());
		this.rbtnCustom.setEnabled(this.cbxProxy.isSelected());
		this.txtProxyHost.setEnabled(this.cbxProxy.isSelected() && this.rbtnCustom.isSelected());
		this.txtProxyPass.setEnabled(this.cbxProxy.isSelected() && this.rbtnCustom.isSelected());
		this.txtProxyPort.setEnabled(this.cbxProxy.isSelected() && this.rbtnCustom.isSelected());
		this.txtProxyUser.setEnabled(this.cbxProxy.isSelected() && this.rbtnCustom.isSelected());
	}

	/**
	 * @param evt
	 */
	private void cbxAddManagedActionPerformed(java.awt.event.ActionEvent evt) {
		LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_ADD_MANAGED, DAOHelper.booleanToString(this.cbxAddManaged.isSelected()));
	}

	private void cbxDetailLogActionPerformed(java.awt.event.ActionEvent evt) {
		LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_DETAIL_LOG, DAOHelper.booleanToString(this.cbxDetailLog.isSelected()));
		if (this.cbxDetailLog.isSelected()) {
			// Turn on detailed logging
			JinxLogger.setLogger(new MyJinxLogger());
		} else {
			JinxLogger.setLogger(null);
		}
	}


	public void updateStatus() {
		String token = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TWITTER_TOKEN);
		final String twitterLabelText;
		final String twitterButtonText;
		final String flickrLabelText;
		final String flickrButtonText;

		if (token == null || token.isEmpty()) {
			twitterLabelText = resourceBundle.getString("Preferences.lblTwitterStatus_LoggedOut.text");
			twitterButtonText = resourceBundle.getString("Preferences.btnTwitter_LoggedOut.text");
		} else {
			twitterLabelText = resourceBundle.getString("Preferences.lblTwitterStatus_LoggedIn.text") + " " + LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TWITTER_USERNAME);
			twitterButtonText = resourceBundle.getString("Preferences.btnTwitter_LoggedIn.text");
		}

		String username = FlickrHelper.getInstance().getUsername();
		if (username == null) {
			flickrLabelText = resourceBundle.getString("Preferences.lblFlickrStatus_LoggedOut.text");
			flickrButtonText = resourceBundle.getString("Preferences.btnFlickr_LoggedOut.text");
		} else {
			flickrLabelText = resourceBundle.getString("Preferences.lblFlickrStatus_LoggedIn.text") + " " + FlickrHelper.getInstance().getUsername();
			flickrButtonText = resourceBundle.getString("Preferences.btnFlickr_LoggedIn.text");
		}

		java.awt.EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				lblTwitterStatus.setText(twitterLabelText);
				btnTwitter.setText(twitterButtonText);
				lblFlickrStatus.setText(flickrLabelText);
				btnFlickr.setText(flickrButtonText);
			}

		});


	}


	public void setTabIndex(int index) {
		this.jTabbedPane1.setSelectedIndex(index);
	}


	public void setMessage(String message) {
		this.lblMessage.setText(message);
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private JTabbedPane jTabbedPane1;
	private JPanel jPanel1;
	private JCheckBox cbxAddVia;
	private JCheckBox cbxAddManaged;
	private JLabel lblRefreshPrefix;
	private JComboBox<String> cmbRefresh;
	private JLabel lblRefreshSuffix;
	private JCheckBox cbxUpdate;
	private JLabel lblFavrPrefix;
	private JComboBox<String> cmbFavr;
	private JCheckBox cbxDetailLog;
	private JLabel lblLogFile;
	private JComboBox<String> cmbLogSize;
	private JLabel lblRetain;
	private JComboBox<String> cmbLogIndex;
	private JLabel lblNote;
	private JPanel pnlAuthorizations;
	private JPanel pnlFlickr;
	private JLabel lblFlickrStatus;
	private JButton btnFlickr;
	private JPanel pnlTwitter;
	private JLabel lblTwitterStatus;
	private JLabel lblMessage;
	private JButton btnTwitter;
	private JPanel pnlProxy;
	private JCheckBox cbxProxy;
	private JRadioButton rbtnSystem;
	private JRadioButton rbtnCustom;
	private JPanel panel2;
	private JLabel lblHost;
	private JTextField txtProxyHost;
	private JLabel lblPort;
	private JTextField txtProxyPort;
	private JLabel lblUsername;
	private JTextField txtProxyUser;
	private JLabel lblPassword;
	private JPasswordField txtProxyPass;
	private JPanel pnlAutoRefresh;
	private JCheckBox cbxAutoRefresh;
	private JCheckBox cbxExitAfter;
	private JLabel label1;
	private JSpinner timeSpinner;
	private JPanel panel1;
	private JButton btnOK;
	// End of variables declaration//GEN-END:variables

}
