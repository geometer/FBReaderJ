package org.geometerplus.zlibrary.core.filesystem;

import java.io.*;
import java.util.*;
import org.geometerplus.zlibrary.core.util.*;


import org.geometerplus.zlibrary.core.library.ZLibrary;

class ZLFSDir extends ZLDir {
	private File myFile;
	
	ZLFSDir(String path) {
		super(path);
		myFile = new File(path);
	}

	public String getDelimiter() {
		return File.separator;
	};
	
	public ArrayList collectSubDirs() {
		File[] dirs = myFile.listFiles();
		
		ArrayList/*<String>*/ newdirs  = new ArrayList();
		
		if ((dirs == null) && isRoot()) {
			dirs = File.listRoots();
			for(int i = 0; i < dirs.length; i++) {			
				newdirs.add(dirs[i].getPath());
			}
		} else if (dirs != null) {
			for(int i = 0; i < dirs.length; i++) {
				if (dirs[i].isDirectory()) {
					newdirs.add(dirs[i].getName());
				}
			}
		}
		return newdirs;
	};
	
	public ArrayList/*<String>*/ collectFiles() {		
		File[] dirs = myFile.listFiles();
		ArrayList/*<String>*/ newdirs  = new ArrayList();
		
		if (dirs == null && isRoot()) {
			dirs = File.listRoots();
			for (int i = 0; i < dirs.length; i++) {			
					newdirs.add(dirs[i].getPath());
			}
		} else if (dirs != null) {
			for (int i = 0; i < dirs.length; i++) {
				if (!dirs[i].isDirectory()) {
					newdirs.add(dirs[i].getName());
				}
			}
		}
		return newdirs;
	};

	public String getItemPath(String itemName) {
		if (itemName == "..") {
			return getParentPath();
		} else {
			return myPath.endsWith(File.separator) || myPath == "" ? myPath + itemName : myPath + File.separator + itemName;
		}
	}
}
