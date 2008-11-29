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
import java.util.zip.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.library.ZLibrary;

public class ZLFile {
	public interface ArchiveType {
		int	NONE = 0;
		int	GZIP = 0x0001;
		int	BZIP2 = 0x0002;
		int	COMPRESSED = 0x00ff;
		int	ZIP = 0x0100;
		int	TAR = 0x0200;
		int	ARCHIVE = 0xff00;
	};
	
	private final String myPath;
	private final String myNameWithExtension;
	private String myNameWithoutExtension;
	private final String myExtension;
	private	int myArchiveType;
	private	ZLFileInfo myInfo;
	
	private static final HashMap ourForcedFiles = new HashMap();
	
	public boolean removeFile(String path) {
		File file = new File(path);
		return file.delete();
	}
	
	public ZLFSDir createNewDirectory(String path) {
		File file = new File(path);
		file.mkdirs();
		return new ZLFSDir(path);
	}

	private static ZLFileInfo getFileInfo(String path) {
		ZLFileInfo info = new ZLFileInfo();
		File file = new File(path);
		info.Exists = file.exists() || ZLFSUtil.getRootDirectoryPath().equals(path);;
		info.Size = file.length();
		info.MTime = file.lastModified();
		info.IsDirectory = file.isDirectory() || ZLFSUtil.getRootDirectoryPath().equals(path);
		return info;
	}

	private void fillInfo() {
		int index = ZLFSUtil.findArchiveFileNameDelimiter(myPath);
		if (index == -1) {
			myInfo = getFileInfo(myPath);
		} else {
			myInfo = getFileInfo(myPath.substring(0, index));
			myInfo.IsDirectory = false;
		}
	}
	
	public ZLFile(String path) {
		if (path.startsWith(ZLibrary.JAR_DATA_PREFIX)) {
			myInfo = new ZLFileInfo();
			myInfo.Exists = true;
			myPath = path;
			myNameWithExtension = path;
		} else {
			myPath = ZLFSUtil.normalize(path);		
			int index = ZLFSUtil.findLastFileNameDelimiter(myPath);
			if (index < myPath.length() - 1) {
				myNameWithExtension = myPath.substring(index + 1);
			} else {
				myNameWithExtension = myPath;
			}
		}
		myNameWithoutExtension = myNameWithExtension;

		Integer value = (Integer)ourForcedFiles.get(myPath);
		if (value != null) {
			myArchiveType = value.intValue();
		} else {
			myArchiveType = ArchiveType.NONE;
			String lowerCaseName = myNameWithoutExtension.toLowerCase();
			if (lowerCaseName.endsWith(".gz")) {
				myNameWithoutExtension = myNameWithoutExtension.substring(0, myNameWithoutExtension.length() - 3);
				lowerCaseName = lowerCaseName.substring(0, lowerCaseName.length() - 3);
				myArchiveType = myArchiveType | ArchiveType.GZIP;
			}
			/*
			if (lowerCaseName.endsWith(".bz2")) {
				myNameWithoutExtension = myNameWithoutExtension.substring(0, myNameWithoutExtension.length() - 4);
				lowerCaseName = lowerCaseName.substring(0, lowerCaseName.length() - 4);
				myArchiveType = myArchiveType | ArchiveType.BZIP2;
			}
			*/
			if (lowerCaseName.endsWith(".zip")) {
				myArchiveType = myArchiveType | ArchiveType.ZIP;
			} else if (lowerCaseName.endsWith(".tar")) {
				myArchiveType = myArchiveType | ArchiveType.TAR;
			} else if (lowerCaseName.endsWith(".tgz") || lowerCaseName.endsWith(".ipk")) {
				//nothing to-do myNameWithoutExtension = myNameWithoutExtension.substr(0, myNameWithoutExtension.length() - 3) + "tar";
				myArchiveType = myArchiveType | ArchiveType.TAR | ArchiveType.GZIP;
			}
		}

		int index = myNameWithoutExtension.lastIndexOf('.');
		if (index > 0) {
			myExtension = myNameWithoutExtension.substring(index + 1);
			myNameWithoutExtension = myNameWithoutExtension.substring(0, index);
		} else {
			myExtension = "";
		}
	}
	
