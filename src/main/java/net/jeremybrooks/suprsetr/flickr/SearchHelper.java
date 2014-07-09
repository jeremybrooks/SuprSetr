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

package net.jeremybrooks.suprsetr.flickr;

import net.jeremybrooks.jinx.JinxConstants;
import net.jeremybrooks.jinx.response.photos.SearchParameters;
import net.jeremybrooks.suprsetr.SSConstants;
import net.jeremybrooks.suprsetr.SSPhotoset;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.GregorianCalendar;


/**
 * This is a wrapper around the Flickr API library.
 * <p/>
 * <p>This wrapper provides access to the search methods.</p>
 * <p/>
 * <p>This class is implemented as a Singleton. Calling <code>SearchHelper.getInstance()</code>
 * will return a reference to the instance of this class.</p>
 *
 * @author jeremyb
 */
public class SearchHelper {

    /**
     * Reference to the only instance of this class.
     */
    private static SearchHelper instance = null;


    /**
     * Private constructor. This class is a Singleton.
     */
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
    0-Interestingness Descending
	1-Interestingness Ascending
	2-Date Taken Descending
	3-Date Taken Ascending
	4-Date Posted Descending
	5-Date Posted Ascending
     */

    /**
     * Create a SearchParameters object based on the SSPhotoset.
     *
     * @param ssPhotoset the photoset to get search parameters for.
     * @return instance of SearchParameters which will match the options set in
     * the specified instance of SSPhotoset.
     * @throws Exception if there are any errors.
     */
    public SearchParameters getSearchParameters(SSPhotoset ssPhotoset) throws Exception {
        SearchParameters sp = new SearchParameters();

        sp.setUserId(FlickrHelper.getInstance().getNSID());

        switch (ssPhotoset.getSortOrder()) {
            case 0:
                sp.setSort(JinxConstants.SortOrder.interestingness_desc);
                break;

            case 1:
                sp.setSort(JinxConstants.SortOrder.interestingness_asc);
                break;

            case 2:
                sp.setSort(JinxConstants.SortOrder.date_taken_desc);
                break;

            case 3:
                sp.setSort(JinxConstants.SortOrder.date_taken_asc);
                break;

            case 4:
                sp.setSort(JinxConstants.SortOrder.date_posted_desc);
                break;

            case 5:
                sp.setSort(JinxConstants.SortOrder.date_posted_asc);
                break;

            default:
                sp.setSort(JinxConstants.SortOrder.interestingness_desc);
                break;
        }
        if (ssPhotoset.getTagMatchMode().equals(SSConstants.TAG_MATCH_MODE_ALL)) {
            sp.setTagMode(JinxConstants.TagMode.all);
            sp.setTags(ssPhotoset.getTags());

        } else if (ssPhotoset.getTagMatchMode().equals(SSConstants.TAG_MATCH_MODE_ANY)) {
            sp.setTagMode(JinxConstants.TagMode.any);
            sp.setTags(ssPhotoset.getTags());
        }

        if (ssPhotoset.getMachineTagMatchMode().equals(SSConstants.TAG_MATCH_MODE_ALL)) {
            sp.setMachineTagMode(JinxConstants.TagMode.all);
            sp.setMachineTags(ssPhotoset.getMachineTags());
//		sp.setMachineTags(ssPhotoset.getMachineTagsAsString());
        } else if (ssPhotoset.getMachineTagMatchMode().equals(SSConstants.TAG_MATCH_MODE_ANY)) {
            sp.setMachineTagMode(JinxConstants.TagMode.any);
            sp.setMachineTags(ssPhotoset.getMachineTags());
//		sp.setMachineTags(ssPhotoset.getMachineTagsAsString());
        }

        if (ssPhotoset.getTextSearch().trim().length() > 0) {
            sp.setText(ssPhotoset.getTextSearch().trim());
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
                sp.setPrivacyFilter(JinxConstants.PrivacyFilter.privacyPublic);
                break;

            case 2:
                sp.setPrivacyFilter(JinxConstants.PrivacyFilter.privacyFriends);
                break;

            case 3:
                sp.setPrivacyFilter(JinxConstants.PrivacyFilter.privacyFamily);
                break;

            case 4:
                sp.setPrivacyFilter(JinxConstants.PrivacyFilter.privacyFriendsAndFamily);
                break;

            case 5:
                sp.setPrivacyFilter(JinxConstants.PrivacyFilter.privacyPrivate);
                break;

            default:
                // do not set parameter; all results will be returned
                break;
        }

        switch (ssPhotoset.getSafeSearch()) {
            // order is safe, moderate, restricted
            // default safe
            case 0:
                sp.setSafetyLevel(JinxConstants.SafetyLevel.safe);
//		sp.setSafeSearch(JinxConstants.SAFE_SEARCH_SAFE);
                break;
            case 1:
                sp.setSafetyLevel(JinxConstants.SafetyLevel.moderate);
//		sp.setSafeSearch(JinxConstants.SAFE_SEARCH_MODERATE);
                break;
            case 2:
                sp.setSafetyLevel(JinxConstants.SafetyLevel.restricted);
//		sp.setSafeSearch(JinxConstants.SAFE_SEARCH_RESTRICTED);
                break;
            default:
                sp.setSafetyLevel(JinxConstants.SafetyLevel.safe);
//		sp.setSafeSearch(JinxConstants.SAFE_SEARCH_SAFE);
                break;
        }

        switch (ssPhotoset.getContentType()) {
            // order is photos, screenshots, other, photos/screenshots,
            // screenshots/other, photos/other, all
            // default all
            case 0:
                sp.setContentType(JinxConstants.ContentType.photo);
                break;
            case 1:
                sp.setContentType(JinxConstants.ContentType.screenshot);
                break;
            case 2:
                sp.setContentType(JinxConstants.ContentType.other);
                break;
            case 3:
                sp.setContentType(JinxConstants.ContentType.photos_and_screenshots);
                break;
            case 4:
                sp.setContentType(JinxConstants.ContentType.screenshots_and_other);
                break;
            case 5:
                sp.setContentType(JinxConstants.ContentType.photos_and_other);
                break;
            case 6:
                sp.setContentType(JinxConstants.ContentType.all);
                break;
            default:
                sp.setContentType(JinxConstants.ContentType.all);
                break;
        }

        switch (ssPhotoset.getMediaType()) {
            // order is all, photos, video
            // default all
            case 0:
                sp.setMediaType(JinxConstants.MediaType.all);
                break;
            case 1:
                sp.setMediaType(JinxConstants.MediaType.photos);
                break;
            case 2:
                sp.setMediaType(JinxConstants.MediaType.videos);
                break;
            default:
                sp.setMediaType(JinxConstants.MediaType.all);
                break;
        }

        switch (ssPhotoset.getGeotagged()) {
            // order is ignore, has, does not have
            // default ignore
            case 0:
                // ignore
                break;
            case 1:
                sp.setHasGeo(true);
                break;
            case 2:
                sp.setHasGeo(false);
                break;
            default:
                // ignore
                break;
        }

        // only set these if needed
        if (ssPhotoset.isInCommons()) {
            sp.setCommons(true);
        }
        if (ssPhotoset.isInGallery()) {
            sp.setInGallery(true);
        }
        if (ssPhotoset.isInGetty()) {
            sp.setGetty(true);
        }

        sp.setExtras(EnumSet.of(JinxConstants.PhotoExtras.media));

        return sp;
    }


    /**
     * Get a search parameters object for a "On This Day" search.
     *
     * @param ssPhotoset photoset defining the search parameters.
     * @param year       the year for the On This Day search.
     * @return search parameters for the day.
     * @throws Exception if there are any errors.
     */
    public SearchParameters getSearchParametersForOnThisDay(SSPhotoset ssPhotoset, int year) throws Exception {
        SearchParameters sp = this.getSearchParameters(ssPhotoset);

        // set the "Date taken" search parameter based on the month, day and year passed in
        // the constructor expects a zero-indexed value for month, so adjust
        GregorianCalendar cal = new GregorianCalendar(year, ssPhotoset.getOnThisDayMonth() - 1, ssPhotoset.getOnThisDayDay());

        // min taken date will be at midnight
        sp.setMinTakenDate(cal.getTime());

        // max taken date is one second before midnight
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        sp.setMaxTakenDate(cal.getTime());

        sp.setExtras(EnumSet.of(JinxConstants.PhotoExtras.media));

        return sp;
    }
}
