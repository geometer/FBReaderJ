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

package org.geometerplus.zlibrary.core.network;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

public class QuietNetworkContext extends ZLNetworkContext {
	@Override
	public Map<String,String> authenticate(URI uri, String realm, Map<String,String> params) {
		return Collections.singletonMap("error", "Required authorization");
	}

	public final boolean downloadToFileQuietly(String url, final File outFile) {
		try {
			downloadToFile(url, outFile);
			return true;
		} catch (ZLNetworkException e) {
			return false;
		}
	}
}
