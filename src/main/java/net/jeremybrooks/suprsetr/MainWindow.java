/*
 *  SuprSetr is Copyright 2010-2020 by Jeremy Brooks
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

import net.jeremybrooks.suprsetr.SetEditor.EditorMode;
import net.jeremybrooks.suprsetr.dao.DAOHelper;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import net.jeremybrooks.suprsetr.dao.PhotosetDAO;
import net.jeremybrooks.suprsetr.flickr.FlickrHelper;
import net.jeremybrooks.suprsetr.tutorial.Tutorial;
import net.jeremybrooks.suprsetr.utils.FilenameContainsFilter;
import net.jeremybrooks.suprsetr.utils.SSUtils;
import net.jeremybrooks.suprsetr.workers.AddPhotosetWorker;
import net.jeremybrooks.suprsetr.workers.DatabaseBackupWorker;
import net.jeremybrooks.suprsetr.workers.DatabaseRestoreWorker;
import net.jeremybrooks.suprsetr.workers.DeletePhotosetWorker;
import net.jeremybrooks.suprsetr.workers.FavDeleteWorker;
import net.jeremybrooks.suprsetr.workers.FavrTagrWorker;
import net.jeremybrooks.suprsetr.workers.FilterSetListWorker;
import net.jeremybrooks.suprsetr.workers.LoadFlickrSetsWorker;
import net.jeremybrooks.suprsetr.workers.RefreshPhotosetWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * @author Jeremy Brooks
 */
public class MainWindow extends javax.swing.JFrame {

  private static final long serialVersionUID = 5381447617741236893L;

  private Logger logger = LogManager.getLogger(MainWindow.class);

  /* List model. */
  private DefaultListModel listModel = new DefaultListModel();

  /* Master list. */
  private List<SSPhotoset> masterList;

  private static MainWindow theWindow;

  private LogWindow logWindow = null;

  /* Timer used to trigger filtering. */
  private Timer filterTimer = null;

  private java.util.Timer autoRefreshTimer = null;

  private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.mainwindow");

  /*
   * Creates new form MainWindow
   */
  private void btnBrowserActionPerformed() {
    this.doOpenInBrowserAction();
  }


  public MainWindow() {
    initComponents();

    switch (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_LIST_SORT_ORDER)) {
      case SSConstants.LIST_SORT_ATOZ:
        this.mnuOrderAlpha.setSelected(true);
        break;
      case SSConstants.LIST_SORT_ZTOA:
        this.mnuOrderAlphaDesc.setSelected(true);
        break;
      case SSConstants.LIST_SORT_VIEW_HIGHLOW:
        this.mnuOrderHighLow.setSelected(true);
        break;
      case SSConstants.LIST_SORT_VIEW_LOWHIGH:
        this.mnuOrderLowHigh.setSelected(true);
        break;
      default:
        this.mnuOrderAlpha.setSelected(true);
        break;
    }
    this.updateStatusBar();

