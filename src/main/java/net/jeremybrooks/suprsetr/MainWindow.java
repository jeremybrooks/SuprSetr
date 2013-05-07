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

import net.jeremybrooks.suprsetr.SetEditor.EditorMode;
import net.jeremybrooks.suprsetr.dao.DAOHelper;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import net.jeremybrooks.suprsetr.dao.PhotosetDAO;
import net.jeremybrooks.suprsetr.flickr.FlickrHelper;
import net.jeremybrooks.suprsetr.tutorial.Tutorial;
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
import net.whirljack.common.util.FilenameContainsFilter;
import net.whirljack.common.util.IOUtil;
import org.apache.log4j.Logger;

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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * @author jeremyb
 */
public class MainWindow extends javax.swing.JFrame {

	private static final long serialVersionUID = 5381447617741236893L;
	/**
	 * Logging.
	 */
	private Logger logger = Logger.getLogger(MainWindow.class);

	/**
	 * List model.
	 */
	private DefaultListModel listModel = new DefaultListModel();

	/**
	 * Master list.
	 */
	private List<SSPhotoset> masterList;

	/**
	 * Reference to this window.
	 */
	private static MainWindow theWindow;

	/**
	 * Log window
	 */
	private LogWindow logWindow = null;

	/**
	 * Timer used to trigger filtering.
	 */
	private Timer filterTimer = null;


	private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.mainwindow");

