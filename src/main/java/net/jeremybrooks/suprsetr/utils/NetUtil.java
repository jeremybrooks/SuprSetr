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

import net.jeremybrooks.jinx.JinxProxy;
import net.jeremybrooks.suprsetr.flickr.JinxFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * @author Jeremy Brooks
 */
public class NetUtil {

  private static JinxProxy jinxProxy;

  public static void enableProxy(String host, String port, final String username, final char[] password) {
    jinxProxy = new JinxProxy(host, Integer.parseInt(port), username, password);
    JinxFactory.getInstance().setProxy(jinxProxy);
  }

  public static void clearProxy() {
    jinxProxy = null;
    JinxFactory.getInstance().setProxy(null);
  }

  /**
   * Get the network proxy.
   * <p>
   * Jinx handles the proxy on its own, but other things that need the network (VersionChecker, for example)
   * need a proxy as well.
   * <p>
   * This method will return Proxy.NO_PROXY if there is no proxy configured.
   * <p>
   * Callers can use the return value of this method in the openConnection method.
   *
   * @return instance of Proxy to use.
   */
  public static Proxy getProxy() {
    if (jinxProxy == null) {
      return Proxy.NO_PROXY;
    } else {
      // note: if proxy authentication is needed, it will already have been set up when the Jinx
      //       proxy was enabled, so no need to set it up again.
      return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(jinxProxy.getProxyHost(), jinxProxy.getProxyPort()));
    }
  }
}
