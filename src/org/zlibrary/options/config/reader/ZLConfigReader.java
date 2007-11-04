package org.zlibrary.options.config.reader;


import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.zlibrary.options.config.*;

/*package*/ class ZLConfigReader implements ZLReadable{
	
	
	private XMLReader myXMLReader;
	private ZLConfig myConfig = new ZLConfig();
	
	
	private class ConfigContentHandler extends DefaultHandler{
		private int myDepth;
		private ZLGroup myCurrentGroup;
		private Map<String, ZLGroup> myCurrentConfig;
		
		public void startDocument() {
			myDepth = 0;
			myCurrentGroup = new ZLGroup();
			myCurrentConfig = new HashMap<String, ZLGroup>();
		}
		
		public void startElement(String uri, String localName, String qName, Attributes atts) {
			switch (myDepth) {
				case 0:
					if (localName != "config") {
						System.out.println("wrong tag : <config> expected!");
					}
				break;
				case 1:
					if (localName == "group") {
						myCurrentGroup = new ZLGroup();
						myCurrentConfig.put(atts.getValue("name"), myCurrentGroup);
					} else {
						System.out.println("wrong tag : <group> expected!");
					}
				break;
				case 2:
					if (localName == "option") { 
						myCurrentGroup.setValue(atts.getValue("name"), atts.getValue("value"));
					} else{
						System.out.println("wrong tag : <option> expected!");
					}
				break;
				default: // big depth
					System.out.println("Too many nesting elements!");
			}
			//System.out.println("New element started!");
			myDepth++;
		}
		public void endElement(String uri, String localName, String qName) {
			myDepth--;
			/*if (myDepth == 1)
				if (localName == "book") {
					System.out.println(": " + currtitle);
				}
			}*/
			// System.out.println("Element finished!");
		}
		
		public void endDocument() {
			myConfig = new ZLConfig(myCurrentConfig);
		}
	}
	
	public ZLConfigReader () {
		try {
			myXMLReader = XMLReaderFactory.createXMLReader();
			myXMLReader.setContentHandler(new ConfigContentHandler());
		} catch (SAXException e) {
			System.err.println(e.getMessage());
		}
	}
	
	/** Прочитать данные из потока в XML */
	public ZLConfig read (InputStream input) {
		try {
			myXMLReader.parse(new InputSource(input));
			return myConfig;
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
			return null;
		} catch (SAXException sae) {
			System.err.println(sae.getMessage());
			return null;
		}
	}
}
