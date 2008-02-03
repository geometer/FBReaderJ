package org.zlibrary.core.filesystem;

import java.io.File;
import java.io.InputStream;
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
		File a = new File(path);
		a.mkdirs();	
		return new ZLFSDir(path);
	}
	
	protected ZLFileInfo getFileInfo(String path) {
		ZLFileInfo info = new ZLFileInfo();
		File file = new File(path);
		info.Exists = (file != null);
		info.Size = file.length();
		info.MTime = file.lastModified();
		info.IsDirectory = file.isDirectory();
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
		return path.lastIndexOf(':');
	}
	
	public int findLastFileNameDelimiter(String path) {
		int index = findArchiveFileNameDelimiter(path);
		if (index == -1) {
			index = path.lastIndexOf(ZLibrary.FileNameDelimiter);
		}
		return index;
	}
	//TODO "" - windows "/"--unix
	public ZLDir getRootDirectory() {
		return createPlainDirectory("");
        		
	}
	
	public String getRootDirectoryPath() {
		return "";
	}
	
	public String getParentPath(String path) {
		File file = new File(path);
		return file.getParent();
	}
}
