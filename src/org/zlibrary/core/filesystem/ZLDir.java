package org.zlibrary.core.filesystem;

import java.io.*;
import java.util.*;
import org.zlibrary.core.util.*;

public abstract class ZLDir {
	protected String myPath;

	public static ZLDir getRoot() {		
		return ZLFSUtil.getRootDirectory();
	}

	public ZLDir(String path) {
		myPath = ZLFSUtil.normalize(path);
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
	
	abstract public String getItemPath(String itemName);
	
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
