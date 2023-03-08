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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Jeremy Brooks
 */
public class ObjectCache {
  private static final Logger logger = LogManager.getLogger(ObjectCache.class);
  private final File cacheDir;

  public ObjectCache() throws IOException {
    this("ObjectCacheDir-" + System.currentTimeMillis() + System.getProperty("java.io.tmpdir"));
  }

  public ObjectCache(String cacheDirName) throws IOException {
    this(new File(System.getProperty("java.io.tmpdir"), cacheDirName));
  }

  public ObjectCache(File cacheDir) throws IOException {
    if (cacheDir == null) {
      throw new IOException("Cannot use a null directory.");
    }
    if (cacheDir.exists()) {
      if (!cacheDir.isDirectory()) {
        throw new IOException(cacheDir.getAbsolutePath() + " is not a directory.");
      }
    } else if (!cacheDir.mkdirs()) {
      throw new IOException("Could not create directory " + cacheDir.getAbsolutePath());
    }

    this.cacheDir = cacheDir;
    this.cacheDir.deleteOnExit();

    if (this.logger.isDebugEnabled())
      this.logger.debug("Using cache directory " + this.cacheDir.getAbsolutePath());
  }

  public void put(String name, Serializable ser) throws IOException {
    if ((name == null) || (name.isEmpty())) {
      throw new IOException("Name cannot be null or empty.");
    }
    if (ser == null) {
      throw new IOException("Cannot cache a null object.");
    }

    File cacheFile = new File(this.cacheDir, name);
    cacheFile.deleteOnExit();
    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cacheFile))) {
      out.writeObject(ser);
      this.logger.debug("Cached object '" + name + "' as " + cacheFile.getAbsolutePath());
      out.flush();
    }
  }

  public Object get(String name) throws IOException {
    if ((name == null) || (name.isEmpty())) {
      throw new IOException("Name cannot be null or empty.");
    }

    Object obj = null;
    File cacheFile = new File(this.cacheDir, name);

    if (cacheFile.exists()) {
      try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(cacheFile))) {
        obj = in.readObject();
      } catch (ClassNotFoundException cnfe) {
        throw new IOException("Unable to read the object from cache.", cnfe);
      }
    }

    return obj;
  }

  public void delete(String name) {
    if ((name == null) || (name.isEmpty())) {
      return;
    }
    File cacheFile = new File(this.cacheDir, name);

    if (cacheFile.exists()) {
      if (cacheFile.delete()) {
        this.logger.debug("Deleted " + cacheFile.getAbsolutePath());
      } else {
        this.logger.warn("Delete failed for file " + cacheFile.getAbsolutePath());
      }
    }
  }
}
