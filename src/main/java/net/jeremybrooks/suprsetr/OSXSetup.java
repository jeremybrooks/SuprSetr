/*
 * SuprSetr is Copyright 2010-2013 by Jeremy Brooks
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
package net.jeremybrooks.suprsetr;

// APPLE STUFF
import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

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

	app.setAboutHandler(new AboutHandler() {

	    @Override
	    public void handleAbout(AboutEvent ae) {
		new AboutDialog(null, true).setVisible(true);
	    }

	});


	app.setQuitHandler(new QuitHandler() {

	    @Override
	    public void handleQuitRequestWith(QuitEvent qe, QuitResponse qr) {
		int confirm = JOptionPane.YES_OPTION;

		// make the user confirm if busy
		if (MainWindow.isBlocked()) {
		    confirm = JOptionPane.showConfirmDialog(MainWindow.getMainWindow(),
			    "SuprSetr is currently busy.\n"
			    + "Are you sure you want to quit now?",
			    "Quit?",
			    JOptionPane.YES_NO_OPTION,
			    JOptionPane.QUESTION_MESSAGE);

		    if (confirm == JOptionPane.YES_OPTION) {
			qr.performQuit();
		    }
		    
		} else {
		    qr.performQuit();
		}
	    }

	});


	app.setPreferencesHandler(new PreferencesHandler() {

	    @Override
	    public void handlePreferences(PreferencesEvent pe) {
		new Preferences(MainWindow.getMainWindow(), true).setVisible(true);
	    }

	});

    }

}
