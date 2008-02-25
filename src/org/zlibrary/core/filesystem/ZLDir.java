package org.zlibrary.core.filesystem;

import java.io.*;
import java.util.*;
import org.zlibrary.core.util.*;

public abstract class ZLDir {
	private String myPath;

	public static ZLDir getRoot() {		
		return ZLFSManager.getInstance().getRootDirectory();
	}

	public ZLDir(String path) {
		myPath = ZLFSManager.getInstance().normalize(path);
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
			return myPath.endsWith(File.separator) || myPath == "" ? myPath + itemName : myPath + File.separator + itemName;
		}
	}
	
	public boolean isRoot() {
		return ZLFSManager.getInstance().getRootDirectoryPath().equals(myPath);
	}

	abstract public ArrayList collectSubDirs();
	abstract public ArrayList collectFiles();
	abstract protected String getDelimiter();
}
