/*
 *  SuprSetr is Copyright 2010-2021 by Jeremy Brooks
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

/*
 * Created by JFormDesigner on Sat Mar 21 11:13:18 PDT 2020
 */

package net.jeremybrooks.suprsetr;

import net.jeremybrooks.suprsetr.dao.LookupDAO;
import net.jeremybrooks.suprsetr.utils.FileIsBackupDirectoryFilter;
import net.jeremybrooks.suprsetr.utils.FilenameComparator;
import net.jeremybrooks.suprsetr.workers.DatabaseRestoreWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * @author Jeremy Brooks
 */
public class RestoreDialog extends JDialog {
  private static final Logger logger = LogManager.getLogger();
  private static final long serialVersionUID = -2778874095923603199L;
  private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.restoredialog");
  File[] files;


  public RestoreDialog(Window owner) {
    super(owner);
    initComponents();
    String backupDirectory = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_BACKUP_DIRECTORY);
    lblBackupDirectory2.setText(backupDirectory);
    files = new File(backupDirectory).listFiles(new FileIsBackupDirectoryFilter());
    if (files == null || files.length == 0) {
      lblSelect.setText(resourceBundle.getString("RestoreDialog.label.nofiles.text"));
      cmbBackupList.setEnabled(false);
      okButton.setEnabled(false);
    } else {
      Arrays.sort(files, new FilenameComparator());
      SimpleDateFormat sdf = new SimpleDateFormat("'Backup created' MMM dd, yyyy 'at' HH:mm:ss");
      for (File f : files) {
        long millis = Long.parseLong(f.getName());
        cmbBackupList.addItem(sdf.format(new Date(millis)));
      }
    }
  }

  private void cancelButtonActionPerformed(ActionEvent e) {
    setVisible(false);
    dispose();
  }

  private void okButtonActionPerformed(ActionEvent e) {
    int response = JOptionPane.showConfirmDialog(this,
        resourceBundle.getString("ResourceDialog.confirmRestore.message"),
        resourceBundle.getString("ResourceDialog.confirmRestore.title"),
        JOptionPane.OK_CANCEL_OPTION);
    if (response == JOptionPane.OK_OPTION) {
      File f = files[cmbBackupList.getSelectedIndex()];
      logger.info("Restoring from backup {}", f.getAbsolutePath());
      BlockerPanel blocker = new BlockerPanel(this, resourceBundle.getString("RestoreDialog.blocker.title"));
      setGlassPane(blocker);
      blocker.block("");
      new DatabaseRestoreWorker(blocker, f).execute();
    }
  }

  private void initComponents() {
    // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
    ResourceBundle bundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.restoredialog");
    dialogPane = new JPanel();
    buttonBar = new JPanel();
    okButton = new JButton();
    cancelButton = new JButton();
    panel1 = new JPanel();
    lblBackupDirectory = new JLabel();
    lblBackupDirectory2 = new JLabel();
    lblSelect = new JLabel();
    cmbBackupList = new JComboBox();

    //======== this ========
    setModal(true);
    setTitle(bundle.getString("RestoreDialog.dialog.title"));
    var contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());

    //======== dialogPane ========
    {
      dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
      dialogPane.setLayout(new BorderLayout());

      //======== buttonBar ========
      {
        buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
        buttonBar.setLayout(new GridBagLayout());
        ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
        ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

        //---- okButton ----
        okButton.setText(bundle.getString("RestoreDialog.okButton.text"));
        okButton.addActionListener(e -> okButtonActionPerformed(e));
        buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 0, 0, 5), 0, 0));

        //---- cancelButton ----
        cancelButton.setText(bundle.getString("RestoreDialog.cancelButton.text"));
        cancelButton.addActionListener(e -> cancelButtonActionPerformed(e));
        buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 0, 0, 0), 0, 0));
      }
      dialogPane.add(buttonBar, BorderLayout.SOUTH);

      //======== panel1 ========
      {
        panel1.setLayout(new GridBagLayout());
        ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
        ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
        ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
        ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

        //---- lblBackupDirectory ----
        lblBackupDirectory.setText(bundle.getString("RestoreDialog.lblBackupDirectory.text"));
        panel1.add(lblBackupDirectory, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 0, 5, 5), 0, 0));

        //---- lblBackupDirectory2 ----
        lblBackupDirectory2.setText(bundle.getString("RestoreDialog.lblBackupDirectory2.text"));
        panel1.add(lblBackupDirectory2, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 0, 5, 0), 0, 0));

        //---- lblSelect ----
        lblSelect.setText(bundle.getString("RestoreDialog.label.text"));
        panel1.add(lblSelect, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 0, 5, 0), 0, 0));
        panel1.add(cmbBackupList, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH,
          new Insets(0, 0, 0, 0), 0, 0));
      }
      dialogPane.add(panel1, BorderLayout.CENTER);
    }
    contentPane.add(dialogPane, BorderLayout.CENTER);
    setSize(550, 200);
    setLocationRelativeTo(getOwner());
    // JFormDesigner - End of component initialization  //GEN-END:initComponents
  }

  // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
  private JPanel dialogPane;
  private JPanel buttonBar;
  private JButton okButton;
  private JButton cancelButton;
  private JPanel panel1;
  private JLabel lblBackupDirectory;
  private JLabel lblBackupDirectory2;
  private JLabel lblSelect;
  private JComboBox cmbBackupList;
  // JFormDesigner - End of variables declaration  //GEN-END:variables
}
