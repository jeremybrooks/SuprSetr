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

package net.jeremybrooks.suprsetr.tutorial;

import org.apache.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ResourceBundle;

/**
 * This is the Tutorial dialog.
 *
 * <p>It will display multiple pages of content, allowing the user to navigate
 * back and forth. When they finish, it will hide itself, and the user has the
 * option to close the window before they finish the tutorial.</p>
 *
 * @author jeremyb
 */
public class Tutorial extends javax.swing.JDialog {

  private static final long serialVersionUID = 1675398273280362432L;
  /* Logging. */
  private Logger logger = Logger.getLogger(Tutorial.class);

  /* The current page of the tutorial. */
  private int currentPage = 1;

  /* The last page. */
  private int lastPage = 5;

  /* Icon for the Next button. */
  private ImageIcon nextIcon = new ImageIcon(getClass().getResource("/images/next16.png"));

  /* Icon for the Finish button */
  private ImageIcon finishIcon = new ImageIcon(getClass().getResource("/images/s16.png"));

  private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.tutorial");

  private String tutorialContentFilename;

  /**
   * Creates new form Tutorial1
   *
   * @param parent the parent component.
   * @param modal  if true, dialog is modal.
   */
  public Tutorial(java.awt.Frame parent, boolean modal) {
    super(parent, modal);
    tutorialContentFilename = resourceBundle.getString("Tutorial.content.file.name");

    initComponents();

    setIconImage(new ImageIcon(getClass().getResource("/images/s16.png")).getImage());

    this.btnBack.setVisible(false);
    this.loadPage();
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
    jEditorPane1 = new JEditorPane();
    panel1 = new JPanel();
    btnBack = new JButton();
    btnNext = new JButton();

    //======== this ========
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        formWindowClosing();
      }
    });
    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());

    //======== jScrollPane1 ========
    {
      jScrollPane1.setBorder(new EmptyBorder(5, 5, 5, 5));

      //---- jEditorPane1 ----
      jEditorPane1.setContentType("text/html");
      jEditorPane1.setEditable(false);
      jScrollPane1.setViewportView(jEditorPane1);
    }
    contentPane.add(jScrollPane1, BorderLayout.CENTER);

    //======== panel1 ========
    {
      panel1.setLayout(new FlowLayout(FlowLayout.RIGHT));

      //---- btnBack ----
      btnBack.setIcon(new ImageIcon(getClass().getResource("/images/back16.png")));
      btnBack.setText(bundle.getString("Tutorial.btnBack.text"));
      btnBack.addActionListener(e -> btnBackActionPerformed());
      panel1.add(btnBack);

      //---- btnNext ----
      btnNext.setIcon(new ImageIcon(getClass().getResource("/images/next16.png")));
      btnNext.setText(bundle.getString("Tutorial.btnNext.text"));
      btnNext.setHorizontalTextPosition(SwingConstants.LEFT);
      btnNext.addActionListener(e -> btnNextActionPerformed());
      panel1.add(btnNext);
    }
    contentPane.add(panel1, BorderLayout.SOUTH);
    setSize(631, 341);
    setLocationRelativeTo(null);
  }// </editor-fold>//GEN-END:initComponents


  /**
   * Increment the current page, then load content.
   * <p>
   * If we are on the last page, close the tutorial.
   */
  private void btnNextActionPerformed() {
    if (this.btnNext.getText().equals(resourceBundle.getString("Tutorial.btnNext.text"))) {
      this.currentPage++;
      if (this.currentPage > this.lastPage) {
        this.currentPage = this.lastPage;
      }
      this.loadPage();
    } else {
      this.dispose();
      this.setVisible(false);
    }
  }


  /**
   * If the user tries to close the tutorial before they are on the last page,
   * make them confirm.
   */
  private void formWindowClosing() {
    if (this.currentPage < this.lastPage) {
      int confirm = JOptionPane.showConfirmDialog(this,
          resourceBundle.getString("Tutorial.dialog.exit.message"),
          resourceBundle.getString("Tutorial.dialog.exit.title"),
          JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE);

      if (confirm == JOptionPane.YES_OPTION) {
        this.dispose();
        this.setVisible(false);
      }
    } else {
      this.dispose();
      this.setVisible(false);
    }
  }


  /**
   * Decrement the current page, then load the content.
   */
  private void btnBackActionPerformed() {
    this.currentPage--;
    if (this.currentPage < 1) {
      this.currentPage = 1;
    }
    this.loadPage();
  }


  /**
   * This method will set the button states, then load the next page of the
   * tutorial.
   */
  private void loadPage() {
    // The text and icon for the Next button changes if we are on the last page
    if (this.currentPage == this.lastPage) {
      this.btnNext.setText(resourceBundle.getString("Tutorial.btnNext.text.start"));
      this.btnNext.setIcon(this.finishIcon);
    } else {
      this.btnNext.setText(resourceBundle.getString("Tutorial.btnNext.text"));
      this.btnNext.setIcon(this.nextIcon);
    }

    // Hide the Back button if we are on the first page
    this.btnBack.setVisible(this.currentPage > 1);

    // Set the title of the tutorial window
    this.setTitle(resourceBundle.getString("Tutorial.title.text") + " " + this.currentPage + "/" + this.lastPage);

    // Now load the content for the current page of the tutorial
    String line;
    StringBuilder sb = new StringBuilder();
    String page = "/net/jeremybrooks/suprsetr/tutorial/" + tutorialContentFilename + this.currentPage + ".html";

    try (BufferedReader in = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(page)))) {
      logger.debug("Loading page " + page);
      while ((line = in.readLine()) != null) {
        sb.append(line);
      }
    } catch (Exception e) {
      logger.warn("COULD NOT LOAD THE RESOURCE FOR PAGE " + this.currentPage, e);
    }
    this.jEditorPane1.setText(sb.toString());
    this.jEditorPane1.setCaretPosition(0);        // Make sure we are at the top of the displayed content
  }


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JScrollPane jScrollPane1;
  private JEditorPane jEditorPane1;
  private JPanel panel1;
  private JButton btnBack;
  private JButton btnNext;
  // End of variables declaration//GEN-END:variables

}
