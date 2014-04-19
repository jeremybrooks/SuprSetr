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
package net.jeremybrooks.suprsetr.dao;

import net.jeremybrooks.suprsetr.SSConstants;
import net.jeremybrooks.suprsetr.SSPhotoset;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


/**
 * Methods to access the Photoset table.
 *
 * @author jeremyb
 */
public class PhotosetDAO {

    /** Logging. */
    private static Logger logger = Logger.getLogger(PhotosetDAO.class);

    /** SQL to insert a new record. */
    private static final String SQL_INSERT_PHOTOSET =
	    "INSERT INTO PHOTOSET "
	    + "(ID, TITLE, DESCRIPTION, FARM, SERVER, "
	    + " PHOTO_COUNT, PRIMARY_PHOTO_ID, SECRET, "
	    + " URL, PRIMARY_PHOTO_ICON, TAG_MATCH_MODE, "
	    + " TAGS, MIN_UPLOAD_DATE, MAX_UPLOAD_DATE, "
	    + " MIN_TAKEN_DATE, MAX_TAKEN_DATE, "
	    + " MATCH_UPLOAD_DATES, MATCH_TAKEN_DATES, "
	    + " LAST_REFRESH_DATE, SYNC_TIMESTAMP, MANAGED, "
	    + " SORT_ORDER, SEND_TWEET, TWEET_TEMPLATE,"
	    + " LOCK_PRIMARY_PHOTO, PRIVACY, SAFE_SEARCH, CONTENT_TYPE, "
	    + " MEDIA_TYPE, GEOTAGGED, IN_COMMONS, IN_GALLERY, IN_GETTY,"
	    + " LIMIT_SIZE, SIZE_LIMIT, "
	    + " ON_THIS_DAY, OTD_MONTH, OTD_DAY, OTD_YEAR_START, OTD_YEAR_END, VIDEO_COUNT,"
		+ " MACHINE_TAGS, MACHINE_TAG_MATCH_MODE) "
	    + "VALUES ("
	    + "?, ?, ?, ?, ?, ?, ?,"
	    + "?, ?, ?, ?, ?, ?, ?,"
	    + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
	    + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
	    + "?, ?, ?, ?, ?, ?, ?, ?)";

    /** SQL to get the photoset id's. */
    private static final String SQL_GET_PHOTOSET_IDS =
	    "SELECT ID "
	    + "FROM PHOTOSET "
	    + "ORDER BY UPPER(TITLE)";

    /** SQL to get the photoset id's. Sort is case sensitive */
    private static final String SQL_GET_PHOTOSET_IDS_CASE_SENSITIVE =
	    "SELECT ID "
	    + "FROM PHOTOSET "
	    + "ORDER BY TITLE";

    /** SQL to get all photosets ordered by title. */
    private static final String SQL_GET_ALL_PHOTOSETS_BY_TITLE =
	    "SELECT "
	    + " ID, TITLE, DESCRIPTION, FARM, SERVER, "
	    + " PHOTO_COUNT, PRIMARY_PHOTO_ID, SECRET, "
	    + " URL, PRIMARY_PHOTO_ICON, TAG_MATCH_MODE, "
	    + " TAGS, MIN_UPLOAD_DATE, MAX_UPLOAD_DATE, "
	    + " MIN_TAKEN_DATE, MAX_TAKEN_DATE, "
	    + " MATCH_UPLOAD_DATES, MATCH_TAKEN_DATES, "
	    + " LAST_REFRESH_DATE, SYNC_TIMESTAMP, MANAGED,"
	    + " SORT_ORDER, SEND_TWEET, TWEET_TEMPLATE, "
	    + " LOCK_PRIMARY_PHOTO, PRIVACY, SAFE_SEARCH, CONTENT_TYPE,"
	    + " MEDIA_TYPE, GEOTAGGED, IN_COMMONS, IN_GALLERY, IN_GETTY, "
	    + " LIMIT_SIZE, SIZE_LIMIT, "
	    + " ON_THIS_DAY, OTD_MONTH, OTD_DAY, OTD_YEAR_START, OTD_YEAR_END, VIDEO_COUNT, "
		+ " MACHINE_TAGS, MACHINE_TAG_MATCH_MODE "
	    + "FROM PHOTOSET "
	    + "ORDER BY UPPER(TITLE)";

