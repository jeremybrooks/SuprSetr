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

package net.jeremybrooks.suprsetr.dao;

import net.jeremybrooks.suprsetr.SSConstants;
import net.jeremybrooks.suprsetr.utils.SSUtils;
import org.apache.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * General database utility methods for SuprSetr.
 *
 * @author jeremyb
 */
public class DAOHelper {

  /**
   * Logging.
   */
  private static Logger logger = Logger.getLogger(DAOHelper.class);

  /**
   * Database connection string.
   */
  private static final String DB_CONN_STRING = "jdbc:derby:SuprSetrDB";

  /**
   * SQL to create the photoset table.
   */
  private static final String SQL_CREATE_TABLE_PHOTOSET =
      "CREATE table PHOTOSET ( "
          + "ID			VARCHAR(30) PRIMARY KEY, "
          + "TITLE		VARCHAR(128), "
          + "DESCRIPTION	VARCHAR(2000), "
          + "FARM		VARCHAR(10), "
          + "SERVER		VARCHAR(10), "
          + "PHOTO_COUNT	INTEGER, "
          + "PRIMARY_PHOTO_ID	VARCHAR(32), "
          + "SECRET		VARCHAR(16), "
          + "URL		VARCHAR(128), "
          + "PRIMARY_PHOTO_ICON	BLOB, "
          + "TAG_MATCH_MODE	VARCHAR(8), "
          + "TAGS		VARCHAR(2000), "
          + "MIN_UPLOAD_DATE	TIMESTAMP, "
          + "MAX_UPLOAD_DATE	TIMESTAMP, "
          + "MIN_TAKEN_DATE	TIMESTAMP, "
          + "MAX_TAKEN_DATE	TIMESTAMP, "
          + "MATCH_UPLOAD_DATES	VARCHAR(1), "
          + "MATCH_TAKEN_DATES	VARCHAR(1), "
          + "LAST_REFRESH_DATE	TIMESTAMP, "
          + "SYNC_TIMESTAMP	BIGINT, "
          + "MANAGED		VARCHAR(1),"
          + "SORT_ORDER	INTEGER,"
          + "SEND_TWEET       VARCHAR(1),"
          + "TWEET_TEMPLATE   VARCHAR(200)"
          + ")";

  /**
   * SQL to create the lookup table.
   */
  private static final String SQL_CREATE_TABLE_LOOKUP =
      "CREATE table LOOKUP ( "
          + "K	    VARCHAR(64),"
          + "VALUE  VARCHAR(1024)"
          + ")";

  /**
   * SQL to compress database tables.
   */
  private static final String SQL_COMPRESS_TABLE =
      "CALL SYSCS_UTIL.SYSCS_COMPRESS_TABLE(?, ?, 1)";

  /**
   * SQL to perform backup.
   */
  private static final String SQL_BACKUP_DATABASE =
      "CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)";


  /**
   * No instances allowed. All methods are public static.
   */
  private DAOHelper() {
  }


  /**
   * Shuts down the database.
   *
   * @throws Exception shutting down the database will always throw this.
   */
  public static void shutdown() throws Exception {
    DriverManager.getConnection("jdbc:derby:SuprSetrDB;shutdown=true");
  }


