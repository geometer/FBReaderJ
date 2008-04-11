package org.test.zlibrary.filesystem;

import junit.framework.TestCase;

import org.geometerplus.zlibrary.core.filesystem.ZLFSDir;
//import org.geometerplus.zlibrary.core.filesystem.ZLFSManagerUtil;
import org.geometerplus.zlibrary.ui.swing.library.ZLSwingLibrary;

public class TestZLFSManager extends TestCase {
	
	private final String filename = "testfb2book.fb2";

	private String myDirectory = "test\\data\\fb2\\filesystem";
	
	public void setUp() {
		//new ZLSwingLibrary().init();
	}
/*
	public void testAddRemoveDir() {
		ZLFSDir dir = ZLFSManagerUtil.getInstance().createNewDirectory(myDirectory + "\\test");
		assertEquals(ZLFSManagerUtil.getInstance().removeFile(dir.getPath()), true);
	}

	public void testAddRemoveDirPlainDirectory() {
		ZLFSDir dir = ZLFSManagerUtil.getInstance().createPlainDirectory(myDirectory + "\\test");
		System.out.println(dir.getName());
		assertEquals(ZLFSManagerUtil.getInstance().removeFile(dir.getPath()), false);
	}
*/	
	//public void testAddRemoveDirPlainDirectory() {
		//ZLFSDir dir = ZLFSManager.getInstance().createPlainDirectory(myDirectory + "\\test");
		//System.out.println(dir.getName());
		//assertEquals(ZLFSManager.getInstance().removeFile(dir.getPath()), false);
	//}

}
