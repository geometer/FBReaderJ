package org.test.zlibrary.filesystem;


import junit.framework.TestCase;

import org.zlibrary.core.filesystem.ZLFSManager;
import org.zlibrary.core.filesystem.ZLFile;
import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.ui.swing.library.ZLSwingLibrary;

public class TestFileManager extends TestCase {
	public void setUp() {
		new ZLSwingLibrary().init();
	}
	
	public void testCreateFile() {
		ZLFile file = new ZLFile("test.fb2");
    	file.getInputStream("test.fb2");
	}
	
	public void testCreateFile2() {
		ZLFile file = new ZLFile("test.fb2");
    	file.getInputStream("test.fb2");
    	System.out.println(file.getDirectory());
    	assertEquals(file.getExtension(), "fb2");
       
       	System.out.println(file.getPath());
       	System.out.println(file.getPhysicalFilePath());
       	assertEquals(file.exists(), true);
       	System.out.println(file.getName(true));
       	System.out.println(file.getName(false));
       	

	}

}
