package org.zlibrary.core.xml.sax;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.zlibrary.core.xml.ZLXMLProcessor;
import org.zlibrary.core.xml.ZLXMLReader;

public class ZLSaxXMLProcessor extends ZLXMLProcessor{

	@Override
	public void read(ZLXMLReader reader, String fileName) {
		try {
			InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
			if (stream == null) {
				stream = new BufferedInputStream(new FileInputStream(fileName));
			}
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(stream, new SAXHandler(reader));
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}
	}

}
