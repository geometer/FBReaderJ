package org.zlibrary.core.filesystem;

import java.io.*;
import java.util.*;
import org.zlibrary.core.util.*;

import org.zlibrary.core.library.ZLibrary;

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
