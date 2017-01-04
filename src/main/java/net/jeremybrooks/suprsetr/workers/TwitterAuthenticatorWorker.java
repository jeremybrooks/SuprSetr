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

package net.jeremybrooks.suprsetr.workers;

import net.jeremybrooks.suprsetr.BlockerPanel;
import net.jeremybrooks.suprsetr.Preferences;
import net.jeremybrooks.suprsetr.twitter.TwitterHelper;
import org.apache.log4j.Logger;

import javax.swing.JDialog;
import javax.swing.SwingWorker;
import java.util.ResourceBundle;


/**
 * This class performs the Twitter authentication operations in the background.
 *
 * <p>This class extends SwingWorker, so the GUI can remain responsive and
 * the user can be updated about the progress of the operation. The
 * BlockerPanel class is used to prevent the user from accessing the GUI during
 * the operation, and to provide the user with feedback.</p>
 *
 * @author jeremyb
 */
public class TwitterAuthenticatorWorker extends SwingWorker<Void, Void> {

	/**
	 * Logging.
	 */
	private Logger logger = Logger.getLogger(TwitterAuthenticatorWorker.class);

	/**
	 * The blocker.
	 */
	private BlockerPanel blocker;

	/**
	 * Parent dialog.
	 */
	private JDialog parent;

	private ResourceBundle resourceBundle = ResourceBundle.getBundle("net.jeremybrooks.suprsetr.workers");


	/**
	 * Create an instance of TwitterAuthenticator.
	 *
	 * @param parent  the parent dialog.
	 * @param blocker the blocker.
	 */
	public TwitterAuthenticatorWorker(JDialog parent, BlockerPanel blocker) {
		this.parent = parent;
		this.blocker = blocker;
	}


	/**
	 * Execute the Twitter authentication operations on a background thread.
	 *
	 * <p>The TwitterHelper class does the real work, but this method allows
	 * that work to happen on a non-GUI thread, so the GUI remains responsive.</p>
	 *
	 * @return this method does not return any data.
	 */
	@Override
	protected Void doInBackground() {
		blocker.block(resourceBundle.getString("TwitterAuthWorker.blocker.authorizing"));
		try {
			TwitterHelper.authenticate();
		} catch (Exception e) {
			logger.warn("COULD NOT AUTHORIZE TWITTER.", e);
			((Preferences) this.parent).setMessage(resourceBundle.getString("TwitterAuthWorker.message.error"));
		}
		return null;
	}


	/**
	 * Finished, so update the status of the parent dialog and unblock.
	 */
	@Override
	protected void done() {
		try {
			((Preferences) this.parent).updateStatus();
		} catch (Exception e) {
			logger.warn("COULD NOT UPDATE STATUS ON PREFERENCES WINDOW.", e);
		}
		blocker.unBlock();

	}
}
