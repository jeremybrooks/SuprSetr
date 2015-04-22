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

package net.jeremybrooks.suprsetr.workers;

import net.jeremybrooks.jinx.response.photosets.PhotosetInfo;
import net.jeremybrooks.suprsetr.MainWindow;
import net.jeremybrooks.suprsetr.SSPhotoset;
import net.jeremybrooks.suprsetr.flickr.PhotosetHelper;
import net.jeremybrooks.suprsetr.utils.SimpleCache;

import javax.swing.*;
import java.util.List;

/**
 * Fetch the view count in the background and trigger GUI update.
 */
public class UpdateViewCountWorker extends SwingWorker<Void, Void> {

    private List<SSPhotoset> photosetList;

    public UpdateViewCountWorker(List<SSPhotoset> photosetList) {
        this.photosetList = photosetList;
    }

    @Override
    protected Void doInBackground() throws Exception {
        if (this.photosetList != null) {
            for (SSPhotoset set : this.photosetList) {
                try {
                    this.updatePhotoset(set);
                    MainWindow.getMainWindow().updatePhotosetInList(set);
                } catch (Exception e) {
                    // error was already logged in the updatePhotoset method
                    // continue with the next set
                }
            }
        }
        return null;
    }

    private void updatePhotoset(SSPhotoset ssPhotoset) throws Exception {
        if (ssPhotoset.getViewCount() == -1) {
            PhotosetInfo info = PhotosetHelper.getInstance().getPhotosetById(ssPhotoset.getPhotosetId());
            ssPhotoset.setViewCount(info.getPhotoset().getCountViews());
            // invalidate so new value gets displayed
            SimpleCache.getInstance().invalidate(ssPhotoset.getPhotosetId());
        }
    }
}
