package org.zlibrary.core.html.own.test;


import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.html.ZLHtmlReaderAdapter;
import org.zlibrary.core.html.own.ZLOwnHtmlProcessorFactory;
import org.zlibrary.ui.swing.library.ZLSwingLibrary;

public class Main {
	
	private static ZLHtmlReaderAdapter myReader = new MyHtmlReader();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ZLOwnHtmlProcessorFactory();
		//myReader.read("src/org/zlibrary/core/html/own/test/test.html");
		myReader.read("src/org/zlibrary/core/html/own/test/subversion.htm");
	}
}
