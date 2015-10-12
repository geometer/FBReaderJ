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

package org.geometerplus.fbreader.book;

import java.util.*;

import org.fbreader.util.ComparisonUtil;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLPhysicalFile;

class DuplicateResolver {
	private final Map<String,List<ZLFile>> myMap =
		Collections.synchronizedMap(new HashMap<String,List<ZLFile>>());

	void addFile(ZLFile file) {
		final String key = file.getShortName();
		List<ZLFile> list;
		synchronized (myMap) {
			list = myMap.get(key);
			if (list == null) {
				list = new LinkedList<ZLFile>();
				myMap.put(key, list);
			}
		}
		synchronized (list) {
			if (!list.contains(file)) {
				list.add(file);
			}
		}
	}

	void removeFile(ZLFile file) {
		final List<ZLFile> list = myMap.get(file.getShortName());
		if (list != null) {
			synchronized (list) {
				list.remove(file);
			}
		}
	}

	private String entryName(ZLFile file) {
		final String path = file.getPath();
		final int index = path.indexOf(":");
		return index == -1 ? null : path.substring(index + 1);
	}

	ZLFile findDuplicate(ZLFile file) {
		final ZLPhysicalFile pFile = file.getPhysicalFile();
		if (pFile == null) {
			return null;
		}
		final List<ZLFile> list = myMap.get(file.getShortName());
		if (list == null || list.isEmpty()) {
			return null;
		}
		final List<ZLFile> copy;
		synchronized (list) {
			copy = new ArrayList<ZLFile>(list);
		}

		final String entryName = entryName(file);
		final String shortName = pFile.getShortName();
		final long size = pFile.size();
		final long lastModified = pFile.javaFile().lastModified();
		for (ZLFile candidate : copy) {
			if (file.equals(candidate)) {
				return candidate;
			}
			final ZLPhysicalFile pCandidate = candidate.getPhysicalFile();
			if (pCandidate != null &&
				ComparisonUtil.equal(entryName, entryName(candidate)) &&
				shortName.equals(pCandidate.getShortName()) &&
				size == pCandidate.size() &&
				lastModified == pCandidate.javaFile().lastModified()
			) {
				return candidate;
			}
		}
		return null;
	}
}
