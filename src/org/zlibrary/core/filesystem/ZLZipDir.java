package org.zlibrary.core.filesystem;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.zlibrary.core.util.*;

public class ZLZipDir extends ZLDir {
	private File myFile;
	
	ZLZipDir(String path) {		
		super(path);
		System.out.println();
		System.out.println(path);

		myFile = new File(path);
	}

	public String getDelimiter() {
		return File.separator;
	};
	
	public ArrayList collectSubDirs() {
	    return null;
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
			return new ArrayList();
		}
		Enumeration/*ZipEntry*/ en = zf.entries();
        while(en.hasMoreElements()) {
        	ZipEntry entry = (ZipEntry)en.nextElement();
        	newdirs.add(entry.getName());
        }
		return newdirs;
	};
	
	public String getItemPath(String itemName) {
		if (itemName == "..") {
			return getParentPath();
		} else {
			return myPath.endsWith(File.separator) || myPath == "" ? myPath + itemName : myPath + ":" + itemName;
		}
	}

}