    /** SQL to get all photosets ordered by title. Sort is case sensitive. */
    private static final String SQL_GET_ALL_PHOTOSETS_BY_TITLE_CASE_SENSITIVE =
	    "SELECT "
	    + " ID, TITLE, DESCRIPTION, FARM, SERVER, "
	    + " PHOTO_COUNT, PRIMARY_PHOTO_ID, SECRET, "
	    + " URL, PRIMARY_PHOTO_ICON, TAG_MATCH_MODE, "
	    + " TAGS, MIN_UPLOAD_DATE, MAX_UPLOAD_DATE, "
	    + " MIN_TAKEN_DATE, MAX_TAKEN_DATE, "
	    + " MATCH_UPLOAD_DATES, MATCH_TAKEN_DATES, "
	    + " LAST_REFRESH_DATE, SYNC_TIMESTAMP, MANAGED,"
	    + " SORT_ORDER, SEND_TWEET, TWEET_TEMPLATE, "
	    + " LOCK_PRIMARY_PHOTO, PRIVACY, SAFE_SEARCH, CONTENT_TYPE,"
	    + " MEDIA_TYPE, GEOTAGGED, IN_COMMONS, IN_GALLERY, IN_GETTY, "
	    + " LIMIT_SIZE, SIZE_LIMIT, "
	    + " ON_THIS_DAY, OTD_MONTH, OTD_DAY, OTD_YEAR_START, OTD_YEAR_END, VIDEO_COUNT, "
		+ " MACHINE_TAGS, MACHINE_TAG_MATCH_MODE "
	    + "FROM PHOTOSET "
	    + "ORDER BY TITLE";

    /** SQL to get all photosets ordered by managed and title. */
    private static final String SQL_GET_ALL_PHOTOSETS_BY_MANAGED_AND_TITLE =
	    "SELECT "
	    + " ID, TITLE, DESCRIPTION, FARM, SERVER, "
	    + " PHOTO_COUNT, PRIMARY_PHOTO_ID, SECRET, "
	    + " URL, PRIMARY_PHOTO_ICON, TAG_MATCH_MODE, "
	    + " TAGS, MIN_UPLOAD_DATE, MAX_UPLOAD_DATE, "
	    + " MIN_TAKEN_DATE, MAX_TAKEN_DATE, "
	    + " MATCH_UPLOAD_DATES, MATCH_TAKEN_DATES, "
	    + " LAST_REFRESH_DATE, SYNC_TIMESTAMP, MANAGED,"
	    + " SORT_ORDER, SEND_TWEET, TWEET_TEMPLATE, "
	    + " LOCK_PRIMARY_PHOTO, PRIVACY, SAFE_SEARCH, CONTENT_TYPE,"
	    + " MEDIA_TYPE, GEOTAGGED, IN_COMMONS, IN_GALLERY, IN_GETTY, "
	    + " LIMIT_SIZE, SIZE_LIMIT, "
	    + " ON_THIS_DAY, OTD_MONTH, OTD_DAY, OTD_YEAR_START, OTD_YEAR_END, VIDEO_COUNT, "
		+ " MACHINE_TAGS, MACHINE_TAG_MATCH_MODE "
	    + "FROM PHOTOSET "
	    + "ORDER BY MANAGED DESC, UPPER(TITLE)";

    /** SQL to get all photosets ordered by managed and title. Sort is case sensitive.  */
    private static final String SQL_GET_ALL_PHOTOSETS_BY_MANAGED_AND_TITLE_CASE_SENSITIVE =
	    "SELECT "
	    + " ID, TITLE, DESCRIPTION, FARM, SERVER, "
	    + " PHOTO_COUNT, PRIMARY_PHOTO_ID, SECRET, "
	    + " URL, PRIMARY_PHOTO_ICON, TAG_MATCH_MODE, "
	    + " TAGS, MIN_UPLOAD_DATE, MAX_UPLOAD_DATE, "
	    + " MIN_TAKEN_DATE, MAX_TAKEN_DATE, "
	    + " MATCH_UPLOAD_DATES, MATCH_TAKEN_DATES, "
	    + " LAST_REFRESH_DATE, SYNC_TIMESTAMP, MANAGED,"
	    + " SORT_ORDER, SEND_TWEET, TWEET_TEMPLATE, "
	    + " LOCK_PRIMARY_PHOTO, PRIVACY, SAFE_SEARCH, CONTENT_TYPE,"
	    + " MEDIA_TYPE, GEOTAGGED, IN_COMMONS, IN_GALLERY, IN_GETTY, "
	    + " LIMIT_SIZE, SIZE_LIMIT, "
	    + " ON_THIS_DAY, OTD_MONTH, OTD_DAY, OTD_YEAR_START, OTD_YEAR_END, VIDEO_COUNT, "
		+ " MACHINE_TAGS, MACHINE_TAG_MATCH_MODE "
	    + "FROM PHOTOSET "
	    + "ORDER BY MANAGED DESC, TITLE";

