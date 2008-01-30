package org.zlibrary.core.filesystem;

import java.util.ArrayList;

public abstract class ZLDir {
	private String myPath;

	public static ZLDir getRoot() {
		return ZLFSManager.getInstance().getRootDirectory();
	}

	public ZLDir(String path) {
		myPath = path;
		ZLFSManager.getInstance().normalize(myPath);
	}
	
	public String getPath() {
		return myPath;
	}
	
	public String getName() {
		int index = ZLFSManager.getInstance().findLastFileNameDelimiter(myPath);
		return myPath.substring(index + 1);
	}
	
	public String getParentPath() {
		return ZLFSManager.getInstance().getParentPath(myPath);
	}
	
	public String getItemPath(String itemName) {
		if (itemName == "..") {
			return getParentPath();
		} else {
			return isRoot() ? myPath + itemName : myPath + getDelimiter() + itemName;
		}

	}
	
	public boolean isRoot() {
		return myPath == ZLFSManager.getInstance().getRootDirectoryPath();
	}

	abstract public ArrayList collectSubDirs();
	abstract public ArrayList collectFiles();

	abstract protected String getDelimiter();
}
