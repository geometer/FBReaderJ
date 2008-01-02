package org.zlibrary.core.xmlconfig;

import java.io.*;
import java.util.Map;

import org.zlibrary.core.xml.ZLXMLReader;

final class ZLConfigReader implements ZLReader {
	private class ConfigReader extends ZLXMLReader {
		private int myDepth = 0;

		private String myCurrentGroup = "";

		public void startDocumentHandler() {
			myDepth = 0;
		}

		public void startElementHandler(String tag, StringMap attributes) {
			switch (myDepth) {
				case 0:
					if (!tag.equals("config")) {
						printError(tag);
					}
					break;
				case 1:
					if (tag.equals("group")) {
						myCurrentGroup = attributes.getValue("name");
					} else {
						printError(tag);
					}
					break;
				case 2:
					if (tag.equals("option")) {
						myConfig.setValueDirectly(myCurrentGroup, attributes.getValue("name"), attributes.getValue("value"), myCategory);
					} else {
						printError(tag);
					}
					break;
				default:
					System.out.println("too many nesting elements! in main");
			}
			myDepth++;
		}

		public void endElementHandler(String tag) {
			myDepth--;
		}

		public void endDocumentHandler() {
			
		}
	}

	private class DeltaConfigReader extends ZLXMLReader {
		private int myDepth = 0;

		private String myCurrentGroup = "";
		private boolean myCurrentGroupIsEmpty = false;
		// private boolean myIsDeleting = false;

		public void startDocumentHandler() {
			myDepth = 0;
			myFile = "delta.xml";
		}

		public void startElementHandler(String tag, StringMap attributes) {
			switch (myDepth) {
				case 0:
					if (!tag.equals("config")) {
						printError(tag);
					}
					break;
				case 1:
					if (tag.equals("group")) {
						myCurrentGroup = attributes.getValue("name");
						myCurrentGroupIsEmpty = true;
					} else {
						printError(tag);
					}
					break;
				case 2:
					if (tag.equals("option")) {
						myCurrentGroupIsEmpty = false;
						String value = attributes.getValue("value");
						String category = attributes.getValue("category");
						String name = attributes.getValue("name");
						if ((value != null) && (category != null)) {
							myConfig.setValue(myCurrentGroup, name, value, category);
						} else {
							myConfig.unsetValue(myCurrentGroup, name);
						}
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
			if ((myDepth == 1) && (myCurrentGroupIsEmpty) 
					&& (tag.equals("group"))) {
				myConfig.removeGroup(myCurrentGroup);
			}
			myDepth--;
		}

		public void endDocumentHandler() {
			
		}
	}

	private ZLXMLReader myXMLReader = new ConfigReader();

	private final ZLConfigImpl myConfig;

	private String myCategory = "";

	private final File myDestinationDirectory;

	private final String myDeltaFilePath;
	
	private String myFile = "";

	protected ZLConfigReader(ZLConfigImpl config, String path) {
		myConfig = config;
		myDestinationDirectory = new File(path);
		myDeltaFilePath = myDestinationDirectory + "/delta.xml";
		if (myDestinationDirectory.exists()) {
			if (!myDestinationDirectory.isDirectory()) {
				System.out.println("Wrong path - directory path expected");
			}
		}
	}

	private void printError(String localName) {
		System.out.println("wrong tag in " + myFile + ", tag \"" + localName
				+ "\" is unexpected!");
	}

	/**
	 * 
	 * Прочитать данные из файла XML
	 * 
	 * @param file
	 *             файл XML
	 */
	private void readFile(File file) {
		myFile = file.getName();
		myCategory = myFile.substring(0, myFile.length() - 4);
		// if (file.exists()) {
		// System.out.println(file.toString());
		myXMLReader.read(file.toString());
	}

	private static boolean isXMLFileName(String fileName) {
		// System.out.println(fileName);
		return fileName.endsWith(".xml");
	}

	public void read() {
		String[] fileList = myDestinationDirectory.list();
		myDestinationDirectory.mkdir();
		
		/**
		 * это важно, так как если дельта сейчас будет непустой, то
		 * то что там было тоже применится к конфигу! (см тест 03 в конфигтестах)
		 */
		myConfig.clearDelta();
		
		if (fileList != null) {
			for (String fileName : fileList) {
				if (isXMLFileName(fileName) && !(fileName.equals("delta.xml"))) {
					readFile(new File(myDestinationDirectory + "/" + fileName));
				}
			}
		}
		myXMLReader = new DeltaConfigReader();
		myXMLReader.read(myDeltaFilePath);
		//myConfig.clearDelta();
		myConfig.applyDelta();
		//System.out.println(myConfig.getDelta());
		new File(myDeltaFilePath).delete();
	}
}
