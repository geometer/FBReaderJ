package org.geometerplus.zlibrary.core.xml.own;

import java.io.InputStream;
import java.io.IOException;

import org.geometerplus.zlibrary.core.xml.ZLXMLProcessor;
import org.geometerplus.zlibrary.core.xml.ZLXMLReader;

public class ZLOwnXMLProcessor extends ZLXMLProcessor {
	public boolean read(ZLXMLReader reader, InputStream stream) {
		try {
			ZLOwnXMLParser parser = new ZLOwnXMLParser(reader, stream);
			reader.startDocumentHandler();
			parser.doIt();
			reader.endDocumentHandler();
		} catch (IOException e) {
			//System.out.println(e);
			return false;
		}
		return true;
	}
}