  /**
   * Create the database.
   * <p/>
   * <p>The photoset and lookup tables will be created.</p>
   *
   * @throws Exception if there are any errors.
   */
  public static void createDatabase() throws Exception {
    Connection conn = null;
    Statement s = null;
    String strUrl = "jdbc:derby:SuprSetrDB;create=true";

    try {
      conn = DriverManager.getConnection(strUrl);
      s = conn.createStatement();
      s.execute(SQL_CREATE_TABLE_PHOTOSET);
      s.execute(SQL_CREATE_TABLE_LOOKUP);

      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_DATABASE_VERSION, "1");
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_ADD_VIA, DAOHelper.booleanToString(true));
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_REFRESH_WAIT, SSConstants.DEFAULT_REFRESH_WAIT);
    } catch (Exception e) {
      logger.error("checkDatabase: ERROR CREATING DATABASE.", e);
    } finally {
      DAOHelper.close(conn, s);
    }
  }


  /**
   * Get a connection to the database.
   *
   * @return database connection.
   * @throws SQLException if there are any errors.
   */
  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(DB_CONN_STRING);
  }


  /**
   * Close database resources.
   * <p/>
   * Errors will be logged, not thrown.
   *
   * @param conn the connection to close.
   * @param s    the statement to close.
   * @param rs   the result set to close.
   */
  public static void close(Connection conn, Statement s, ResultSet rs) {
    DAOHelper.close(rs);
    DAOHelper.close(s);
    DAOHelper.close(conn);
  }


  /**
   * Close database resources.
   * <p/>
   * Errors will be logged, not thrown.
   *
   * @param conn the connection to close.
   * @param s    the statement to close.
   */
  public static void close(Connection conn, Statement s) {
    DAOHelper.close(conn, s, null);
  }


  /**
   * Close database resources.
   * <p/>
   * Errors will be logged, not thrown.
   *
   * @param conn the connection to close.
   * @param rs   the result set to close.
   */
  public static void close(Connection conn, ResultSet rs) {
    DAOHelper.close(conn, null, rs);
  }


  /**
   * Close database resources.
   * <p/>
   * Errors will be logged, not thrown.
   *
   * @param s  the statement to close.
   * @param rs the result set to close.
   */
  public static void close(Statement s, ResultSet rs) {
    DAOHelper.close(null, s, rs);
  }


  /**
   * Close database resources.
   * <p/>
   * Errors will be logged, not thrown.
   *
   * @param conn the connection to close.
   */
  public static void close(Connection conn) {
    if (conn != null) {
      try {
        conn.close();
      } catch (Exception e) {
        logger.warn("ERROR CLOSING CONNECTION.", e);
      }
    }
  }


  /**
   * Close database resources.
   * <p/>
   * Errors will be logged, not thrown.
   *
   * @param s the statement to close.
   */
  public static void close(Statement s) {
    if (s != null) {
      try {
        s.close();
      } catch (Exception e) {
        logger.warn("ERROR CLOSING STATEMENT.", e);
      }
    }
  }


  /**
   * Close database resources.
   * <p/>
   * Errors will be logged, not thrown.
   *
   * @param rs the result set to close.
   */
  public static void close(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (Exception e) {
        logger.warn("ERROR CLOSING CONNECTION.", e);
      }
    }
  }


  /**
   * Convert an icon to bytes.
   * <p/>
   * <p>If the icon is null, or if there is an error, this method will
   * return null.</p>
   *
   * @param icon the icon to convert.
   * @return byte array representing the icon.
   */
  static byte[] iconToBytes(ImageIcon icon) {
    if (icon == null) {
      return null;
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] bytes = null;

    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(icon);
      oos.flush();
      bytes = baos.toByteArray();
    } catch (Exception e) {
      logger.error("COULD NOT CONVERT ICON TO BYTES.", e);
    }

    return bytes;
  }


  /**
   * Convert bytes to icon.
   * <p/>
   * <p>If the icon is null, or there is an error, this method will return
   * null</p>
   *
   * @param bytes the bytes to convert into an icon.
   * @return the icon.
   */
  static ImageIcon bytesToIcon(byte[] bytes) {
    if (bytes == null) {
      return null;
    }

    ImageIcon icon = null;

    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
      Object o = ois.readObject();
      if (o instanceof ImageIcon) {
        icon = (ImageIcon) o;
      }
    } catch (Exception e) {
      logger.error("COULD NOT CONVERT BYTES TO ICON.", e);
    }

    return icon;

  }


  /**
   * Convert a string to a boolean value.
   * <p/>
   * <p>If the string is equal to Y or y, return true, otherwise return
   * false.</p>
   *
   * @param flag the string value.
   * @return true if the string is Y or y.
   */
  public static boolean stringToBoolean(String flag) {
    boolean ret = false;

    if (flag != null) {
      if (flag.trim().equalsIgnoreCase("y")) {
        ret = true;
      }
    }

    return ret;
  }


  /**
   * Convert a boolean value to a string.
   * <p/>
   * <p>Return Y for true, N for false.</p>
   *
   * @param flag boolean value.
   * @return Y for true, N for false.
   */
  public static String booleanToString(boolean flag) {
    return flag ? "Y" : "N";
  }


  public static void upgradeDatabase() throws Exception {
    int version = LookupDAO.getDatabaseVersion();
    if (version > SSConstants.DATABASE_SCHEMA_CURRENT_VERSION) {
      logger.fatal("Database schema is " + version + ", but expecting version " +
          SSConstants.DATABASE_SCHEMA_CURRENT_VERSION + ".");

      JOptionPane.showMessageDialog(null, "The database schema is version " + version + ",\n" +
              "but this version of SuprSetr requires schema version " +
              SSConstants.DATABASE_SCHEMA_CURRENT_VERSION + ".\n" +
              "Are you running an old version of SuprSetr?",
          "Incompatible Schema",
          JOptionPane.ERROR_MESSAGE);

      throw new Exception("Incompatible schema.");
    }

    switch (version) {

      case 1:
        // DB is at version 1, so upgrade to version 2
        logger.info("Attempting to upgrade schema to version 2.");
        DAOHelper.upgradeToVersion2();
        logger.info("Upgrade to schema version 2: success.");
        break;

      case 2:
        // DB is at version 2, so upgrade to version 3
        logger.info("Attempting to upgrade schema to version 3.");
        DAOHelper.upgradeToVersion3();
        logger.info("Upgrade to schema version 3: success.");
        break;

      case 3:
        logger.info("Attempting to upgrade schema to version 4.");
        DAOHelper.upgradeToVersion4();
        logger.info("Upgrade to schema version 4: success.");
        break;

      case 4:
        logger.info("Attempting to upgrade schema to version 5.");
        DAOHelper.upgradeToVersion5();
        logger.info("Upgrade to schema version 5: success.");
        break;

      case 5:
        logger.info("Attempting to upgrade schema to version 6.");
        DAOHelper.upgradeToVersion6();
        logger.info("Upgrade to schema version 6: success.");
        break;

      case 6:
        logger.info("Attempting to upgrade schema to version 7.");
        DAOHelper.upgradeToVersion7();
        logger.info("Upgrade to schema version 7: success.");
        break;

      case 7:
        logger.info("Attempting to upgrade schema to version 8.");
        DAOHelper.upgradeToVersion8();
        logger.info("Upgrade to schema version 8: success.");
        break;

      case 8:
        // DB is already up to date; nothing to do
        break;

      default:
        break;
    }
  }


  private static void upgradeToVersion2() throws Exception {
    Connection conn = null;
    Statement s = null;

    try {
      conn = DAOHelper.getConnection();
      s = conn.createStatement();
      s.execute("ALTER TABLE PHOTOSET ADD COLUMN LOCK_PRIMARY_PHOTO VARCHAR(1)");
      logger.info("Added column LOCK_PRIMARY_PHOTO to PHOTOSET table.");

      s.execute("UPDATE PHOTOSET SET LOCK_PRIMARY_PHOTO = 'N'");
      logger.info("Added default of 'N' to LOCK_PRIMARY_PHOTO column.");

      s.execute("ALTER TABLE PHOTOSET ALTER COLUMN DESCRIPTION SET DATA TYPE VARCHAR(32000)");
      logger.info("Increased size of DESCRIPTION column.");

      // no errors, so update the version
      LookupDAO.setDatabaseVersion(2);

    } catch (Exception e) {
      logger.error("COULD NOT UPGRADE SCHEMA TO VERSION 2!");
      throw e;
    } finally {
      DAOHelper.close(conn, s);
    }

  }

  private static void upgradeToVersion3() throws Exception {
    Connection conn = null;
    Statement s = null;

    try {
      conn = DAOHelper.getConnection();
      s = conn.createStatement();

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN PRIVACY INTEGER");
      s.execute("UPDATE PHOTOSET SET PRIVACY = 0");
      logger.info("Added column PRIVACY to PHOTOSET table with value 0.");

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN SAFE_SEARCH INTEGER");
      s.execute("UPDATE PHOTOSET SET SAFE_SEARCH = 0");
      logger.info("Added column SAFE_SEARCH to PHOTOSET table with value 0.");

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN CONTENT_TYPE INTEGER");
      s.execute("UPDATE PHOTOSET SET CONTENT_TYPE = 6");
      logger.info("Added column CONTENT_TYPE to PHOTOSET table with value 6.");

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN MEDIA_TYPE INTEGER");
      s.execute("UPDATE PHOTOSET SET MEDIA_TYPE = 0");
      logger.info("Added column MEDIA_TYPE to PHOTOSET table with value 0.");

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN GEOTAGGED INTEGER");
      s.execute("UPDATE PHOTOSET SET GEOTAGGED = 0");
      logger.info("Added column GEOTAGGED to PHOTOSET table with value 0.");

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN IN_COMMONS VARCHAR(1)");
      s.execute("UPDATE PHOTOSET SET IN_COMMONS = 'N'");
      logger.info("Added column IN_COMMONS to PHOTOSET table with value 'N'.");

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN IN_GALLERY VARCHAR(1)");
      s.execute("UPDATE PHOTOSET SET IN_GALLERY = 'N'");
      logger.info("Added column IN_GALLERY to PHOTOSET table with value 'N'.");

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN IN_GETTY VARCHAR(1)");
      s.execute("UPDATE PHOTOSET SET IN_GETTY = 'N'");
      logger.info("Added column IN_GETTY to PHOTOSET table with value 'N'.");

      // no errors, so update the version
      LookupDAO.setDatabaseVersion(3);

    } catch (Exception e) {
      logger.error("COULD NOT UPGRADE SCHEMA TO VERSION 3!");
      throw e;
    } finally {
      DAOHelper.close(conn, s);
    }
  }

  private static void upgradeToVersion4() throws Exception {
    Connection conn = null;
    Statement s = null;

    try {
      conn = DAOHelper.getConnection();
      s = conn.createStatement();

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN LIMIT_SIZE VARCHAR(1)");
      s.execute("UPDATE PHOTOSET SET LIMIT_SIZE = 'N'");
      logger.info("Added column LIMIT_SIZE to PHOTOSET table with value 'N'.");

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN SIZE_LIMIT INTEGER");
      s.execute("UPDATE PHOTOSET SET SIZE_LIMIT = 0");
      logger.info("Added column SIZE_LIMIT to PHOTOSET table with value 0.");

      // no errors, so update the version
      LookupDAO.setDatabaseVersion(4);

    } catch (Exception e) {
      logger.error("COULD NOT UPGRADE SCHEMA TO VERSION 4!");
      throw e;
    } finally {
      DAOHelper.close(conn, s);
    }
  }

  /*
   * Schema version 5 supports the "On This Day" set creation.
   */
  private static void upgradeToVersion5() throws Exception {
    Connection conn = null;
    Statement s = null;

    try {
      conn = DAOHelper.getConnection();
      s = conn.createStatement();

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN ON_THIS_DAY VARCHAR(1)");
      s.execute("UPDATE PHOTOSET SET ON_THIS_DAY = 'N'");
      logger.info("Added column ON_THIS_DAY to PHOTOSET table with value 'N'.");

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN OTD_MONTH INTEGER");
      s.execute("UPDATE PHOTOSET SET OTD_MONTH = 1");
      logger.info("Added column OTD_MONTH to PHOTOSET table with value 1.");

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN OTD_DAY INTEGER");
      s.execute("UPDATE PHOTOSET SET OTD_DAY = 1");
      logger.info("Added column OTD_DAY to PHOTOSET table with value 1.");

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN OTD_YEAR_START INTEGER");
      s.execute("UPDATE PHOTOSET SET OTD_YEAR_START = 1995");
      logger.info("Added column OTD_YEAR_START to PHOTOSET table with value 1995.");

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN OTD_YEAR_END INTEGER");
      s.execute("UPDATE PHOTOSET SET OTD_YEAR_END = " + SSUtils.getCurrentYear());
      logger.info("Added column OTD_YEAR_END to PHOTOSET table with value " + SSUtils.getCurrentYear() + ".");

      // no errors, so update the version
      LookupDAO.setDatabaseVersion(5);

    } catch (Exception e) {
      logger.error("COULD NOT UPGRADE SCHEMA TO VERSION 5!");
      throw e;
    } finally {
      DAOHelper.close(conn, s);
    }
  }

  /*
   * Schema version 6 supports video counts
   */
  private static void upgradeToVersion6() throws Exception {
    Connection conn = null;
    Statement s = null;

    try {
      conn = DAOHelper.getConnection();
      s = conn.createStatement();

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN VIDEO_COUNT INTEGER");
      s.execute("UPDATE PHOTOSET SET VIDEO_COUNT = 0");
      logger.info("Added column VIDEO_COUNT to PHOTOSET table with value 0.");

      // no errors, so update the version
      LookupDAO.setDatabaseVersion(6);

    } catch (Exception e) {
      logger.error("COULD NOT UPGRADE SCHEMA TO VERSION 6!");
      throw e;
    } finally {
      DAOHelper.close(conn, s);
    }
  }

  /*
   * Schema version 7 supports machine tag and full text searches.
   */
  private static void upgradeToVersion7() throws Exception {
    Connection conn = null;
    Statement s = null;
    try {
      conn = DAOHelper.getConnection();
      s = conn.createStatement();

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN MACHINE_TAGS VARCHAR(2000)");
      logger.info("Added column MACHINE_TAGS to PHOTOSET table.");

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN MACHINE_TAG_MATCH_MODE VARCHAR(8)");
      s.execute("UPDATE PHOTOSET SET MACHINE_TAG_MATCH_MODE = '" + SSConstants.TAG_MATCH_MODE_NONE + "'");
      logger.info("Added column MACHINE_TAG_MATCH_MODE to PHOTOSET table.");

      // no errors, so update the version
      LookupDAO.setDatabaseVersion(7);
    } catch (Exception e) {
      logger.error("COULD NOT UPGRADE SCHEMA TO VERSION 7!", e);
      throw e;
    } finally {
      DAOHelper.close(conn, s);
    }
  }

  /*
   * Schema version 8 supports full text search.
   */
  private static void upgradeToVersion8() throws Exception {
    Connection conn = null;
    Statement s = null;
    try {
      conn = DAOHelper.getConnection();
      s = conn.createStatement();

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN TEXT_SEARCH VARCHAR(2000)");
      logger.info("Added column TEXT_SEARCH to PHOTOSET table.");

      s.execute("ALTER TABLE PHOTOSET ADD COLUMN VIEW_COUNT INTEGER");
      logger.info("Added column VIEW_COUNT to PHOTOSET table.");

      s.execute("UPDATE PHOTOSET SET VIEW_COUNT = -1");
      logger.info("Set default values for VIEW_COUNT.");

      // no errors, so update the version
      LookupDAO.setDatabaseVersion(8);
    } catch (Exception e) {
      logger.error("COULD NOT UPGRADE SCHEMA TO VERSION 8!", e);
      throw e;
    } finally {
      DAOHelper.close(conn, s);
    }
  }


  public static void compressTables() throws Exception {
    Connection conn = null;
    CallableStatement cs = null;

    try {
      conn = DAOHelper.getConnection();
      cs = conn.prepareCall(SQL_COMPRESS_TABLE);

      logger.info("Compressing LOOKUP");

      cs.setString(1, "APP");
      cs.setString(2, "LOOKUP");
      cs.execute();

      logger.info("Compressing PHOTOSET");
      cs.setString(2, "PHOTOSET");
      cs.execute();
    } finally {
      DAOHelper.close(conn, cs);
    }
  }


  /**
   * Back up the database.
   *
   * @param backupDirectory the directory to use for backup.
   * @throws Exception if there are any errors.
   */
  public static void performBackup(File backupDirectory) throws Exception {
    logger.info("Backing up database to " + backupDirectory.getAbsolutePath());
    Connection conn = null;
    CallableStatement cs = null;
    try {
      conn = DAOHelper.getConnection();
      cs = conn.prepareCall(SQL_BACKUP_DATABASE);
      cs.setString(1, backupDirectory.getAbsolutePath());
      cs.execute();
    } finally {
      DAOHelper.close(conn, cs);
    }
  }


  public static void restoreDatabase(File restoreDirectory) throws Exception {
    logger.info("Shutting down database.");
    try {
      DAOHelper.shutdown();
    } catch (Exception e) {
      // ignore; this always throws an exception
    }
    logger.info("Restoring database from " + restoreDirectory.getAbsolutePath());
    DriverManager.getConnection("jdbc:derby:SuprSetrDB;restoreFrom=" + restoreDirectory.getAbsolutePath());
  }

}
