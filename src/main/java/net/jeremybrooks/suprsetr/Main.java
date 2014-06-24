/*
 * SuprSetr is Copyright 2010-2014 by Jeremy Brooks
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
import net.jeremybrooks.suprsetr.flickr.JinxFactory;
import net.jeremybrooks.suprsetr.tutorial.Tutorial;
import net.jeremybrooks.suprsetr.utils.NetUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.swing.JOptionPane;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Application entry point.
 * <p/>
 * <p>This class should set things up for the rest of the application, make sure
 * necessary resources are available, and then display the main window.</p>
 *
 * @author jeremyb
 */
public class Main {

    /**
     * Logging.
     */
    private static Logger logger = Logger.getLogger(Main.class);

    /**
     * The application version.
     */
    public static String VERSION = "";

    /**
     * Location of SuprSetr's files.
     */
    public static File configDir;

    /**
     * Some logging properties are here.
     */
    private static Properties loggingProperties;

    /**
     * These are the "private" properties, such as API keys.
     */
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
            new OSXSetup();
        }

        // ADD SHUTDOWN HOOK
        Runtime.getRuntime().addShutdownHook(new Thread(new Reaper(), "ReaperThread"));

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

        // LOG FILE SIZE IS STORED IN A PROPERTIES FILE, SO WE CAN GET IT
        // BEFORE THE DB IS SET UP
        loggingProperties = new Properties();
        try {
            loggingProperties.load(new FileInputStream(new File(Main.configDir, "log.properties")));
        } catch (Exception e) {
            loggingProperties.setProperty("size", "1MB");
            loggingProperties.setProperty("index", "2");
            storeLoggingProperties();
        }

        // SET UP LOGGING
        Properties p = new Properties();
        p.setProperty("log4j.rootLogger", "DEBUG,FILE");
        p.setProperty("log4j.appender.FILE", "org.apache.log4j.RollingFileAppender");
        p.setProperty("log4j.appender.FILE.Threshold", "DEBUG");
        p.setProperty("log4j.appender.FILE.layout", "org.apache.log4j.PatternLayout");
        p.setProperty("log4j.appender.FILE.layout.ConversionPattern", "%p %c [%t] %d{ISO8601} - %m%n");
        p.setProperty("log4j.appender.FILE.File", (new File(Main.configDir, "suprsetr.log")).getAbsolutePath());
        p.setProperty("log4j.appender.FILE.MaxFileSize", loggingProperties.getProperty("size"));
        p.setProperty("log4j.appender.FILE.MaxBackupIndex", loggingProperties.getProperty("index"));

        PropertyConfigurator.configure(p);

        logger.info("Logging configuration: " + p);
        logger.info("SuprSetr version " + Main.VERSION + " starting with Java version " + System.getProperty("java.version") +
                " in " + System.getProperty("java.home"));

        // Set the default database directory
        System.setProperty("derby.system.home", configDir.getAbsolutePath());

        if (new File(configDir, "SuprSetrDB").exists()) {
            // DB exists, so make sure we can connect to it
            try {
                Connection conn = DAOHelper.getConnection();
                DAOHelper.close(conn);
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
                logger.error("Could not create/upgrade database.", e);
                JOptionPane.showMessageDialog(null,
                        resourceBundle.getString("Main.dialog.error.db.message"),
                        resourceBundle.getString("Main.dialog.error.db.title"),
                        JOptionPane.ERROR_MESSAGE);

                System.exit(1);
            }
        }

        // check database schema version
        int dbVersion = LookupDAO.getDatabaseVersion();
        logger.info("Database schema version " + dbVersion);
        while (dbVersion != SSConstants.DATABASE_SCHEMA_CURRENT_VERSION) {
            try {
                DAOHelper.upgradeDatabase();
            } catch (Exception e) {
                logger.error("COULD NOT UPGRADE SCHEMA.", e);

                JOptionPane.showMessageDialog(null,
                        resourceBundle.getString("Main.dialog.error.dbschema.message"),
                        resourceBundle.getString("Main.dialog.error.dbschema.title"),
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            dbVersion = LookupDAO.getDatabaseVersion();
            logger.info("Database schema version " + dbVersion);
        }


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

        JinxFactory.getInstance().init(getPrivateProperty("FLICKR_KEY"), getPrivateProperty("FLICKR_SECRET"));
//		Jinx.getInstance().init(getPrivateProperty("FLICKR_KEY"), getPrivateProperty("FLICKR_SECRET"));
        // Turn on Jinx logging if needed
        if (DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_DETAIL_LOG))) {
            JinxFactory.getInstance().setLogger(new MyJinxLogger());
        }

        // Set up proxy
        if (DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_USE_PROXY))) {
            if (DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_USE_SYSTEM))) {
                logger.info("Using system proxy settings");
                NetUtil.enableSystemProxy();
            } else {
                String host = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_HOST);
                String port = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_PORT);
                final String user = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_USER);
                final String pass = LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_PROXY_PASS);
                logger.info("Using proxy " + host + ":" + port);
                NetUtil.enableProxy(host, port, user, pass.toCharArray());
            }
        }


        // Display tutorial
        if (LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TUTORIAL_DISPLAYED) == null ||
                LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_TUTORIAL_DISPLAYED).isEmpty()) {
            LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_TUTORIAL_DISPLAYED, DAOHelper.booleanToString(true));
            new Tutorial(null, true).setVisible(true);
        }

        try {
            // Finally, show the main window
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    MainWindow main = new MainWindow();
                    main.setVisible(true);
                    main.doAuth();
                    main.executeLoadFlickrSetsWorker();
                }
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

    static void storeLoggingProperties() {
        try (FileOutputStream out = new FileOutputStream(new File(Main.configDir, "log.properties"))) {
            loggingProperties.store(out, "SuprSetr logging properties");
        } catch (Exception e) {
            logger.warn("Error saving logging properties.");
        }
    }

    static Properties getLoggingProperties() {
        return Main.loggingProperties;
    }

    public static String getPrivateProperty(String key) {
        return Main.privateProperties.getProperty(key);
    }
}
