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

package net.jeremybrooks.suprsetr.dao;

import net.jeremybrooks.suprsetr.SSConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.Optional;
import java.util.TreeSet;


/**
 * General database utility methods for SuprSetr.
 *
 * @author Jeremy Brooks
 */
public class DAOHelper {
  private static Logger logger = LogManager.getLogger(DAOHelper.class);

  /* Database connection string. */
  private static final String DB_CONN_STRING = "jdbc:derby:SuprSetrDB";

  /* SQL to create the photoset table. */
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

  /* SQL to create the lookup table. */
  private static final String SQL_CREATE_TABLE_LOOKUP =
      "CREATE table LOOKUP ( "
          + "K	    VARCHAR(64),"
          + "VALUE  VARCHAR(1024)"
          + ")";

  /* SQL to compress database tables. */
  private static final String SQL_COMPRESS_TABLE =
      "CALL SYSCS_UTIL.SYSCS_COMPRESS_TABLE(?, ?, 1)";

  /* SQL to perform backup. */
  private static final String SQL_BACKUP_DATABASE =
      "CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)";


  /* No instances allowed. All methods are public static. */
  private DAOHelper() {
  }


  /**
   * Shuts down the database.
   *
   * @throws Exception shutting down the database will always throw this.
   */
  public static void shutdown() throws Exception {
    DriverManager.getConnection(DB_CONN_STRING + ";shutdown=true");
  }


  /**
   * Create the database.
   *
   * <p>The photoset and lookup tables will be created.</p>
   *
   * @throws Exception if there are any errors.
   */
  public static void createDatabase() throws Exception {

    try (Connection conn = DriverManager.getConnection(DB_CONN_STRING + ";create=true");
         Statement s = conn.createStatement()) {
      s.execute(SQL_CREATE_TABLE_PHOTOSET);
      s.execute(SQL_CREATE_TABLE_LOOKUP);

      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_DATABASE_VERSION, "1");
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_ADD_VIA, DAOHelper.booleanToString(true));
      LookupDAO.setKeyAndValue(SSConstants.LOOKUP_KEY_REFRESH_WAIT, SSConstants.DEFAULT_REFRESH_WAIT);
    } catch (Exception e) {
      logger.error("checkDatabase: ERROR CREATING DATABASE.", e);
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
   * Convert an icon to bytes.
   *
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
   *
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
   *
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
   *
   * <p>Return Y for true, N for false.</p>
   *
   * @param flag boolean value.
   * @return Y for true, N for false.
   */
  public static String booleanToString(boolean flag) {
    return flag ? "Y" : "N";
  }


  public static void upgradeDatabase() throws Exception {
    int databaseVersion = LookupDAO.getDatabaseVersion();
    Optional<Integer> maxPatchVersion = SSConstants.DB_UPGRADES.keySet().stream().max(Comparator.naturalOrder());

    if (maxPatchVersion.isEmpty()) {
      logger.fatal("Could not determine maximum database patch level.");
      JOptionPane.showMessageDialog(null,
          "Could not determine maximum database patch level.",
          "Runtime error.",
          JOptionPane.ERROR_MESSAGE);
      throw new Exception("Could not determine maximum database patch level.");
    } else if (databaseVersion > maxPatchVersion.get()) {
      logger.fatal("Database schema is " + databaseVersion + ", but expecting version " +
          SSConstants.DATABASE_SCHEMA_CURRENT_VERSION + ".");

      JOptionPane.showMessageDialog(null, "The database schema is version " + databaseVersion + ",\n" +
              "but this version of SuprSetr requires schema version " +
              SSConstants.DATABASE_SCHEMA_CURRENT_VERSION + ".\n" +
              "Are you running an old version of SuprSetr?",
          "Incompatible Schema",
          JOptionPane.ERROR_MESSAGE);
      throw new Exception("Incompatible schema.");
    }

    // apply all patches greater than the current version
    for (Integer patchVersion : new TreeSet<>(SSConstants.DB_UPGRADES.keySet())) {
      if (patchVersion > databaseVersion) {
        logger.info("Attempting to upgrade schema to version {}.", patchVersion);
        applyPatch(patchVersion);
        logger.info("Upgrade to schema version {}: success.", patchVersion);
      }
    }
  }

  private static void applyPatch(int version) throws Exception {
    try (Connection conn = getConnection()) {
      for (String sql : SSConstants.DB_UPGRADES.get(version)) {
        try (Statement s = conn.createStatement()) {
          s.execute(sql);
          logger.info("  - Applied database patch {}", sql);
        }
        // no errors so update the version
        LookupDAO.setDatabaseVersion(version);
      }
    }
  }

  /**
   * Compress database.
   *
   * @throws Exception if there are any errors.
   */
  public static void compressTables() throws Exception {
    try (Connection conn = getConnection();
         CallableStatement cs = conn.prepareCall(SQL_COMPRESS_TABLE)) {
      logger.info("Compressing LOOKUP");

      cs.setString(1, "APP");
      cs.setString(2, "LOOKUP");
      cs.execute();

      logger.info("Compressing PHOTOSET");
      cs.setString(2, "PHOTOSET");
      cs.execute();
    }
  }


  /**
   * Back up the database.
   *
   * @param backupDirectory the directory to use for backup.
   * @throws Exception if there are any errors.
   */
  public static void performBackup(File backupDirectory) throws Exception {
    logger.info("Backing up database to {}", backupDirectory.getAbsolutePath());
    try (Connection conn = getConnection();
         CallableStatement cs = conn.prepareCall(SQL_BACKUP_DATABASE)) {
      cs.setString(1, backupDirectory.getAbsolutePath());
      cs.execute();
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
    DriverManager.getConnection(String.format("%s;restoreFrom=%s/SuprSetrDB",
        DB_CONN_STRING, restoreDirectory.getAbsolutePath()));
  }
}
