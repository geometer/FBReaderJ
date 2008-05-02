/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.library.ZLibrary;

abstract class ZLFSUtil {
	static String normalize(String path) {
		if (getRootDirectoryPath().equals(path)) {
			return path;
		}
		try {
			path = new File(path).getCanonicalPath();
		} catch (IOException e) {
		}
		return path;
	}

	//public OutputStream createOutputStream(String path);
	
	static ZLDir getRootDirectory() {
		return new ZLFSDir(getRootDirectoryPath());
	}
	
	static String getRootDirectoryPath() {
		return File.listRoots().length == 1 ? File.listRoots()[0].getPath().trim() : "";
	}
	
	static String getParentPath(String path) {
		File file = new File(path);
		String parent = file.getParent();
		if (parent == null) {
			File [] roots = File.listRoots();
			for (int i = 0; i < roots.length; i++) {
				if (roots[i].equals(file)) {
					parent = getRootDirectoryPath();
					break;
				}
			}
		}
		return parent;
	}

	static int findArchiveFileNameDelimiter(String path) {
		int index = path.lastIndexOf(':');
		if (path.startsWith(ZLibrary.JAR_DATA_PREFIX)) {
			return (index < ZLibrary.JAR_DATA_PREFIX.length()) ? -1 : index;
		}
		if (System.getProperty("os.name").startsWith("Windows")) {
			return (index == 1) ? -1 : index;
		}
		return index;
	}
	
	static int findLastFileNameDelimiter(String path) {
		int index = findArchiveFileNameDelimiter(path);
		if (index == -1) {
			index = path.lastIndexOf(File.separatorChar);
		}
		return index;
	}
}
