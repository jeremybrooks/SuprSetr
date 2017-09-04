/*
 * SuprSetr is Copyright 2010-2017 by Jeremy Brooks
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


import net.jeremybrooks.suprsetr.dao.DAOHelper;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import net.jeremybrooks.suprsetr.flickr.FlickrHelper;
import net.jeremybrooks.suprsetr.flickr.JinxFactory;
import net.jeremybrooks.suprsetr.twitter.TwitterHelper;
import net.jeremybrooks.suprsetr.utils.NetUtil;
import net.jeremybrooks.suprsetr.utils.SimpleCache;
import net.jeremybrooks.suprsetr.workers.TwitterAuthenticatorWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;


/**
 * @author Jeremy Brooks
 */
public class Preferences extends javax.swing.JDialog {

  private static final long serialVersionUID = 6297020767085159090L;
  /**
   * Logging.
   */
  private Logger logger = LogManager.getLogger(Preferences.class);

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


  /*
   * Update and close the window.
   * When the user clicks OK, we need to update the list view because the
   * options affect the state of the photosets.
   */
  private void btnOKActionPerformed(ActionEvent e) {
    if (!this.validateProxyInput()) {
      JOptionPane.showMessageDialog(this,
          resourceBundle.getString("Preferences.message.proxyErrorMsg"),
          resourceBundle.getString("Preferences.message.proxyErrorTitle"),
          JOptionPane.WARNING_MESSAGE);
    } else if (this.validateCustomFavrInterval()) {
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

        logger.info("Using proxy " + host + ":" + port);
        NetUtil.enableProxy(host, port, user, pass.toCharArray());

      } else {
        // Save proxy setting and clear system properties
        LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_USE_PROXY, DAOHelper.booleanToString(false));
        NetUtil.clearProxy();
      }

