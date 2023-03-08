/*
 *  SuprSetr is Copyright 2010-2023 by Jeremy Brooks
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

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

public class FilenameComparatorTest {

  @Test
  public void compare() {
    File[] files = {
        new File("xxx"),
        new File("abc"),
        new File("mmm")
    };

    Arrays.sort(files, new FilenameComparator());

    Assert.assertEquals("abc", files[0].getName());
    Assert.assertEquals("mmm", files[1].getName());
    Assert.assertEquals("xxx", files[2].getName());
  }
}