    /** SQL to get a specific photoset by ID. */
    private static final String SQL_GET_PHOTOSET_BY_ID =
	    "SELECT "
	    + " ID, TITLE, DESCRIPTION, FARM, SERVER, "
	    + " PHOTO_COUNT, PRIMARY_PHOTO_ID, SECRET, "
	    + " URL, PRIMARY_PHOTO_ICON, TAG_MATCH_MODE, "
	    + " TAGS, MIN_UPLOAD_DATE, MAX_UPLOAD_DATE, "
	    + " MIN_TAKEN_DATE, MAX_TAKEN_DATE, "
	    + " MATCH_UPLOAD_DATES, MATCH_TAKEN_DATES, "
	    + " LAST_REFRESH_DATE, SYNC_TIMESTAMP, MANAGED,"
	    + " SORT_ORDER, SEND_TWEET, TWEET_TEMPLATE,"
	    + " LOCK_PRIMARY_PHOTO, PRIVACY, SAFE_SEARCH, CONTENT_TYPE,"
	    + " MEDIA_TYPE, GEOTAGGED, IN_COMMONS, IN_GALLERY, IN_GETTY, "
	    + " LIMIT_SIZE, SIZE_LIMIT, "
	    + " ON_THIS_DAY, OTD_MONTH, OTD_DAY, OTD_YEAR_START, OTD_YEAR_END, VIDEO_COUNT, "
		+ " MACHINE_TAGS, MACHINE_TAG_MATCH_MODE "
	    + "FROM PHOTOSET "
	    + "WHERE ID = ?";

    /** SQL to update a photoset record. */
    private static final String SQL_UPDATE_PHOTOSET =
	    "UPDATE PHOTOSET "
	    + " SET TITLE = ?, "
	    + " DESCRIPTION = ?, "
	    + " FARM = ?, "
	    + " SERVER = ?, "
	    + " PHOTO_COUNT = ?, "
	    + " PRIMARY_PHOTO_ID = ?, "
	    + " SECRET = ?, "
	    + " URL = ?, "
	    + " PRIMARY_PHOTO_ICON = ?, "
	    + " TAG_MATCH_MODE = ?, "
	    + " TAGS = ?, "
	    + " MIN_UPLOAD_DATE = ?, "
	    + " MAX_UPLOAD_DATE = ?, "
	    + " MIN_TAKEN_DATE = ?, "
	    + " MAX_TAKEN_DATE = ?, "
	    + " MATCH_UPLOAD_DATES = ?, "
	    + " MATCH_TAKEN_DATES = ?, "
	    + " LAST_REFRESH_DATE = ?, "
	    + " SYNC_TIMESTAMP = ?, "
	    + " MANAGED = ?,"
	    + " SORT_ORDER = ?,"
	    + " SEND_TWEET = ?,"
	    + " TWEET_TEMPLATE = ?,"
	    + " LOCK_PRIMARY_PHOTO = ?, "
	    + " PRIVACY = ?, "
	    + " SAFE_SEARCH = ?, "
	    + " CONTENT_TYPE = ?, "
	    + " MEDIA_TYPE = ?, "
	    + " GEOTAGGED = ?, "
	    + " IN_COMMONS = ?, "
	    + " IN_GALLERY = ?, "
	    + " IN_GETTY = ?, "
	    + " LIMIT_SIZE = ?, "
	    + " SIZE_LIMIT = ?, "
	    + " ON_THIS_DAY = ?, "
	    + " OTD_MONTH = ?, "
	    + " OTD_DAY = ?, "
	    + " OTD_YEAR_START = ?, "
	    + " OTD_YEAR_END = ?,"
		+ " VIDEO_COUNT = ?, "
		+ " MACHINE_TAGS = ?, "
		+ " MACHINE_TAG_MATCH_MODE = ? "
	    + "WHERE ID = ?";

    /** SQL to update the metadata for a photoset. */
    private static final String SQL_UPDATE_METADATA_FOR_PHOTOSET =
	    "UPDATE PHOTOSET "
	    + "SET TITLE = ?, "
	    + " DESCRIPTION = ?, "
	    + " PHOTO_COUNT = ? "
	    + "WHERE ID = ?";

