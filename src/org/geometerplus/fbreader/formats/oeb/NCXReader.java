/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.oeb;

import java.util.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLArchiveEntryFile;
import org.geometerplus.zlibrary.core.xml.*;

import org.geometerplus.fbreader.bookmodel.*;
import org.geometerplus.fbreader.formats.util.MiscUtil;

class NCXReader extends ZLXMLReaderAdapter {
	static class NavPoint {
		final int Order;
		final int Level;
		String Text = "";
		String ContentHRef = "";

		NavPoint(int order, int level) {
			Order = order;
			Level = level;
		}
	}

	private final TreeMap<Integer,NavPoint> myNavigationMap = new TreeMap<Integer,NavPoint>();
	private final ArrayList<NavPoint> myPointStack = new ArrayList<NavPoint>();

	private static final int READ_NONE = 0;
	private static final int READ_MAP = 1;
	private static final int READ_POINT = 2;
	private static final int READ_LABEL = 3;
	private static final int READ_TEXT = 4;

	int myReadState = READ_NONE;
	int myPlayIndex = -65535;
	private String myLocalPathPrefix;

	NCXReader(BookReader modelReader) {
	}

	boolean readFile(String filePath) {
		final ZLFile file = ZLFile.createFileByPath(filePath);
		myLocalPathPrefix = MiscUtil.archiveEntryName(MiscUtil.htmlDirectoryPrefix(file));
		return read(file);
	}

	Map<Integer,NavPoint> navigationMap() {
		return myNavigationMap;
	}

	private static final String TAG_NAVMAP = "navmap";
	private static final String TAG_NAVPOINT = "navpoint";
	private static final String TAG_NAVLABEL = "navlabel";
	private static final String TAG_CONTENT = "content";
	private static final String TAG_TEXT = "text";

	private static final String ATTRIBUTE_PLAYORDER = "playOrder";

	private int atoi(String number) {
		try {
			return Integer.parseInt(number);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		tag = tag.toLowerCase().intern();
		switch (myReadState) {
			case READ_NONE:
				if (tag == TAG_NAVMAP) {
					myReadState = READ_MAP;
				}
				break;
			case READ_MAP:
				if (tag == TAG_NAVPOINT) {
					final String order = attributes.getValue(ATTRIBUTE_PLAYORDER);
					final int index = (order != null) ? atoi(order) : myPlayIndex++;
					myPointStack.add(new NavPoint(index, myPointStack.size()));
					myReadState = READ_POINT;
				}
				break;
			case READ_POINT:
				if (tag == TAG_NAVPOINT) {
					final String order = attributes.getValue(ATTRIBUTE_PLAYORDER);
					final int index = (order != null) ? atoi(order) : myPlayIndex++;
					myPointStack.add(new NavPoint(index, myPointStack.size()));
				} else if (tag == TAG_NAVLABEL) {
					myReadState = READ_LABEL;
				} else if (tag == TAG_CONTENT) {
					final int size = myPointStack.size();
					if (size > 0) {
						myPointStack.get(size - 1).ContentHRef =
							ZLArchiveEntryFile.normalizeEntryName(
								myLocalPathPrefix + MiscUtil.decodeHtmlReference(attributes.getValue("src"))
							);
					}
				}
				break;
			case READ_LABEL:
				if (TAG_TEXT == tag) {
					myReadState = READ_TEXT;
				}
				break;
			case READ_TEXT:
				break;
		}
		return false;
	}
	
	@Override
	public boolean endElementHandler(String tag) {
		tag = tag.toLowerCase().intern();
		switch (myReadState) {
			case READ_NONE:
				break;
			case READ_MAP:
				if (TAG_NAVMAP == tag) {
					myReadState = READ_NONE;
				}
				break;
			case READ_POINT:
				if (TAG_NAVPOINT == tag) {
					NavPoint last = myPointStack.get(myPointStack.size() - 1);
					if (last.Text.length() == 0) {
						last.Text = "...";
					}
					myNavigationMap.put(last.Order, last);
					myPointStack.remove(myPointStack.size() - 1);
					myReadState = (myPointStack.isEmpty()) ? READ_MAP : READ_POINT;
				}
			case READ_LABEL:
				if (TAG_NAVLABEL == tag) {
					myReadState = READ_POINT;
				}
				break;
			case READ_TEXT:
				if (TAG_TEXT == tag) {
					myReadState = READ_LABEL;
				}
				break;
		}
		return false;
	}
	
	@Override
	public void characterDataHandler(char[] ch, int start, int length) {
		if (myReadState == READ_TEXT) {
			final ArrayList<NavPoint> stack = myPointStack;
			final NavPoint last = stack.get(stack.size() - 1);
			last.Text += new String(ch, start, length);
		}
	}

	@Override
	public boolean dontCacheAttributeValues() {
		return true;
	}
}
