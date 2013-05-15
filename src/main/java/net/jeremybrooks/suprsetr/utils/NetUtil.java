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

package net.jeremybrooks.suprsetr.utils;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * @author Jeremy Brooks
 */
public class NetUtil {

	public static void enableSystemProxy() {
		System.setProperty("java.net.useSystemProxies", "true");
	}

	public static void enableProxy(String host, String port, final String username, final char[] password) {
		System.setProperty("http.proxyHost", host);
		System.setProperty("http.proxyPort", port);

		if ((username != null) && (!username.isEmpty()))
			Authenticator.setDefault(new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
	}

	public static void clearProxy() {
		System.clearProperty("http.proxyHost");
		System.clearProperty("http.proxyPort");
		System.clearProperty("java.net.useSystemProxies");
	}
}
