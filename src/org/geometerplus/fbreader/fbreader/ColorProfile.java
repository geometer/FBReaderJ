/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.options.*;

public class ColorProfile {
	public static final String DAY = "defaultLight";
	public static final String NIGHT = "defaultDark";

	private static final ArrayList<String> ourNames = new ArrayList<String>();
	private static final HashMap<String,ColorProfile> ourProfiles = new HashMap<String,ColorProfile>();

	public static List<String> names() {
		if (ourNames.isEmpty()) {
			final int size = new ZLIntegerOption("Colors", "NumberOfSchemes", 0).getValue();
			if (size == 0) {
				ourNames.add(DAY);
				ourNames.add(NIGHT);
			} else for (int i = 0; i < size; ++i) {
				ourNames.add(new ZLStringOption("Colors", "Scheme" + i, "").getValue());
			}
		}
		return Collections.unmodifiableList(ourNames);
	}

	public static ColorProfile get(String name) {
		ColorProfile profile = ourProfiles.get(name);
		if (profile == null) {
			profile = new ColorProfile(name);
			ourProfiles.put(name, profile);
		}
		return profile;
	}

	public final ZLColorOption BackgroundOption;
	public final ZLColorOption SelectionBackgroundOption;
	public final ZLColorOption HighlightingOption;
	public final ZLColorOption RegularTextOption;
	public final ZLColorOption HyperlinkTextOption;
	public final ZLColorOption FooterFillOption;

	private ColorProfile(String name, ColorProfile base) {
		this(name);
		BackgroundOption.setValue(base.BackgroundOption.getValue());
		SelectionBackgroundOption.setValue(base.SelectionBackgroundOption.getValue());
		HighlightingOption.setValue(base.HighlightingOption.getValue());
		RegularTextOption.setValue(base.RegularTextOption.getValue());
		HyperlinkTextOption.setValue(base.HyperlinkTextOption.getValue());
		FooterFillOption.setValue(base.FooterFillOption.getValue());
	}

	private ColorProfile(String name) {
		if (NIGHT.equals(name)) {
			BackgroundOption =
				new ZLColorOption("Colors", name + ":Background", new ZLColor(0, 0, 0));
			SelectionBackgroundOption =
				new ZLColorOption("Colors", name + ":SelectionBackground", new ZLColor(82, 131, 194));
			HighlightingOption =
				new ZLColorOption("Colors", name + ":Highlighting", new ZLColor(96, 96, 128));
			RegularTextOption =
				new ZLColorOption("Colors", name + ":Text", new ZLColor(192, 192, 192));
			HyperlinkTextOption =
				new ZLColorOption("Colors", name + ":Hyperlink", new ZLColor(60, 142, 224));
			FooterFillOption =
				new ZLColorOption("Colors", name + ":FooterFillOption", new ZLColor(85, 85, 85));
		} else {
			BackgroundOption =
				new ZLColorOption("Colors", name + ":Background", new ZLColor(255, 255, 255));
			SelectionBackgroundOption =
				new ZLColorOption("Colors", name + ":SelectionBackground", new ZLColor(82, 131, 194));
			HighlightingOption =
				new ZLColorOption("Colors", name + ":Highlighting", new ZLColor(255, 192, 128));
			RegularTextOption =
				new ZLColorOption("Colors", name + ":Text", new ZLColor(0, 0, 0));
			HyperlinkTextOption =
				new ZLColorOption("Colors", name + ":Hyperlink", new ZLColor(60, 139, 255));
			FooterFillOption =
				new ZLColorOption("Colors", name + ":FooterFillOption", new ZLColor(170, 170, 170));
		}
	}
}
