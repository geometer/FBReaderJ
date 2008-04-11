package org.geometerplus.zlibrary.core.xml.sax;

import java.io.InputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.geometerplus.zlibrary.core.xml.ZLXMLProcessor;
import org.geometerplus.zlibrary.core.xml.ZLXMLReader;

public class ZLSaxXMLProcessor extends ZLXMLProcessor {
	public boolean read(ZLXMLReader reader, InputStream stream) {
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(stream, new SAXHandler(reader));
		} catch (IOException e) {
			return false;
		} catch (ParserConfigurationException e) {
			return false;
		} catch (SAXException e) {
			return false;
		}
		return true;
	}
}