    /** SQL to update the icon for a photoset. */
    private static final String SQL_UPDATE_ICON_FOR_PHOTOSET =
	    "UPDATE PHOTOSET "
	    + "SET PRIMARY_PHOTO_ICON = ? "
	    + "WHERE ID = ?";

    /** SQL to delete a photoset record. */
    private static final String SQL_DELETE_PHOTOSET =
	    "DELETE "
	    + "FROM PHOTOSET "
	    + "WHERE ID = ?";


    /** No instances. */
    private PhotosetDAO() {
    }


    /**
     * Insert a new record.
     *
     * <p>If the photoset is null, this method will throw an Exception.</p>
     *
     * @param p the photoset to insert.
     * @return number of rows inserted.
     * @throws Exception if there are any errors, or the photoset is null.
     */
    public static int insertPhotoset(SSPhotoset p) throws Exception {
	if (p == null) {
	    throw new Exception("insertPhotoset: CANNOT INSERT A NULL PHOTOSET");
	}
	Connection conn = null;
	PreparedStatement ps = null;
	int count = 0;

	logger.info("Adding photoset " + p + " to database.");

	try {
	    conn = DAOHelper.getConnection();
	    ps = conn.prepareStatement(SQL_INSERT_PHOTOSET);
	    ps.setString(1, p.getId());
	    ps.setString(2, p.getTitle());
	    ps.setString(3, p.getDescription());
	    ps.setString(4, p.getFarm());
	    ps.setString(5, p.getServer());
	    ps.setInt(6, p.getPhotos());
	    ps.setString(7, p.getPrimary());
	    ps.setString(8, p.getSecret());
	    ps.setString(9, p.getUrl());

	    ps.setBytes(10, DAOHelper.iconToBytes(p.getPrimaryPhotoIcon()));

	    ps.setString(11, p.getTagMatchMode());
	    ps.setString(12, p.getTagsAsString());

	    if (p.getMinUploadDate() == null) {
		ps.setTimestamp(13, null);
	    } else {
		ps.setTimestamp(13, new Timestamp(p.getMinUploadDate().getTime()));
	    }

	    if (p.getMaxUploadDate() == null) {
		ps.setTimestamp(14, null);
	    } else {
		ps.setTimestamp(14, new Timestamp(p.getMaxUploadDate().getTime()));
	    }

	    if (p.getMinTakenDate() == null) {
		ps.setTimestamp(15, null);
	    } else {
		ps.setTimestamp(15, new Timestamp(p.getMinTakenDate().getTime()));
	    }

	    if (p.getMaxTakenDate() == null) {
		ps.setTimestamp(16, null);
	    } else {
		ps.setTimestamp(16, new Timestamp(p.getMaxTakenDate().getTime()));
	    }

	    ps.setString(17, DAOHelper.booleanToString(p.isMatchUploadDates()));
	    ps.setString(18, DAOHelper.booleanToString(p.isMatchTakenDates()));

	    if (p.getLastRefreshDate() == null) {
		ps.setTimestamp(19, null);
	    } else {
		ps.setTimestamp(19, new Timestamp(p.getLastRefreshDate().getTime()));
	    }
	    ps.setLong(20, p.getSyncTimestamp());
	    ps.setString(21, DAOHelper.booleanToString(p.isManaged()));

	    ps.setInt(22, p.getSortOrder());
	    ps.setString(23, DAOHelper.booleanToString(p.isSendTweet()));
	    ps.setString(24, p.getTweetTemplate());

	    ps.setString(25, DAOHelper.booleanToString(p.isLockPrimaryPhoto()));

	    ps.setInt(26, p.getPrivacy());
	    ps.setInt(27, p.getSafeSearch());
	    ps.setInt(28, p.getContentType());
	    ps.setInt(29, p.getMediaType());
	    ps.setInt(30, p.getGeotagged());
	    ps.setString(31, DAOHelper.booleanToString(p.isInCommons()));
	    ps.setString(32, DAOHelper.booleanToString(p.isInGallery()));
	    ps.setString(33, DAOHelper.booleanToString(p.isInGetty()));

	    ps.setString(34, DAOHelper.booleanToString(p.isLimitSize()));
	    ps.setInt(35, p.getSizeLimit());

	    ps.setString(36, DAOHelper.booleanToString(p.isOnThisDay()));
	    ps.setInt(37, p.getOnThisDayMonth());
	    ps.setInt(38, p.getOnThisDayDay());
	    ps.setInt(39, p.getOnThisDayYearStart());
	    ps.setInt(40, p.getOnThisDayYearEnd());

		ps.setInt(41, p.getVideos());

		ps.setString(42, p.getMachineTagsAsString());
		ps.setString(43, p.getMachineTagMatchMode());

	    count = ps.executeUpdate();

	} catch (Exception e) {
	    logger.info("insertPhotoset: ERROR.", e);
	    throw e;

	} finally {
	    DAOHelper.close(conn, ps);
	}

	return count;
    }


