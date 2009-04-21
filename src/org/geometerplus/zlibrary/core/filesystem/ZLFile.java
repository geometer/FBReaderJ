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
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.library.ZLibrary;

public abstract class ZLFile {
	public interface ArchiveType {
		int	NONE = 0;
		int	GZIP = 0x0001;
		int	BZIP2 = 0x0002;
		int	COMPRESSED = 0x00ff;
		int	ZIP = 0x0100;
		int	TAR = 0x0200;
		int	ARCHIVE = 0xff00;
	};
	
	private String myNameWithoutExtension;
	private String myExtension;
	protected int myArchiveType;

	protected void init() {
		final String name = getNameWithExtension();
		final int index = name.lastIndexOf('.');
		myNameWithoutExtension = (index != -1) ? name.substring(0, index) : name;
		myExtension = (index != -1) ? name.substring(index + 1).toLowerCase().intern() : "";

		/*
		if (lowerCaseName.endsWith(".gz")) {
			myNameWithoutExtension = myNameWithoutExtension.substring(0, myNameWithoutExtension.length() - 3);
			lowerCaseName = lowerCaseName.substring(0, lowerCaseName.length() - 3);
			myArchiveType = myArchiveType | ArchiveType.GZIP;
		}
		if (lowerCaseName.endsWith(".bz2")) {
			myNameWithoutExtension = myNameWithoutExtension.substring(0, myNameWithoutExtension.length() - 4);
			lowerCaseName = lowerCaseName.substring(0, lowerCaseName.length() - 4);
			myArchiveType = myArchiveType | ArchiveType.BZIP2;
		}
		*/
		int archiveType = ArchiveType.NONE;
		if (myExtension == "zip") {
			archiveType |= ArchiveType.ZIP;
		} else if (myExtension == "oebzip") {
			archiveType |= ArchiveType.ZIP;
		} else if (myExtension == "epub") {
			archiveType |= ArchiveType.ZIP;
		} else if (myExtension == "tar") {
			archiveType |= ArchiveType.TAR;
		//} else if (lowerCaseName.endsWith(".tgz")) {
			//nothing to-do myNameWithoutExtension = myNameWithoutExtension.substr(0, myNameWithoutExtension.length() - 3) + "tar";
			//myArchiveType = myArchiveType | ArchiveType.TAR | ArchiveType.GZIP;
		}
		myArchiveType = archiveType;
	}
	
	public static ZLFile createFile(String path) {
		if (path == null) {
			return null;
		}
		if (!path.startsWith("/")) {
			return ZLResourceFile.createResourceFile(path);
		}
		int index = path.lastIndexOf(':');
		if (index > 1) {
			return new ZLArchiveEntryFile(createFile(path.substring(0, index)), path.substring(index + 1));
		}
		return new ZLPhysicalFile(path);
	}

	public abstract boolean exists();
	public abstract boolean isDirectory();
	public abstract String getPath();
	public abstract ZLFile getParent();
	public abstract ZLPhysicalFile getPhysicalFile();
	public abstract InputStream getInputStream() throws IOException;

	public final boolean isCompressed() {
		return (0 != (myArchiveType & ArchiveType.COMPRESSED)); 
	}
	
	public final boolean isArchive() {
		return (0 != (myArchiveType & ArchiveType.ARCHIVE));
	}

	protected abstract String getNameWithExtension();
	public final String getName(boolean hideExtension) {
		return hideExtension ? myNameWithoutExtension : getNameWithExtension();
	}
	
	public final String getExtension() {
		return myExtension;
	}

    /*
	public InputStream getInputStream() throws IOException {
		InputStream stream = null;
		int index = ZLFSUtil.findArchiveFileNameDelimiter(myPath);
		if (index == -1) {
			stream = ZLibrary.Instance().getInputStream(myPath);
		} else {
			ZLFile baseFile = new ZLFile(myPath.substring(0, index));
			InputStream base = baseFile.getInputStream();
			if (base != null) {
				if (0 != (baseFile.myArchiveType & ArchiveType.ZIP)) {
					ZipFile zf = getZipFile(myPath.substring(0, index));
					/*
					ZipEntry entry = zf.getEntry(myPath.substring(index+1));
					stream = zf.getInputStream(entry);
					* /
					final String entryName = myPath.substring(index + 1);
					stream = zf.getInputStream(entryName);
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
					* /
				} else if (0 != (baseFile.myArchiveType & ArchiveType.TAR)) {
					stream = new ZLTarInputStream(base, myPath.substring(index + 1));
				}
			}
		}

		if (stream != null) {
			if (0 != (myArchiveType & ArchiveType.GZIP)) {
				return new java.util.zip.GZIPInputStream(stream, 8192);
			}
			//if (0 != (myArchiveType & ArchiveType.BZIP2)) {
				//return new ZLBzip2InputStream(stream);
			//}
		}
		return stream;
	}
	*/
	
	public final ZLDir getDirectory(boolean createUnexisting) {
		if (exists()) {
			if (isDirectory()) {
				return new ZLFSDir(getPath());
			} else if (0 != (myArchiveType & ArchiveType.ZIP)) {
				return new ZLZipDir(getPath());
			} else if (0 != (myArchiveType & ArchiveType.TAR)) {
				return new ZLTarDir(getPath());
			}
		} else if (createUnexisting) {
			return createUnexistingDirectory();
		}
		return null;
	}

	protected abstract ZLDir createUnexistingDirectory();
}
