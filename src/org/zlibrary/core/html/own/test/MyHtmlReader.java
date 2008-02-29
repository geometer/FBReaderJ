package org.zlibrary.core.html.own.test;

import org.zlibrary.core.html.ZLHtmlReaderAdapter;
import org.zlibrary.core.xml.ZLStringMap;


public class MyHtmlReader extends ZLHtmlReaderAdapter {
	public void startDocumentHandler() {
		System.out.print("START DOCUMENT");
	}
	
	public void endDocumentHandler() {
		System.out.println("END DOCUMENT");
	}

	public void startElementHandler(String tag, ZLStringMap attributes){
		System.out.print("<" + tag);
		String key;
		for (int i = 0; i < attributes.getSize(); i++) {
			key = attributes.getKey(i);
			System.out.print(" " + key + "=\"");
			System.out.print(attributes.getValue(key) + "\"");
		}
		System.out.print(">");
	}
	
	public void endElementHandler(String tag){
		System.out.print("</" + tag + ">");
	}
	
	public void characterDataHandler(char[] ch, int start, int length){
	}
	
	public void characterDataHandlerFinal(char[] ch, int start, int length){
		for (int i = 0; i < length; i++) {
			System.out.print(ch[i + start]);
		}
	}
};
