package org.zlibrary.core.filesystem;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.zlibrary.core.util.*;

public class ZLZipDir extends ZLDir {
	private File myFile;
	
	ZLZipDir(String path) {		
		super(path);
		myFile = new File(path);
	}

	public String getDelimiter() {
		return ":";
	};
	
	private static ArrayList EMPTY = new ArrayList();
	public ArrayList collectSubDirs() {
		return EMPTY;
	};
	
	public ArrayList/*<String>*/ collectFiles() {		
		File[] dirs = myFile.listFiles();
		ArrayList/*<String>*/ newdirs  = new ArrayList();
		
		ZipFile zf = null;
		try {
			zf = new ZipFile(myFile);
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		if ((zf == null) || (zf.entries() == null)) {		
			return EMPTY;
		}
		Enumeration/*ZipEntry*/ en = zf.entries();
        while(en.hasMoreElements()) {
        	ZipEntry entry = (ZipEntry)en.nextElement();
        	newdirs.add(entry.getName());
        }
		return newdirs;
	};
}
