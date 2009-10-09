package org.test.fbreader.collection;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.geometerplus.fbreader.collection.BookList;
import org.geometerplus.zlibrary.ui.swing.library.ZLSwingLibrary;

public class TestBookList extends TestCase {
	private final String filename = "testfb2book.fb2";
	private final String filenameZip = "testbookZip.zip";

	private String myDirectory = "test/data/fb2/filesystem";
	
	public void setUp() {
		//new ZLSwingLibrary().init();
	}

	
	
	public void test() {
		BookList bookList= new BookList();
		ArrayList list = bookList.fileNames();
		bookList.addFileName(myDirectory+"/"+filename);
		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i));
		}
		bookList.removeFileName(myDirectory+"/"+filename);
		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i));
		}
 	}

}
