package org.zlibrary.core.filesystem;

import java.io.*;
import java.util.*;
import org.zlibrary.core.util.*;


import org.zlibrary.core.library.ZLibrary;

public class ZLFSManagerUtil {
	
	private static ZLFSManagerUtil ourInstance;
	
	public static void deleteInstance() {
		if (ourInstance != null) {
			ourInstance = null;
		}
	}
	
	
	public static ZLFSManagerUtil getInstance() {
		if (ourInstance == null) {
		    ourInstance = new ZLFSManagerUtil();  
		}
		return ourInstance;
	}

		
	private ZLFSManagerUtil() {	}
	
		
	public String normalize(String path) {
		try {
			path = new File(path).getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return path;
	}

	protected InputStream createPlainInputStream(String path) {
		return ZLibrary.getInstance().getInputStream(path);
	}
	
	//public OutputStream createOutputStream(String path);
	
	/*public ZLFSDir createPlainDirectory(String path) {
		return new ZLFSDir(path);
	}*/
	
	
	
	//TODO "" - windows "/"--unix
	static public ZLDir getRootDirectory() {
		return new ZLFSDir(getRootDirectoryPath());
	}
	
	static public String getRootDirectoryPath() {
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
	
	protected ZLFileInfo getFileInfo(String path) {
		ZLFileInfo info = new ZLFileInfo();
		File file = new File(path);
		info.Exists = (file != null);
		info.Size = file.length();
		info.MTime = file.lastModified();
		info.IsDirectory = file.isDirectory() || getRootDirectoryPath().equals(path);
		return info;
	}

	

	public int findArchiveFileNameDelimiter(String path) {
		if (System.getProperty("os.name").startsWith("Windows")) {
			int index = path.lastIndexOf(':');
			return (index == 1) ? -1 : index;
		}
		return path.lastIndexOf(':');
	}
	
	public int findLastFileNameDelimiter(String path) {
		int index = findArchiveFileNameDelimiter(path);
		if (index == -1) {
			index = path.lastIndexOf(File.separatorChar);
		}
		return index;
	}
}