      if (this.refreshList) {
        SimpleCache.getInstance().invalidateAll();
        try {
          MainWindow.getMainWindow().updateMasterList(null);
        } catch (Exception ex) {
          logger.warn("Could not update the list.", ex);
        }
      }

      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_AUTO_REFRESH, DAOHelper.booleanToString(this.cbxAutoRefresh.isSelected()));
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_AUTO_REFRESH_EXIT_AFTER, DAOHelper.booleanToString(this.cbxExitAfter.isSelected()));
      String time = autoRefreshFormat.format((Date) timeSpinner.getValue());
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_AUTO_REFRESH_TIME, time);
      MainWindow.getMainWindow().updateStatusBar();

      this.setVisible(false);
      this.dispose();
    }
  }


  private void cbxAddViaActionPerformed(ActionEvent e) {
    LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_ADD_VIA, DAOHelper.booleanToString(this.cbxAddVia.isSelected()));
  }

  private void cbxAutoRefreshActionPerformed() {
    this.timeSpinner.setEnabled(cbxAutoRefresh.isSelected());
    this.cbxExitAfter.setEnabled(cbxAutoRefresh.isSelected());
  }

  private void btnCustomHelpActionPerformed() {
    JOptionPane.showMessageDialog(this,
        resourceBundle.getString("Preferences.customhelp.message"),
        resourceBundle.getString("Preferences.customhelp.title"),
        JOptionPane.INFORMATION_MESSAGE);
  }

  private void btnTagTypeHelpActionPerformed(ActionEvent e) {
    JOptionPane.showMessageDialog(this,
        resourceBundle.getString("Preferences.tagtypehelp.message"),
        resourceBundle.getString("Preferences.tagtypehelp.title"),
        JOptionPane.INFORMATION_MESSAGE);
  }

  private void cmbTagTypeActionPerformed(ActionEvent e) {
    LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_TAG_TYPE, String.valueOf(this.cmbTagType.getSelectedIndex()));
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

    // The value "0" indicates a special selection for interval, so set accordingly
    // The value "c,...." indicates custom interval, so set accordingly
    String interval = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_FAVRTAGR_INTERVAL);
    if (interval.startsWith("c,")) {
      this.displayCustomIntervalFields(true);
      this.txtCustom.setText(interval.substring(2));
      this.cmbFavr.setSelectedIndex(5);
    } else {
      this.displayCustomIntervalFields(false);
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
    }
    try {
      this.cmbTagType.setSelectedIndex(Integer.parseInt(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TAG_TYPE)));
    } catch (Exception e) {
      this.cmbTagType.setSelectedIndex(0);
    }

    this.cbxUpdate.setSelected(DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_CHECK_FOR_UPDATE)));
    this.updateStatus();

    this.cbxProxy.setSelected(DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_USE_PROXY)));
    this.txtProxyHost.setText(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_HOST));
    this.txtProxyPort.setText(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_PORT));
    this.txtProxyUser.setText(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_USER));
    this.txtProxyPass.setText(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_PASS));

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
    cbxUpdate = new JCheckBox();
    lblRefreshPrefix = new JLabel();
    cmbRefresh = new JComboBox<>();
    lblRefreshSuffix = new JLabel();
    cbxDetailLog = new JCheckBox();
    pnlFavrTagr = new JPanel();
    lblFavrPrefix = new JLabel();
    cmbFavr = new JComboBox<>();
    lblCustom = new JLabel();
    txtCustom = new JTextField();
    btnCustomHelp = new JButton();
    lblTagType = new JLabel();
    cmbTagType = new JComboBox<>();
    btnTagTypeHelp = new JButton();
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
        jPanel1.setLayout(new GridBagLayout());
        ((GridBagLayout)jPanel1.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
        ((GridBagLayout)jPanel1.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
        ((GridBagLayout)jPanel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
        ((GridBagLayout)jPanel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

        //---- cbxAddVia ----
        cbxAddVia.setText(bundle.getString("Preferences.cbxAddVia.text"));
        cbxAddVia.addActionListener(e -> cbxAddViaActionPerformed(e));
        jPanel1.add(cbxAddVia, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 0), 0, 0));

        //---- cbxAddManaged ----
        cbxAddManaged.setText(bundle.getString("Preferences.cbxAddManaged.text"));
        cbxAddManaged.addActionListener(e -> cbxAddManagedActionPerformed(e));
        jPanel1.add(cbxAddManaged, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 5, 5, 0), 0, 0));

        //---- cbxUpdate ----
        cbxUpdate.setText(bundle.getString("Preferences.cbxUpdate.text"));
        cbxUpdate.addActionListener(e -> cbxUpdateActionPerformed(e));
        jPanel1.add(cbxUpdate, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 5, 5, 5), 0, 0));

        //---- lblRefreshPrefix ----
        lblRefreshPrefix.setText(bundle.getString("Preferences.lblRefreshPrefix.text"));
        jPanel1.add(lblRefreshPrefix, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
          GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
          new Insets(0, 5, 5, 5), 0, 0));

        //---- cmbRefresh ----
        cmbRefresh.setModel(new DefaultComboBoxModel<>(new String[] {
          "6",
          "12",
          "24",
          "48",
          "72"
        }));
        cmbRefresh.addActionListener(e -> cmbRefreshActionPerformed(e));
        jPanel1.add(cmbRefresh, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
          GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
          new Insets(0, 0, 5, 5), 0, 0));

        //---- lblRefreshSuffix ----
        lblRefreshSuffix.setText(bundle.getString("Preferences.lblRefreshSuffix.text"));
        jPanel1.add(lblRefreshSuffix, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0,
          GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
          new Insets(0, 0, 5, 0), 0, 0));

        //---- cbxDetailLog ----
        cbxDetailLog.setText(bundle.getString("Preferences.cbxDetailLog.text"));
        cbxDetailLog.setToolTipText(bundle.getString("Preferences.cbxDetailLog.toolTipText"));
        cbxDetailLog.addActionListener(e -> cbxDetailLogActionPerformed(e));
        jPanel1.add(cbxDetailLog, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 5, 0, 5), 0, 0));
      }
      jTabbedPane1.addTab(bundle.getString("Preferences.jPanel1.tab.title"), jPanel1);

      //======== pnlFavrTagr ========
      {
        pnlFavrTagr.setLayout(new GridBagLayout());
        ((GridBagLayout)pnlFavrTagr.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
        ((GridBagLayout)pnlFavrTagr.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
        ((GridBagLayout)pnlFavrTagr.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
        ((GridBagLayout)pnlFavrTagr.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

        //---- lblFavrPrefix ----
        lblFavrPrefix.setText(bundle.getString("Preferences.lblFavrPrefix.text"));
        pnlFavrTagr.add(lblFavrPrefix, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));

        //---- cmbFavr ----
        cmbFavr.setModel(new DefaultComboBoxModel<>(new String[] {
          "every 10 favorites",
          "every 25 favorites",
          "every 100 favorites",
          "every 10 favorites up to 100, then every 100 favorites",
          "only 10, 25, 50, and 100 favorites",
          "custom interval"
        }));
        cmbFavr.addActionListener(e -> cmbFavrActionPerformed(e));
        pnlFavrTagr.add(cmbFavr, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(5, 0, 5, 5), 0, 0));

        //---- lblCustom ----
        lblCustom.setText(bundle.getString("Preferences.lblCustom.text"));
        pnlFavrTagr.add(lblCustom, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
          GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
          new Insets(0, 5, 5, 5), 0, 0));

        //---- txtCustom ----
        txtCustom.setToolTipText(bundle.getString("Preferences.txtCustom.toolTipText"));
        pnlFavrTagr.add(txtCustom, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 0, 5, 5), 0, 0));

        //---- btnCustomHelp ----
        btnCustomHelp.setIcon(new ImageIcon(getClass().getResource("/images/739-question-selected.png")));
        btnCustomHelp.addActionListener(e -> btnCustomHelpActionPerformed());
        pnlFavrTagr.add(btnCustomHelp, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 0, 5, 0), 0, 0));

        //---- lblTagType ----
        lblTagType.setText("Tag Type");
        lblTagType.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlFavrTagr.add(lblTagType, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 5, 0, 5), 0, 0));

        //---- cmbTagType ----
        cmbTagType.setModel(new DefaultComboBoxModel<>(new String[] {
          "Create using normal tag (favxx)",
          "Create using machine tag (favrtagr:count=xx)"
        }));
        cmbTagType.addActionListener(e -> cmbTagTypeActionPerformed(e));
        pnlFavrTagr.add(cmbTagType, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 0, 0, 5), 0, 0));

        //---- btnTagTypeHelp ----
        btnTagTypeHelp.setIcon(new ImageIcon(getClass().getResource("/images/739-question-selected.png")));
        btnTagTypeHelp.addActionListener(e -> btnTagTypeHelpActionPerformed(e));
        pnlFavrTagr.add(btnTagTypeHelp, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 0, 0, 0), 0, 0));
      }
      jTabbedPane1.addTab("FavrTagr", pnlFavrTagr);

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
          lblFlickrStatus.setIcon(new ImageIcon(getClass().getResource("/images/1262-flickr-toolbar.png")));
          pnlFlickr.add(lblFlickrStatus, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 5, 0, 0), 0, 0));

          //---- btnFlickr ----
          btnFlickr.setText(bundle.getString("Preferences.btnFlickr.text"));
          btnFlickr.addActionListener(e -> btnFlickrActionPerformed(e));
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
          lblTwitterStatus.setIcon(new ImageIcon(getClass().getResource("/images/1282-twitter-toolbar.png")));
          pnlTwitter.add(lblTwitterStatus, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 5, 5, 0), 0, 0));
          pnlTwitter.add(lblMessage, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 5), 0, 0));

          //---- btnTwitter ----
          btnTwitter.setText(bundle.getString("Preferences.btnTwitter.text"));
          btnTwitter.addActionListener(e -> btnTwitterActionPerformed(e));
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
        ((GridBagLayout)pnlProxy.getLayout()).rowHeights = new int[] {0, 0, 0};
        ((GridBagLayout)pnlProxy.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
        ((GridBagLayout)pnlProxy.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

        //---- cbxProxy ----
        cbxProxy.setText(bundle.getString("Preferences.cbxProxy.text"));
        cbxProxy.addActionListener(e -> cbxProxyActionPerformed(e));
        pnlProxy.add(cbxProxy, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
          GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
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
        pnlProxy.add(panel2, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
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
        cbxAutoRefresh.addActionListener(e -> cbxAutoRefreshActionPerformed());
        pnlAutoRefresh.add(cbxAutoRefresh, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(5, 5, 5, 5), 0, 0));

        //---- cbxExitAfter ----
        cbxExitAfter.setText(bundle.getString("Preferences.cbxExitAfter.text"));
        pnlAutoRefresh.add(cbxExitAfter, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 5, 5, 5), 0, 0));

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
      btnOK.addActionListener(e -> btnOKActionPerformed(e));
      panel1.add(btnOK);
    }
    contentPane.add(panel1, BorderLayout.SOUTH);
    pack();
    setLocationRelativeTo(null);
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

  private boolean validateCustomFavrInterval() {
    // have not selected custom interval, so just move on
    if (this.cmbFavr.getSelectedIndex() != 5) {
      return true;
    }

    boolean valid = true;
    String interval = this.txtCustom.getText().replaceAll(" ", "");
    if (interval.isEmpty()) {
      valid = false;
      JOptionPane.showMessageDialog(this,
          resourceBundle.getString("Preferences.emptyinterval.message"),
          resourceBundle.getString("Preferences.emptyinterval.title"),
          JOptionPane.ERROR_MESSAGE);
    } else {
      List<Integer> list = new ArrayList<>();
      for (String s : interval.split(",")) {
        try {
          Integer count = Integer.parseInt(s);
          // add each fave count to a list, removing duplicates
          if (count > 0) {
            if (!list.contains(count)) {
              list.add(count);
            }
          }
        } catch (Exception e) {
          logger.warn("Bad content in custom interval field: " + s, e);
          valid = false;
        }
      }
      Collections.sort(list);
      if (list.isEmpty()) {
        valid = false;
      }

      if (valid) {
        // create a new interval string, and check the length
        StringBuilder sb = new StringBuilder();
        for (Integer i : list) {
          sb.append(i).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        String newInterval = sb.toString();
        if (newInterval.length() > 1022) {
          valid = false;
          JOptionPane.showMessageDialog(this,
              resourceBundle.getString("Preferences.intervaltoolong.message"),
              resourceBundle.getString("Preferences.intervaltoolong.title"),
              JOptionPane.ERROR_MESSAGE);
        } else {
          LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_FAVRTAGR_INTERVAL, "c," + newInterval);
        }
      } else {
        JOptionPane.showMessageDialog(this,
            resourceBundle.getString("Preferences.badinterval.message"),
            resourceBundle.getString("Preferences.badinterval.title"),
            JOptionPane.ERROR_MESSAGE);
      }
    }
    return valid;
  }


  private void cbxUpdateActionPerformed(java.awt.event.ActionEvent evt) {
    LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_CHECK_FOR_UPDATE, DAOHelper.booleanToString(this.cbxUpdate.isSelected()));
  }

  private void cmbFavrActionPerformed(java.awt.event.ActionEvent evt) {
    switch (this.cmbFavr.getSelectedIndex()) {
      case 0:
        this.displayCustomIntervalFields(false);
        LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_FAVRTAGR_INTERVAL, "10");
        break;
      case 1:
        this.displayCustomIntervalFields(false);
        LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_FAVRTAGR_INTERVAL, "25");
        break;
      case 2:
        this.displayCustomIntervalFields(false);
        LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_FAVRTAGR_INTERVAL, "100");
        break;
      case 3:
        this.displayCustomIntervalFields(false);
        LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_FAVRTAGR_INTERVAL, "0");
        break;
      case 4:
        this.displayCustomIntervalFields(false);
        LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_FAVRTAGR_INTERVAL, "4");
        break;
      case 5:
        this.displayCustomIntervalFields(true);
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
        MainWindow.getMainWindow().updateMasterList(null);
      } catch (Exception e) {
        logger.warn("Could not update the list.", e);
      }
    }
  }//GEN-LAST:event_formWindowClosed


  /*
   * Respond to clicks on the "Use Proxy" checkbox.
   *
   * <p>The text entryfields will be enabled/disabled based on the state of
   * the checkbox.</p>
   */
  private void cbxProxyActionPerformed(java.awt.event.ActionEvent evt) {
    setProxyComponentStates();
  }

  private void setProxyComponentStates() {
    this.txtProxyHost.setEnabled(this.cbxProxy.isSelected());
    this.txtProxyPass.setEnabled(this.cbxProxy.isSelected());
    this.txtProxyPort.setEnabled(this.cbxProxy.isSelected());
    this.txtProxyUser.setEnabled(this.cbxProxy.isSelected());
  }

  private void cbxAddManagedActionPerformed(java.awt.event.ActionEvent evt) {
    LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_ADD_MANAGED, DAOHelper.booleanToString(this.cbxAddManaged.isSelected()));
  }

  private void cbxDetailLogActionPerformed(java.awt.event.ActionEvent evt) {
    LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_DETAIL_LOG, DAOHelper.booleanToString(this.cbxDetailLog.isSelected()));
    if (this.cbxDetailLog.isSelected()) {
      JinxFactory.getInstance().setLogger(new MyJinxLogger());
    } else {
      JinxFactory.getInstance().setLogger(null);
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

  private void displayCustomIntervalFields(boolean display) {
    this.lblCustom.setVisible(display);
    this.txtCustom.setVisible(display);
    this.btnCustomHelp.setVisible(display);
  }


   void setTabIndex(int index) {
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
  private JCheckBox cbxUpdate;
  private JLabel lblRefreshPrefix;
  private JComboBox<String> cmbRefresh;
  private JLabel lblRefreshSuffix;
  private JCheckBox cbxDetailLog;
  private JPanel pnlFavrTagr;
  private JLabel lblFavrPrefix;
  private JComboBox<String> cmbFavr;
  private JLabel lblCustom;
  private JTextField txtCustom;
  private JButton btnCustomHelp;
  private JLabel lblTagType;
  private JComboBox<String> cmbTagType;
  private JButton btnTagTypeHelp;
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