	/**
	 * Creates new form MainWindow
	 */
	public MainWindow() {
		initComponents();

		this.mnuHideUnmanaged.setSelected(DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_HIDE_UNMANAGED)));
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
		if (System.getProperty("mrj.version") != null) {
			this.mnuFile.remove(this.mnuQuit);
			this.mnuEdit.remove(this.mnuPreferences);
			this.mnuHelp.remove(this.mnuAbout);
		}

		MainWindow.theWindow = this;

		this.logWindow = new LogWindow();
		LogWindow.addLogMessage("Started up at " + new Date());

		this.filterTimer = new Timer(500, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				doFilter();
			}

		});
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
		mnuCaseSensitive = new JCheckBoxMenuItem();
		mnuLogWindow = new JMenuItem();
		mnuTools = new JMenu();
		mnuFavr = new JMenuItem();
		mnuClearFave = new JMenuItem();
		mnuSetOrder = new JMenuItem();
		mnuLogs = new JMenuItem();
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
		jLabel1 = new JLabel();
		txtFilter = new JTextField();
		jScrollPane1 = new JScrollPane();
		jList1 = new JList();
		mnuPopup = new JPopupMenu();
		mnuPopupCreate = new JMenuItem();
		mnuPopupEdit = new JMenuItem();
		mnuPopupDelete = new JMenuItem();
		mnuPopupRefresh = new JMenuItem();
		mnuPopupOpen = new JMenuItem();

		//======== this ========
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle(bundle.getString("MainWindow.this.title"));
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== jMenuBar1 ========
		{

			//======== mnuFile ========
			{
				mnuFile.setText(bundle.getString("MainWindow.mnuFile.text"));

				//---- mnuBrowser ----
				mnuBrowser.setIcon(new ImageIcon(getClass().getResource("/images/web16.png")));
				mnuBrowser.setText(bundle.getString("MainWindow.mnuBrowser.text"));
				mnuBrowser.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuBrowserActionPerformed();
					}
				});
				mnuFile.add(mnuBrowser);

				//---- mnuBackup ----
				mnuBackup.setIcon(new ImageIcon(getClass().getResource("/images/database16.png")));
				mnuBackup.setText(bundle.getString("MainWindow.mnuBackup.text"));
				mnuBackup.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuBackupActionPerformed();
					}
				});
				mnuFile.add(mnuBackup);

				//---- mnuRestore ----
				mnuRestore.setIcon(new ImageIcon(getClass().getResource("/images/database16.png")));
				mnuRestore.setText(bundle.getString("MainWindow.mnuRestore.text"));
				mnuRestore.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuRestoreActionPerformed();
					}
				});
				mnuFile.add(mnuRestore);
				mnuFile.addSeparator();

				//---- mnuQuit ----
				mnuQuit.setIcon(new ImageIcon(getClass().getResource("/images/quit16.png")));
				mnuQuit.setText(bundle.getString("MainWindow.mnuQuit.text"));
				mnuQuit.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuQuitActionPerformed();
					}
				});
				mnuFile.add(mnuQuit);
			}
			jMenuBar1.add(mnuFile);

			//======== mnuEdit ========
			{
				mnuEdit.setText(bundle.getString("MainWindow.mnuEdit.text"));

				//---- mnuCreateSet ----
				mnuCreateSet.setIcon(new ImageIcon(getClass().getResource("/images/add16.png")));
				mnuCreateSet.setText(bundle.getString("MainWindow.mnuCreateSet.text"));
				mnuCreateSet.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuCreateSetActionPerformed();
					}
				});
				mnuEdit.add(mnuCreateSet);

				//---- mnuEditSet ----
				mnuEditSet.setIcon(new ImageIcon(getClass().getResource("/images/edit16.png")));
				mnuEditSet.setText(bundle.getString("MainWindow.mnuEditSet.text"));
				mnuEditSet.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuEditSetActionPerformed();
					}
				});
				mnuEdit.add(mnuEditSet);

				//---- mnuDeleteSet ----
				mnuDeleteSet.setIcon(new ImageIcon(getClass().getResource("/images/delete16.png")));
				mnuDeleteSet.setText(bundle.getString("MainWindow.mnuDeleteSet.text"));
				mnuDeleteSet.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuDeleteSetActionPerformed();
					}
				});
				mnuEdit.add(mnuDeleteSet);

				//---- mnuRefreshSet ----
				mnuRefreshSet.setIcon(new ImageIcon(getClass().getResource("/images/refresh16.png")));
				mnuRefreshSet.setText(bundle.getString("MainWindow.mnuRefreshSet.text"));
				mnuRefreshSet.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuRefreshSetActionPerformed();
					}
				});
				mnuEdit.add(mnuRefreshSet);

				//---- mnuRefreshAll ----
				mnuRefreshAll.setIcon(new ImageIcon(getClass().getResource("/images/refreshall16.png")));
				mnuRefreshAll.setText(bundle.getString("MainWindow.mnuRefreshAll.text"));
				mnuRefreshAll.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuRefreshAllActionPerformed();
					}
				});
				mnuEdit.add(mnuRefreshAll);

				//---- mnuPreferences ----
				mnuPreferences.setIcon(new ImageIcon(getClass().getResource("/images/process16.png")));
				mnuPreferences.setText(bundle.getString("MainWindow.mnuPreferences.text"));
				mnuPreferences.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuPreferencesActionPerformed();
					}
				});
				mnuEdit.add(mnuPreferences);
			}
			jMenuBar1.add(mnuEdit);

			//======== mnuView ========
			{
				mnuView.setText(bundle.getString("MainWindow.mnuView.text"));
				mnuView.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						mnuViewMouseClicked();
					}
				});

				//---- mnuHideUnmanaged ----
				mnuHideUnmanaged.setText(bundle.getString("MainWindow.mnuHideUnmanaged.text"));
				mnuHideUnmanaged.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuHideUnmanagedActionPerformed();
					}
				});
				mnuView.add(mnuHideUnmanaged);

				//---- mnuCaseSensitive ----
				mnuCaseSensitive.setText(bundle.getString("MainWindow.mnuCaseSensitive.text"));
				mnuCaseSensitive.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuCaseSensitiveActionPerformed();
					}
				});
				mnuView.add(mnuCaseSensitive);

				//---- mnuLogWindow ----
				mnuLogWindow.setText(bundle.getString("MainWindow.mnuLogWindow.text"));
				mnuLogWindow.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuLogWindowActionPerformed();
					}
				});
				mnuView.add(mnuLogWindow);
			}
			jMenuBar1.add(mnuView);

			//======== mnuTools ========
			{
				mnuTools.setText(bundle.getString("MainWindow.mnuTools.text"));

				//---- mnuFavr ----
				mnuFavr.setIcon(new ImageIcon(getClass().getResource("/images/tag16.png")));
				mnuFavr.setText(bundle.getString("MainWindow.mnuFavr.text"));
				mnuFavr.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuFavrActionPerformed();
					}
				});
				mnuTools.add(mnuFavr);

				//---- mnuClearFave ----
				mnuClearFave.setIcon(new ImageIcon(getClass().getResource("/images/deletetag16.png")));
				mnuClearFave.setText(bundle.getString("MainWindow.mnuClearFave.text"));
				mnuClearFave.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuClearFaveActionPerformed();
					}
				});
				mnuTools.add(mnuClearFave);

				//---- mnuSetOrder ----
				mnuSetOrder.setIcon(new ImageIcon(getClass().getResource("/images/order16.png")));
				mnuSetOrder.setText(bundle.getString("MainWindow.mnuSetOrder.text"));
				mnuSetOrder.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuSetOrderActionPerformed();
					}
				});
				mnuTools.add(mnuSetOrder);

				//---- mnuLogs ----
				mnuLogs.setIcon(new ImageIcon(getClass().getResource("/images/compress16.png")));
				mnuLogs.setText(bundle.getString("MainWindow.mnuLogs.text"));
				mnuLogs.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuLogsActionPerformed();
					}
				});
				mnuTools.add(mnuLogs);
			}
			jMenuBar1.add(mnuTools);

			//======== mnuHelp ========
			{
				mnuHelp.setText(bundle.getString("MainWindow.mnuHelp.text"));

				//---- mnuAbout ----
				mnuAbout.setIcon(new ImageIcon(getClass().getResource("/images/help16.png")));
				mnuAbout.setText(bundle.getString("MainWindow.mnuAbout.text"));
				mnuAbout.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuAboutActionPerformed();
					}
				});
				mnuHelp.add(mnuAbout);

				//---- mnuTutorial ----
				mnuTutorial.setIcon(new ImageIcon(getClass().getResource("/images/info16.png")));
				mnuTutorial.setText(bundle.getString("MainWindow.mnuTutorial.text"));
				mnuTutorial.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuTutorialActionPerformed();
					}
				});
				mnuHelp.add(mnuTutorial);

				//---- mnuSSHelp ----
				mnuSSHelp.setIcon(new ImageIcon(getClass().getResource("/images/help16.png")));
				mnuSSHelp.setText(bundle.getString("MainWindow.mnuSSHelp.text"));
				mnuSSHelp.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuSSHelpActionPerformed();
					}
				});
				mnuHelp.add(mnuSSHelp);

				//---- mnuCheckUpdates ----
				mnuCheckUpdates.setIcon(new ImageIcon(getClass().getResource("/images/new16.png")));
				mnuCheckUpdates.setText(bundle.getString("MainWindow.mnuCheckUpdates.text"));
				mnuCheckUpdates.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mnuCheckUpdatesActionPerformed();
					}
				});
				mnuHelp.add(mnuCheckUpdates);
			}
			jMenuBar1.add(mnuHelp);
		}
		setJMenuBar(jMenuBar1);

		//======== jToolBar1 ========
		{
			jToolBar1.setRollover(true);

			//---- btnAddSet ----
			btnAddSet.setIcon(new ImageIcon(getClass().getResource("/images/add16.png")));
			btnAddSet.setToolTipText(bundle.getString("MainWindow.btnAddSet.toolTipText"));
			btnAddSet.setFocusable(false);
			btnAddSet.setHorizontalTextPosition(SwingConstants.CENTER);
			btnAddSet.setVerticalTextPosition(SwingConstants.BOTTOM);
			btnAddSet.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					btnAddSetActionPerformed();
				}
			});
			jToolBar1.add(btnAddSet);

			//---- btnEditSet ----
			btnEditSet.setIcon(new ImageIcon(getClass().getResource("/images/edit16.png")));
			btnEditSet.setToolTipText(bundle.getString("MainWindow.btnEditSet.toolTipText"));
			btnEditSet.setFocusable(false);
			btnEditSet.setHorizontalTextPosition(SwingConstants.CENTER);
			btnEditSet.setVerticalTextPosition(SwingConstants.BOTTOM);
			btnEditSet.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					btnEditSetActionPerformed();
				}
			});
			jToolBar1.add(btnEditSet);

			//---- btnDeleteSet ----
			btnDeleteSet.setIcon(new ImageIcon(getClass().getResource("/images/delete16.png")));
			btnDeleteSet.setToolTipText(bundle.getString("MainWindow.btnDeleteSet.toolTipText"));
			btnDeleteSet.setFocusable(false);
			btnDeleteSet.setHorizontalTextPosition(SwingConstants.CENTER);
			btnDeleteSet.setVerticalTextPosition(SwingConstants.BOTTOM);
			btnDeleteSet.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					btnDeleteSetActionPerformed();
				}
			});
			jToolBar1.add(btnDeleteSet);

			//---- btnRefreshSet ----
			btnRefreshSet.setIcon(new ImageIcon(getClass().getResource("/images/refresh16.png")));
			btnRefreshSet.setToolTipText(bundle.getString("MainWindow.btnRefreshSet.toolTipText"));
			btnRefreshSet.setFocusable(false);
			btnRefreshSet.setHorizontalTextPosition(SwingConstants.CENTER);
			btnRefreshSet.setVerticalTextPosition(SwingConstants.BOTTOM);
			btnRefreshSet.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					btnRefreshSetActionPerformed();
				}
			});
			jToolBar1.add(btnRefreshSet);

			//---- btnRefreshAll ----
			btnRefreshAll.setIcon(new ImageIcon(getClass().getResource("/images/refreshall16.png")));
			btnRefreshAll.setToolTipText(bundle.getString("MainWindow.btnRefreshAll.toolTipText"));
			btnRefreshAll.setFocusable(false);
			btnRefreshAll.setHorizontalTextPosition(SwingConstants.CENTER);
			btnRefreshAll.setVerticalTextPosition(SwingConstants.BOTTOM);
			btnRefreshAll.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					btnRefreshAllActionPerformed();
				}
			});
			jToolBar1.add(btnRefreshAll);
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

				@Override
				public void mouseClicked(MouseEvent e) {
					jList1MouseClicked(e);
				}
			});
			this.jList1.setModel(this.listModel);
			this.jList1.setCellRenderer(new SetListRenderer());
			jScrollPane1.setViewportView(jList1);
		}
		contentPane.add(jScrollPane1, BorderLayout.CENTER);
		setSize(488, 522);
		setLocationRelativeTo(null);

		//======== mnuPopup ========
		{
			mnuPopup.setLayout(null);

			//---- mnuPopupCreate ----
			mnuPopupCreate.setIcon(new ImageIcon(getClass().getResource("/images/add16.png")));
			mnuPopupCreate.setText(bundle.getString("MainWindow.mnuPopupCreate.text"));
			mnuPopupCreate.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mnuPopupCreateActionPerformed();
				}
			});
			mnuPopup.add(mnuPopupCreate);
			mnuPopupCreate.setBounds(0, 4, 205, mnuPopupCreate.getPreferredSize().height);

			//---- mnuPopupEdit ----
			mnuPopupEdit.setIcon(new ImageIcon(getClass().getResource("/images/edit16.png")));
			mnuPopupEdit.setText(bundle.getString("MainWindow.mnuPopupEdit.text"));
			mnuPopupEdit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mnuPopupEditActionPerformed();
				}
			});
			mnuPopup.add(mnuPopupEdit);
			mnuPopupEdit.setBounds(0, 23, 205, mnuPopupEdit.getPreferredSize().height);

			//---- mnuPopupDelete ----
			mnuPopupDelete.setIcon(new ImageIcon(getClass().getResource("/images/delete16.png")));
			mnuPopupDelete.setText(bundle.getString("MainWindow.mnuPopupDelete.text"));
			mnuPopupDelete.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mnuPopupDeleteActionPerformed();
				}
			});
			mnuPopup.add(mnuPopupDelete);
			mnuPopupDelete.setBounds(0, 42, 205, mnuPopupDelete.getPreferredSize().height);

			//---- mnuPopupRefresh ----
			mnuPopupRefresh.setIcon(new ImageIcon(getClass().getResource("/images/refresh16.png")));
			mnuPopupRefresh.setText(bundle.getString("MainWindow.mnuPopupRefresh.text"));
			mnuPopupRefresh.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mnuPopupRefreshActionPerformed();
				}
			});
			mnuPopup.add(mnuPopupRefresh);
			mnuPopupRefresh.setBounds(0, 61, 205, mnuPopupRefresh.getPreferredSize().height);

			//---- mnuPopupOpen ----
			mnuPopupOpen.setIcon(new ImageIcon(getClass().getResource("/images/web16.png")));
			mnuPopupOpen.setText(bundle.getString("MainWindow.mnuPopupOpen.text"));
			mnuPopupOpen.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mnuPopupOpenActionPerformed();
				}
			});
			mnuPopup.add(mnuPopupOpen);
			mnuPopupOpen.setBounds(new Rectangle(new Point(0, 80), mnuPopupOpen.getPreferredSize()));

			{ // compute preferred size
				Dimension preferredSize = new Dimension();
				for(int i = 0; i < mnuPopup.getComponentCount(); i++) {
					Rectangle bounds = mnuPopup.getComponent(i).getBounds();
					preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
					preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
				}
				Insets insets = mnuPopup.getInsets();
				preferredSize.width += insets.right;
				preferredSize.height += insets.bottom;
				mnuPopup.setMinimumSize(preferredSize);
				mnuPopup.setPreferredSize(preferredSize);
			}
		}
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


	private void jList1MouseClicked(java.awt.event.MouseEvent evt) {
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
		this.executeRefreshSetWorker(list);
	}


	private void doCreateSetAction() {
		SSPhotoset ssp = new SSPhotoset();
		ssp.setTagMatchMode(SSConstants.TAG_MATCH_MODE_NONE);
		ssp.setTags("");
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

		String filename = "suprsetr_logs-"
				+ FlickrHelper.getInstance().getUsername() + "-"
				+ new java.util.Date() + ".zip";
		filename = filename.replaceAll(" ", "_");

		if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File[] source = Main.configDir.listFiles(new FilenameContainsFilter("suprsetr.log"));
			logger.info("Adding " + source.length + " files to zip.");
			byte[] buf = new byte[1024];
			try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(new File(jfc.getSelectedFile(), filename)))) {
				for (File logFile : source) {
					logger.info("Adding file " + logFile.getAbsolutePath() + " to archive.");
					FileInputStream in = new FileInputStream(logFile);
					// Add ZIP entry to output stream.
					out.putNextEntry(new ZipEntry(logFile.getName()));
					// Transfer bytes from the file to the ZIP file
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					// Complete the entry
					out.closeEntry();
					IOUtil.close(in);
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
		int confirm = JOptionPane.showConfirmDialog(this,
				resourceBundle.getString("MainWindow.dialog.runfavrtagr.message"),
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
		boolean hide = this.mnuHideUnmanaged.isSelected();
		LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_HIDE_UNMANAGED, DAOHelper.booleanToString(hide));
		String filter = this.getFilter();
		this.listModel.clear();
		BlockerPanel blocker = new BlockerPanel(this, resourceBundle.getString("MainWindow.blocker.filter"));
		setGlassPane(blocker);
		new FilterSetListWorker(blocker, masterList, filter, listModel, this.mnuHideUnmanaged.isSelected(), null).execute();
	}
	private void mnuTutorialActionPerformed() {
		new Tutorial(this, true).setVisible(true);
	}
	private void mnuLogWindowActionPerformed() {
		this.logWindow.setVisible(!this.logWindow.isVisible());
	}

	private void mnuViewMouseClicked() {
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
		new FilterSetListWorker(blocker, masterList, filter, listModel, this.mnuHideUnmanaged.isSelected(), visibleId).execute();

	}


	private void mnuClearFaveActionPerformed() {
		int confirm = JOptionPane.showConfirmDialog(this,
				resourceBundle.getString("MainWindow.dialog.deletefav.message"),
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
		new FilterSetListWorker(blocker, masterList, filter, listModel, this.mnuHideUnmanaged.isSelected(), null).execute();
	}

	private void btnRefreshAllActionPerformed() {
		this.mnuRefreshAllActionPerformed();
	}


	/*
	 * Respond to KEY_TYPED events in the filter box.
	 * <p/>
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
	 * <p/>
	 * There is not a reliable way to intercept keyboard events with a GlassPane,
	 * so this is used as a workaround.
	 * <p/>
	 * Note that after the focus is requested, the text in the text box will
	 * be selected. To remove the selection, the FOCUS_GAINED event is used
	 * to know when the focus is actually gained, and we remove the selection.
	 *
	 * @param enabled true if filter should be enabled.
	 */
	public void enableFilter(boolean enabled) {
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


	public void doAuth() {
		while (!FlickrHelper.getInstance().authorize()) {
			// Display the modal login dialog
			(new LoginDialog(this, true)).setVisible(true);
		}
		logger.info("Authentication OK");
		this.setTitle("SuprSetr :: " + FlickrHelper.getInstance().getUsername());
	}


	public boolean setTitleExists(String title) {
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
	 * <p/>
	 * This can be called to determine if SuprSetr is currently in the middle of
	 * a transaction with Flickr.
	 *
	 * @return true if the window is in a blocked state.
	 */
	public static boolean isBlocked() {
		return theWindow.getGlassPane().isVisible();
	}


	public static MainWindow getMainWindow() {
		return theWindow;
	}


	public void scrollToPhotoset(String id) {
		for (int i = 0; i < this.listModel.size(); i++) {
			SSPhotoset set = (SSPhotoset) this.listModel.elementAt(i);
			if (id.equals(set.getId())) {
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
	public String getFilter() {
		String filter = this.txtFilter.getText().toLowerCase().trim();
		if (filter.isEmpty()) {
			filter = null;
		}
		return filter;
	}

	/**
	 * This will replace the master list that backs the list model.
	 * <p/>
	 * The list model will be refreshed by the FilterSetListWorker class.
	 * <p/>
	 * If you need to add, delete, or update a single set in the list model,
	 * use one of the other methods. This method should only be used when the
	 * entire list needs to be refreshed, as it can take time if the user has a
	 * lot of sets.
	 *
	 * @param masterList the master list.
	 * @param visiblePhotosetId id of the photoset that should be visible.
	 */
	public void setMasterList(List<SSPhotoset> masterList, String visiblePhotosetId) {
		this.masterList = masterList;
		String filter = this.getFilter();
		this.listModel.clear();
		BlockerPanel blocker = new BlockerPanel(this, resourceBundle.getString("MainWindow.blocker.filter"));
		setGlassPane(blocker);
		new FilterSetListWorker(blocker, masterList, filter, listModel, this.mnuHideUnmanaged.isSelected(), visiblePhotosetId).execute();
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
	 * <p/>
	 * The photoset must exist in the supplied master list as well. The master
	 * list will be set, and the specified photoset will be added to the
	 * list model.
	 * <p/>
	 * The purpose of having this method is to allow us to add a single set
	 * without forcing an update of the entire list in the GUI.
	 * <p/>
	 * This method must honor the filter text and the hide unmanaged sets menu
	 * selection.
	 *
	 * @param masterList the master list.
	 * @param photoset photoset to insert in list model.
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
						LogWindow.addLogMessage(resourceBundle.getString("MainWindow.log.addtolistmodel.error") + " " + photoset.getId());
						logger.warn("Error while executing AddToListModel for photoset " + photoset.getId());
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
			LogWindow.addLogMessage(resourceBundle.getString("MainWindow.log.updatephotoset.error") + " " + photoset.getId());
			logger.warn("Error while executing UpdatePhotosetInListModel for photoset " + photoset.getId());
		}
	}


	public void updateTitle() {
		this.setTitle("SuprSetr :: " + FlickrHelper.getInstance().getUsername()
				+ " :: " +
				resourceBundle.getString("MainWindow.this.title.showing") + " " + this.listModel.size() +
				" " + resourceBundle.getString("MainWindow.this.title.of") + " " +
				+ this.masterList.size() + " " +
				resourceBundle.getString("MainWindow.this.title.sets"));
	}


	public void showUpdateDialog() {
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

	public void showNoUpdateDialog() {
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
	private JCheckBoxMenuItem mnuCaseSensitive;
	private JMenuItem mnuLogWindow;
	private JMenu mnuTools;
	private JMenuItem mnuFavr;
	private JMenuItem mnuClearFave;
	private JMenuItem mnuSetOrder;
	private JMenuItem mnuLogs;
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
	private JLabel jLabel1;
	private JTextField txtFilter;
	private JScrollPane jScrollPane1;
	private JList jList1;
	private JPopupMenu mnuPopup;
	private JMenuItem mnuPopupCreate;
	private JMenuItem mnuPopupEdit;
	private JMenuItem mnuPopupDelete;
	private JMenuItem mnuPopupRefresh;
	private JMenuItem mnuPopupOpen;
	// End of variables declaration//GEN-END:variables


	public void executeAddSetWorker(SSPhotoset ssPhotoset) {
		BlockerPanel blocker = new BlockerPanel(this, resourceBundle.getString("MainWindow.blocker.addingset") + ssPhotoset.getTitle());
		setGlassPane(blocker);
		blocker.block("");
		new AddPhotosetWorker(blocker, ssPhotoset).execute();
	}


	public void executeRefreshSetWorker(SSPhotoset ssPhotoset) {
		BlockerPanel blocker = new BlockerPanel(this,
				resourceBundle.getString("MainWindow.blocker.refreshingset") +
						" \"" +
						ssPhotoset.getTitle() +
						"\"");
		setGlassPane(blocker);
		blocker.block("");
		List<SSPhotoset> list = new ArrayList<>();
		list.add(ssPhotoset);
		new RefreshPhotosetWorker(blocker, list).execute();
	}


	public void executeRefreshSetWorker(List<SSPhotoset> list) {
		BlockerPanel blocker = new BlockerPanel(this,
				resourceBundle.getString("MainWindow.blocker.refreshing1") +
				" " +
				list.size() +
				" " +
				resourceBundle.getString("MainWindow.blocker.refreshing2"));
		setGlassPane(blocker);
		blocker.block("");
		new RefreshPhotosetWorker(blocker, list).execute();
	}


	public void executeLoadFlickrSetsWorker() {
		BlockerPanel blocker = new BlockerPanel(this, resourceBundle.getString("MainWindow.blocker.loadingsets"));
		setGlassPane(blocker);
		blocker.block("");
		new LoadFlickrSetsWorker(blocker).execute();
	}


	public void executeDeleteSetWorker(SSPhotoset ssPhotoset) {
		BlockerPanel blocker = new BlockerPanel(this,
				resourceBundle.getString("MainWindow.blocker.deletingset") +
						" \"" +
						ssPhotoset.getTitle() +
						"\"");
		setGlassPane(blocker);
		blocker.block("");
		new DeletePhotosetWorker(blocker, ssPhotoset).execute();
	}


	public void showPopup(MouseEvent e) {
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
				scrollToPhotoset(photoset.getId());
			} else {
				doFilter(photoset.getId());
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
}