    this.mnuHideUnmanaged.setSelected(DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_HIDE_UNMANAGED)));
    this.mnuHideManaged.setSelected(DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_HIDE_MANAGED)));
    this.mnuCaseSensitive.setSelected(DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_CASE_SENSITIVE)));

    try {
      setBounds(
          Integer.parseInt(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_X)),
          Integer.parseInt(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_Y)),
          Integer.parseInt(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_WIDTH)),
          Integer.parseInt(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_HEIGHT)));
    } catch (Exception e) {
      // ignore
    }

    // remove some menus that live in other places on a Mac
    if (System.getProperty("os.name").contains("Mac")) {
      this.mnuFile.remove(this.mnuQuit);
      this.mnuEdit.remove(this.mnuPreferences);
      this.mnuHelp.remove(this.mnuAbout);
    }

    MainWindow.theWindow = this;

    this.logWindow = new LogWindow();
    LogWindow.addLogMessage("Started up at " + new Date());

    this.filterTimer = new Timer(500, e -> doFilter());
    this.filterTimer.setRepeats(false);
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
    jMenuBar1 = new JMenuBar();
    mnuFile = new JMenu();
    mnuBrowser = new JMenuItem();
    mnuBackup = new JMenuItem();
    mnuRestore = new JMenuItem();
    mnuQuit = new JMenuItem();
    mnuEdit = new JMenu();
    mnuCreateSet = new JMenuItem();
    mnuEditSet = new JMenuItem();
    mnuDeleteSet = new JMenuItem();
    mnuRefreshSet = new JMenuItem();
    mnuRefreshAll = new JMenuItem();
    mnuPreferences = new JMenuItem();
    mnuView = new JMenu();
    mnuHideUnmanaged = new JCheckBoxMenuItem();
    mnuHideManaged = new JCheckBoxMenuItem();
    mnuCaseSensitive = new JCheckBoxMenuItem();
    mnuOrderAlpha = new JRadioButtonMenuItem();
    mnuOrderAlphaDesc = new JRadioButtonMenuItem();
    mnuOrderHighLow = new JRadioButtonMenuItem();
    mnuOrderLowHigh = new JRadioButtonMenuItem();
    mnuTools = new JMenu();
    mnuFavr = new JMenuItem();
    mnuClearFave = new JMenuItem();
    mnuSetOrder = new JMenuItem();
    mnuLogs = new JMenuItem();
    mnuLogWindow = new JMenuItem();
    mnuHelp = new JMenu();
    mnuAbout = new JMenuItem();
    mnuTutorial = new JMenuItem();
    mnuSSHelp = new JMenuItem();
    mnuCheckUpdates = new JMenuItem();
    jToolBar1 = new JToolBar();
    btnAddSet = new JButton();
    btnEditSet = new JButton();
    btnDeleteSet = new JButton();
    btnRefreshSet = new JButton();
    btnRefreshAll = new JButton();
    btnBrowser = new JButton();
    jLabel1 = new JLabel();
    txtFilter = new JTextField();
    jScrollPane1 = new JScrollPane();
    jList1 = new JList();
    lblStatus = new JLabel();
    mnuPopup = new JPopupMenu();
    mnuPopupCreate = new JMenuItem();
    mnuPopupEdit = new JMenuItem();
    mnuPopupDelete = new JMenuItem();
    mnuPopupRefresh = new JMenuItem();
    mnuPopupOpen = new JMenuItem();

    //======== this ========
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setTitle(bundle.getString("MainWindow.this.title"));
    setIconImage(new ImageIcon(getClass().getResource("/images/s16.png")).getImage());
    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());

    //======== jMenuBar1 ========
    {

      //======== mnuFile ========
      {
        mnuFile.setText(bundle.getString("MainWindow.mnuFile.text"));

        //---- mnuBrowser ----
        mnuBrowser.setIcon(new ImageIcon(getClass().getResource("/images/786-browser-toolbar-22x22.png")));
        mnuBrowser.setText(bundle.getString("MainWindow.mnuBrowser.text"));
        mnuBrowser.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        mnuBrowser.addActionListener(e -> mnuBrowserActionPerformed());
        mnuFile.add(mnuBrowser);

        //---- mnuBackup ----
        mnuBackup.setIcon(new ImageIcon(getClass().getResource("/images/1052-database-toolbar-22x22.png")));
        mnuBackup.setText(bundle.getString("MainWindow.mnuBackup.text"));
        mnuBackup.addActionListener(e -> mnuBackupActionPerformed());
        mnuFile.add(mnuBackup);

        //---- mnuRestore ----
        mnuRestore.setIcon(new ImageIcon(getClass().getResource("/images/1052-database-toolbar-22x22.png")));
        mnuRestore.setText(bundle.getString("MainWindow.mnuRestore.text"));
        mnuRestore.addActionListener(e -> mnuRestoreActionPerformed());
        mnuFile.add(mnuRestore);
        mnuFile.addSeparator();

        //---- mnuQuit ----
        mnuQuit.setIcon(new ImageIcon(getClass().getResource("/images/602-exit.png")));
        mnuQuit.setText(bundle.getString("MainWindow.mnuQuit.text"));
        mnuQuit.addActionListener(e -> mnuQuitActionPerformed());
        mnuFile.add(mnuQuit);
      }
      jMenuBar1.add(mnuFile);

      //======== mnuEdit ========
      {
        mnuEdit.setText(bundle.getString("MainWindow.mnuEdit.text"));

        //---- mnuCreateSet ----
        mnuCreateSet.setIcon(new ImageIcon(getClass().getResource("/images/746-plus-circle-toolbar.png")));
        mnuCreateSet.setText(bundle.getString("MainWindow.mnuCreateSet.text"));
        mnuCreateSet.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        mnuCreateSet.addActionListener(e -> mnuCreateSetActionPerformed());
        mnuEdit.add(mnuCreateSet);

        //---- mnuEditSet ----
        mnuEditSet.setIcon(new ImageIcon(getClass().getResource("/images/830-pencil-toolbar.png")));
        mnuEditSet.setText(bundle.getString("MainWindow.mnuEditSet.text"));
        mnuEditSet.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        mnuEditSet.addActionListener(e -> mnuEditSetActionPerformed());
        mnuEdit.add(mnuEditSet);

        //---- mnuDeleteSet ----
        mnuDeleteSet.setIcon(new ImageIcon(getClass().getResource("/images/711-trash-toolbar-22x22.png")));
        mnuDeleteSet.setText(bundle.getString("MainWindow.mnuDeleteSet.text"));
        mnuDeleteSet.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        mnuDeleteSet.addActionListener(e -> mnuDeleteSetActionPerformed());
        mnuEdit.add(mnuDeleteSet);

        //---- mnuRefreshSet ----
        mnuRefreshSet.setIcon(new ImageIcon(getClass().getResource("/images/759-refresh-2-toolbar.png")));
        mnuRefreshSet.setText(bundle.getString("MainWindow.mnuRefreshSet.text"));
        mnuRefreshSet.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        mnuRefreshSet.addActionListener(e -> mnuRefreshSetActionPerformed());
        mnuEdit.add(mnuRefreshSet);

        //---- mnuRefreshAll ----
        mnuRefreshAll.setIcon(new ImageIcon(getClass().getResource("/images/759-refresh-2-toolbar-infinity.png")));
        mnuRefreshAll.setText(bundle.getString("MainWindow.mnuRefreshAll.text"));
        mnuRefreshAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK));
        mnuRefreshAll.addActionListener(e -> mnuRefreshAllActionPerformed());
        mnuEdit.add(mnuRefreshAll);

        //---- mnuPreferences ----
        mnuPreferences.setIcon(new ImageIcon(getClass().getResource("/images/740-gear-toolbar.png")));
        mnuPreferences.setText(bundle.getString("MainWindow.mnuPreferences.text"));
        mnuPreferences.addActionListener(e -> mnuPreferencesActionPerformed());
        mnuEdit.add(mnuPreferences);
      }
      jMenuBar1.add(mnuEdit);

      //======== mnuView ========
      {
        mnuView.setText(bundle.getString("MainWindow.mnuView.text"));

        //---- mnuHideUnmanaged ----
        mnuHideUnmanaged.setText(bundle.getString("MainWindow.mnuHideUnmanaged.text"));
        mnuHideUnmanaged.addActionListener(e -> mnuHideUnmanagedActionPerformed());
        mnuView.add(mnuHideUnmanaged);

        //---- mnuHideManaged ----
        mnuHideManaged.setText(bundle.getString("MainWindow.mnuHideManaged.text"));
        mnuHideManaged.addActionListener(e -> mnuHideManagedActionPerformed());
        mnuView.add(mnuHideManaged);
        mnuView.addSeparator();

        //---- mnuCaseSensitive ----
        mnuCaseSensitive.setText(bundle.getString("MainWindow.mnuCaseSensitive.text"));
        mnuCaseSensitive.addActionListener(e -> mnuCaseSensitiveActionPerformed());
        mnuView.add(mnuCaseSensitive);
        mnuView.addSeparator();

        //---- mnuOrderAlpha ----
        mnuOrderAlpha.setText(bundle.getString("MainWindow.mnuOrderAlpha.text"));
        mnuOrderAlpha.addActionListener(e -> mnuOrderAlphaActionPerformed());
        mnuView.add(mnuOrderAlpha);

        //---- mnuOrderAlphaDesc ----
        mnuOrderAlphaDesc.setText(bundle.getString("MainWindow.mnuOrderAlphaDesc.text"));
        mnuOrderAlphaDesc.addActionListener(e -> mnuOrderAlphaDescActionPerformed());
        mnuView.add(mnuOrderAlphaDesc);

        //---- mnuOrderHighLow ----
        mnuOrderHighLow.setText(bundle.getString("MainWindow.mnuOrderHighLow.text"));
        mnuOrderHighLow.addActionListener(e -> mnuOrderHighLowActionPerformed());
        mnuView.add(mnuOrderHighLow);

        //---- mnuOrderLowHigh ----
        mnuOrderLowHigh.setText(bundle.getString("MainWindow.mnuOrderLowHigh.text"));
        mnuOrderLowHigh.addActionListener(e -> mnuOrderLowHighActionPerformed());
        mnuView.add(mnuOrderLowHigh);
        mnuView.addSeparator();
      }
      jMenuBar1.add(mnuView);

      //======== mnuTools ========
      {
        mnuTools.setText(bundle.getString("MainWindow.mnuTools.text"));

        //---- mnuFavr ----
        mnuFavr.setIcon(new ImageIcon(getClass().getResource("/images/909-tags-toolbar.png")));
        mnuFavr.setText(bundle.getString("MainWindow.mnuFavr.text"));
        mnuFavr.addActionListener(e -> mnuFavrActionPerformed());
        mnuTools.add(mnuFavr);

        //---- mnuClearFave ----
        mnuClearFave.setIcon(new ImageIcon(getClass().getResource("/images/909-tags-toolbar-x.png")));
        mnuClearFave.setText(bundle.getString("MainWindow.mnuClearFave.text"));
        mnuClearFave.addActionListener(e -> mnuClearFaveActionPerformed());
        mnuTools.add(mnuClearFave);

        //---- mnuSetOrder ----
        mnuSetOrder.setIcon(new ImageIcon(getClass().getResource("/images/707-albums-toolbar-22x22.png")));
        mnuSetOrder.setText(bundle.getString("MainWindow.mnuSetOrder.text"));
        mnuSetOrder.addActionListener(e -> mnuSetOrderActionPerformed());
        mnuTools.add(mnuSetOrder);

        //---- mnuLogs ----
        mnuLogs.setIcon(new ImageIcon(getClass().getResource("/images/797-archive-toolbar-22x22.png")));
        mnuLogs.setText(bundle.getString("MainWindow.mnuLogs.text"));
        mnuLogs.addActionListener(e -> mnuLogsActionPerformed());
        mnuTools.add(mnuLogs);

        //---- mnuLogWindow ----
        mnuLogWindow.setText(bundle.getString("MainWindow.mnuLogWindow.text"));
        mnuLogWindow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        mnuLogWindow.setIcon(new ImageIcon(getClass().getResource("/images/1072-terminal-toolbar-22x22.png")));
        mnuLogWindow.addActionListener(e -> mnuLogWindowActionPerformed());
        mnuTools.add(mnuLogWindow);
      }
      jMenuBar1.add(mnuTools);

      //======== mnuHelp ========
      {
        mnuHelp.setText(bundle.getString("MainWindow.mnuHelp.text"));

        //---- mnuAbout ----
        mnuAbout.setIcon(new ImageIcon(getClass().getResource("/images/739-question-toolbar.png")));
        mnuAbout.setText(bundle.getString("MainWindow.mnuAbout.text"));
        mnuAbout.addActionListener(e -> mnuAboutActionPerformed());
        mnuHelp.add(mnuAbout);

        //---- mnuTutorial ----
        mnuTutorial.setIcon(new ImageIcon(getClass().getResource("/images/724-info-toolbar.png")));
        mnuTutorial.setText(bundle.getString("MainWindow.mnuTutorial.text"));
        mnuTutorial.addActionListener(e -> mnuTutorialActionPerformed());
        mnuHelp.add(mnuTutorial);

        //---- mnuSSHelp ----
        mnuSSHelp.setIcon(new ImageIcon(getClass().getResource("/images/739-question-toolbar.png")));
        mnuSSHelp.setText(bundle.getString("MainWindow.mnuSSHelp.text"));
        mnuSSHelp.addActionListener(e -> mnuSSHelpActionPerformed());
        mnuHelp.add(mnuSSHelp);

        //---- mnuCheckUpdates ----
        mnuCheckUpdates.setIcon(new ImageIcon(getClass().getResource("/images/55-network-22x22.png")));
        mnuCheckUpdates.setText(bundle.getString("MainWindow.mnuCheckUpdates.text"));
        mnuCheckUpdates.addActionListener(e -> mnuCheckUpdatesActionPerformed());
        mnuHelp.add(mnuCheckUpdates);
      }
      jMenuBar1.add(mnuHelp);
    }
    setJMenuBar(jMenuBar1);

    //======== jToolBar1 ========
    {
      jToolBar1.setRollover(true);

      //---- btnAddSet ----
      btnAddSet.setIcon(new ImageIcon(getClass().getResource("/images/746-plus-circle-toolbar.png")));
      btnAddSet.setToolTipText(bundle.getString("MainWindow.btnAddSet.toolTipText"));
      btnAddSet.setFocusable(false);
      btnAddSet.setHorizontalTextPosition(SwingConstants.CENTER);
      btnAddSet.setVerticalTextPosition(SwingConstants.BOTTOM);
      btnAddSet.addActionListener(e -> btnAddSetActionPerformed());
      jToolBar1.add(btnAddSet);

      //---- btnEditSet ----
      btnEditSet.setIcon(new ImageIcon(getClass().getResource("/images/830-pencil-toolbar.png")));
      btnEditSet.setToolTipText(bundle.getString("MainWindow.btnEditSet.toolTipText"));
      btnEditSet.setFocusable(false);
      btnEditSet.setHorizontalTextPosition(SwingConstants.CENTER);
      btnEditSet.setVerticalTextPosition(SwingConstants.BOTTOM);
      btnEditSet.addActionListener(e -> btnEditSetActionPerformed());
      jToolBar1.add(btnEditSet);

      //---- btnDeleteSet ----
      btnDeleteSet.setIcon(new ImageIcon(getClass().getResource("/images/711-trash-toolbar-22x22.png")));
      btnDeleteSet.setToolTipText(bundle.getString("MainWindow.btnDeleteSet.toolTipText"));
      btnDeleteSet.setFocusable(false);
      btnDeleteSet.setHorizontalTextPosition(SwingConstants.CENTER);
      btnDeleteSet.setVerticalTextPosition(SwingConstants.BOTTOM);
      btnDeleteSet.addActionListener(e -> btnDeleteSetActionPerformed());
      jToolBar1.add(btnDeleteSet);

      //---- btnRefreshSet ----
      btnRefreshSet.setIcon(new ImageIcon(getClass().getResource("/images/759-refresh-2-toolbar.png")));
      btnRefreshSet.setToolTipText(bundle.getString("MainWindow.btnRefreshSet.toolTipText"));
      btnRefreshSet.setFocusable(false);
      btnRefreshSet.setHorizontalTextPosition(SwingConstants.CENTER);
      btnRefreshSet.setVerticalTextPosition(SwingConstants.BOTTOM);
      btnRefreshSet.addActionListener(e -> btnRefreshSetActionPerformed());
      jToolBar1.add(btnRefreshSet);

      //---- btnRefreshAll ----
      btnRefreshAll.setIcon(new ImageIcon(getClass().getResource("/images/759-refresh-2-toolbar-infinity.png")));
      btnRefreshAll.setToolTipText(bundle.getString("MainWindow.btnRefreshAll.toolTipText"));
      btnRefreshAll.setFocusable(false);
      btnRefreshAll.setHorizontalTextPosition(SwingConstants.CENTER);
      btnRefreshAll.setVerticalTextPosition(SwingConstants.BOTTOM);
      btnRefreshAll.addActionListener(e -> btnRefreshAllActionPerformed());
      jToolBar1.add(btnRefreshAll);

      //---- btnBrowser ----
      btnBrowser.setIcon(new ImageIcon(getClass().getResource("/images/786-browser-toolbar-22x22.png")));
      btnBrowser.setToolTipText(bundle.getString("MainWindow.btnBrowser.toolTipText"));
      btnBrowser.addActionListener(e -> btnBrowserActionPerformed());
      jToolBar1.add(btnBrowser);
      jToolBar1.addSeparator();

      //---- jLabel1 ----
      jLabel1.setText("Filter");
      jToolBar1.add(jLabel1);

      //---- txtFilter ----
      txtFilter.setToolTipText(bundle.getString("MainWindow.txtFilter.toolTipText"));
      txtFilter.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
          txtFilterFocusGained();
        }
      });
      txtFilter.addKeyListener(new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent e) {
          txtFilterKeyTyped(e);
        }
      });
      jToolBar1.add(txtFilter);
    }
    contentPane.add(jToolBar1, BorderLayout.NORTH);

    //======== jScrollPane1 ========
    {

      //---- jList1 ----
      jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      jList1.setCellRenderer(null);
      jList1.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          jList1MousePressed(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
          jList1MouseReleased(e);
        }
      });
      this.jList1.setModel(this.listModel);
      this.jList1.setCellRenderer(new SetListRenderer());
      jScrollPane1.setViewportView(jList1);
    }
    contentPane.add(jScrollPane1, BorderLayout.CENTER);

    //---- lblStatus ----
    lblStatus.setText(bundle.getString("MainWindow.lblStatus.text"));
    contentPane.add(lblStatus, BorderLayout.SOUTH);
    setSize(488, 522);
    setLocationRelativeTo(null);

    //======== mnuPopup ========
    {
      mnuPopup.setLayout(new BoxLayout(mnuPopup, BoxLayout.Y_AXIS));

      //---- mnuPopupCreate ----
      mnuPopupCreate.setIcon(new ImageIcon(getClass().getResource("/images/746-plus-circle-toolbar.png")));
      mnuPopupCreate.setText(bundle.getString("MainWindow.mnuPopupCreate.text"));
      mnuPopupCreate.addActionListener(e -> mnuPopupCreateActionPerformed());
      mnuPopup.add(mnuPopupCreate);

      //---- mnuPopupEdit ----
      mnuPopupEdit.setIcon(new ImageIcon(getClass().getResource("/images/830-pencil-toolbar.png")));
      mnuPopupEdit.setText(bundle.getString("MainWindow.mnuPopupEdit.text"));
      mnuPopupEdit.addActionListener(e -> mnuPopupEditActionPerformed());
      mnuPopup.add(mnuPopupEdit);

      //---- mnuPopupDelete ----
      mnuPopupDelete.setIcon(new ImageIcon(getClass().getResource("/images/711-trash-toolbar-22x22.png")));
      mnuPopupDelete.setText(bundle.getString("MainWindow.mnuPopupDelete.text"));
      mnuPopupDelete.addActionListener(e -> mnuPopupDeleteActionPerformed());
      mnuPopup.add(mnuPopupDelete);

      //---- mnuPopupRefresh ----
      mnuPopupRefresh.setIcon(new ImageIcon(getClass().getResource("/images/759-refresh-2-toolbar.png")));
      mnuPopupRefresh.setText(bundle.getString("MainWindow.mnuPopupRefresh.text"));
      mnuPopupRefresh.addActionListener(e -> mnuPopupRefreshActionPerformed());
      mnuPopup.add(mnuPopupRefresh);

      //---- mnuPopupOpen ----
      mnuPopupOpen.setIcon(new ImageIcon(getClass().getResource("/images/786-browser-toolbar-22x22.png")));
      mnuPopupOpen.setText(bundle.getString("MainWindow.mnuPopupOpen.text"));
      mnuPopupOpen.addActionListener(e -> mnuPopupOpenActionPerformed());
      mnuPopup.add(mnuPopupOpen);
    }

    //---- buttonGroup1 ----
    ButtonGroup buttonGroup1 = new ButtonGroup();
    buttonGroup1.add(mnuOrderAlpha);
    buttonGroup1.add(mnuOrderAlphaDesc);
    buttonGroup1.add(mnuOrderHighLow);
    buttonGroup1.add(mnuOrderLowHigh);
  }// </editor-fold>//GEN-END:initComponents


  private void mnuQuitActionPerformed() {
    this.confirmQuit();
  }

  private void mnuCreateSetActionPerformed() {
    this.doCreateSetAction();
  }

  private void mnuEditSetActionPerformed() {
    this.doEditSetAction();
  }


  private void mnuDeleteSetActionPerformed() {
    this.doDeleteSetAction();
  }

  private void mnuRefreshSetActionPerformed() {
    this.doRefreshSetAction();
  }

  private void mnuBrowserActionPerformed() {
    this.doOpenInBrowserAction();
  }

  private void mnuRefreshAllActionPerformed() {
    SSPhotoset ssPhotoset;
    List<SSPhotoset> list = new ArrayList<>();
    // Put all sets that are ready for refresh in a list
    for (int i = 0; i < this.listModel.getSize(); i++) {
      ssPhotoset = (SSPhotoset) this.listModel.getElementAt(i);
      if (ssPhotoset.isManaged() && SSUtils.readyForUpdate(ssPhotoset.getLastRefreshDate())) {
        list.add(ssPhotoset);
      }
    }

    // If the list is empty, warn the user
    if (list.isEmpty()) {
      int confirm = JOptionPane.showConfirmDialog(this,
          resourceBundle.getString("MainWindow.dialog.confirmRefresh.message"),
          resourceBundle.getString("MainWindow.dialog.confirmRefresh.title"),
          JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE);
      if (confirm == JOptionPane.YES_OPTION) {

        // Put all managed sets in the list
        for (int i = 0; i < this.listModel.getSize(); i++) {
          ssPhotoset = (SSPhotoset) this.listModel.getElementAt(i);
          if (ssPhotoset.isManaged()) {
            list.add(ssPhotoset);
          }
        }
      }
    }

    // send the list to the worker
    // if the list is empty, nothing will happen, so there's no need
    // to check here
    this.executeRefreshSetWorker(list, false);
  }


  private void mnuOrderAlphaActionPerformed() {
    LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_LIST_SORT_ORDER, SSConstants.LIST_SORT_ATOZ);
    reloadListDueToSortMenuSelection();
  }

  private void mnuOrderAlphaDescActionPerformed() {
    LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_LIST_SORT_ORDER, SSConstants.LIST_SORT_ZTOA);
    reloadListDueToSortMenuSelection();
  }

  private void mnuOrderHighLowActionPerformed() {
    LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_LIST_SORT_ORDER, SSConstants.LIST_SORT_VIEW_HIGHLOW);
    reloadListDueToSortMenuSelection();
  }

  private void mnuOrderLowHighActionPerformed() {
    LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_LIST_SORT_ORDER, SSConstants.LIST_SORT_VIEW_LOWHIGH);
    reloadListDueToSortMenuSelection();
  }

  /*
   * User has selected one of the menu items that affect list sort order.
   * Determine the selected list item (if any) and then call updateMasterList.
   */
  private void reloadListDueToSortMenuSelection() {
    String id = null;
    if (jList1.getSelectedIndex() != -1) {
      id = ((SSPhotoset) jList1.getSelectedValue()).getPhotosetId();
    }
    this.updateMasterList(id);
  }


  private void doCreateSetAction() {
    SSPhotoset ssp = new SSPhotoset();
    ssp.setTagMatchMode(SSConstants.TAG_MATCH_MODE_NONE);
    ssp.setTags("");
    ssp.setMachineTagMatchMode(SSConstants.TAG_MATCH_MODE_NONE);
    ssp.setMachineTags("");
    ssp.setTweetTemplate(SSConstants.DEFAULT_TWEET_TEMPLATE);

    (new SetEditor(this, EditorMode.CREATE, ssp)).setVisible(true);
  }


  private void doEditSetAction() {
    int index = this.jList1.getSelectedIndex();
    if (index == -1) {
      JOptionPane.showMessageDialog(this,
          resourceBundle.getString("MainWindow.dialog.editset.message"),
          resourceBundle.getString("MainWindow.dialog.editset.title"),
          JOptionPane.INFORMATION_MESSAGE);
    } else {
      SSPhotoset set = (SSPhotoset) this.listModel.get(index);
      (new SetEditor(this, EditorMode.EDIT, set)).setVisible(true);
    }
  }


  private void doDeleteSetAction() {
    int index = jList1.getSelectedIndex();
    if (index != -1) {
      SSPhotoset ssPhotoset = (SSPhotoset) listModel.get(index);
      int confirm = JOptionPane.showConfirmDialog(this,
          resourceBundle.getString("MainWindow.dialog.deleteset.message1") +
              "\"" + ssPhotoset.getTitle() + "\"\n" +
              resourceBundle.getString("MainWindow.dialog.deleteset.message2"),
          resourceBundle.getString("MainWindow.dialog.deleteset.title"),
          JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE);
      if (confirm == JOptionPane.YES_OPTION) {
        try {
          this.executeDeleteSetWorker(ssPhotoset);
        } catch (Exception e) {
          logger.error("Error deleting photoset " + ssPhotoset, e);
          JOptionPane.showMessageDialog(this,
              resourceBundle.getString("MainWindow.dialog.deleteset.error.message") + e.getMessage(),
              resourceBundle.getString("MainWindow.dialog.deleteset.error.title"),
              JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }

  private void doRefreshSetAction() {
    int index = jList1.getSelectedIndex();
    int confirm = JOptionPane.YES_OPTION;
    if (index != -1) {
      SSPhotoset ssPhotoset = (SSPhotoset) listModel.get(index);
      // to update, set must be managed and ready for refresh
      // if not ready for refresh, give user option to force refresh
      if (!ssPhotoset.isManaged()) {
        confirm = JOptionPane.NO_OPTION;
        JOptionPane.showMessageDialog(this,
            resourceBundle.getString("MainWindow.dialog.unmanagedSet.message1") +
                " '" + ssPhotoset.getTitle() + "'\n" +
                resourceBundle.getString("MainWindow.dialog.unmanagedSet.message2"),
            resourceBundle.getString("MainWindow.dialog.unmanagedSet.title"),
            JOptionPane.INFORMATION_MESSAGE);
      } else if (!SSUtils.readyForUpdate(ssPhotoset.getLastRefreshDate())) {
        confirm = JOptionPane.showConfirmDialog(this,
            resourceBundle.getString("MainWindow.dialog.recentSet.message"),
            resourceBundle.getString("MainWindow.dialog.recentSet.title"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
      }
      if (confirm == JOptionPane.YES_OPTION) {
        this.executeRefreshSetWorker(ssPhotoset);
      }
    } else {
      JOptionPane.showMessageDialog(this,
          resourceBundle.getString("MainWindow.dialog.selectSet.message"),
          resourceBundle.getString("MainWindow.dialog.selectSet.title"),
          JOptionPane.INFORMATION_MESSAGE);
    }
  }

  private void doOpenInBrowserAction() {
    int index = jList1.getSelectedIndex();
    if (index != -1) {
      SSPhotoset ssPhotoset = (SSPhotoset) listModel.get(index);
      try {
        Desktop.getDesktop().browse(new URL(ssPhotoset.getUrl()).toURI());
      } catch (Exception e) {
        logger.error("COULD NOT LAUNCH URL " + ssPhotoset.getUrl() + " IN BROWSER.", e);
        JOptionPane.showMessageDialog(this,
            resourceBundle.getString("MainWindow.dialog.browserError.message"),
            resourceBundle.getString("MainWindow.dialog.browserError.title"),
            JOptionPane.ERROR_MESSAGE);
      }
    } else {
      JOptionPane.showMessageDialog(this,
          resourceBundle.getString("MainWindow.dialog.selectSet.message"),
          resourceBundle.getString("MainWindow.dialog.selectSet.title"),
          JOptionPane.INFORMATION_MESSAGE);
    }
  }

  private void mnuPreferencesActionPerformed() {
    new Preferences(this, true).setVisible(true);
  }

  private void mnuLogsActionPerformed() {
    JFileChooser jfc = new JFileChooser();
    jfc.setDialogTitle(resourceBundle.getString("MainWindow.ziplogs.dialog.title.text"));
    jfc.setDialogType(JFileChooser.OPEN_DIALOG);
    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
      String filename = "suprsetr_logs-"
          + FlickrHelper.getInstance().getUsername() + "-"
          + sdf.format(new Date()) + ".zip";
      filename = filename.replaceAll(" ", "_");
      File zipFile = new File(jfc.getSelectedFile(), filename);
      logger.info("Creating archive " + zipFile.getAbsolutePath());
      File[] source = Main.configDir.listFiles(new FilenameContainsFilter("suprsetr.log"));
      logger.info("Adding " + source.length + " files to zip.");
      byte[] buf = new byte[1024];
      try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile))) {
        zipFile.createNewFile();
        for (File logFile : source) {
          logger.info("Adding file " + logFile.getAbsolutePath() + " to archive.");
          try (FileInputStream in = new FileInputStream(logFile)) {
            // Add ZIP entry to output stream.
            out.putNextEntry(new ZipEntry(logFile.getName()));
            // Transfer bytes from the file to the ZIP file
            int len;
            while ((len = in.read(buf)) > 0) {
              out.write(buf, 0, len);
            }
            // Complete the entry
            out.closeEntry();
          }
        }
        JOptionPane.showMessageDialog(this,
            resourceBundle.getString("MainWindow.dialog.zipfile.created.message1") +
                " " + new File(jfc.getSelectedFile(), filename).getAbsolutePath() + "\n" +
                resourceBundle.getString("MainWindow.dialog.zipfile.created.message2"),
            resourceBundle.getString("MainWindow.dialog.zipfile.created.title"),
            JOptionPane.INFORMATION_MESSAGE);
      } catch (Exception e) {
        logger.warn("ERROR CREATING ZIP.", e);
        JOptionPane.showMessageDialog(this,
            resourceBundle.getString("MainWindow.dialog.zipfile.error.message"),
            resourceBundle.getString("MainWindow.dialog.zipfile.error.title"),
            JOptionPane.INFORMATION_MESSAGE);
      }
    }

  }

  private void btnAddSetActionPerformed() {
    this.mnuCreateSetActionPerformed();
  }

  private void btnEditSetActionPerformed() {
    this.mnuEditSetActionPerformed();
  }

  private void btnDeleteSetActionPerformed() {
    this.mnuDeleteSetActionPerformed();
  }

  private void btnRefreshSetActionPerformed() {
    this.mnuRefreshSetActionPerformed();
  }

  private void mnuAboutActionPerformed() {
    new AboutDialog(this, true).setVisible(true);
  }

  private void mnuFavrActionPerformed() {
    String message;
    switch (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TAG_TYPE)) {
      case "0":
        message = resourceBundle.getString("MainWindow.dialog.runfavrtagr.message.tags");
        break;
      case "1":
        message = resourceBundle.getString("MainWindow.dialog.runfavrtagr.message.machinetags");
        break;
      default:
        message = resourceBundle.getString("MainWindow.dialog.runfavrtagr.message.tags");
    }
    int confirm = JOptionPane.showConfirmDialog(this,
        message,
        resourceBundle.getString("MainWindow.dialog.runfavrtagr.title"),
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);
    if (confirm == JOptionPane.YES_OPTION) {
      BlockerPanel blocker = new BlockerPanel(this, resourceBundle.getString("MainWindow.blocker.favtags"));
      setGlassPane(blocker);
      blocker.block("");
      new FavrTagrWorker(blocker).execute();
    }
  }

  private void mnuPopupCreateActionPerformed() {
    this.doCreateSetAction();
  }

  private void mnuPopupEditActionPerformed() {
    this.doEditSetAction();
  }

  private void mnuPopupDeleteActionPerformed() {
    this.doDeleteSetAction();
  }

  private void mnuPopupRefreshActionPerformed() {
    this.doRefreshSetAction();
  }

  private void mnuPopupOpenActionPerformed() {
    this.doOpenInBrowserAction();
  }

  /*
   * Check for popup and double click.
   *
   * Note: must check for popup in mouseReleased as well to be compatible
   * across all platforms.
   */
  private void jList1MousePressed(java.awt.event.MouseEvent evt) {
    if (evt.isPopupTrigger()) {
      this.showPopup(evt);
    } else {
      if (evt.getClickCount() == 2) {
        SSPhotoset ssp = (SSPhotoset) jList1.getSelectedValue();
        (new SetEditor(this, EditorMode.EDIT, ssp)).setVisible(true);
      }
    }
  }


  private void mnuSetOrderActionPerformed() {
    new SetOrderer(this, true).setVisible(true);
  }

  private void mnuSSHelpActionPerformed() {
    int option =
        JOptionPane.showConfirmDialog(this,
            resourceBundle.getString("MainWindow.dialog.help.message"),
            resourceBundle.getString("MainWindow.dialog.help.title"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

    if (option == JOptionPane.YES_OPTION) {
      try {
        Desktop.getDesktop().browse(new URL("http://jeremybrooks.net/suprsetr/faq.html").toURI());
      } catch (Exception e) {
        logger.error("Could not open help URL.", e);
        JOptionPane.showMessageDialog(this,
            resourceBundle.getString("MainWindow.dialog.browserError.message"),
            resourceBundle.getString("MainWindow.dialog.browserError.title"),
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void mnuHideUnmanagedActionPerformed() {
    boolean hideUnmanaged = this.mnuHideUnmanaged.isSelected();
    if (hideUnmanaged) {
      this.mnuHideManaged.setSelected(false);
    }
    this.setLookupForHideItems();
    this.doFilter();
  }

  private void mnuHideManagedActionPerformed() {
    boolean hideManaged = this.mnuHideManaged.isSelected();
    if (hideManaged) {
      this.mnuHideUnmanaged.setSelected(false);
    }
    this.setLookupForHideItems();
    this.doFilter();
  }

  private void setLookupForHideItems() {
    LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_HIDE_UNMANAGED,
        DAOHelper.booleanToString(this.mnuHideUnmanaged.isSelected()));
    LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_HIDE_MANAGED,
        DAOHelper.booleanToString(this.mnuHideManaged.isSelected()));
  }

  private void mnuTutorialActionPerformed() {
    new Tutorial(this, true).setVisible(true);
  }

  private void mnuLogWindowActionPerformed() {
    this.logWindow.setVisible(!this.logWindow.isVisible());
    if (this.logWindow.isVisible()) {
      this.mnuLogWindow.setText(resourceBundle.getString("MainWindow.mnuLogWindow.text.hide"));
    } else {
      this.mnuLogWindow.setText(resourceBundle.getString("MainWindow.mnuLogWindow.text"));
    }
  }


  private void mnuBackupActionPerformed() {
    JFileChooser jfc = new JFileChooser();
    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    jfc.setMultiSelectionEnabled(false);
    jfc.setDialogTitle(resourceBundle.getString("MainWindow.backup.dialog.title.text"));
    int option = jfc.showOpenDialog(this);
    if (option == JFileChooser.APPROVE_OPTION) {
      BlockerPanel blocker = new BlockerPanel(this, resourceBundle.getString("MainWindow.blocker.backup"));
      setGlassPane(blocker);
      blocker.block("");
      new DatabaseBackupWorker(blocker, jfc.getSelectedFile()).execute();
    }
  }

  private void mnuRestoreActionPerformed() {
    JFileChooser jfc = new JFileChooser();
    jfc.setDialogTitle(resourceBundle.getString("MainWindow.restore.dialog.title.text"));
    jfc.setDialogType(JFileChooser.OPEN_DIALOG);
    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int option = jfc.showOpenDialog(this);
    if (option == JFileChooser.APPROVE_OPTION) {
      BlockerPanel blocker = new BlockerPanel(this, resourceBundle.getString("MainWindow.blocker.restore"));
      setGlassPane(blocker);
      blocker.block("");
      new DatabaseRestoreWorker(blocker, jfc.getSelectedFile()).execute();
    }
  }


  public void doFilter() {
    this.doFilter(null);
  }

  public void doFilter(String visibleId) {
    String filter = this.getFilter();
    this.listModel.clear();
    BlockerPanel blocker = new BlockerPanel(this, resourceBundle.getString("MainWindow.blocker.filter"));
    setGlassPane(blocker);
    new FilterSetListWorker(blocker, masterList, filter, listModel, this.mnuHideUnmanaged.isSelected(), this.mnuHideManaged.isSelected(), visibleId).execute();
  }


  private void mnuClearFaveActionPerformed() {
    String message;
    switch (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TAG_TYPE)) {
      case "0":
        message = resourceBundle.getString("MainWindow.dialog.deletefav.message.tags");
        break;
      case "1":
        message = resourceBundle.getString("MainWindow.dialog.deletefav.message.machinetags");
        break;
      default:
        message = resourceBundle.getString("MainWindow.dialog.deletefav.message.tags");
    }
    int confirm = JOptionPane.showConfirmDialog(this,
        message,
        resourceBundle.getString("MainWindow.dialog.deletefav.title"),
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);
    if (confirm == JOptionPane.YES_OPTION) {
      BlockerPanel blocker = new BlockerPanel(this, resourceBundle.getString("MainWindow.blocker.deletefav"));
      setGlassPane(blocker);
      blocker.block("");
      new FavDeleteWorker(blocker).execute();
    }
  }

  private void mnuCaseSensitiveActionPerformed() {
    boolean caseSensitive = this.mnuCaseSensitive.isSelected();
    LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_CASE_SENSITIVE, DAOHelper.booleanToString(caseSensitive));
    String filter = this.getFilter();
    this.listModel.clear();
    try {
      this.masterList = PhotosetDAO.getPhotosetListOrderByManagedAndTitle();
    } catch (Exception e) {
      logger.error("Error while getting photoset list.", e);
    }
    BlockerPanel blocker = new BlockerPanel(this, resourceBundle.getString("MainWindow.blocker.filter"));
    setGlassPane(blocker);
    new FilterSetListWorker(blocker, masterList, filter, listModel, this.mnuHideUnmanaged.isSelected(), this.mnuHideManaged.isSelected(), null).execute();
  }

  private void btnRefreshAllActionPerformed() {
    this.mnuRefreshAllActionPerformed();
  }


  /*
   * Respond to KEY_TYPED events in the filter box.
   *
   * The list will filter when the filterTimer fires. If the user types the
   * enter key, the list will filter immediately.
   *
   * @param e the key event
   */
  private void txtFilterKeyTyped(KeyEvent e) {
    if (e.getKeyChar() == '\n') {
      this.filterTimer.stop();
      doFilter();
    } else {
      this.filterTimer.stop();
      this.filterTimer.start();
    }
  }


  /**
   * This method is called by the BlockerPanel to disable keyboard input
   * while some task is running.
   * <p>
   * There is not a reliable way to intercept keyboard events with a GlassPane,
   * so this is used as a workaround.
   * <p>
   * Note that after the focus is requested, the text in the text box will
   * be selected. To remove the selection, the FOCUS_GAINED event is used
   * to know when the focus is actually gained, and we remove the selection.
   *
   * @param enabled true if filter should be enabled.
   */
  void enableFilter(boolean enabled) {
    this.txtFilter.setEnabled(enabled);
    if (enabled) {
      this.txtFilter.requestFocusInWindow();
    }
  }


  /*
   * When the filter box gets the focus, the text will be selected. We respond
   * to the FOCUS_GAINED event and move the caret to the end of the text, which
   * removes the selection.
   *
   * @param evt
   */
  private void txtFilterFocusGained() {
    this.txtFilter.setCaretPosition(this.txtFilter.getText().length());
  }

  private void mnuCheckUpdatesActionPerformed() {
    new Thread(new VersionChecker(true, false)).start();
  }

  /*
   * Check for popup trigger.
   *
   * Note: Must check in mousePressed as well to be compatible across all
   * platforms.
   */
  private void jList1MouseReleased(java.awt.event.MouseEvent evt) {
    if (evt.isPopupTrigger()) {
      this.showPopup(evt);
    }
  }


  void doAuth() {
    while (!FlickrHelper.getInstance().authorize()) {
      (new LoginDialog(this, true)).setVisible(true);
    }
    logger.info("Authentication OK");
    this.setTitle("SuprSetr :: " + FlickrHelper.getInstance().getUsername());
  }


  boolean setTitleExists(String title) {
    boolean exists = false;

    for (int i = 0; i < listModel.size(); i++) {
      SSPhotoset setDef = (SSPhotoset) listModel.elementAt(i);
      if (setDef.getTitle().equals(title)) {
        exists = true;
        break;
      }
    }

    return exists;
  }


  private void confirmQuit() {
    int confirm = JOptionPane.YES_OPTION;
    // make the user confirm if busy
    if (MainWindow.isBlocked()) {
      confirm = JOptionPane.showConfirmDialog(this,
          resourceBundle.getString("MainWindow.dialog.busy.message"),
          resourceBundle.getString("MainWindow.dialog.busy.title"),
          JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE);
    }
    if (confirm == JOptionPane.YES_OPTION) {
      System.exit(0);
    }
  }


  /**
   * Determine if the window is in a blocked state.
   * <p>
   * This can be called to determine if SuprSetr is currently in the middle of
   * a transaction with Flickr.
   *
   * @return true if the window is in a blocked state.
   */
  static boolean isBlocked() {
    return theWindow.getGlassPane().isVisible();
  }


  public static MainWindow getMainWindow() {
    return theWindow;
  }

  void updateStatusBar() {
    SwingUtilities.invokeLater(new UpdateStatusBar());
  }

  public void scrollToPhotoset(String id) {
    for (int i = 0; i < this.listModel.size(); i++) {
      SSPhotoset set = (SSPhotoset) this.listModel.elementAt(i);
      if (id.equals(set.getPhotosetId())) {
        this.makeIndexVisibleAndSelected(i);
        break;
      }
    }
  }


  public void makeIndexVisibleAndSelected(int index) {
    if (this.listModel.size() > 0) {
      if (this.listModel.size() < index) {
        index = 0;
      }
      this.jList1.scrollRectToVisible(this.jList1.getCellBounds(index, index));
      this.jList1.setSelectedIndex(index);
    }
  }

  /**
   * Get the filter text. This will be returned in lower case, so comparisons
   * should be done in lower case.
   *
   * @return the filter text in lower case, or null if there is no filter text.
   */
  private String getFilter() {
    String filter = this.txtFilter.getText().toLowerCase().trim();
    if (filter.isEmpty()) {
      filter = null;
    }
    return filter;
  }

  /**
   * This will replace the master list that backs the list model.
   * <p>
   * The list model will be refreshed by the FilterSetListWorker class.
   * <p>
   * If you need to add, delete, or update a single set in the list model,
   * use one of the other methods. This method should only be used when the
   * entire list needs to be refreshed, as it can take time if the user has a
   * lot of sets.
   *
   * @param visiblePhotosetId id of the photoset that should be visible.
   */
  public void updateMasterList(String visiblePhotosetId) {
    try {
      switch (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_LIST_SORT_ORDER)) {
        case SSConstants.LIST_SORT_ATOZ:
          this.masterList = PhotosetDAO.getPhotosetListOrderByManagedAndTitle();
          break;
        case SSConstants.LIST_SORT_ZTOA:
          this.masterList = PhotosetDAO.getPhotosetListOrderByManagedAndTitleDescending();
          break;
        case SSConstants.LIST_SORT_VIEW_HIGHLOW:
          this.masterList = PhotosetDAO.getPhotosetListOrderByManagedAndViewCountHighToLow();
          break;
        case SSConstants.LIST_SORT_VIEW_LOWHIGH:
          this.masterList = PhotosetDAO.getPhotosetListOrderByManagedAndViewCountLowToHigh();
          break;
        default:
          this.masterList = PhotosetDAO.getPhotosetListOrderByManagedAndTitle();
          break;
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this,
          resourceBundle.getString("MainWindow.listupdate.error.message"),
          resourceBundle.getString("MainWindow.listupdate.error.title"),
          JOptionPane.WARNING_MESSAGE);
    }
    if (this.masterList != null) {
      this.doFilter(visiblePhotosetId);
    }
  }


  /**
   * Delete the specified set from the backing list and list model.
   *
   * @param photoset photoset to delete from list model.
   */
  public void deletePhotosetFromListModel(SSPhotoset photoset) {
    this.listModel.removeElement(photoset);
    this.masterList.remove(photoset);
    this.updateTitle();
  }


  /**
   * This method will insert a single photoset into the list model.
   * <p>
   * The photoset must exist in the supplied master list as well. The master
   * list will be set, and the specified photoset will be added to the
   * list model.
   * <p>
   * The purpose of having this method is to allow us to add a single set
   * without forcing an update of the entire list in the GUI.
   * <p>
   * This method must honor the filter text and the hide unmanaged sets menu
   * selection.
   *
   * @param masterList the master list.
   * @param photoset   photoset to insert in list model.
   */
  public void insertPhotosetInListModel(List<SSPhotoset> masterList, SSPhotoset photoset) {
    int index = masterList.indexOf(photoset);
    if (index != -1) {
      String filter = this.txtFilter.getText();
      this.masterList = masterList;

      // update the list model if the photoset should be displayed
      if (filter == null || photoset.getTitle().toLowerCase().contains(filter)) {
        if ((!mnuHideUnmanaged.isSelected()) || (photoset.isManaged())) {
          try {
            SwingUtilities.invokeLater(new AddToListModel(index, photoset));
          } catch (Exception e) {
            LogWindow.addLogMessage(resourceBundle.getString("MainWindow.log.addtolistmodel.error") + " " + photoset.getPhotosetId());
            logger.warn("Error while executing AddToListModel for photoset " + photoset.getPhotosetId());
          }
        }
      }
      this.updateTitle();
    } else {
      logger.warn("ATTEMPTING TO INSERT A NEW PHOTOSET, BUT THE PHOTOSET IS NOT IN THE MASTER LIST.");
    }
  }


  /**
   * Update the photoset object in the list model.
   *
   * @param photoset photoset to be updated.
   */
  public void updatePhotosetInList(SSPhotoset photoset) {
    try {
      SwingUtilities.invokeLater(new UpdatePhotosetInListModel(photoset));
    } catch (Exception e) {
      LogWindow.addLogMessage(resourceBundle.getString("MainWindow.log.updatephotoset.error") + " " + photoset.getPhotosetId());
      logger.warn("Error while executing UpdatePhotosetInListModel for photoset " + photoset.getPhotosetId());
    }
  }


  public void updateTitle() {
    this.setTitle("SuprSetr :: " + FlickrHelper.getInstance().getUsername()
        + " :: " +
        resourceBundle.getString("MainWindow.this.title.showing") + " " + this.listModel.size() +
        " " + resourceBundle.getString("MainWindow.this.title.of") + " " +
        +this.masterList.size() + " " +
        resourceBundle.getString("MainWindow.this.title.sets"));
  }


  void showUpdateDialog() {
    int response = JOptionPane.showConfirmDialog(this,
        resourceBundle.getString("MainWindow.dialog.newversion.message"),
        resourceBundle.getString("MainWindow.dialog.newversion.title"),
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE);

    if (response == JOptionPane.YES_OPTION) {
      try {
        Desktop.getDesktop().browse(new URL(SSConstants.DOWNLOAD_URL).toURI());
      } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            resourceBundle.getString("MainWindow.dialog.newversion.error.message") + SSConstants.DOWNLOAD_URL,
            resourceBundle.getString("MainWindow.dialog.browserError.title"),
            JOptionPane.ERROR_MESSAGE);
        logger.warn("ERROR OPENING DOWNLOAD URL.", e);
      }
    }
  }

  void showNoUpdateDialog() {
    JOptionPane.showMessageDialog(this,
        resourceBundle.getString("MainWindow.dialog.noupdate.message"),
        resourceBundle.getString("MainWindow.dialog.noupdate.title"),
        JOptionPane.INFORMATION_MESSAGE);
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JMenuBar jMenuBar1;
  private JMenu mnuFile;
  private JMenuItem mnuBrowser;
  private JMenuItem mnuBackup;
  private JMenuItem mnuRestore;
  private JMenuItem mnuQuit;
  private JMenu mnuEdit;
  private JMenuItem mnuCreateSet;
  private JMenuItem mnuEditSet;
  private JMenuItem mnuDeleteSet;
  private JMenuItem mnuRefreshSet;
  private JMenuItem mnuRefreshAll;
  private JMenuItem mnuPreferences;
  private JMenu mnuView;
  private JCheckBoxMenuItem mnuHideUnmanaged;
  private JCheckBoxMenuItem mnuHideManaged;
  private JCheckBoxMenuItem mnuCaseSensitive;
  private JRadioButtonMenuItem mnuOrderAlpha;
  private JRadioButtonMenuItem mnuOrderAlphaDesc;
  private JRadioButtonMenuItem mnuOrderHighLow;
  private JRadioButtonMenuItem mnuOrderLowHigh;
  private JMenu mnuTools;
  private JMenuItem mnuFavr;
  private JMenuItem mnuClearFave;
  private JMenuItem mnuSetOrder;
  private JMenuItem mnuLogs;
  private JMenuItem mnuLogWindow;
  private JMenu mnuHelp;
  private JMenuItem mnuAbout;
  private JMenuItem mnuTutorial;
  private JMenuItem mnuSSHelp;
  private JMenuItem mnuCheckUpdates;
  private JToolBar jToolBar1;
  private JButton btnAddSet;
  private JButton btnEditSet;
  private JButton btnDeleteSet;
  private JButton btnRefreshSet;
  private JButton btnRefreshAll;
  private JButton btnBrowser;
  private JLabel jLabel1;
  private JTextField txtFilter;
  private JScrollPane jScrollPane1;
  private JList jList1;
  private JLabel lblStatus;
  private JPopupMenu mnuPopup;
  private JMenuItem mnuPopupCreate;
  private JMenuItem mnuPopupEdit;
  private JMenuItem mnuPopupDelete;
  private JMenuItem mnuPopupRefresh;
  private JMenuItem mnuPopupOpen;
  // End of variables declaration//GEN-END:variables


  void executeAddSetWorker(SSPhotoset ssPhotoset) {
    BlockerPanel blocker = new BlockerPanel(this, resourceBundle.getString("MainWindow.blocker.addingset") + " " + ssPhotoset.getTitle());
    setGlassPane(blocker);
    blocker.block("");
    new AddPhotosetWorker(blocker, ssPhotoset).execute();
  }


  void executeRefreshSetWorker(SSPhotoset ssPhotoset) {
    BlockerPanel blocker = new BlockerPanel(this,
        resourceBundle.getString("MainWindow.blocker.refreshingset") +
            " \"" +
            ssPhotoset.getTitle() +
            "\"");
    setGlassPane(blocker);
    blocker.block("");
    List<SSPhotoset> list = new ArrayList<>();
    list.add(ssPhotoset);
    new RefreshPhotosetWorker(blocker, list, false).execute();
  }


  private void executeRefreshSetWorker(List<SSPhotoset> list, boolean exitWhenDone) {
    BlockerPanel blocker = new BlockerPanel(this,
        resourceBundle.getString("MainWindow.blocker.refreshing1") +
            " " +
            list.size() +
            " " +
            resourceBundle.getString("MainWindow.blocker.refreshing2"));
    setGlassPane(blocker);
    blocker.block("");
    new RefreshPhotosetWorker(blocker, list, exitWhenDone).execute();
  }


  void executeLoadFlickrSetsWorker() {
    BlockerPanel blocker = new BlockerPanel(this, resourceBundle.getString("MainWindow.blocker.loadingsets"));
    setGlassPane(blocker);
    blocker.block("");
    new LoadFlickrSetsWorker(blocker).execute();
  }


  private void executeDeleteSetWorker(SSPhotoset ssPhotoset) {
    BlockerPanel blocker = new BlockerPanel(this,
        resourceBundle.getString("MainWindow.blocker.deletingset") +
            " \"" +
            ssPhotoset.getTitle() +
            "\"");
    setGlassPane(blocker);
    blocker.block("");
    new DeletePhotosetWorker(blocker, ssPhotoset).execute();
  }


  private void showPopup(MouseEvent e) {
    if (e.getComponent() instanceof JList) {
      JList list = (JList) e.getComponent();

      int index = e.getY() / (int) list.getCellBounds(0, 0).getHeight();
      if (list.getModel().getSize() >= index) {
        list.setSelectedIndex(index);
        this.mnuPopup.show(e.getComponent(), e.getX(), e.getY());
      }
    }
  }


  class AddToListModel implements Runnable {
    private SSPhotoset photoset;
    private int index;

    AddToListModel(int index, SSPhotoset photoset) {
      this.index = index;
      this.photoset = photoset;
    }

    @Override
    public void run() {
      if (getFilter() == null) {
        listModel.add(index, photoset);
        scrollToPhotoset(photoset.getPhotosetId());
      } else {
        doFilter(photoset.getPhotosetId());
      }
    }
  }


  class UpdatePhotosetInListModel implements Runnable {
    private SSPhotoset photoset;

    UpdatePhotosetInListModel(SSPhotoset photoset) {
      this.photoset = photoset;
    }

    @Override
    public void run() {
      int index = listModel.indexOf(photoset);
      if (index != -1) {
        listModel.set(index, photoset);
      }
      index = masterList.indexOf(photoset);
      if (index != -1) {
        masterList.set(index, photoset);
      }
    }
  }

  class UpdateStatusBar implements Runnable {
    @Override
    public void run() {
      if (DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_AUTO_REFRESH))) {
        lblStatus.setText(resourceBundle.getString("MainWindow.lblStatus.text.enabled") +
            " (" +
            LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_AUTO_REFRESH_TIME) +
            ")");

        if (autoRefreshTimer != null) {
          autoRefreshTimer.cancel();
        }
        autoRefreshTimer = new java.util.Timer("AutoRefreshTimer", true);
        autoRefreshTimer.schedule(new AutoRefreshTimerTask(), 1000, 30000);
        logger.info("Auto-refresh timer scheduled.");

      } else {
        lblStatus.setText(resourceBundle.getString("MainWindow.lblStatus.text"));
        if (autoRefreshTimer != null) {
          autoRefreshTimer.cancel();
          autoRefreshTimer = null;
          logger.info("Auto-refresh timer canceled.");
        }
      }
    }
  }

  class AutoRefreshTimerTask extends TimerTask {
    private String time = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_AUTO_REFRESH_TIME);
    private SimpleDateFormat format = new SimpleDateFormat("HH:mm");

    @Override
    public void run() {
      String currentTime = format.format(new Date());
      if (time.equals(currentTime) && !getGlassPane().isVisible()) {
        boolean exitWhenDone = DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_AUTO_REFRESH_EXIT_AFTER));
        logger.info("Auto-refresh triggered.");
        LogWindow.addLogMessage(resourceBundle.getString("MainWindow.log.message.autorefresh") + " " + new Date());
        SSPhotoset ssPhotoset;
        List<SSPhotoset> list = new ArrayList<>();
        // Put all managed sets in the list
        for (int i = 0; i < listModel.getSize(); i++) {
          ssPhotoset = (SSPhotoset) listModel.getElementAt(i);
          if (ssPhotoset.isManaged()) {
            list.add(ssPhotoset);
          }
        }
        executeRefreshSetWorker(list, exitWhenDone);
      }
    }
  }
}
