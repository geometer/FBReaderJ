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

package org.geometerplus.fbreader.network.opds;

import java.util.*;
import java.io.*;

import org.geometerplus.zlibrary.core.filesystem.ZLPhysicalFile;
import org.geometerplus.zlibrary.core.network.*;

import org.geometerplus.fbreader.network.*;

public class OPDSLinkReader {
	static final String CATALOGS_URL = "https://data.fbreader.org/catalogs/generic-2.0.xml";
	static final String FILE_NAME = "fbreader_catalogs-"
			+ CATALOGS_URL.substring(CATALOGS_URL.lastIndexOf("/") + 1);

	public enum CacheMode {
		LOAD,
		UPDATE,
		CLEAR
	};

	public static List<INetworkLink> loadOPDSLinks(NetworkLibrary library, ZLNetworkContext nc, CacheMode cacheMode) throws ZLNetworkException {
		final OPDSLinkXMLReader xmlReader = new OPDSLinkXMLReader(library);

		final File dirFile = new File(library.SystemInfo.networkCacheDirectory());
		if (!dirFile.exists() && !dirFile.mkdirs()) {
			nc.perform(new ZLNetworkRequest.Get(CATALOGS_URL) {
				@Override
				public void handleStream(InputStream inputStream, int length) throws IOException, ZLNetworkException {
					xmlReader.read(inputStream);
				}
			});
			return xmlReader.links();
		}

		boolean cacheIsGood = false;
		File oldCache = null;
		final File catalogsFile = new File(dirFile, FILE_NAME);
		if (catalogsFile.exists()) {
			switch (cacheMode) {
				case UPDATE:
					final long diff = System.currentTimeMillis() - catalogsFile.lastModified();
					if (diff >= 0 && diff <= 7 * 24 * 60 * 60 * 1000) { // one week
						return Collections.emptyList();
					}
					/* FALLTHROUGH */
				case CLEAR:
					oldCache = new File(dirFile, "_" + FILE_NAME);
					oldCache.delete();
					if (!catalogsFile.renameTo(oldCache)) {
						catalogsFile.delete();
						oldCache = null;
					}
					break;
				case LOAD:
					cacheIsGood = true;
					break;
			}
		}

		if (!cacheIsGood) {
			try {
				nc.downloadToFile(CATALOGS_URL, catalogsFile);
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
			xmlReader.read(new ZLPhysicalFile(catalogsFile));
			return xmlReader.links();
		} catch (IOException e) {
			return Collections.emptyList();
		}
	}
}
