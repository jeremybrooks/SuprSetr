/*
 * SuprSetr is Copyright 2010 by Jeremy Brooks
 *
 * This file is part of SuprSetr.
 *
 *  SuprSetr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  SuprSetr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with SuprSetr.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.jeremybrooks.suprsetr.flickr;

//import com.aetrion.flickr.photos.SearchParameters;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import net.jeremybrooks.jinx.JinxConstants;
import net.jeremybrooks.jinx.dto.SearchParameters;
import net.jeremybrooks.suprsetr.SSConstants;
import net.jeremybrooks.suprsetr.SSPhotoset;



/**
 * This is a wrapper around the Flickr API library.
 *
 * <p>This wrapper provides access to the search methods.</p>
 *
 * <p>This class is implemented as a Singleton. Calling <code>SearchHelper.getInstance()</code>
 * will return a reference to the instance of this class.</p>
 *
 * @author jeremyb
 */
public class SearchHelper {

    /** Reference to the only instance of this class. */
    private static SearchHelper instance = null;


    /** Private constructor. This class is a Singleton. */
    private SearchHelper() {
    }


    /**
     * Gets a reference to the only instance of this class.
     *
     * @return reference to the only instance of SearchHelper.
     */
    public static SearchHelper getInstance() {
	if (SearchHelper.instance == null) {
	    SearchHelper.instance = new SearchHelper();
	}

	return SearchHelper.instance;
    }


    /*
	Interestingness Descending
	Interestingness Ascending
	Date Taken Descending
	Date Taken Ascending
	Date Posted Descending
	Date Posted Ascending
     */

