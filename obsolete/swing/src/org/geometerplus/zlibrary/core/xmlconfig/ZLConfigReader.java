/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.core.xmlconfig;

import java.io.*;
import java.util.Map;

import org.geometerplus.zlibrary.core.xml.*;

final class ZLConfigReader implements ZLReader {
	private class ConfigReader extends ZLXMLReaderAdapter {
		private int myDepth = 0;

		private String myCurrentGroup = "";

		public void startDocumentHandler() {
			myDepth = 0;
		}

		public boolean startElementHandler(String tag, ZLStringMap attributes) {
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
			return false;
		}

		public boolean endElementHandler(String tag) {
			myDepth--;
			return false;
		}

		public void endDocumentHandler() {
			
		}
	}

	private class DeltaConfigReader extends ZLXMLReaderAdapter {
		private int myDepth = 0;

		private String myCurrentGroup = "";
		private boolean myCurrentGroupIsEmpty = false;
		// private boolean myIsDeleting = false;

		public void startDocumentHandler() {
			myDepth = 0;
			myFile = "delta.xml";
		}

		public boolean startElementHandler(String tag, ZLStringMap attributes) {
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
			return false;
		}

		public boolean endElementHandler(String tag) {
			if ((myDepth == 1) && (myCurrentGroupIsEmpty) 
					&& (tag.equals("group"))) {
				myConfig.removeGroup(myCurrentGroup);
			}
			myDepth--;
			return false;
		}
	}

	private ZLXMLReaderAdapter myXMLReader = new ConfigReader();

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
