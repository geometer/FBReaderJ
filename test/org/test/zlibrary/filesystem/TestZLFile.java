package org.test.zlibrary.filesystem;


import junit.framework.TestCase;

import org.geometerplus.zlibrary.core.filesystem.ZLDir;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.ui.swing.library.ZLSwingLibrary;

public class TestZLFile extends TestCase {
	private final String filename = "testfb2book.fb2";
	private final String filenameZip = "testbookZip.zip";

	private String myDirectory = "test/data/fb2/filesystem";
	
	public void setUp() {
		new ZLSwingLibrary();
	}
	
	public void testCreateFile() {
		ZLFile file = new ZLFile(myDirectory+ "/" + filename);
		//System.out.println(arg0);
    	//file.getInputStream("test\\data\\fb2\\filesystem" + "\\" + filename);

		file.getInputStream(myDirectory + "/" + filename);
	}
	
	public void testCreateFile2() {
		ZLFile file = new ZLFile(myDirectory +"/"+ filename);
    	//file.getInputStream(myDirectory + "/"+filename);
    	System.out.println(file.getPhysicalFilePath());
    	assertEquals(file.getDirectory(), null);
    	assertEquals(file.getExtension(), "fb2");       
       	assertEquals(file.exists(), true);
       	assertEquals(file.getName(true)+".fb2", file.getName(false));
       	assertEquals(file.isArchive(), false);
       	assertEquals(file.isArchive(), false);
       	assertEquals(file.isDirectory(), false);
       	assertEquals(file.isCompressed(), false);

	}
	
	public void testCreateFile3() {
		ZLFile file = new ZLFile(myDirectory + filenameZip);
    	//file.getInputStream(myDirectory + filenameZip);
    	assertEquals(file.getDirectory(), null);
    	assertEquals(file.getExtension(), "zip");       
       	assertEquals(file.exists(), true);
       	assertEquals(file.isArchive(), true);
       	assertEquals(file.isDirectory(), false);
       	assertEquals(file.isCompressed(), false);
       	assertEquals(file.getName(true)+".zip", file.getName(false));
	}
	
	public void testCreateFile4() {
		ZLFile file = new ZLFile(myDirectory);
    	//file.getInputStream(myDirectory);
    	ZLDir dir = file.getDirectory();
    	//assertEquals(dir.getName(), myDirectory);
        //System.out.println(dir.getItemPath(".."));
    	//assertEquals(dir.getItemPath(".."), "test/data/fb2");
        //System.out.println(dir.getItemPath("marina"));
        //assertEquals(dir.getItemPath("marina"), "test/data/fb2/filesystem/marina");
        //System.out.println(dir.getParentPath());
        
        //assertEquals(dir.getParentPath(), "test/data/fb2");
    	assertEquals(dir.getPath(), "test/data/fb2/filesystem");
    	assertEquals(dir.isRoot(), false);
    	//assertEquals(dir.getParentPath(), "test/data/fb2");
        
    	
    	assertEquals(file.getExtension(), null);       
       	assertEquals(file.exists(), true);
       	assertEquals(file.isArchive(), false);
       	assertEquals(file.isDirectory(), true);
       	assertEquals(file.isCompressed(), false);
       	assertEquals(file.getName(true), file.getName(false));
	}
}
