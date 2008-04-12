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

class ZLFSDir extends ZLDir {
	private File myFile;
	
	ZLFSDir(String path) {
		super(path);
		myFile = new File(path);
	}

	public String getDelimiter() {
		return File.separator;
	};
	
	public ArrayList collectSubDirs() {
		File[] dirs = myFile.listFiles();
		
		ArrayList/*<String>*/ newdirs  = new ArrayList();
		
		if ((dirs == null) && isRoot()) {
			dirs = File.listRoots();
			for(int i = 0; i < dirs.length; i++) {			
				newdirs.add(dirs[i].getPath());
			}
		} else if (dirs != null) {
			for(int i = 0; i < dirs.length; i++) {
				if (dirs[i].isDirectory()) {
					newdirs.add(dirs[i].getName());
				}
			}
		}
		return newdirs;
	};
	
	public ArrayList/*<String>*/ collectFiles() {		
		File[] dirs = myFile.listFiles();
		ArrayList/*<String>*/ newdirs  = new ArrayList();
		
		if (dirs == null && isRoot()) {
			dirs = File.listRoots();
			for (int i = 0; i < dirs.length; i++) {			
					newdirs.add(dirs[i].getPath());
			}
		} else if (dirs != null) {
			for (int i = 0; i < dirs.length; i++) {
				if (!dirs[i].isDirectory()) {
					newdirs.add(dirs[i].getName());
				}
			}
		}
		return newdirs;
	};

	public String getItemPath(String itemName) {
		if (itemName == "..") {
			return getParentPath();
		} else {
			return myPath.endsWith(File.separator) || myPath == "" ? myPath + itemName : myPath + File.separator + itemName;
		}
	}
}
