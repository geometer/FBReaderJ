package org.zlibrary.options.config.reader;


import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.zlibrary.options.config.*;

/*package*/ class ZLConfigReader implements ZLReader{
	
	
	private XMLReader myXMLReader;
	private ZLConfig myConfig;
	private String myCategory = "";
	
	private class ConfigContentHandler extends DefaultHandler{
		private int myDepth = 0;
		private String myCurrentGroup = "";
		
		public void startDocument() {
		}
		
		public void startElement(String uri, String localName, String qName, Attributes atts) {
			switch (myDepth) {
				case 0:
					if (!localName.equals("config")) {
						System.out.println("wrong tag : <config> expected!");
					}
				break;
				case 1:
					if (localName.equals("group")) {
						myCurrentGroup = atts.getValue("name");
					} else {
						System.out.println("wrong tag : <group> expected!");
					}
				break;
				case 2:
					if (localName.equals("option")) { 
						myConfig.setValue(myCategory, myCurrentGroup, 
                                atts.getValue("name"), atts.getValue("value"));
					} else{
						System.out.println("wrong tag : <option> expected!");
					}
				break;
				default: 
					System.out.println("too many nesting elements!");
			}
			myDepth++;
		}
        
		public void endElement(String uri, String localName, String qName) {
			myDepth--;
		}
		
		public void endDocument() {
		}
	}
	
	public ZLConfigReader () {
        myConfig = ZLConfigInstance.getInstance();
		try {
			myXMLReader = XMLReaderFactory.createXMLReader();
			myXMLReader.setContentHandler(new ConfigContentHandler());
		} catch (SAXException e) {
			System.out.println(e.getMessage());
		}
	}
	
	/** Прочитать данные из файла XML */
	public ZLConfig read (File file) {
		try {
            InputStream input = new FileInputStream(file);
            myCategory = file.getName().split(".xml")[0];
			myXMLReader.parse(new InputSource(input));
			return myConfig;
		} catch (FileNotFoundException fnfException) {
            System.err.println(fnfException.getMessage());
            return null;
        } catch (IOException ioException) {
            System.err.println(ioException.getMessage());
            return null;
        } catch (SAXException saxException) {
			System.err.println(saxException.getMessage());
			return null;
		}
	}
}
