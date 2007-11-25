package org.zlibrary.core.options.config.reader;


import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.zlibrary.core.options.config.*;

/*package*/ class ZLConfigReader implements ZLReader{
	
	private class ConfigContentHandler extends DefaultHandler{
		private int myDepth = 0;
		private String myCurrentGroup = "";
		
		public void startDocument() {
			myDepth = 0;
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
						myConfig.setValue(myCurrentGroup, 
								atts.getValue("name"), atts.getValue("value"), myCategory);
					} else{
						System.out.println("wrong tag : <option> expected!");
						System.out.println(myCategory);
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
			myConfig.applyDelta();
		}
	}
	
	private XMLReader myXMLReader;
	private ZLConfig myConfig;
	private String myCategory = "";
	private File myDestinationDirectory;
	
	public ZLConfigReader (String path) {
		myConfig = ZLConfigInstance.getInstance();
		myDestinationDirectory = new File(path);
		if (myDestinationDirectory.isDirectory()){
			try {
				myXMLReader = XMLReaderFactory.createXMLReader();
				myXMLReader.setContentHandler(new ConfigContentHandler());
			} catch (SAXException e) {
				System.out.println(e.getMessage());
			}
		} else {
			System.out.println("Wrong path - directory path expected");
		}
	}
	
	/**
	 *  
	 * Прочитать данные из файла XML 
	 * @param file - файл XML
	 */
	public void readFile(File file) {
		try {
			InputStream input = new FileInputStream(file);
			myCategory = file.getName().split(".xml")[0];
			myXMLReader.parse(new InputSource(input));
		} catch (FileNotFoundException fnfException) {
			System.err.println(fnfException.getMessage());
		} catch (IOException ioException) {
			System.err.println(ioException.getMessage());
		} catch (SAXException saxException) {
			System.err.println(saxException.getMessage());
		}
	}
	
	private boolean isXMLFileName(String fileName){
		String name = fileName.toLowerCase();
		return name.endsWith(".xml");
	}
	
	public void read(){
		String[] fileList = myDestinationDirectory.list();
		if (fileList != null) {
			for (String fileName : fileList){
				if (isXMLFileName(fileName)){
					readFile(new File(myDestinationDirectory + "/" + fileName));
				}
			}
		}
	}
}
