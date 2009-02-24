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
import java.util.*;

final class ZLConfigWriter implements ZLWriter {

	private final ZLConfigImpl myConfig;

	private final File myDestinationDirectory;

	protected ZLConfigWriter(ZLConfigImpl config, String path) {
		myConfig = config;
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		myDestinationDirectory = file;
	}

	private void deleteConfigFile(String filePath) {
		File file = new File(filePath);
		file.delete();
	}

	// TODO пока public в целях отладки
	public void writeDelta() {
		this.writeFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ myConfig.getDelta(), myDestinationDirectory + "/delta.xml");
	}

	private void writeFile(String content, String path) {
		File file = new File(path);
		try {
			PrintWriter pw = new PrintWriter(file, "UTF-8");
			try {
				pw.write(content);
			} finally {
				pw.close();
			}
		} catch (FileNotFoundException fnfException) {
			//TODO handle exception
		} catch (UnsupportedEncodingException e) {
		}
	}

	private String configFilePath(String category) {
		return myDestinationDirectory + "/" + category + ".xml";
	}

	public void write() {
		
		// usedCategories contains A if and only if 
		// there's at least one option in config, which category name = A 
		final Set<String> usedCategories = myConfig.applyDelta();
				
		// list of writers. one for each category-file
		final HashMap<String, PrintWriter> categoryWriters 
			= new HashMap<String, PrintWriter>();

		// groupExistsIn - list of category-files where 
		// current group must be written
		final Set<String> groupExistsIn = new HashSet<String>();

		// for every group.....
		for (String groupName : myConfig.groupNames()) {

			ZLGroup group = myConfig.getGroup(groupName);
			
			// for every option in this group
			for (String optionName : group.optionNames()) {
				
				ZLOptionInfo option = group.getOption(optionName);
				String category = option.getCategory();
				
				PrintWriter categoryWriter = categoryWriters.get(category);

				if (categoryWriter == null) {
					try {
						categoryWriter = new PrintWriter(configFilePath(category), "UTF-8");
						categoryWriters.put(category, categoryWriter);
						categoryWriter.write("<?xml version=\"1.0\" " +
								"encoding=\"UTF-8\"?>\n<config>\n");
					} catch (FileNotFoundException fnf) {
						//TODO handle exception
					} catch (UnsupportedEncodingException e) {
					}
				}
				
				// open group in current category, if it wasn't
				if (!groupExistsIn.contains(category)) {
					groupExistsIn.add(category);
					categoryWriter.write("  <group name=\"" + groupName + "\">\n");
				}
				
				// write each option
				categoryWriter.write(option.toXML(optionName) + "");
			}

			// close group everywhere, where it was opened, after handling
			for (String category : groupExistsIn) {
				categoryWriters.get(category).write("  </group>\n");
			}
			
			groupExistsIn.clear();
		}

		// записываем в концы всех файлов закрывающий тэг, закрываем соответствующие
		// потоки
		for (PrintWriter writer : categoryWriters.values()) {
			writer.write("</config>");
			writer.close();
		}
		
		// delete files, according to empty categories
		for (String category : usedCategories) {
			if (!categoryWriters.keySet().contains(category)) {
				deleteConfigFile(configFilePath(category));
			}
		}
	}
}
