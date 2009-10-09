package org.test.zlibrary.filesystem;

import java.util.ArrayList;

import org.geometerplus.zlibrary.core.filesystem.ZLDir;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.ui.swing.library.ZLSwingLibrary;

import junit.framework.TestCase;

public class TestZLDir extends TestCase {
	private final String filename = "testfb2book.fb2";
	private final String filenameZip = "testbookZip.zip";
	private final String filenameDir = "directory";

	private String myDirectory = "test/data/fb2/filesystem";
	
	public void setUp() {
		new ZLSwingLibrary();
	}

	public void testCollectFiles() {
		ZLFile file = new ZLFile(myDirectory);
    	ZLDir dir = file.getDirectory();
    	ArrayList/*<String>*/ list = dir.collectFiles();
    	
    	for (int i = 0; i < list.size(); i++) {
    		
    		String str = (String)list.get(i);
    		if (str.equals(filename) || str.equals(filenameZip) || str.equals(filenameDir) ||str.equals(".svn")) {
    			assertEquals(true, true);
    		} else {
    			assertEquals(false, true);
    		}
        }       
	}
	
	public void testCollectSubDirs() {
		ZLFile file = new ZLFile(myDirectory);
    	
		ZLDir dir = file.getDirectory();
		ArrayList/*<String>*/ list = dir.collectSubDirs();
    	
    	for (int i = 0; i < list.size(); i++) {
    		  
    		if (list.get(i).equals(filenameDir) || list.get(i).equals(".svn")) {
    			assertEquals(true, true);
    		} else {
    			assertEquals(false, true);
    		}
        }       
	}	
}
