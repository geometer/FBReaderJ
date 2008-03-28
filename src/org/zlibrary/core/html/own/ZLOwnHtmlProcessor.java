package org.zlibrary.core.html.own;

import java.io.InputStream;
import java.io.IOException;

import org.zlibrary.core.html.ZLHtmlProcessor;
import org.zlibrary.core.html.ZLHtmlReader;

public class ZLOwnHtmlProcessor extends ZLHtmlProcessor {
	public boolean read(ZLHtmlReader reader, InputStream stream) {
		try {
			ZLOwnHtmlParser parser = new ZLOwnHtmlParser(reader, stream);
			reader.startDocumentHandler();
			parser.doIt();
			reader.endDocumentHandler();
		} catch (Exception e) {
//			System.out.println(e);
			//e.printStackTrace();
			return false;
		}
		return true;
	}
}
