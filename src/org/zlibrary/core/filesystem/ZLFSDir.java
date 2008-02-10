package org.zlibrary.core.filesystem;

import java.io.*;
import java.util.*;
import org.zlibrary.core.util.*;


import org.zlibrary.core.library.ZLibrary;

public class ZLFSDir extends ZLDir {
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
		for(int i = 0; i < dirs.length; i++) {
			if (dirs[i].isDirectory()) {
				newdirs.add(dirs[i].getName());
			}
		}
		return newdirs;
	};
	
	public ArrayList/*<String>*/ collectFiles() {		
		File[] dirs = myFile.listFiles();
		ArrayList/*<String>*/ newdirs  = new ArrayList();
		for(int i = 0; i < dirs.length; i++) {
				newdirs.add(dirs[i].getName());
		}
		return newdirs;
	};



}
