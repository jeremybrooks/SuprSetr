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

import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.flickr.FlickrHelper;
import org.apache.log4j.Logger;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.awt.Desktop;
import java.net.URL;
import java.util.ResourceBundle;


/**
 * This class performs Flickr authentication on a background thread.
 * <p/>
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 * @author jeremyb
 */
public class FlickrAuthenticatorWorker extends SwingWorker<Void, Void> {

	/**
	 * Logging.
	 */
	private Logger logger = Logger.getLogger(FlickrAuthenticatorWorker.class);

	/**
	 * The blocker instance.
	 */
	private BlockerPanel blocker;

	/**
	 * The parent dialog.
	 */
	private JDialog parent;
	private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.workers");


	/**
	 * Create a new instance of FlickrAuthenticator.
	 *
	 * @param parent  the parent dialog.
	 * @param blocker the blocker.
	 */
	public FlickrAuthenticatorWorker(JDialog parent, BlockerPanel blocker) {
		this.parent = parent;
		this.blocker = blocker;
	}


	/**
	 * Execute the Flickr operation and database operations on a background
	 * thread.
	 * <p/>
	 * <p>The user's browser will be opened to the Flickr auth page. Once the
	 * user has authorized SuprSetr, control will return to the parent dialog.</p>
	 *
	 * @return this method does not return any data.
	 */
	@Override
	protected Void doInBackground() {
		blocker.block(resourceBundle.getString("FlickrAuthWorker.blocker.gettingurl"));
		try {
			URL url = FlickrHelper.getInstance().getAuthenticationURL();

			Desktop.getDesktop().browse(url.toURI());

			blocker.updateMessage(resourceBundle.getString("FlickrAuthWorker.blocker.waiting"));

			JOptionPane.showMessageDialog(this.parent,
					resourceBundle.getString("FlickrAuthWorker.dialog.open.message"),
					resourceBundle.getString("FlickrAuthWorker.dialog.open.title"),
					JOptionPane.INFORMATION_MESSAGE);

			blocker.updateMessage(resourceBundle.getString("FlickrAuthWorker.blocker.completing"));

			FlickrHelper.getInstance().completeAuthentication();

			logger.info("Authentication success.");

		} catch (Exception e) {
			logger.error("Error while attempting to authenticate.", e);
			JOptionPane.showMessageDialog(this.parent,
					resourceBundle.getString("FlickrAuthWorker.dialog.error.message"),
					e.getMessage(),
					JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}


	/**
	 * Finished, so unblock and return control to the parent dialog.
	 */
	@Override
	protected void done() {
		blocker.unBlock();
		this.parent.dispose();
		this.parent.setVisible(false);
	}
}
