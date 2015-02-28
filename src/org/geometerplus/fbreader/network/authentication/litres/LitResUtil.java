/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.network.authentication.litres;

import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;

import org.geometerplus.fbreader.network.INetworkLink;

class LitResUtil {
	public static final String HOST_NAME = "litres.ru";

	public static String url(String path) {
		final String url = "://robot.litres.ru/" + path;
		if (ZLNetworkUtil.hasParameter(url, "sid") || ZLNetworkUtil.hasParameter(url, "pwd")) {
			return "https" + url;
		} else {
			return "http" + url;
		}
	}

	public static String url(INetworkLink link, String path) {
		return link.rewriteUrl(url(path), false);
	}
}
