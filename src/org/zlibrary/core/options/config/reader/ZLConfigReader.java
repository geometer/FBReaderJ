package org.zlibrary.core.options.config.reader;

import java.io.*;
import java.util.Map;

import org.zlibrary.core.options.config.*;
import org.zlibrary.core.xml.ZLXMLReader;

/*package*/class ZLConfigReader implements ZLReader {
	
	private class ConfigReader extends ZLXMLReader  {
		private int myDepth = 0;

		private String myCurrentGroup = "";

		public void startDocumentHandler() {
			myDepth = 0;
		}
		
		public void startElementHandler(String tag, 
				Map<String, String> attributes) {
			
			tag = tag.toLowerCase();
			switch (myDepth) {
				case 0:
					if (!tag.equals("config")) {
						printError(tag);
					}
					break;
				case 1:
					if (tag.equals("group")) {
						myCurrentGroup = attributes.get("name");
					} else {
						printError(tag);
					}
					break;
				case 2:
					if (tag.equals("option")) {
						myConfig.setValue(myCurrentGroup, attributes.get("name"), 
								attributes.get("value"), myCategory);
					} else {
						printError(tag);
					}
					break;
				default:
					System.out.println("too many nesting elements!");
			}
			myDepth++;
		}

		public void endElementHandler(String tag) {
			myDepth--;
			//System.out.println("kgf");
		}

		public void endDocumentHandler() {
			System.out.println("kgf");
			myConfig.applyDelta();
		}
	}
	
	private class DeltaConfigReader extends ZLXMLReader {
		private int myDepth = 0;

		private String myCurrentGroup = "";

		//private boolean myIsDeleting = false;
		
		public void startDocumentHandler() {
			myDepth = 0;
		}

		public void startElementHandler(String tag, 
				Map<String, String> attributes) {
			
			tag = tag.toLowerCase();
			switch (myDepth) {
				case 0:
					if (!tag.equals("delta")) {
						printError(tag);
					}
					break;
				case 1:
					if (tag.equals("group")) {
						myCurrentGroup = attributes.get("name");
					} else {
						if (tag.equals("delete")) {
							//myIsDeleting = true;
						} else {
							printError(tag);
						}
					}
					break;
				case 2:
					if (tag.equals("option")) {
						myConfig.setValue(myCurrentGroup, attributes.get("name"), 
								attributes.get("value"), attributes.get("category"));
					} else {
						if (tag.equals("group")) {
							myCurrentGroup = attributes.get("name");
						} else {
							printError(tag);
						}
					}
					break;
				case 3:
					if (tag.equals("option")) {
						myConfig.unsetValue(myCurrentGroup, attributes.get("name"));
					} else {
						printError(tag);
					}
					break;
				default:
					System.out.println("too many nesting elements!");
			}
			myDepth++;
		}

		public void endElementHandler(String tag) {
			if ((myDepth == 1) && (tag.equals("delete"))) {
				//myIsDeleting = false;
			}
			myDepth--;
		}

		public void endDocumentHandler() {
			myConfig.applyDelta();
		}
	}
	
	private ZLXMLReader myXMLReader = new ConfigReader();

	private ZLConfig myConfig;

	private String myCategory = "";

	private File myDestinationDirectory;

	private String myFile = "";
	
	public ZLConfigReader(String path) {
		myConfig = ZLConfigInstance.getInstance();
		myDestinationDirectory = new File(path);
		if (myDestinationDirectory.exists()) {
			if (!myDestinationDirectory.isDirectory()) {
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
	 * @param file файл XML
	 */
	public void readFile(File file) {
		myFile = file.getName().toLowerCase();
		myCategory = file.getName().split(".xml")[0];
		//if (file.exists()) {
			//System.out.println(file.toString());
		myXMLReader.read(file.toString());
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
		myXMLReader = new DeltaConfigReader();
		myXMLReader.read(myDestinationDirectory + "/delta.xml");
		myConfig.clearDelta();
	}
}
