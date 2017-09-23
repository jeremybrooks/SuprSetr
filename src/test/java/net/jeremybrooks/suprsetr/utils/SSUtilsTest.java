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

package net.jeremybrooks.suprsetr.utils;

import net.jeremybrooks.jinx.JinxConstants;
import net.jeremybrooks.jinx.OAuthAccessToken;
import net.jeremybrooks.jinx.response.photos.Photo;
import net.jeremybrooks.jinx.response.photos.PhotoInfo;
import net.jeremybrooks.jinx.response.photos.SearchParameters;
import net.jeremybrooks.jinx.response.photos.Tag;
import net.jeremybrooks.suprsetr.flickr.FlickrHelper;
import net.jeremybrooks.suprsetr.flickr.JinxFactory;
import net.jeremybrooks.suprsetr.flickr.PhotoHelper;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * @author Jeremy Brooks
 *
 */
public class SSUtilsTest {
  @Test
  public void colorCodeSetToString() throws Exception {
    List<JinxConstants.ColorCode> colorCodes = new ArrayList<>();
    colorCodes.add(JinxConstants.ColorCode.red);
    colorCodes.add(JinxConstants.ColorCode.blue);
    colorCodes.add(JinxConstants.ColorCode.black);
    String result = SSUtils.colorCodeListToString(colorCodes);
    assertEquals("Red,Blue,Black", result);
    assertEquals("", SSUtils.colorCodeListToString(null));
  }

  @Test
  public void stringToColorCodes() throws Exception {
    String string = "Red,Blue,Black";
    List<JinxConstants.ColorCode> colorCodes = SSUtils.stringToColorCodeList(string);
    assertTrue(colorCodes.size() == 3);
    assertTrue(colorCodes.contains(JinxConstants.ColorCode.black));
    assertTrue(colorCodes.contains(JinxConstants.ColorCode.blue));
    assertTrue(colorCodes.contains(JinxConstants.ColorCode.red));

    colorCodes = SSUtils.stringToColorCodeList(null);
    assertTrue(colorCodes.isEmpty());
  }

  @Test
  public void pictureStyleListToString() throws Exception {
    List<JinxConstants.PictureStyle> list = new ArrayList<>();
    list.add(JinxConstants.PictureStyle.blackandwhite);
    list.add(JinxConstants.PictureStyle.depthoffield);
    String result = SSUtils.pictureStyleListToString(list);
    assertEquals("Black and White,Depth of Field", result);
    assertEquals("", SSUtils.pictureStyleListToString(null));
  }

  @Test
  public void stringToPictureStyleList() throws Exception {
    String string = "Depth of Field,Pattern";
    List<JinxConstants.PictureStyle> list = SSUtils.stringToPictureStyleList(string);
    assertTrue(list.size() == 2);
    assertTrue(list.contains(JinxConstants.PictureStyle.depthoffield));
    assertTrue(list.contains(JinxConstants.PictureStyle.pattern));
    list = SSUtils.stringToPictureStyleList(null);
    assertTrue(list.isEmpty());
  }

  @Test
  public void orientationListToString() throws Exception {
    List<JinxConstants.Orientation> list = new ArrayList<>();
    list.add(JinxConstants.Orientation.landscape);
    list.add(JinxConstants.Orientation.square);
    String result = SSUtils.orientationListToString(list);
    assertEquals("landscape,square", result);
    assertEquals("", SSUtils.orientationListToString(null));
  }

  @Test
  public void stringToOrientationList() throws Exception {
    String string = "landscape,square";
    List<JinxConstants.Orientation> list = SSUtils.stringToOrientationList(string);
    assertTrue(list.size() == 2);
    assertTrue(list.contains(JinxConstants.Orientation.landscape));
    assertTrue(list.contains(JinxConstants.Orientation.square));
    list = SSUtils.stringToOrientationList(null);
    assertTrue(list.isEmpty());
  }

  @Test
  public void x() throws Exception {
    Properties privateProperties = new Properties();
    privateProperties.load(SSUtilsTest.class.getClassLoader().getResourceAsStream("net/jeremybrooks/suprsetr/private.properties"));
    JinxFactory.getInstance().init(privateProperties.getProperty("FLICKR_KEY"), privateProperties.getProperty("FLICKR_SECRET"));
    OAuthAccessToken oAuthAccessToken = new OAuthAccessToken();
    InputStream in = null;
      in = new FileInputStream(new File("/Users/jeremyb/.suprsetr/jinx_oauth.token"));
      oAuthAccessToken.load(in);
      JinxFactory.getInstance().setAccessToken(oAuthAccessToken);

    List<Photo> photos;
    int processed = 0;
    int total;
    int count = 0;
    String tagType = "favrtagr:count=";
    try {

      // Search for:
      //    All media types
      //    Uploaded from the beginning of time until tomorrow
      //    Return tags as well
      SearchParameters params = new SearchParameters();
      params.setUserId(FlickrHelper.getInstance().getNSID());
      params.setMediaType(JinxConstants.MediaType.all);
      params.setMinUploadDate(new Date(0));
      params.setMaxUploadDate(new Date(System.currentTimeMillis() + 86400000));
      if (tagType.equals("fav")) {
        params.setExtras(EnumSet.of(JinxConstants.PhotoExtras.tags));
      } else {
        params.setExtras(EnumSet.of(JinxConstants.PhotoExtras.machine_tags));
      }
      photos = PhotoHelper.getInstance().getPhotos(params);

      total = photos.size();

      System.out.println("got " + total + " photos");

      // iterate through all photos
      for (Photo p : photos) {
        // if it looks like we might have some fav tags, get the photo info
        boolean containsTag = false;
        if (tagType.equals("fav")) {
          String tags = p.getMachineTags();
          if (tags != null) {
            containsTag = tags.contains(tagType);
          }
        } else {
          String machineTags = p.getMachineTags();
          if (machineTags != null) {
            System.out.println(machineTags);
            containsTag = machineTags.contains(tagType);
          }
        }
        if (containsTag) {
          PhotoInfo pi = PhotoHelper.getInstance().getPhotoInfo(p);
          // Look for this.tagType tags, and delete them.
          for (Tag tag : pi.getTags()) {
            if (tag.getRaw().startsWith(tagType)) {
              try {
                if (Integer.parseInt(tag.getRaw().substring(3)) > 0) {
                  System.out.println("Removing tag " + tag.toString() + " from photo " + p.getPhotoId());
                  PhotoHelper.getInstance().removeTag(tag.getTagId());
                  count++;
                  System.out.println("removed " + tag.getTag() + " from photo " + p.getPhotoId());
                }
              } catch (Exception e) {
                // ignore
              }
            }
          }
        }
        processed++;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}