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

package org.geometerplus.fbreader.fbreader;

import java.util.HashMap;

import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

public class TapZoneMap {
	public static enum Tap {
		singleTap,
		singleNotDoubleTap,
		doubleTap
	};

	private int myVerticalSize = 3;
	private int myHorizontalSize = 3;
	private final HashMap<Zone,String> myZoneMap = new HashMap<Zone,String>();
	private final HashMap<Zone,String> myZoneMap2 = new HashMap<Zone,String>();

	TapZoneMap(int v, int h) {
		myVerticalSize = v;
		myHorizontalSize = h;
	}

	TapZoneMap(String name) {
		final ZLFile mapFile = ZLFile.createFileByPath(
			"default/tapzones/" + name.toLowerCase() + ".xml"
		);
		new Reader().read(mapFile);
	}

	public String getActionByCoordinates(int x, int y, int width, int height, Tap tap) {
		if (width == 0 || height == 0) {
			return null;
		}
		final Zone zone = new Zone(myHorizontalSize * x / width, myVerticalSize * y / height);
		switch (tap) {
			case singleTap:
			{
				final String action = myZoneMap.get(zone);
				return action != null ? action : myZoneMap2.get(zone);
			}
			case singleNotDoubleTap:
				return myZoneMap.get(zone);
			case doubleTap:
				return myZoneMap2.get(zone);
		}
		return null;
	}

	private static class Zone {
		int HIndex;
		int VIndex;

		Zone(int h, int v) {
			HIndex = h;
			VIndex = v;
		}

		/*void mirror45() {
			final int swap = HIndex;
			HIndex = VIndex;
			VIndex = swap;
		}*/

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}

			if (!(o instanceof Zone)) {
				return false;
			}

			final Zone tz = (Zone)o;
			return HIndex == tz.HIndex && VIndex == tz.VIndex;
		}

		@Override
		public int hashCode() {
			return (HIndex << 5) + VIndex;
		}
	}

	private class Reader extends ZLXMLReaderAdapter {
		@Override
		public boolean startElementHandler(String tag, ZLStringMap attributes) {
			try {
				if ("zone".equals(tag)) {
					final int x = Integer.parseInt(attributes.getValue("x"));
					final int y = Integer.parseInt(attributes.getValue("y"));
					final String action = attributes.getValue("action");
					final String action2 = attributes.getValue("action2");
					if (action != null) {
						myZoneMap.put(new Zone(x, y), action);
					}
					if (action2 != null) {
						myZoneMap2.put(new Zone(x, y), action2);
					}
				} else if ("tapZones".equals(tag)) {
					final String v = attributes.getValue("v");
					if (v != null) {
						myVerticalSize = Integer.parseInt(v);
					}
					final String h = attributes.getValue("h");
					if (h != null) {
						myHorizontalSize = Integer.parseInt(h);
					}
				}
			} catch (Throwable e) {
			}
			return false;
		}
	}
}
