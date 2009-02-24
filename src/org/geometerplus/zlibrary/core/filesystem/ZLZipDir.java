/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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
import org.geometerplus.zlibrary.core.util.*;

public class ZLZipDir extends ZLDir {
	private File myFile;
	
	ZLZipDir(String path) {		
		super(path);
		myFile = new File(path);
	}

	public String getDelimiter() {
		return ":";
	};
	
	private static ArrayList EMPTY = new ArrayList();
	public ArrayList collectSubDirs() {
		return EMPTY;
	};
	
	public ArrayList/*<String>*/ collectFiles() {		
		ZipFile zf = null;
		try {
			zf = new ZipFile(myFile.getCanonicalPath());
		} catch (IOException e) {
		}
		if (zf == null) {		
			return EMPTY;
		}

		ArrayList fileNames  = new ArrayList();
		for (LocalFileHeader header : zf.headers()) {
			fileNames.add(header.FileName);
		}
		return fileNames;
		
//		try {
//			zf = new ZipFile(myFile);
//		} catch (ZipException e) {
//		} catch (IOException e) {
//		}
//		if ((zf == null) || (zf.entries() == null)) {		
//			return EMPTY;
//		}
//		Enumeration/*ZipEntry*/ en = zf.entries();
//		while (en.hasMoreElements()) {
//			ZipEntry entry = (ZipEntry)en.nextElement();
//			fileNames.add(entry.getName());
//		}
//		return fileNames;
	}
}
