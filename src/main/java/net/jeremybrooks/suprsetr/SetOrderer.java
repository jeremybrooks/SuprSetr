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

package net.jeremybrooks.suprsetr;

import net.jeremybrooks.suprsetr.workers.OrderSetsWorker;
import net.jeremybrooks.suprsetr.workers.SetOrdererDTOListWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Allow the user to change the order of their photosets.
 *
 * <p>The user can drag and drop the sets in the list.</p>
 *
 * @author Jeremy Brooks
 */
public class SetOrderer extends javax.swing.JDialog {

  private static final long serialVersionUID = -8987356410180796854L;
  private Logger logger = LogManager.getLogger(SetOrderer.class);

  private DefaultListModel<SetOrdererDTO> listModel;

  /* The data list. */
  private List<SetOrdererDTO> dtoList;

  private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.setorderer");

  private String[] comboBoxModel = new String[7];

  /**
   * Creates new form SetOrdererDialog
   *
   * @param parent the parent component.
   * @param modal  should this be modal?
   */
  public SetOrderer(java.awt.Frame parent, boolean modal) {
    super(parent, modal);

    this.listModel = new DefaultListModel<>();

    for (int i = 0; i < 7; i++) {
      this.comboBoxModel[i] = resourceBundle.getString("SetOrderer.comboBoxModel." + i);
    }

    initComponents();

    setIconImage(new ImageIcon(getClass().getResource("/images/s16.png")).getImage());

    ReorderListener reorderListener = new ReorderListener(this.jList1);
    this.jList1.addMouseListener(reorderListener);
    this.jList1.addMouseMotionListener(reorderListener);
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
    JLabel jLabel1 = new JLabel();
    cmbOrder = new JComboBox();
    JScrollPane jScrollPane1 = new JScrollPane();
    jList1 = new JList<>();
    JPanel panel1 = new JPanel();
    JButton btnCancel = new JButton();
    JButton btnSave = new JButton();

    //======== this ========
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setTitle(bundle.getString("SetOrderer.this.title"));
    Container contentPane = getContentPane();
    contentPane.setLayout(new GridBagLayout());

    //---- jLabel1 ----
    jLabel1.setText(bundle.getString("SetOrderer.jLabel1.text"));
    contentPane.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
      GridBagConstraints.WEST, GridBagConstraints.NONE,
      new Insets(3, 3, 3, 3), 0, 0));

    //---- cmbOrder ----
    cmbOrder.addActionListener(e -> cmbOrderActionPerformed());
    cmbOrder.setModel(new DefaultComboBoxModel(this.comboBoxModel));
    contentPane.add(cmbOrder, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
      GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
      new Insets(3, 0, 3, 3), 0, 0));

    //======== jScrollPane1 ========
    {

      //---- jList1 ----
      jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      jList1.setModel(this.listModel);
      jList1.setCellRenderer(new SetOrdererRenderer());
      jScrollPane1.setViewportView(jList1);
    }
    contentPane.add(jScrollPane1, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0,
      GridBagConstraints.CENTER, GridBagConstraints.BOTH,
      new Insets(3, 3, 3, 3), 0, 0));

    //======== panel1 ========
    {
      panel1.setLayout(new FlowLayout(FlowLayout.RIGHT));

      //---- btnCancel ----
      btnCancel.setText(bundle.getString("SetOrderer.btnCancel.text"));
      btnCancel.addActionListener(e -> btnCancelActionPerformed());
      panel1.add(btnCancel);

      //---- btnSave ----
      btnSave.setText(bundle.getString("SetOrderer.btnSave.text"));
      btnSave.addActionListener(e -> btnSaveActionPerformed());
      panel1.add(btnSave);
    }
    contentPane.add(panel1, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,
      GridBagConstraints.CENTER, GridBagConstraints.BOTH,
      new Insets(0, 0, 0, 0), 0, 0));
    setSize(480, 604);
    setLocationRelativeTo(null);
  }// </editor-fold>//GEN-END:initComponents


  /*
   * Sort the list when the user changes the sort scheme.
   */
  private void cmbOrderActionPerformed() {
    switch (this.cmbOrder.getSelectedIndex()) {
      case 0:
        break;
      case 1:
        this.sort(new AlphaAscending());
        break;
      case 2:
        this.sort(new AlphaDescending());
        break;
      case 3:
        this.sort(new CountHighToLow());
        break;
      case 4:
        this.sort(new CountLowToHigh());
        break;
      case 5:
        this.sort(new ViewsHighToLow());
        break;
      case 6:
        this.sort(new ViewsLowToHigh());
      default:
        break;
    }
  }


  /*
   * Reorder the sets.
   */
  private void btnSaveActionPerformed() {
    List<String> photosetIds = new ArrayList<>();
    logger.info("New photoset order");
    for (int i = 0; i < this.listModel.size(); i++) {
      SetOrdererDTO dto = this.listModel.getElementAt(i);
      logger.info(dto.getId() + " : " + dto.getTitle());
      photosetIds.add(dto.getId());
    }
    BlockerPanel blocker = new BlockerPanel(this, resourceBundle.getString("SetOrderer.blocker.ordering"));
    setGlassPane(blocker);
    new OrderSetsWorker(blocker, photosetIds).execute();
  }


  /*
   * Cancel operation.
   */
  private void btnCancelActionPerformed() {
    this.setVisible(false);
    this.dispose();
  }


  /**
   * Sort the dto list using the specified comparator.
   *
   * <p>The list will be sorted, and the ListModel updated.</p>
   *
   * @param comparator the comparator used to sort the list.
   */
  private void sort(Comparator<SetOrdererDTO> comparator) {
    dtoList.sort(comparator);
    SwingUtilities.invokeLater(new UpdateListModel());
    jList1.scrollRectToVisible(jList1.getCellBounds(0, 0));
  }


  /**
   * Create a blocker panel, then start the worker and make the
   * dialog visible.
   */
  @Override
  public void setVisible(boolean visible) {
    // get to work
    BlockerPanel blocker = new BlockerPanel(this, resourceBundle.getString("SetOrderer.blocker.gettingsets"));
    this.setGlassPane(blocker);
    blocker.block(resourceBundle.getString("SetOrderer.blocker.working"));
    new SetOrdererDTOListWorker(blocker, listModel, this).execute();
    super.setVisible(visible);
  }


  /**
   * Sets the DTO list.
   *
   * @param dtoList list of set orderer dto's.
   */
  public void setDtoList(List<SetOrdererDTO> dtoList) {
    this.dtoList = dtoList;
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JComboBox cmbOrder;
  private JList<SetOrdererDTO> jList1;
  // End of variables declaration//GEN-END:variables


  /**
   * Class to update the ListModel.
   * <p>
   * This should be executed on the EventDispatch Thread using
   * SwingUtilities.invokeAndWait().
   */
  class UpdateListModel implements Runnable {
    @Override
    public void run() {
      listModel.clear();
      for (SetOrdererDTO dto : dtoList) {
        listModel.addElement(dto);
      }
    }
  }


  /**
   * Sort the list alphabetically by title.
   */
  class AlphaAscending implements Comparator<SetOrdererDTO>, Serializable {

    private static final long serialVersionUID = -7403145864736423464L;

    /**
     * Compares its two arguments for order.
     * Returns a negative integer, zero, or a positive integer as the
     * first argument is less than, equal to, or greater than the second.
     */
    @Override
    public int compare(SetOrdererDTO o1, SetOrdererDTO o2) {
      return (o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase()));
    }

  }


  /**
   * Sort the list reverse-alphabetically by title.
   */
  class AlphaDescending implements Comparator<SetOrdererDTO>, Serializable {

    private static final long serialVersionUID = 1132663312078002723L;

    /**
     * Compares its two arguments for order.
     * Returns a negative integer, zero, or a positive integer as the
     * first argument is less than, equal to, or greater than the second.
     */
    @Override
    public int compare(SetOrdererDTO o1, SetOrdererDTO o2) {
      return -(o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase()));
    }

  }


  /**
   * Sort the list by number of photos from high to low.
   */
  class CountHighToLow implements Comparator<SetOrdererDTO>, Serializable {

    private static final long serialVersionUID = -7208888297992426646L;

    /**
     * Compares its two arguments for order.
     * Returns a negative integer, zero, or a positive integer as the
     * first argument is less than, equal to, or greater than the second.
     */
    @Override
    public int compare(SetOrdererDTO o1, SetOrdererDTO o2) {
      if (o1.getPhotoCount() == o2.getPhotoCount()) {
        return 0;
      }

      return o1.getPhotoCount() > o2.getPhotoCount() ? -1 : 1;

    }

  }


  /**
   * Sort the list by number of photos from low to high.
   */
  class CountLowToHigh implements Comparator<SetOrdererDTO>, Serializable {

    private static final long serialVersionUID = -8559282815270125576L;

    /**
     * Compares its two arguments for order.
     * Returns a negative integer, zero, or a positive integer as the
     * first argument is less than, equal to, or greater than the second.
     */
    @Override
    public int compare(SetOrdererDTO o1, SetOrdererDTO o2) {
      if (o1.getPhotoCount() == o2.getPhotoCount()) {
        return 0;
      }

      return o1.getPhotoCount() > o2.getPhotoCount() ? 1 : -1;
    }
  }

  class ViewsHighToLow implements Comparator<SetOrdererDTO>, Serializable {
    private static final long serialVersionUID = 312216680968178328L;

    @Override
    public int compare(SetOrdererDTO o1, SetOrdererDTO o2) {
      if (o1.getViewCount() == o2.getViewCount()) {
        return 0;
      }

      return o1.getViewCount() > o2.getViewCount() ? -1 : 1;
    }
  }

  class ViewsLowToHigh implements Comparator<SetOrdererDTO>, Serializable {
    private static final long serialVersionUID = 8977498108185209791L;

    @Override
    public int compare(SetOrdererDTO o1, SetOrdererDTO o2) {
      if (o1.getViewCount() == o2.getViewCount()) {
        return 0;
      }

      return o1.getViewCount() > o2.getViewCount() ? 1 : -1;
    }
  }


  /**
   * MouseListener to implement drag and drop reordering of sets.
   */
  class ReorderListener extends MouseAdapter {

    private JList<SetOrdererDTO> list;

    private int pressIndex = 0;

    private int releaseIndex = 0;


    ReorderListener(JList<SetOrdererDTO> list) {
      if (!(list.getModel() instanceof DefaultListModel)) {
        throw new IllegalArgumentException("List must have a DefaultListModel");
      }
      this.list = list;
    }


    @Override
    public void mousePressed(MouseEvent e) {
      pressIndex = list.locationToIndex(e.getPoint());
    }


    @Override
    public void mouseReleased(MouseEvent e) {
      releaseIndex = list.locationToIndex(e.getPoint());
      if (releaseIndex != pressIndex && releaseIndex != -1) {
        reorder();
      }
    }


    @Override
    public void mouseDragged(MouseEvent e) {
      mouseReleased(e);
      pressIndex = releaseIndex;
    }


    private void reorder() {
      DefaultListModel<SetOrdererDTO> model = (DefaultListModel<SetOrdererDTO>) list.getModel();
      SetOrdererDTO dragee = model.elementAt(pressIndex);
      model.removeElementAt(pressIndex);
      model.insertElementAt(dragee, releaseIndex);
    }

  }
}
