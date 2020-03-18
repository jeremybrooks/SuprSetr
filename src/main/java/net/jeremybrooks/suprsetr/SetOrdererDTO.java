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

package net.jeremybrooks.suprsetr;

import javax.swing.ImageIcon;

/**
 * Encapsulates the data needed by the SetOrderer tool.
 *
 * <p>This class is used rather than the SSPhotoset object because we do not
 * need all that data. Using a smaller class may conserve some memory.</p>
 *
 * @author Jeremy Brooks
 */
public class SetOrdererDTO {

  /**
   * The set ID.
   */
  private String id;

  /**
   * The set title.
   */
  private String title;

  /**
   * The description of the set.
   */
  private String description;

  /**
   * The photo count.
   */
  private int photoCount;

  /**
   * The view count.
   */
  private int viewCount;

  private int videoCount;

  /**
   * The icon.
   */
  private ImageIcon icon;


  /**
   * @return the id
   */
  public String getId() {
    return id;
  }


  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }


  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }


  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }


  /**
   * @return the photoCount
   */
  public int getPhotoCount() {
    return photoCount;
  }


  /**
   * @param photoCount the photoCount to set
   */
  public void setPhotoCount(int photoCount) {
    this.photoCount = photoCount;
  }


  /**
   * @return the icon
   */
  public ImageIcon getIcon() {
    return icon;
  }


  /**
   * @param icon the icon to set
   */
  public void setIcon(ImageIcon icon) {
    this.icon = icon;
  }


  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return the view count.
   */
  public int getViewCount() {
    return this.viewCount;
  }

  /**
   * @param viewCount the view count.
   */
  public void setViewCount(int viewCount) {
    this.viewCount = viewCount;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }


  public int getVideoCount() {
    return videoCount;
  }

  public void setVideoCount(int videoCount) {
    this.videoCount = videoCount;
  }
}
