package org.zlibrary.core.xml.own;

import java.io.InputStream;
import java.io.IOException;

import org.zlibrary.core.xml.ZLXMLProcessor;
import org.zlibrary.core.xml.ZLXMLReader;

public class ZLOwnXMLProcessor implements ZLXMLProcessor {
	public boolean read(ZLXMLReader reader, InputStream stream) {
		try {
			ZLOwnXMLParser parser = new ZLOwnXMLParser(reader, stream);
			reader.startDocumentHandler();
			parser.doIt();
			reader.endDocumentHandler();
			System.gc();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
