package org.zlibrary.core.options.config.reader;

import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.zlibrary.core.options.config.*;

/*package*/class ZLConfigReader implements ZLReader {
	
	private class ConfigContentHandler extends DefaultHandler {
		private int myDepth = 0;

		private String myCurrentGroup = "";

		public void startDocument() {
			myDepth = 0;
		}

		public void startElement(String uri, String localName, 
				String qName, Attributes atts) {
			
			localName = localName.toLowerCase();
			switch (myDepth) {
				case 0:
					if (!localName.equals("config")) {
						printError(localName);
					}
					break;
				case 1:
					if (localName.equals("group")) {
						myCurrentGroup = atts.getValue("name");
					} else {
						printError(localName);
					}
					break;
				case 2:
					if (localName.equals("option")) {
						myConfig.setValue(myCurrentGroup, atts.getValue("name"), 
								atts.getValue("value"), myCategory);
					} else {
						printError(localName);
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
	
	private class DeltaConfigContentHandler extends DefaultHandler {
		private int myDepth = 0;

		private String myCurrentGroup = "";

		//private boolean myIsDeleting = false;
		
		public void startDocument() {
			myDepth = 0;
		}

		public void startElement(String uri, String localName, 
				String qName, Attributes atts) {
			
			localName = localName.toLowerCase();
			switch (myDepth) {
				case 0:
					if (!localName.equals("delta")) {
						printError(localName);
					}
					break;
				case 1:
					if (localName.equals("group")) {
						myCurrentGroup = atts.getValue("name");
					} else {
						if (localName.equals("delete")) {
							//myIsDeleting = true;
						} else {
							printError(localName);
						}
					}
					break;
				case 2:
					if (localName.equals("option")) {
						myConfig.setValue(myCurrentGroup, atts.getValue("name"), 
								atts.getValue("value"), atts.getValue("category"));
					} else {
						if (localName.equals("group")) {
							myCurrentGroup = atts.getValue("name");
						} else {
							printError(localName);
						}
					}
					break;
				case 3:
					if (localName.equals("option")) {
						myConfig.unsetValue(myCurrentGroup, atts.getValue("name"));
					} else {
						printError(localName);
					}
					break;
				default:
					System.out.println("too many nesting elements!");
			}
			myDepth++;
		}

		public void endElement(String uri, String localName, String qName) {
			if ((myDepth == 1) && (localName.equals("delete"))) {
				//myIsDeleting = false;
			}
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

	private String myFile = "";
	
	public ZLConfigReader(String path) {
		myConfig = ZLConfigInstance.getInstance();
		myDestinationDirectory = new File(path);
		if (myDestinationDirectory.exists()) {
			if (myDestinationDirectory.isDirectory()) {
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
	}

	private void printError(String localName) {
		System.out.println("wrong tag in " + myFile + ", tag \"" 
				+ localName + "\" is unexpected!");
	}
	
	/**
	 * 
	 * Прочитать данные из файла XML
	 * 
	 * @param file -
	 *            файл XML
	 */
	public void readFile(File file) {
		try {
			myFile = file.getName().toLowerCase();
			InputStream input = new FileInputStream(file);
			myCategory = file.getName().split(".xml")[0];
			myXMLReader.parse(new InputSource(input));
		} catch (FileNotFoundException fnfException) {
			if (!myFile.equals("delta.xml")) {
				System.err.println(fnfException.getMessage());
			}
		} catch (IOException ioException) {
			System.err.println(ioException.getMessage());
		} catch (SAXException saxException) {
			System.err.println(saxException.getMessage());
		}
	}

	private boolean isXMLFileName(String fileName) {
		String name = fileName.toLowerCase();
		//System.out.println(fileName);
		return name.endsWith(".xml");
	}

	public void read() {
		String[] fileList = myDestinationDirectory.list();
		myDestinationDirectory.mkdir();
		if (fileList != null) {
			for (String fileName : fileList) {
				if (isXMLFileName(fileName)
						&& !(fileName.toLowerCase().equals("delta.xml"))) {
					readFile(new File(myDestinationDirectory + "/" + fileName));
				}
			}
		}
		if (myXMLReader != null) {
			myXMLReader.setContentHandler(new DeltaConfigContentHandler());
			readFile(new File(myDestinationDirectory + "/delta.xml"));
			myConfig.clearDelta();
		}
	}
}
