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

import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.tar.ZLTarEntryFile;

public abstract class ZLArchiveEntryFile extends ZLFile {
	public static String normalizeEntryName(String entryName) {
		while (entryName.startsWith("./")) {
			entryName = entryName.substring(2);
		}
		while (true) {
			final int index = entryName.lastIndexOf("/./");
			if (index == -1) {
				break;
			}
			entryName = entryName.substring(0, index) + entryName.substring(index + 2);
		}
		while (true) {
			final int index = entryName.indexOf("/../");
			if (index <= 0) {
				break;
			}
			final int prevIndex = entryName.lastIndexOf('/', index - 1);
			if (prevIndex == -1) {
				entryName = entryName.substring(index + 4);
				break;
			}
			entryName = entryName.substring(0, prevIndex) + entryName.substring(index + 3);
		}
		return entryName;
	}

	public static ZLArchiveEntryFile createArchiveEntryFile(ZLFile archive, String entryName) {
		if (archive == null) {
			return null;
		}
		entryName = normalizeEntryName(entryName);
		switch (archive.myArchiveType & ArchiveType.ARCHIVE) {
			case ArchiveType.ZIP:
				return new ZLZipEntryFile(archive, entryName);
			case ArchiveType.TAR:
				return new ZLTarEntryFile(archive, entryName);
			default:
				return null;
		}
	}

	static List<ZLFile> archiveEntries(ZLFile archive) {
		switch (archive.myArchiveType & ArchiveType.ARCHIVE) {
			case ArchiveType.ZIP:
				return ZLZipEntryFile.archiveEntries(archive);
			case ArchiveType.TAR:
				return ZLTarEntryFile.archiveEntries(archive);
			default:
				return Collections.emptyList();
		}
	}

	protected final ZLFile myParent;
	protected final String myName;

	protected ZLArchiveEntryFile(ZLFile parent, String name) {
		myParent = parent;
		myName = name;
		init();
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public String getPath() {
		return myParent.getPath() + ":" + myName;
	}

	@Override
	public String getLongName() {
		return myName;
	}

	@Override
	public ZLFile getParent() {
		return myParent;
	}

	@Override
	public ZLPhysicalFile getPhysicalFile() {
		ZLFile ancestor = myParent;
		while ((ancestor != null) && !(ancestor instanceof ZLPhysicalFile)) {
			ancestor = ancestor.getParent();
		}
		return (ZLPhysicalFile)ancestor;
	}
}
