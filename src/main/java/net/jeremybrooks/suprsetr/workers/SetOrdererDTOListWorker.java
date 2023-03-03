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

package net.jeremybrooks.suprsetr.workers;


import net.jeremybrooks.jinx.response.photosets.Photoset;
import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.SSPhotoset;
import net.jeremybrooks.suprsetr.SetOrderer;
import net.jeremybrooks.suprsetr.SetOrdererDTO;
import net.jeremybrooks.suprsetr.dao.PhotosetDAO;
import net.jeremybrooks.suprsetr.flickr.FlickrHelper;
import net.jeremybrooks.suprsetr.flickr.PhotosetHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * This class loads photosets from Flickr, then updates the SetOrderer list
 * model with the results.
 *
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 * @author Jeremy Brooks
 */
public class SetOrdererDTOListWorker extends SwingWorker<List<SetOrdererDTO>, SSPhotoset> {

  private static final Logger logger = LogManager.getLogger(SetOrdererDTOListWorker.class);

  private final BlockerPanel blocker;
  private final DefaultListModel<SetOrdererDTO> listModel;
  private final SetOrderer dialog;
  private final ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.workers");

  /**
   * Create a new instance of LoadFlickrSets.
   *
   * @param blocker   the blocker.
   * @param listModel the list model.
   * @param dialog    set orderer dialog.
   */
  public SetOrdererDTOListWorker(BlockerPanel blocker, DefaultListModel<SetOrdererDTO> listModel, SetOrderer dialog) {
    this.blocker = blocker;
    this.listModel = listModel;
    this.dialog = dialog;
  }


  /**
   * Execute the Flickr operation and database operations on a background
   * thread.
   *
   * @return this method does not return any data.
   */
  @Override
  protected List<SetOrdererDTO> doInBackground() {
    blocker.updateMessage(resourceBundle.getString("SetOrdererDTOListWorker.blocker.getting"));
    String nsid = FlickrHelper.getInstance().getNSID();
    List<SetOrdererDTO> dtoList = new ArrayList<>();

    try {
      List<Photoset> photosets = PhotosetHelper.getInstance().getPhotosets(nsid);
        for (Photoset p : photosets) {
          blocker.updateMessage(resourceBundle.getString("SetOrdererDTOListWorker.blocker.processing") + " \"" + p.getTitle() + "\"");

          // populate a DTO
          SetOrdererDTO sod = new SetOrdererDTO();
          sod.setDescription(p.getDescription());
          sod.setPhotoCount(p.getPhotos());
          sod.setVideoCount(p.getVideos());
          sod.setId(p.getPhotosetId());
          sod.setTitle(p.getTitle());
          sod.setViewCount(p.getCountViews());

          // get the icon from the database if possible
          SSPhotoset ssp = PhotosetDAO.getPhotosetForId(p.getPhotosetId());

          if (ssp == null) {
            // This means the set was created manually on Flickr after
            // SuprSetr was launched. So load the icon from Flickr
            // This is slower than loading from the database, but at
            // least we get the icon
            sod.setIcon(PhotosetHelper.getInstance().getIconForPhotoset(p));
          } else {
            // Check for missing icon, setting if needed
            if (ssp.getPrimaryPhotoIcon() == null) {
              sod.setIcon(PhotosetHelper.getInstance().getIconForPhotoset(p));
            } else {
              sod.setIcon(ssp.getPrimaryPhotoIcon());
            }
          }
          // add to the list
          dtoList.add(sod);
        }
    } catch (Exception e) {
      logger.error("ERROR GETTING PHOTOSET LIST FOR SET ORDERER.", e);

      JOptionPane.showMessageDialog(null,
          resourceBundle.getString("LoadFlickrSetsWorker.dialog.error.message") + e.getMessage(),
          resourceBundle.getString("LoadFlickrSetsWorker.dialog.error.title"),
          JOptionPane.ERROR_MESSAGE);
    }

    return dtoList;
  }


  /**
   * Finished, so update the GUI and unblock.
   */
  @Override
  protected void done() {
    // UPDATE THE LIST MODEL
    try {
      List<SetOrdererDTO> list = get();
      this.dialog.setDtoList(list);
      for (SetOrdererDTO sod : list) {
        this.listModel.addElement(sod);
      }
    } catch (Exception e) {
      logger.warn("ERROR DURING UPDATE.", e);
    }
    blocker.unBlock();
  }
}
