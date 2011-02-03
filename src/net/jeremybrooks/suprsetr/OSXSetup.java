/*
 * SuprSetr is Copyright 2010 by Jeremy Brooks
 *
 * This file is part of SuprSetr.
 *
 *  SuprSetr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  SuprSetr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with SuprSetr.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.jeremybrooks.suprsetr;

// APPLE STUFF
import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationAdapter;

import javax.swing.JOptionPane;


/**
 * Handle Mac-specific events.
 *
 *
 * @author jeremyb
 */
public class OSXSetup {

    public OSXSetup() {
	Application app = Application.getApplication();
	app.addApplicationListener(new MacOSEventHandler());
	app.setEnabledAboutMenu(true);
	app.setEnabledPreferencesMenu(true);
    }


    class MacOSEventHandler extends ApplicationAdapter {

	/**
	 * Handle the About event.
	 *
	 * @param event the application event.
	 */
	@Override
	public void handleAbout(ApplicationEvent event) {
	    event.setHandled(true);
	    new AboutDialog(null, true).setVisible(true);
	}


	/**
	 * Handle the Quit event.
	 *
	 * @param event the application event.
	 */
	@Override
	public void handleQuit(ApplicationEvent event) {
	    int confirm = JOptionPane.YES_OPTION;

	    // make the user confirm if busy
	    if (MainWindow.isBlocked()) {
		confirm = JOptionPane.showConfirmDialog(MainWindow.getMainWindow(),
			"SuprSetr is currently busy.\n"
			+ "Are you sure you want to quit now?",
			"Quit?",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE);
	    }


	    if (confirm == JOptionPane.YES_OPTION) {
		System.exit(0);
	    }
	}


	/**
	 * Handle the OpenApplication event.
	 *
	 * <p>Currently, we do not do anything with this event.</p>
	 *
	 * @param event the application event.
	 */
	public void handleOpenApplication(ApplicationEvent event) {
	}


	/**
	 * Handle the OpenFile event.
	 *
	 * <p>Currently, we do not do anything with this event.</p>
	 *
	 * @param event the application event.
	 */
	public void handleOpenFile(ApplicationEvent event) {
	}


	/**
	 * Handle the Preferences event.
	 *
	 * <p>Currently, we do not do anything with this event.</p>
	 *
	 * @param event the application event.
	 */
	@Override
	public void handlePreferences(ApplicationEvent event) {
	    new Preferences(MainWindow.getMainWindow(), true).setVisible(true);
	}


	/**
	 * Handle the PrintFile event.
	 *
	 * <p>Currently, we do not do anything with this event.</p>
	 *
	 * @param event the application event.
	 */
	public void handlePrintFile(ApplicationEvent event) {
	}


	/**
	 * Handle the ReOpenApplication event.
	 *
	 * <p>Currently, we do not do anything with this event.</p>
	 *
	 * @param event the application event.
	 */
	public void handleReOpenApplication(ApplicationEvent event) {
	}

    }
}
