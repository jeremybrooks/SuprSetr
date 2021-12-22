/*
 *  SuprSetr is Copyright 2010-2021 by Jeremy Brooks
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

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileIsBackupDirectoryFilterTest {


  private static File testDir = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
  private static File nope1 = new File(testDir, ".DS_Store");     // should fail regex
  private static File nope2 = new File(testDir, "20200320_2009"); // should fail dir check
  private static File nope3 = new File(testDir, "20200321_2009");  // should fail regex
  private static File yes1 = new File(testDir, "202003202109");  // should pass

  @BeforeClass
  public static void setup() throws Exception {
    Assert.assertTrue(testDir.mkdirs());
    Assert.assertTrue(nope1.mkdirs());
    Assert.assertTrue(nope2.createNewFile());
    Assert.assertTrue(nope3.mkdirs());
    Assert.assertTrue(yes1.mkdirs());
  }

  @AfterClass
  public static void cleanup() throws Exception {
    FileUtils.deleteDirectory(testDir);
  }

  @Test
  public void accept() {
    FilenameFilter filter = new FileIsBackupDirectoryFilter();
    assertFalse(filter.accept(nope1, nope1.getName()));
    assertFalse(filter.accept(nope2, nope2.getName()));
    assertFalse(filter.accept(nope3, nope3.getName()));
    assertTrue(filter.accept(yes1, yes1.getName()));
  }
}