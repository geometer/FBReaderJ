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

public abstract class ZLDir {
	protected String myPath;

	public static ZLDir getRoot() {		
		return ZLFSUtil.getRootDirectory();
	}

	ZLDir(String path) {
		myPath = path;
	}
	
	public String getPath() {
		return myPath;
	}
	
	public String getName() {
		int index = ZLFSUtil.findLastFileNameDelimiter(myPath);
		return myPath.substring(index + 1);
	}
	
	public String getParentPath() {
		return ZLFSUtil.getParentPath(myPath);
	}
	
	public String getItemPath(String itemName) {
		if (itemName == "..") {
			return getParentPath();
		} else {
			return myPath.endsWith(File.separator) || myPath == "" ? myPath + itemName : myPath + getDelimiter() + itemName;
		}
	}
	
	public boolean isRoot() {
		return ZLFSUtil.getRootDirectoryPath().equals(myPath);
	}
	
	//TODO "" - windows "/"--unix
	public ZLDir getRootDirectory() {
		return new ZLFSDir(getRootDirectoryPath());
	}
	
	public String getRootDirectoryPath() {
		return File.listRoots().length == 1 ? File.listRoots()[0].getPath() : "";
	}
	
	public String getParentPath(String path) {
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


	abstract public ArrayList collectSubDirs();
	abstract public ArrayList collectFiles();
	abstract protected String getDelimiter();
}