    /**
     * Create a SearchParameters object based on the SSPhotoset.
     *
     * @param ssPhotoset the photoset to get search parameters for.
     * @return instance of SearchParameters which will match the options set in
     *         the specified instance of SSPhotoset.
     * @throws Exception if there are any errors.
     */
    public SearchParameters getSearchParameters(SSPhotoset ssPhotoset) throws Exception {
	SearchParameters sp = new SearchParameters();

	sp.setUserId(FlickrHelper.getInstance().getNSID());

	switch (ssPhotoset.getSortOrder()) {
	    case 0:
		sp.setSort(JinxConstants.SORT_INTERESTINGNESS_DESC);
		break;

	    case 1:
		sp.setSort(JinxConstants.SORT_INTERESTINGNESS_ASC);
		break;

	    case 2:
		sp.setSort(JinxConstants.SORT_DATE_TAKEN_DESC);
		break;

	    case 3:
		sp.setSort(JinxConstants.SORT_DATE_TAKEN_ASC);
		break;

	    case 4:
		sp.setSort(JinxConstants.SORT_DATE_POSTED_DESC);
		break;

	    case 5:
		sp.setSort(JinxConstants.SORT_DATE_POSTED_ASC);
		break;

	    default:
		sp.setSort(JinxConstants.SORT_INTERESTINGNESS_DESC);
		break;
	}
	if (ssPhotoset.getTagMatchMode().equals(SSConstants.TAG_MATCH_MODE_ALL)) {
	    sp.setTagMode(JinxConstants.TAG_MODE_ALL);
	    sp.setTags(ssPhotoset.getTags());

	} else if (ssPhotoset.getTagMatchMode().equals(SSConstants.TAG_MATCH_MODE_ANY)) {
	    sp.setTagMode(JinxConstants.TAG_MODE_ANY);
	    sp.setTags(ssPhotoset.getTags());
	}

	if (ssPhotoset.isMatchTakenDates()) {
	    sp.setMaxTakenDate(ssPhotoset.getMaxTakenDate());
	    sp.setMinTakenDate(ssPhotoset.getMinTakenDate());
	}

	if (ssPhotoset.isMatchUploadDates()) {
	    sp.setMaxUploadDate(ssPhotoset.getMaxUploadDate());
	    sp.setMinUploadDate(ssPhotoset.getMinUploadDate());
	}


	switch (ssPhotoset.getPrivacy()) {
	    // order is public, friends, family, friends/family, private
	    // default: do not set this parameter
	    case 0:
		// do not set parameter; all results will be returned
		break;
	    case 1:
		sp.setPrivacyFilter(JinxConstants.PRIVACY_PUBLIC);
		break;

	    case 2:
		sp.setPrivacyFilter(JinxConstants.PRIVACY_FRIENDS);
		break;

	    case 3:
		sp.setPrivacyFilter(JinxConstants.PRIVACY_FAMILY);
		break;

	    case 4:
		sp.setPrivacyFilter(JinxConstants.PRIVACY_FRIENDS_FAMILY);
		break;

	    case 5:
		sp.setPrivacyFilter(JinxConstants.PRIVACY_PRIVATE);
		break;

	    default:
		// do not set parameter; all results will be returned
		break;
	}

	switch (ssPhotoset.getSafeSearch()) {
	    // order is safe, moderate, restricted
	    // default safe
	    case 0:
		sp.setSafeSearch(JinxConstants.SAFE_SEARCH_SAFE);
		break;
	    case 1:
		sp.setSafeSearch(JinxConstants.SAFE_SEARCH_MODERATE);
		break;
	    case 2:
		sp.setSafeSearch(JinxConstants.SAFE_SEARCH_RESTRICTED);
		break;
	    default:
		sp.setSafeSearch(JinxConstants.SAFE_SEARCH_SAFE);
		break;
	}

	switch (ssPhotoset.getContentType()) {
	    // order is photos, screenshots, other, photos/screenshots,
	    // screenshots/other, photos/other, all
	    // default all
	    case 0:
		sp.setContentType(JinxConstants.CONTENT_PHOTOS);
		break;
	    case 1:
		sp.setContentType(JinxConstants.CONTENT_SCREENSHOTS);
		break;
	    case 2:
		sp.setContentType(JinxConstants.CONTENT_OTHER);
		break;
	    case 3:
		sp.setContentType(JinxConstants.CONTENT_PHOTOS_SCREENSHOTS);
		break;
	    case 4:
		sp.setContentType(JinxConstants.CONTENT_SCREENSHOTS_OTHER);
		break;
	    case 5:
		sp.setContentType(JinxConstants.CONTENT_PHOTOS_OTHER);
		break;
	    case 6:
		sp.setContentType(JinxConstants.CONTENT_ALL);
		break;
	    default:
		sp.setContentType(JinxConstants.CONTENT_ALL);
		break;
	}

	switch (ssPhotoset.getMediaType()) {
	    // order is all, photos, video
	    // default all
	    case 0:
		sp.setMedia(JinxConstants.MEDIA_ALL);
		break;
	    case 1:
		sp.setMedia(JinxConstants.MEDIA_PHOTOS);
		break;
	    case 2:
		sp.setMedia(JinxConstants.MEDIA_VIDEOS);
		break;
	    default:
		sp.setMedia(JinxConstants.MEDIA_ALL);
		break;
	}

	switch (ssPhotoset.getGeotagged()) {
	    // order is ignore, has, does not have
	    // default ignore
	    case 0:
		// ignore
		break;
	    case 1:
		sp.setHasGeo(JinxConstants.HAS_GEOTAG);
		break;
	    case 2:
		sp.setHasGeo(JinxConstants.HAS_NO_GEOTAG);
		break;
	    default:
		// ignore
		break;
	}

	// only set these if needed
	if (ssPhotoset.isInCommons()) {
	    sp.setIsCommons(true);
	}
	if (ssPhotoset.isInGallery()) {
	    sp.setInGallery(true);
	}
	if(ssPhotoset.isInGetty()) {
	    sp.setIsGetty(true);
	}
	
	return sp;
    }


    /**
     * Get a search parameters object for a "On This Day" search.
     *
     * @param ssPhotoset photoset defining the search parameters.
     * @param year the year for the On This Day search.
     * @return search parameters for the day.
     * @throws Exception if there are any errors.
     */
    public SearchParameters getSearchParametersForOnThisDay(SSPhotoset ssPhotoset, int year) throws Exception {
	SearchParameters sp = this.getSearchParameters(ssPhotoset);

	// set the "Date taken" search parameter based on the month, day and year passed in
	// the constructor expects a zero-indexed value for month, so adjust
	GregorianCalendar cal = new GregorianCalendar(year, ssPhotoset.getOnThisDayMonth()-1, ssPhotoset.getOnThisDayDay());
	
	// min taken date will be at midnight
	sp.setMinTakenDate(cal.getTime());
	
	// max taken date is one second before midnight
	cal.set(Calendar.HOUR_OF_DAY, 23);
	cal.set(Calendar.MINUTE, 59);
	cal.set(Calendar.SECOND, 59);
	sp.setMaxTakenDate(cal.getTime());

	return sp;
    }
}