	public boolean exists() {
		if (myInfo == null) 
			fillInfo(); 
		return myInfo.Exists;
	}
	
	public long getMTime() {
		if (myInfo == null) 
			fillInfo(); 
		return myInfo.MTime; 
	}
	
	public long size() {
		if (myInfo == null) 
			fillInfo(); 
		return myInfo.Size;
	}	
	
	public void forceArchiveType(int type) {
		if (myArchiveType != type) {
			myArchiveType = type;
			ourForcedFiles.put(myPath, myArchiveType);
		}
	}
	
	public boolean isCompressed() {
		return (0 != (myArchiveType & ArchiveType.COMPRESSED)); 
	}
	
	public boolean isDirectory() {
		if (myInfo == null)
			fillInfo(); 
		return myInfo.IsDirectory;
	}
	
	public boolean isArchive() {
		return (0 != (myArchiveType & ArchiveType.ARCHIVE));
	}

	public boolean remove() {
		if (removeFile(myPath)) {
			myInfo = null;
			return true;
		} else {
			return false;
		}
	}

	public String getPath() {
		return myPath;
	}
	
	public String getName(boolean hideExtension) {
		return hideExtension ? myNameWithoutExtension : myNameWithExtension;
	}
	
	public String getExtension() {
		return myExtension;
	}

	public String getPhysicalFilePath() {
		String path = myPath;
		if (path.startsWith(ZLibrary.JAR_DATA_PREFIX)) {
			return path;
		}
		int index;
		while ((index = ZLFSUtil.findArchiveFileNameDelimiter(path)) != -1) {
			path = path.substring(0, index);
		}
		return path;
	}
    
	public InputStream getInputStream() throws IOException {
		if (isDirectory()) {
			return null;
		}

		InputStream stream = null;
		int index = ZLFSUtil.findArchiveFileNameDelimiter(myPath);
		if (index == -1) {
			stream = ZLibrary.Instance().getInputStream(myPath);
		} else {
			ZLFile baseFile = new ZLFile(myPath.substring(0, index));
			InputStream base = baseFile.getInputStream();
			if (base != null) {
				if (0 != (baseFile.myArchiveType & ArchiveType.ZIP)) {
					/*
					ZipFile zf = new ZipFile(myPath.substring(0, index));
					ZipEntry entry = zf.getEntry (myPath.substring(index+1));
					stream = zf.getInputStream (entry);
					*/
					final ZipInputStream zipStream = new ZipInputStream(base);
					final String entryName = myPath.substring(index + 1);
					while (true) {
						ZipEntry entry = zipStream.getNextEntry();
						if (entry == null) {
							break;
						} else if (entryName.equals(entry.getName())) {
							stream = zipStream;
							break;
						}
					}
				} else if (0 != (baseFile.myArchiveType & ArchiveType.TAR)) {
					stream = new ZLTarInputStream(base, myPath.substring(index + 1));
				}
			}
		}

		if (stream != null) {
			if (0 != (myArchiveType & ArchiveType.GZIP)) {
				return new GZIPInputStream(stream, 8192);
			}
			//if (0 != (myArchiveType & ArchiveType.BZIP2)) {
				//return new ZLBzip2InputStream(stream);
			//}
		}
		return stream;
	}
	
	public ZLDir getDirectory() {
		return getDirectory(false);
	}
	
	public ZLDir getDirectory(boolean createUnexisting) {
		if (exists()) {
			if (isDirectory()) {
				return new ZLFSDir(myPath);
			} else if (0 != (myArchiveType & ArchiveType.ZIP)) {
				return new ZLZipDir(myPath);
			} else if (0 != (myArchiveType & ArchiveType.TAR)) {
				return new ZLTarDir(myPath);
			}
		} else if (createUnexisting) {
			myInfo = null;
			return createNewDirectory(myPath);
		}
		return null;
	}
}
