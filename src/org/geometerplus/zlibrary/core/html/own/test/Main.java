package org.geometerplus.zlibrary.core.html.own.test;


import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.html.ZLHtmlReaderAdapter;
import org.geometerplus.zlibrary.core.html.own.ZLOwnHtmlProcessorFactory;

public class Main {
	
	private static ZLHtmlReaderAdapter myReader = new MyHtmlReader();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ZLOwnHtmlProcessorFactory();
		//myReader.read("src/org.geometerplus.zlibrary/core/html/own/test/test.html");
		myReader.read("src/org.geometerplus.zlibrary/core/html/own/test/subversion.htm");
	}
}
