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

import net.jeremybrooks.suprsetr.SetListCell;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple cache implementation for the SetListCell.
 *
 * The SetListCell object is complex, and takes a relatively long time to
 * create. This cache allows us to reuse instances of the SetListCell. The
 * CellRenderer will use cached instances unless the instance has been marked
 * as invalid.
 *
 * This class is implemented as a Singleton. When a photoset is possibly changed,
 * for example by a refresh, the SetListCell in the cache will be marked as
 * invalid.
 *
 * @author Jeremy Brooks
 */
public class SimpleCache {


  /**
   * Maps the photoset ID to the list cell instance.
   */
  private Map<String, SetListCell> cache;

  /**
   * Reference to this class.
   */
  private static SimpleCache instance = null;


  /**
   * Constructor.
   */
  private SimpleCache() {
    this.cache = new HashMap<>();
  }


  /**
   * Get a reference to the only instance of this class.
   *
   * @return instance of this class.
   */
  public static SimpleCache getInstance() {
    if (instance == null) {
      instance = new SimpleCache();
    }

    return instance;
  }


  /**
   * Get the cell for the specified photoset ID.
   *
   * @param id the photoset ID for the cell.
   * @return the instance of the cell, or null if it does not exist in cache.
   */
  public SetListCell getFromCache(String id) {
    return this.cache.get(id);
  }


  /**
   * Put a cell in the cache.
   *
   * @param id   the photoset ID to associate with the cell instance.
   * @param cell the cell to put in cache.
   */
  public void putInCache(String id, SetListCell cell) {
    this.cache.put(id, cell);
  }


  /**
   * Marks the cell associated with the ID as invalid. This will cause the
   * CellRenderer to update the fields on the cell. If there is no cell for
   * the ID, nothing happens.
   *
   * @param id the photoset ID to mark as invalid.
   */
  public void invalidate(String id) {
    if (this.cache.containsKey(id)) {
      this.cache.get(id).setCacheValid(false);
    }
  }

  /**
   * Invalidate the state of all the cells in the cache.
   *
   * <p>This is used when we want to force the cells to be redrawn, for example when the user
   * changes the refresh time in settings.</p>
   */
  public void invalidateAll() {
    for (SetListCell cell : this.cache.values()) {
      cell.setCacheValid(false);
    }
  }

}
