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

import java.util.HashMap;
import java.io.*;
import org.amse.ys.zip.*;

import org.geometerplus.zlibrary.core.library.ZLibrary;

public final class ZLArchiveEntryFile extends ZLFile {
	private static HashMap<String,ZipFile> ourZipFileMap = new HashMap<String,ZipFile>();

	static ZipFile getZipFile(String fileName) throws IOException {
		synchronized (ourZipFileMap) {
			ZipFile zf = ourZipFileMap.get(fileName);
			if (zf == null) {
				zf = new ZipFile(fileName);
				ourZipFileMap.put(fileName, zf);
			}
			return zf;
		}
	}

	private final ZLFile myParent;
	private final String myName;
	private final String myShortName;
	
	public ZLArchiveEntryFile(ZLFile parent, String name) {
		myParent = parent;
		myName = name;
		myShortName = name.substring(name.lastIndexOf('/') + 1);
		init();
	}
	
	public boolean exists() {
		return myParent.exists();
	}
	
	public boolean isDirectory() {
		return false;
	}
	
	public String getPath() {
		return myParent.getPath() + ":" + myName;
	}
	
	public String getNameWithExtension() {
		return myShortName;
	}

	public ZLFile getParent() {
		return myParent;
	}

	public ZLPhysicalFile getPhysicalFile() {
		ZLFile ancestor = myParent;
		while ((ancestor != null) && !(ancestor instanceof ZLPhysicalFile)) {
			ancestor = ancestor.getParent();
		}
		return (ZLPhysicalFile)ancestor;
	}
    
	public InputStream getInputStream() throws IOException {
		if (0 != (myParent.myArchiveType & ArchiveType.ZIP)) {
			ZipFile zf = getZipFile(myParent.getPath());
			/*
			ZipEntry entry = zf.getEntry(myPath.substring(index+1));
			stream = zf.getInputStream(entry);
			*/
			return zf.getInputStream(myName);
			/*
			while (true) {
				ZipEntry entry = zipStream.getNextEntry();
				if (entry == null) {
					break;
				} else if (entryName.equals(entry.getName())) {
					stream = zipStream;
					break;
				}
			}
			*/
		} else if (0 != (myParent.myArchiveType & ArchiveType.TAR)) {
			InputStream base = myParent.getInputStream();
			if (base != null) {
				return new ZLTarInputStream(base, myName);
			}
		}
		return null;
	}

	protected ZLDir createUnexistingDirectory() {
		return null;
	}
}
