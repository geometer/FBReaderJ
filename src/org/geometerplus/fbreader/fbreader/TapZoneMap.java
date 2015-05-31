/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.util.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.util.XmlUtil;

public class TapZoneMap {
	private static final List<String> ourPredefinedMaps = new LinkedList<String>();
	private static final ZLStringListOption ourMapsOption;
	static {
		// TODO: list files from default/tapzones
		ourPredefinedMaps.add("right_to_left");
		ourPredefinedMaps.add("left_to_right");
		ourPredefinedMaps.add("down");
		ourPredefinedMaps.add("up");
		ourMapsOption = new ZLStringListOption("TapZones", "List", ourPredefinedMaps, "\000");
	}
	private static final Map<String,TapZoneMap> ourMaps = new HashMap<String,TapZoneMap>();

	public static List<String> zoneMapNames() {
		return ourMapsOption.getValue();
	}

	public static TapZoneMap zoneMap(String name) {
		TapZoneMap map = ourMaps.get(name);
		if (map == null) {
			map = new TapZoneMap(name);
			ourMaps.put(name, map);
		}
		return map;
	}

	public static TapZoneMap createZoneMap(String name, int width, int height) {
		if (ourMapsOption.getValue().contains(name)) {
			return null;
		}

		final TapZoneMap map = zoneMap(name);
		map.myWidth.setValue(width);
		map.myHeight.setValue(height);
		final List<String> lst = new LinkedList<String>(ourMapsOption.getValue());
		lst.add(name);
		ourMapsOption.setValue(lst);
		return map;
	}

	public static void deleteZoneMap(String name) {
		if (ourPredefinedMaps.contains(name)) {
			return;
		}

		ourMaps.remove(name);

		final List<String> lst = new LinkedList<String>(ourMapsOption.getValue());
		lst.remove(name);
		ourMapsOption.setValue(lst);
	}

	public static enum Tap {
		singleTap,
		singleNotDoubleTap,
		doubleTap
	};

	public final String Name;
	private final String myOptionGroupName;
	private ZLIntegerRangeOption myHeight;
	private ZLIntegerRangeOption myWidth;
	private final HashMap<Zone,ZLStringOption> myZoneMap = new HashMap<Zone,ZLStringOption>();
	private final HashMap<Zone,ZLStringOption> myZoneMap2 = new HashMap<Zone,ZLStringOption>();

	private TapZoneMap(String name) {
		Name = name;
		myOptionGroupName = "TapZones:" + name;
		myHeight = new ZLIntegerRangeOption(myOptionGroupName, "Height", 2, 5, 3);
		myWidth = new ZLIntegerRangeOption(myOptionGroupName, "Width", 2, 5, 3);
		final ZLFile mapFile = ZLFile.createFileByPath(
			"default/tapzones/" + name.toLowerCase() + ".xml"
		);
		XmlUtil.parseQuietly(mapFile, new Reader());
	}

	public boolean isCustom() {
		return !ourPredefinedMaps.contains(Name);
	}

	public int getHeight() {
		return myHeight.getValue();
	}

	public int getWidth() {
		return myWidth.getValue();
	}

	public String getActionByCoordinates(int x, int y, int width, int height, Tap tap) {
		if (width == 0 || height == 0) {
			return null;
		}
		x = Math.max(0, Math.min(width - 1, x));
		y = Math.max(0, Math.min(height - 1, y));
		return getActionByZone(myWidth.getValue() * x / width, myHeight.getValue() * y / height, tap);
	}

	public String getActionByZone(int h, int v, Tap tap) {
		final ZLStringOption option = getOptionByZone(new Zone(h, v), tap);
		return option != null ? option.getValue() : null;
	}

	private ZLStringOption getOptionByZone(Zone zone, Tap tap) {
		switch (tap) {
			default:
				return null;
			case singleTap:
			{
				final ZLStringOption option = myZoneMap.get(zone);
				return option != null ? option : myZoneMap2.get(zone);
			}
			case singleNotDoubleTap:
				return myZoneMap.get(zone);
			case doubleTap:
				return myZoneMap2.get(zone);
		}
	}

	private ZLStringOption createOptionForZone(Zone zone, boolean singleTap, String action) {
		return new ZLStringOption(
			myOptionGroupName,
			(singleTap ? "Action" : "Action2") + ":" + zone.HIndex + ":" + zone.VIndex,
			action
		);
	}

	public void setActionForZone(int h, int v, boolean singleTap, String action) {
		final Zone zone = new Zone(h, v);
		final HashMap<Zone,ZLStringOption> map = singleTap ? myZoneMap : myZoneMap2;
		ZLStringOption option = map.get(zone);
		if (option == null) {
			option = createOptionForZone(zone, singleTap, null);
			map.put(zone, option);
		}
		option.setValue(action);
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

	private class Reader extends DefaultHandler {
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			try {
				if ("zone".equals(localName)) {
					final Zone zone = new Zone(
						Integer.parseInt(attributes.getValue("x")),
						Integer.parseInt(attributes.getValue("y"))
					);
					final String action = attributes.getValue("action");
					final String action2 = attributes.getValue("action2");
					if (action != null) {
						myZoneMap.put(zone, createOptionForZone(zone, true, action));
					}
					if (action2 != null) {
						myZoneMap2.put(zone, createOptionForZone(zone, false, action2));
					}
				} else if ("tapZones".equals(localName)) {
					final String v = attributes.getValue("v");
					if (v != null) {
						myHeight.setValue(Integer.parseInt(v));
					}
					final String h = attributes.getValue("h");
					if (h != null) {
						myWidth.setValue(Integer.parseInt(h));
					}
				}
			} catch (Throwable e) {
			}
		}
	}
}
