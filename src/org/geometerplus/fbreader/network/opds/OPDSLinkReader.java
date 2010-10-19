/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network.opds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.net.URLConnection;

import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.atom.ATOMUpdated;


public class OPDSLinkReader {

	static final String CATALOGS_URL = "http://data.fbreader.org/catalogs/generic-1.0.xml";

	public static ICustomNetworkLink createCustomLink(int id, String siteName, String title, String summary, String icon, Map<String, String> links) {
		if (siteName == null || title == null || links.get(INetworkLink.URL_MAIN) == null) {
			return null;
		}
		return new OPDSCustomLink(id, siteName, title, summary, icon, links);
	}

	public static ICustomNetworkLink createCustomLinkWithoutInfo(String siteName, String url) {
		final HashMap<String, String> links = new HashMap<String, String>();
		links.put(INetworkLink.URL_MAIN, url);
		return new OPDSCustomLink(ICustomNetworkLink.INVALID_ID, siteName, null, null, null, links);
	}

	public static final int CACHE_LOAD = 0;
	public static final int CACHE_UPDATE = 1;
	public static final int CACHE_CLEAR = 2;

	public static void loadOPDSLinks(int cacheMode, final NetworkLibrary.OnNewLinkListener listener) throws ZLNetworkException {
		final File dirFile = new File(Paths.networkCacheDirectory());
		if (!dirFile.exists() && !dirFile.mkdirs()) {
			ZLNetworkManager.Instance().perform(new ZLNetworkRequest(CATALOGS_URL) {
				@Override
				public void handleStream(URLConnection connection, InputStream inputStream) throws IOException, ZLNetworkException {
					new OPDSLinkXMLReader(listener, null).read(inputStream);
				}
			});
			// TODO: Is this error is needed?
			//throw new ZLNetworkException("cacheDirectoryError");
			return;
		}

		final String fileName = "fbreader_catalogs-"
			+ CATALOGS_URL.substring(CATALOGS_URL.lastIndexOf(File.separator) + 1);

		boolean cacheIsGood = false;
		File oldCache = null;
		ATOMUpdated cacheUpdatedTime = null;
		final File catalogsFile = new File(dirFile, fileName);
		if (catalogsFile.exists()) {
			switch (cacheMode) {
			case CACHE_UPDATE:
				final long diff = System.currentTimeMillis() - catalogsFile.lastModified();
				final long valid = 7 * 24 * 60 * 60 * 1000; // one week in milliseconds; FIXME: hardcoded const
				if (diff >= 0 && diff <= valid) {
					return;
				}
				/* FALLTHROUGH */
			case CACHE_CLEAR:
				try {
					final OPDSLinkXMLReader reader = new OPDSLinkXMLReader();
					reader.read(new FileInputStream(catalogsFile));
					cacheUpdatedTime = reader.getUpdatedTime();
				} catch (FileNotFoundException e) {
					throw new RuntimeException("That's impossible!!!", e); 
				}

				oldCache = new File(dirFile, "_" + fileName);
				oldCache.delete();
				if (!catalogsFile.renameTo(oldCache)) {
					catalogsFile.delete();
					oldCache = null;
				}
				break;
			case CACHE_LOAD:
				cacheIsGood = true;
				break;
			default:
				throw new IllegalArgumentException("Invalid cacheMode value (" + cacheMode
						+ ") in OPDSLinkReader.loadOPDSLinks method");
			}
		}

		if (!cacheIsGood) {
			try {
				ZLNetworkManager.Instance().downloadToFile(CATALOGS_URL, catalogsFile);
			} catch (ZLNetworkException e) {
				if (oldCache == null) {
					throw e;
				}
				catalogsFile.delete();
				if (!oldCache.renameTo(catalogsFile)) {
					oldCache.delete();
					oldCache = null;
					throw e;
				}
			} finally {
				if (oldCache != null) {
					oldCache.delete();
					oldCache = null;
				}
			}
		}

		try {
			new OPDSLinkXMLReader(listener, cacheUpdatedTime).read(new FileInputStream(catalogsFile));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("That's impossible!!!", e); 
		}
	}
}
