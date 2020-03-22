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

package net.jeremybrooks.suprsetr.utils;

import net.jeremybrooks.jinx.JinxConstants;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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

}