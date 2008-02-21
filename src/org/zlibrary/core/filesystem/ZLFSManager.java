package org.zlibrary.core.filesystem;

import java.io.*;
import java.util.*;
import org.zlibrary.core.util.*;


import org.zlibrary.core.library.ZLibrary;

public class ZLFSManager {
	private final HashMap myForcedFiles = new HashMap();
	private static ZLFSManager ourInstance;
	
	public static void deleteInstance() {
		if (ourInstance != null) {
			ourInstance = null;
		}
	}
	
	public HashMap getForcedFiles() {
		return myForcedFiles;
	}
	
	public void putForcedFile(String key, int value) {
		myForcedFiles.put(key, value);
	}
	
	public static ZLFSManager getInstance() {
		if (ourInstance == null) {
		    ourInstance = new ZLFSManager();  
		}
		return ourInstance;
	}

		
	private ZLFSManager() {	}
	
		
	public void normalize(String path) {}

	protected InputStream createPlainInputStream(String path) {
		return ZLibrary.getInstance().getInputStream(path);
	}
	
	//public OutputStream createOutputStream(String path);
	
	public ZLFSDir createPlainDirectory(String path) {
		return new ZLFSDir(path);
	}
	
	public ZLFSDir createNewDirectory(String path) {
		File file = new File(path);
		file.mkdirs();
		return new ZLFSDir(path);
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
	
	public boolean removeFile(String path) {
		File file = new File(path);
		return file.delete();
	}
	
	//TODO
	public String convertFilenameToUtf8(String name) {
		return name;		
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
			index = path.lastIndexOf(File.separator);
		}
		return index;
	}
	//TODO "" - windows "/"--unix
	public ZLDir getRootDirectory() {
		return createPlainDirectory(getRootDirectoryPath());
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
}
