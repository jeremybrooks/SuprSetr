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

import net.jeremybrooks.suprsetr.dao.DAOHelper;
import net.jeremybrooks.suprsetr.dao.LookupDAO;
import net.jeremybrooks.suprsetr.flickr.JinxFactory;
import net.jeremybrooks.suprsetr.tutorial.Tutorial;
import net.jeremybrooks.suprsetr.utils.NetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JOptionPane;
import java.awt.Desktop;
import java.io.File;
import java.sql.Connection;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Application entry point.
 *
 * <p>This class should set things up for the rest of the application, make sure
 * necessary resources are available, and then display the main window.</p>
 *
 * @author Jeremy Brooks
 */
public class Main {

  private static Logger logger = LogManager.getLogger(Main.class);

  /**
   * The application version.
   */
  static String VERSION = "";

  /**
   * Location of SuprSetr's files.
   */
  public static File configDir;
  private static File backupDir;

  /* These are the "private" properties, such as API keys. */
  private static Properties privateProperties;

  private static ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.misc");

  /**
   * Main.
   * <p>No command line arguments are supported.</p>
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {

    // test for Desktop API support
    if (!Desktop.isDesktopSupported()) {
      JOptionPane.showMessageDialog(null,
          resourceBundle.getString("Main.dialog.nodesktop.message"),
          resourceBundle.getString("Main.dialog.nodesktop.title"),
          JOptionPane.ERROR_MESSAGE);
      System.exit(2);
    }

    // If running on a Mac, set up the event handler
    if (System.getProperty("os.name").contains("Mac")) {
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      try {
        Class.forName("net.jeremybrooks.suprsetr.OSXSetup").getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        logger.error("Could not find class.", e);
      }
    }

    // SET VERSION
    try {
      Main.VERSION = Main.class.getPackage().getImplementationVersion();
      privateProperties = new Properties();
      privateProperties.load(Main.class.getClassLoader().getResourceAsStream("net/jeremybrooks/suprsetr/private.properties"));
    } catch (Exception e) {
      Main.VERSION = "0.0.0";
    }

    // SET CONFIG DIR BASED ON USER HOME
    Main.configDir = new File(System.getProperty("user.home"), ".suprsetr");
    if (!Main.configDir.exists()) {
      if (!Main.configDir.mkdirs()) {
        JOptionPane.showMessageDialog(null,
            resourceBundle.getString("Main.dialog.error.noconfig.message"),
            resourceBundle.getString("Main.dialog.error.noconfig.title"),
            JOptionPane.ERROR_MESSAGE);
        System.exit(1);
      }
    }

    // this is the first logging - log4j2 will find its configuration file in the classpath and configure itself
    logger.info("SuprSetr version {} starting with Java version {} in {}",
        Main.VERSION, System.getProperty("java.version"), System.getProperty("java.home"));

    // delete old files that are no longer needed
    File oldLogConfig = new File(Main.configDir, "log.properties");
    if (oldLogConfig.exists()) {
      if (oldLogConfig.delete()) {
        logger.info("Deleted old config file {}", oldLogConfig.getAbsolutePath());
      } else {
        logger.warn("Could not delete old file {}", oldLogConfig.getAbsolutePath());
      }
    }

    // Set the default database directory
    System.setProperty("derby.system.home", configDir.getAbsolutePath());

    if (new File(configDir, "SuprSetrDB").exists()) {
      // DB exists, so make sure we can connect to it
      try (Connection conn = DAOHelper.getConnection()) {
        logger.info("Database connection test: success.");
      } catch (Exception e) {
        logger.error("Database connection failed.", e);
        JOptionPane.showMessageDialog(null,
            resourceBundle.getString("Main.dialog.error.dbconn.message"),
            resourceBundle.getString("Main.dialog.error.dbconn.title"),
            JOptionPane.ERROR_MESSAGE);
        System.exit(1);
      }
    } else {
      // DB does not exist, so create it
      try {
        DAOHelper.createDatabase();
      } catch (Exception e) {
        logger.error("Could not create database.", e);
        JOptionPane.showMessageDialog(null,
            resourceBundle.getString("Main.dialog.error.db.message"),
            resourceBundle.getString("Main.dialog.error.db.title"),
            JOptionPane.ERROR_MESSAGE);

        System.exit(1);
      }
    }

    try {
      DAOHelper.upgradeDatabase();
    } catch (Exception e) {
      logger.error("Could not upgrade database schema.", e);

      JOptionPane.showMessageDialog(null,
          resourceBundle.getString("Main.dialog.error.dbschema.message"),
          resourceBundle.getString("Main.dialog.error.dbschema.title"),
          JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }

    logger.info("Database schema version {}", LookupDAO.getDatabaseVersion());

    // Set some default key/value pairs in the database
    if (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_CHECK_FOR_UPDATE) == null) {
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_CHECK_FOR_UPDATE, DAOHelper.booleanToString(true));
    }
    if (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_FAVRTAGR_INTERVAL) == null) {
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_FAVRTAGR_INTERVAL, SSConstants.DEFAULT_FAVRTAGR_INTERVAL);
    }
    if (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_USE_PROXY) == null) {
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_USE_PROXY, DAOHelper.booleanToString(false));
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_PROXY_HOST, "");
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_PROXY_PORT, "");
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_PROXY_USER, "");
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_PROXY_PASS, "");
    }
    if (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_HIDE_UNMANAGED) == null) {
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_HIDE_UNMANAGED, DAOHelper.booleanToString(false));
    }
    if (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_ADD_MANAGED) == null) {
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_ADD_MANAGED, DAOHelper.booleanToString(false));
    }
    if (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_CASE_SENSITIVE) == null) {
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_CASE_SENSITIVE, DAOHelper.booleanToString(false));
    }
    if (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_USE_SYSTEM) == null) {
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_PROXY_USE_SYSTEM, DAOHelper.booleanToString(true));
    }
    if (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_LIST_SORT_ORDER) == null) {
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_LIST_SORT_ORDER, SSConstants.LIST_SORT_ATOZ);
    }
    if (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_DETAIL_LOG) == null) {
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_DETAIL_LOG, DAOHelper.booleanToString(false));
    }
    if (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TAG_TYPE) == null) {
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_TAG_TYPE, "0");
    }
    if (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_BACKUP_AT_EXIT) == null) {
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_BACKUP_AT_EXIT, DAOHelper.booleanToString(true));
    }
    if (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_BACKUP_COUNT) == null) {
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_BACKUP_COUNT, "10");
    }
    if (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_BACKUP_DIRECTORY) == null) {
      backupDir = new File(configDir, "backup");
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_BACKUP_DIRECTORY, backupDir.getAbsolutePath());
    } else {
      backupDir = new File(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_BACKUP_DIRECTORY));
    }
    if (!backupDir.exists()) {
      if (!backupDir.mkdirs()) {
        logger.warn("Could not create the backup directory {}", backupDir.getAbsolutePath());
      }
    }

    JinxFactory.getInstance().init(getPrivateProperty("FLICKR_KEY"), getPrivateProperty("FLICKR_SECRET"));

    // Turn on Jinx logging if needed
    if (DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_DETAIL_LOG))) {
      JinxFactory.getInstance().setLogger(new MyJinxLogger());
    }

    // Set up proxy
    if (DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_USE_PROXY))) {
      String host = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_HOST);
      String port = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_PORT);
      final String user = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_USER);
      final String pass = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_PASS);
      logger.info("Using proxy " + host + ":" + port);
      NetUtil.enableProxy(host, port, user, pass.toCharArray());
    }


    // Display tutorial
    if (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TUTORIAL_DISPLAYED) == null ||
        LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TUTORIAL_DISPLAYED).isEmpty()) {
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_TUTORIAL_DISPLAYED, DAOHelper.booleanToString(true));
      new Tutorial(null, true).setVisible(true);
    }

    try {
      // Finally, show the main window
      java.awt.EventQueue.invokeLater(() -> {
        MainWindow main = new MainWindow();
        main.setVisible(true);
        main.doAuth();
        main.executeLoadFlickrSetsWorker();
      });
    } catch (Throwable t) {
      System.out.println("A fatal error has occurred.");
      t.printStackTrace();
      logger.fatal("A fatal error has occurred.", t);
      JOptionPane.showMessageDialog(null,
          resourceBundle.getString("Main.dialog.error.message"),
          resourceBundle.getString("Main.dialog.error.title"),
          JOptionPane.ERROR_MESSAGE);
      System.exit(2);
    }


    // Check for updates
    if (DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_CHECK_FOR_UPDATE))) {
      (new Thread(new VersionChecker(), "VersionCheckerThread")).start();
    }
  }

  public static String getPrivateProperty(String key) {
    return Main.privateProperties.getProperty(key);
  }
}
