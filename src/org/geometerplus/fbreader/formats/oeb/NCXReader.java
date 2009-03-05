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

package org.geometerplus.fbreader.formats.oeb;

import java.util.*;

import org.geometerplus.zlibrary.core.xml.*;

import org.geometerplus.fbreader.bookmodel.*;

class NCXReader extends ZLXMLReaderAdapter {
	static class NavPoint {
		final int Order;
		final int Level;
		String Text;
		String ContentHRef;

		/*
		NavPoint() {
			this(-1, -1);
		}
		*/

		NavPoint(int order, int level) {
			Order = order;
			Level = level;
		}
	}

	private final BookReader myModelReader;
	private HashMap<Integer,NavPoint> myNavigationMap;
	private ArrayList<NavPoint> myPointStack;

	private static final int READ_NONE = 0;
	private static final int READ_MAP = 1;
	private static final int READ_POINT = 2;
	private static final int READ_LABEL = 3;
	private static final int READ_TEXT = 4;

	int myReadState = READ_NONE;
	int myPlayIndex = -65535;

	NCXReader(BookReader modelReader) {
		myModelReader = modelReader;
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
						myPointStack.get(size - 1).ContentHRef = attributes.getValue("src");
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
	
	public boolean endElementHandler(String tag) {
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
					/*
					if (myPointStack.back().Text.empty()) {
						myPointStack.back().Text = "...";
					}
					myNavigationMap[myPointStack.back().Order] = myPointStack.back();
					myPointStack.pop_back();
					myReadState = myPointStack.empty() ? READ_MAP : READ_POINT;
					*/
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
	
	public void characterDataHandler(char[] ch, int start, int length) {
		if (myReadState == READ_TEXT) {
			final ArrayList<NavPoint> stack = myPointStack;
			final NavPoint last = stack.get(stack.size() - 1);
			//last.Text = .append(text, len);
		}
	}
}