    /**
     * Get a list of all photoset ID's in the database.
     *
     *
     * @return list of all photoset ID's.
     * @throws Exception if there are any errors.
     */
    public static List<String> getPhotosetIdList() throws Exception {
	ArrayList<String> list = new ArrayList<String>();
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;

	try {
	    conn = DAOHelper.getConnection();
	    if (DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_CASE_SENSITIVE))) {
		ps = conn.prepareStatement(SQL_GET_PHOTOSET_IDS_CASE_SENSITIVE);
	    } else {
		ps = conn.prepareStatement(SQL_GET_PHOTOSET_IDS);
	    }
	    rs = ps.executeQuery();
	    while (rs.next()) {
		list.add(rs.getString("ID"));
	    }
	} catch (Exception e) {
	    logger.info("getPhotosetIdList: ERROR GETTING PHOTOSET ID LIST.", e);
	    throw e;
	} finally {
	    DAOHelper.close(conn, ps, rs);
	}


	return list;
    }


    /**
     * Get a photoset by ID.
     *
     * <p>If the ID exists, an SSPhotoset object representing the data will be
     * returned. If the ID does not exist, this method will return null.</p>
     *
     * @param id the photoset ID to look up.
     * @return instance of a photoset, or null if the ID does not exist.
     * @throws Exception if there are any errors.
     */
    public static SSPhotoset getPhotosetForId(String id) throws Exception {
	SSPhotoset ssp = null;
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;

	try {
	    conn = DAOHelper.getConnection();
	    ps = conn.prepareStatement(SQL_GET_PHOTOSET_BY_ID);
	    ps.setString(1, id);
	    rs = ps.executeQuery();
	    if (rs.next()) {
		ssp = PhotosetDAO.buildPhotoset(rs);
	    }
	} catch (Exception e) {
	    logger.error("getPhotosetForId(" + id + "): ERROR GETTING DATA.", e);
	    throw e;
	} finally {
	    DAOHelper.close(conn, ps, rs);
	}

	return ssp;
    }


    /**
     * Get a list of all photosets, ordered by managed and title.
     *
     *
     * @return list of SSPhotoset objects ordred by managed and title, or an
     *         empty list if there are no photosets.
     * @throws Exception if there are any errors.
     */
    public static List<SSPhotoset> getPhotosetListOrderByManagedAndTitle() throws Exception {
	String sql;
	if (DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_CASE_SENSITIVE))) {
	    sql = SQL_GET_ALL_PHOTOSETS_BY_MANAGED_AND_TITLE_CASE_SENSITIVE;
	} else {
	    sql = SQL_GET_ALL_PHOTOSETS_BY_MANAGED_AND_TITLE;
	}
	return PhotosetDAO.getPhotosetList(sql);
    }


    /**
     * Get a list of all photosets, ordered by title.
     *
     * @return list of SSPhotoset objects ordred by title, or an
     *         empty list if there are no photosets.
     * @throws Exception if there are any errors.
     */
    public static List<SSPhotoset> getPhotosetListOrderByTitle() throws Exception {
	String sql;
	if (DAOHelper.stringToBoolean(LookupDAO.getValueForKey(SSConstants.LOOKUP_KEY_CASE_SENSITIVE))) {
	    sql = SQL_GET_ALL_PHOTOSETS_BY_TITLE_CASE_SENSITIVE;
	} else {
	    sql = SQL_GET_ALL_PHOTOSETS_BY_TITLE;
	}
	return PhotosetDAO.getPhotosetList(sql);
    }


    /**
     * Do the work of getting a list.
     *
     * @param SQL executed to get records to build a list from.
     * @return list of SSPhotoset objects, or empty list if no records are returned
     *         by the SQL.
     * @throws Exception if there are any errors.
     */
    private static List<SSPhotoset> getPhotosetList(String SQL) throws Exception {
	List<SSPhotoset> list = new ArrayList<SSPhotoset>();
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;

	try {
	    conn = DAOHelper.getConnection();
	    ps = conn.prepareStatement(SQL);
	    rs = ps.executeQuery();

	    while (rs.next()) {
		list.add(PhotosetDAO.buildPhotoset(rs));
	    }

	} catch (Exception e) {
	    logger.error("getPhotosetList: ERROR GETTING LIST.", e);
	    throw e;
	} finally {
	    DAOHelper.close(conn, ps, rs);
	}

	return list;
    }


    /**
     * Update the metadata for the photoset.
     *
     * <p>This method updates the title, description, and photo count for
     * the specified photoset.</p>
     *
     * @param photoset the photoset to update.
     * @return number of rows affected.
     * @throws Exception if there are any errors, or if the photoset is null.
     */
    public static int updateMetadataForPhotoset(SSPhotoset photoset) throws Exception {
	if (photoset == null) {
	    throw new Exception("PARAMETER PHOTOSET CANNOT BE NULL OR EMPTY.");
	}

	int count = -1;
	Connection conn = null;
	PreparedStatement ps = null;

	try {
	    conn = DAOHelper.getConnection();
	    ps = conn.prepareStatement(SQL_UPDATE_METADATA_FOR_PHOTOSET);
	    ps.setString(1, photoset.getTitle());
	    ps.setString(2, photoset.getDescription());
	    ps.setInt(3, photoset.getPhotos());
	    ps.setString(4, photoset.getId());

	    count = ps.executeUpdate();

	} catch (Exception e) {
	    logger.error("updateMetadataForPhotoset(" + photoset + "): "
		    + "ERROR WHILE UPDATING PHOTOSET RECORD.", e);
	    throw e;
	} finally {
	    DAOHelper.close(conn, ps);
	}

	return count;
    }


    /**
     * Update a photoset record.
     *
     * @param ssPhotoset the photoset to update.
     * @return number of rows affected.
     * @throws Exception if there are any errors, or if the photoset is null.
     */
    public static int updatePhotoset(SSPhotoset ssPhotoset) throws Exception {
	if (ssPhotoset == null) {
	    throw new Exception("PARAMETER PHOTOSET CANNOT BE NULL OR EMPTY.");
	}

	int count = -1;
	Connection conn = null;
	PreparedStatement ps = null;

	logger.info("updatePhotoset: " + ssPhotoset);

	try {
	    conn = DAOHelper.getConnection();
	    ps = conn.prepareStatement(SQL_UPDATE_PHOTOSET);
	    ps.setString(1, ssPhotoset.getTitle());
	    ps.setString(2, ssPhotoset.getDescription());
	    ps.setString(3, ssPhotoset.getFarm());
	    ps.setString(4, ssPhotoset.getServer());
	    ps.setInt(5, ssPhotoset.getPhotos());
	    ps.setString(6, ssPhotoset.getPrimary());
	    ps.setString(7, ssPhotoset.getSecret());
	    ps.setString(8, ssPhotoset.getUrl());
	    ps.setBytes(9, DAOHelper.iconToBytes(ssPhotoset.getPrimaryPhotoIcon()));
	    ps.setString(10, ssPhotoset.getTagMatchMode());
	    ps.setString(11, ssPhotoset.getTagsAsString());
	    if (ssPhotoset.getMinUploadDate() == null) {
		ps.setTimestamp(12, null);
	    } else {
		ps.setTimestamp(12, new Timestamp(ssPhotoset.getMinUploadDate().getTime()));
	    }
	    if (ssPhotoset.getMaxUploadDate() == null) {
		ps.setTimestamp(13, null);
	    } else {
		ps.setTimestamp(13, new Timestamp(ssPhotoset.getMaxUploadDate().getTime()));
	    }
	    if (ssPhotoset.getMinTakenDate() == null) {
		ps.setTimestamp(14, null);
	    } else {
		ps.setTimestamp(14, new Timestamp(ssPhotoset.getMinTakenDate().getTime()));
	    }
	    if (ssPhotoset.getMaxTakenDate() == null) {
		ps.setTimestamp(15, null);
	    } else {
		ps.setTimestamp(15, new Timestamp(ssPhotoset.getMaxTakenDate().getTime()));
	    }
	    ps.setString(16, DAOHelper.booleanToString(ssPhotoset.isMatchUploadDates()));
	    ps.setString(17, DAOHelper.booleanToString(ssPhotoset.isMatchTakenDates()));
	    if (ssPhotoset.getLastRefreshDate() == null) {
		ps.setTimestamp(18, null);
	    } else {
		ps.setTimestamp(18, new Timestamp(ssPhotoset.getLastRefreshDate().getTime()));
	    }
	    ps.setLong(19, ssPhotoset.getSyncTimestamp());
	    ps.setString(20, DAOHelper.booleanToString(ssPhotoset.isManaged()));
	    ps.setInt(21, ssPhotoset.getSortOrder());
	    ps.setString(22, DAOHelper.booleanToString(ssPhotoset.isSendTweet()));
	    ps.setString(23, ssPhotoset.getTweetTemplate());

	    ps.setString(24, DAOHelper.booleanToString(ssPhotoset.isLockPrimaryPhoto()));

	    ps.setInt(25, ssPhotoset.getPrivacy());
	    ps.setInt(26, ssPhotoset.getSafeSearch());
	    ps.setInt(27, ssPhotoset.getContentType());
	    ps.setInt(28, ssPhotoset.getMediaType());
	    ps.setInt(29, ssPhotoset.getGeotagged());

	    ps.setString(30, DAOHelper.booleanToString(ssPhotoset.isInCommons()));
	    ps.setString(31, DAOHelper.booleanToString(ssPhotoset.isInGallery()));
	    ps.setString(32, DAOHelper.booleanToString(ssPhotoset.isInGetty()));

	    ps.setString(33, DAOHelper.booleanToString(ssPhotoset.isLimitSize()));
	    ps.setInt(34, ssPhotoset.getSizeLimit());

	    ps.setString(35, DAOHelper.booleanToString(ssPhotoset.isOnThisDay()));
	    ps.setInt(36, ssPhotoset.getOnThisDayMonth());
	    ps.setInt(37, ssPhotoset.getOnThisDayDay());
	    ps.setInt(38, ssPhotoset.getOnThisDayYearStart());
	    ps.setInt(39, ssPhotoset.getOnThisDayYearEnd());

		ps.setInt(40, ssPhotoset.getVideos());

		ps.setString(41, ssPhotoset.getMachineTagsAsString());
		ps.setString(42, ssPhotoset.getMachineTagMatchMode());

	    // where....
	    ps.setString(43, ssPhotoset.getId());

	    logger.info("Updating record for photoset " + ssPhotoset.getId()
		    + " [" + ssPhotoset.getTitle() + "]");

	    count = ps.executeUpdate();

	} catch (Exception e) {
	    logger.error("updateMetadataForPhotoset(" + ssPhotoset + "): "
		    + "ERROR WHILE UPDATING PHOTOSET RECORD.", e);
	    throw e;
	} finally {
	    DAOHelper.close(conn, ps);
	}

	return count;
    }


    /**
     * Update the icon for the photoset.
     *
     * @param photoset the photoset to update.
     * @return number of rows affected.
     * @throws Exception if there are any errors, or if the photoset is null.
     */
    public static int updateIconForPhotoset(SSPhotoset photoset) throws Exception {
	if (photoset == null) {
	    throw new Exception("PARAMETER PHOTOSET CANNOT BE NULL OR EMPTY.");
	}

	int count = -1;
	Connection conn = null;
	PreparedStatement ps = null;

	try {
	    conn = DAOHelper.getConnection();
	    ps = conn.prepareStatement(SQL_UPDATE_ICON_FOR_PHOTOSET);
	    ps.setBytes(1, DAOHelper.iconToBytes(photoset.getPrimaryPhotoIcon()));
	    ps.setString(2, photoset.getId());

	    count = ps.executeUpdate();

	} catch (Exception e) {
	    logger.error("updateIconForPhotoset(" + photoset + "): "
		    + "ERROR WHILE UPDATING ICON.", e);
	    throw e;
	} finally {
	    DAOHelper.close(conn, ps);
	}

	return count;
    }


    /**
     * Delete a record of the photoset.
     *
     * @param ssPhotoset photoset to delete record of.
     * @return number of rows deleted.
     * @throws Exception if there are any errors, or if the photoset is null.
     */
    public static int delete(SSPhotoset ssPhotoset) throws Exception {
	if (ssPhotoset == null) {
	    throw new Exception("delete: PARAMETER CANNOT BE NULL.");
	}

	int count = -1;
	Connection conn = null;
	PreparedStatement ps = null;

	try {
	    conn = DAOHelper.getConnection();
	    ps = conn.prepareStatement(SQL_DELETE_PHOTOSET);
	    ps.setString(1, ssPhotoset.getId());

	    logger.info("Deleting photoset " + ssPhotoset.getId() + "["
		    + ssPhotoset.getTitle() + "]");
	    count = ps.executeUpdate();

	} catch (Exception e) {
	    logger.error("delete(" + ssPhotoset.getId() + "): ERROR WHILE DELETING RECORD.", e);
	    throw e;
	} finally {
	    DAOHelper.close(conn, ps);
	}

	return count;
    }


    /**
     * Build a photoset object from a database record.
     */
    private static SSPhotoset buildPhotoset(ResultSet rs) throws Exception {
	SSPhotoset ssp = null;

	ssp = new SSPhotoset();

	ssp.setId(rs.getString("ID"));
	ssp.setTitle(rs.getString("TITLE"));
	ssp.setDescription(rs.getString("DESCRIPTION"));
	ssp.setFarm(rs.getString("FARM"));
	ssp.setServer(rs.getString("SERVER"));
	ssp.setPhotos(rs.getInt("PHOTO_COUNT"));
	ssp.setPrimary(rs.getString("PRIMARY_PHOTO_ID"));
	ssp.setSecret(rs.getString("SECRET"));
	ssp.setUrl(rs.getString("URL"));
	ssp.setPrimaryPhotoIcon(DAOHelper.bytesToIcon(rs.getBytes("PRIMARY_PHOTO_ICON")));
	ssp.setTagMatchMode(rs.getString("TAG_MATCH_MODE"));
	ssp.setTags(rs.getString("TAGS"));
	ssp.setMachineTagMatchMode(rs.getString("MACHINE_TAG_MATCH_MODE"));
	ssp.setMachineTags(rs.getString("MACHINE_TAGS"));
	ssp.setMinUploadDate(rs.getTimestamp("MIN_UPLOAD_DATE"));
	ssp.setMaxUploadDate(rs.getTimestamp("MAX_UPLOAD_DATE"));
	ssp.setMinTakenDate(rs.getTimestamp("MIN_TAKEN_DATE"));
	ssp.setMaxTakenDate(rs.getTimestamp("MAX_TAKEN_DATE"));
	ssp.setMatchUploadDates(DAOHelper.stringToBoolean(rs.getString("MATCH_UPLOAD_DATES")));
	ssp.setMatchTakenDates(DAOHelper.stringToBoolean(rs.getString("MATCH_TAKEN_DATES")));
	ssp.setLastRefreshDate(rs.getTimestamp("LAST_REFRESH_DATE"));
	ssp.setSyncTimestamp(rs.getLong("SYNC_TIMESTAMP"));
	ssp.setManaged(DAOHelper.stringToBoolean(rs.getString("MANAGED")));
	ssp.setSortOrder(rs.getInt("SORT_ORDER"));
	ssp.setSendTweet(DAOHelper.stringToBoolean(rs.getString("SEND_TWEET")));
	ssp.setTweetTemplate(rs.getString("TWEET_TEMPLATE"));
	ssp.setLockPrimaryPhoto(DAOHelper.stringToBoolean(rs.getString("LOCK_PRIMARY_PHOTO")));
	ssp.setPrivacy(rs.getInt("PRIVACY"));
	ssp.setSafeSearch(rs.getInt("SAFE_SEARCH"));
	ssp.setContentType(rs.getInt("CONTENT_TYPE"));
	ssp.setMediaType(rs.getInt("MEDIA_TYPE"));
	ssp.setGeotagged(rs.getInt("GEOTAGGED"));
	ssp.setInCommons(DAOHelper.stringToBoolean(rs.getString("IN_COMMONS")));
	ssp.setInGallery(DAOHelper.stringToBoolean(rs.getString("IN_GALLERY")));
	ssp.setInGetty(DAOHelper.stringToBoolean(rs.getString("IN_GETTY")));
	ssp.setLimitSize(DAOHelper.stringToBoolean(rs.getString("LIMIT_SIZE")));
	ssp.setSizeLimit(rs.getInt("SIZE_LIMIT"));
	ssp.setOnThisDay(DAOHelper.stringToBoolean(rs.getString("ON_THIS_DAY")));
	ssp.setOnThisDayMonth(rs.getInt("OTD_MONTH"));
	ssp.setOnThisDayDay(rs.getInt("OTD_DAY"));
	ssp.setOnThisDayYearStart(rs.getInt("OTD_YEAR_START"));
	ssp.setOnThisDayYearEnd(rs.getInt("OTD_YEAR_END"));
		ssp.setVideos(rs.getInt("VIDEO_COUNT"));

	return ssp;
    }

}
