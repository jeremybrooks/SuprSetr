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

package net.jeremybrooks.suprsetr.dao;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Methods to interact with the Lookup table.
 *
 * @author jeremyb
 */
public class LookupDAO {

  /**
   * Logging.
   */
  private static Logger logger = Logger.getLogger(LookupDAO.class);

  /**
   * SQL to look up a value based on the key.
   */
  private static final String SQL_LOOKUP_VALUE =
      "SELECT VALUE " +
          "FROM LOOKUP " +
          "WHERE K = ?";

  /**
   * SQL to insert a new key and value.
   */
  private static final String SQL_INSERT_KEY_AND_VALUE =
      "INSERT INTO LOOKUP " +
          "(K, VALUE) " +
          "VALUES (?, ?)";

  /**
   * SQL to update the value for a key.
   */
  private static final String SQL_UPDATE_VALUE =
      "UPDATE LOOKUP " +
          "SET VALUE = ? " +
          "WHERE K = ?";

  /**
   * SQL to set the database version.
   */
  private static final String SQL_SET_DATABASE_VERSION =
      "UPDATE LOOKUP " +
          "SET VALUE = ? " +
          "WHERE K = 'DATABASE_VERSION'";


  /**
   * No instances.
   */
  private LookupDAO() {

  }


  /**
   * Get the value for the specified key.
   *
   * <p>If the key is null or empty, this method will return null. If the key
   * does not exist, this method will return null.</p>
   *
   * @param key the key to look up.
   * @return value for the key, or null.
   */
  public static String getValueForKey(String key) {
    if (key == null || key.isEmpty()) {
      return null;
    }

    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    String value = null;

    try {
      conn = DAOHelper.getConnection();
      ps = conn.prepareStatement(SQL_LOOKUP_VALUE);
      ps.setString(1, key);
      rs = ps.executeQuery();

      if (rs.next()) {
        value = rs.getString("VALUE");
      }
    } catch (Exception e) {
      logger.error("getValueForKey(" + key + "): ERROR.", e);
    } finally {
      DAOHelper.close(conn, ps, rs);
    }

    return value;
  }


  /**
   * Set the value for a key.
   *
   * <p>If the key is null or empty, this method will do nothing and return
   * -1. IF the value is null, an empty value will be set. This method can be
   * called to insert or update the key/value pair. It will detect if the
   * key is in the database and do the right thing.</p>
   *
   * @param key   the key to set.
   * @param value the value to set.
   * @return number of rows inserted or updated.
   */
  public static int setKeyAndValue(String key, String value) {
    if (key == null || key.isEmpty()) {
      return -1;
    }
    if (value == null) {
      value = "";
    }

    Connection conn = null;
    PreparedStatement ps = null;
    int rowCount = 0;

    try {
      conn = DAOHelper.getConnection();

      // does this key already exist?
      if (LookupDAO.getValueForKey(key) == null) {
        logger.info("Inserting key '" + key + "' and value '" + value + "'");
        ps = conn.prepareStatement(SQL_INSERT_KEY_AND_VALUE);
        ps.setString(1, key);
        ps.setString(2, value);
      } else {

        logger.info("Updating key '" + key + "' and value '" + value + "'");
        ps = conn.prepareStatement(SQL_UPDATE_VALUE);
        ps.setString(1, value);
        ps.setString(2, key);
      }

      rowCount = ps.executeUpdate();
    } catch (Exception e) {
      logger.error("setValueForKey(" + key + ", " + value + "): ERROR SETTING VALUE.", e);
    } finally {
      DAOHelper.close(conn, ps);
    }
    return rowCount;
  }


  /**
   * Get the current database version from the lookup table.
   *
   * <p>If there is no version yet, this method will return zero. If there
   * are any errors, this method will return zero.</p>
   *
   * @return current database version as found in the lookup table.
   */
  public static int getDatabaseVersion() {
    int version = 0;
    try {
      version = Integer.parseInt(LookupDAO.getValueForKey("DATABASE_VERSION"));
    } catch (Exception e) {
      logger.error("getDatabasVersion: THERE WAS AN ERROR. RETURNING 0.", e);
    }
    return version;
  }


  /**
   * Set the database version.
   *
   * @param version database version.
   * @throws Exception if there are any errors.
   */
  static void setDatabaseVersion(int version) throws Exception {
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = DAOHelper.getConnection();
      ps = conn.prepareStatement(SQL_SET_DATABASE_VERSION);
      ps.setString(1, Integer.toString(version));
      ps.execute();
    } finally {
      DAOHelper.close(conn, ps);
    }
  }
}
