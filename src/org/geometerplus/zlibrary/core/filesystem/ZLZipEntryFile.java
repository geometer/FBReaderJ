/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.zlibrary.core.filesystem;

import java.io.*;
import java.util.*;

import org.amse.ys.zip.*;

final class ZLZipEntryFile extends ZLArchiveEntryFile {
	static List<ZLFile> archiveEntries(ZLFile archive) {
		try {
			final ZipFile zf = ZLZipEntryFile.getZipFile(archive);
			final Collection<LocalFileHeader> headers = zf.headers();
			if (!headers.isEmpty()) {
				ArrayList<ZLFile> entries = new ArrayList<ZLFile>(headers.size());
				for (LocalFileHeader h : headers) {
					entries.add(new ZLZipEntryFile(archive, h.FileName));
				}
				return entries;
			}
		} catch (IOException e) {
		}
		return Collections.emptyList();
	}

	private static HashMap<ZLFile,ZipFile> ourZipFileMap = new HashMap<ZLFile,ZipFile>();

	private static ZipFile getZipFile(final ZLFile file) throws IOException {
		synchronized (ourZipFileMap) {
			ZipFile zf = file.isCached() ? ourZipFileMap.get(file) : null;
			if (zf == null) {
				zf = new ZipFile(file);
				if (file.isCached()) {
					ourZipFileMap.put(file, zf);
				}
			}
			return zf;
		}
	}

	static void removeFromCache(ZLFile file) {
		ourZipFileMap.remove(file);
	}

	ZLZipEntryFile(ZLFile parent, String name) {
		super(parent, name);
	}

	@Override
	public boolean exists() {
		try {
			return myParent.exists() && getZipFile(myParent).entryExists(myName);
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public long size() {
		try {
			return getZipFile(myParent).getEntrySize(myName);
		} catch (IOException e) {
			return 0;
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return getZipFile(myParent).getInputStream(myName);
	}
}
