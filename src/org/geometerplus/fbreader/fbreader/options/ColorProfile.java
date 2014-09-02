/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.fbreader.options;

import java.util.*;

import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.options.*;

public class ColorProfile {
	public static final String DAY = "defaultLight";
	public static final String NIGHT = "defaultDark";

    public static final String YOTA_BS_WHITE = "yota_backscreen_white";
    public static final String YOTA_BS_BLACK = "yota_backscreen_black";

    public static final String YOTA_FS_WHITE = "yota_frontscreen_white";
    public static final String YOTA_FS_BLACK = "yota_frontscreen_black";
    public static final String YOTA_FS_SEPIA = "yota_frontscreen_sepia";

	private static final ArrayList<String> ourNames = new ArrayList<String>();
	private static final HashMap<String,ColorProfile> ourProfiles = new HashMap<String,ColorProfile>();

	public static List<String> names() {
		if (ourNames.isEmpty()) {
			final int size = new ZLIntegerOption("Colors", "NumberOfSchemes", 0).getValue();
			if (size == 0) {
				ourNames.add(DAY);
				ourNames.add(NIGHT);
                ourNames.add(YOTA_BS_BLACK);
                ourNames.add(YOTA_BS_WHITE);
                ourNames.add(YOTA_FS_BLACK);
                ourNames.add(YOTA_FS_WHITE);
                ourNames.add(YOTA_FS_SEPIA);
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

	public final String Name;

	public final ZLStringOption WallpaperOption;
	public final ZLColorOption BackgroundOption;
	public final ZLColorOption SelectionBackgroundOption;
	public final ZLColorOption SelectionForegroundOption;
	public final ZLColorOption HighlightingOption;
	public final ZLColorOption RegularTextOption;
	public final ZLColorOption HyperlinkTextOption;
	public final ZLColorOption VisitedHyperlinkTextOption;
	public final ZLColorOption FooterFillOption;
    public final ZLColorOption SelectionCursorOption;
    public final ZLColorOption SelectionCursorFillOption;

	private ColorProfile(String name, ColorProfile base) {
		this(name);
		BackgroundOption.setValue(base.BackgroundOption.getValue());
		SelectionBackgroundOption.setValue(base.SelectionBackgroundOption.getValue());
		SelectionForegroundOption.setValue(base.SelectionForegroundOption.getValue());
		HighlightingOption.setValue(base.HighlightingOption.getValue());
		RegularTextOption.setValue(base.RegularTextOption.getValue());
		HyperlinkTextOption.setValue(base.HyperlinkTextOption.getValue());
		VisitedHyperlinkTextOption.setValue(base.VisitedHyperlinkTextOption.getValue());
		FooterFillOption.setValue(base.FooterFillOption.getValue());
	}

	private static ZLColorOption createOption(String profileName, String optionName, int r, int g, int b) {
		return new ZLColorOption("Colors", profileName + ':' + optionName, new ZLColor(r, g, b));
	}

	private ColorProfile(String name) {
		Name = name;
		if (NIGHT.equals(name)) {
			WallpaperOption =
				new ZLStringOption("Colors", name + ":Wallpaper", "");
			BackgroundOption =
				createOption(name, "Background", 0, 0, 0);
			SelectionBackgroundOption =
				createOption(name, "SelectionBackground", 82, 131, 194);
			SelectionForegroundOption =
				createOption(name, "SelectionForeground", 255, 255, 220);
			HighlightingOption =
				createOption(name, "Highlighting", 96, 96, 128);
			RegularTextOption =
				createOption(name, "Text", 192, 192, 192);
			HyperlinkTextOption =
				createOption(name, "Hyperlink", 60, 142, 224);
			VisitedHyperlinkTextOption =
				createOption(name, "VisitedHyperlink", 200, 139, 255);
			FooterFillOption =
				createOption(name, "FooterFillOption", 85, 85, 85);
            SelectionCursorOption =
                createOption(name, "SelectionCursorOption", 19, 157, 235);
            SelectionCursorFillOption =
                    createOption(name, "SelectionCursorFillOption", 19, 157, 235);

		}
        else if (YOTA_FS_BLACK.equals(name)) {
            WallpaperOption =
                    new ZLStringOption("Colors", name + ":Wallpaper", "");
            BackgroundOption =
                    createOption(name, "Background", 0, 0, 0);
            SelectionBackgroundOption =
                    createOption(name, "SelectionBackground", 192, 204, 212);
            SelectionForegroundOption =
                    createOption(name, "SelectionForeground", 255, 255, 220);
            HighlightingOption =
                    createOption(name, "Highlighting", 96, 96, 128);
            RegularTextOption =
                    createOption(name, "Text", 192, 192, 192);
            HyperlinkTextOption =
                    createOption(name, "Hyperlink", 60, 142, 224);
            VisitedHyperlinkTextOption =
                    createOption(name, "VisitedHyperlink", 200, 139, 255);
            FooterFillOption =
                    createOption(name, "FooterFillOption", 85, 85, 85);
            SelectionCursorOption =
                    createOption(name, "SelectionCursorOption", 19, 157, 235);
            SelectionCursorFillOption =
                    createOption(name, "SelectionCursorFillOption", 19, 157, 235);
        }
        else if (YOTA_FS_WHITE.equals(name)) {
            WallpaperOption =
                    new ZLStringOption("Colors", name + ":Wallpaper", "");
            BackgroundOption =
                    createOption(name, "Background", 255, 255, 255);
            SelectionBackgroundOption =
                    createOption(name, "SelectionBackground", 192, 204, 212);
            SelectionForegroundOption =
                    createOption(name, "SelectionForeground", 0, 0, 0);
            HighlightingOption =
                    createOption(name, "Highlighting", 255, 192, 128);
            RegularTextOption =
                    createOption(name, "Text", 0, 0, 0);
            HyperlinkTextOption =
                    createOption(name, "Hyperlink", 60, 139, 255);
            VisitedHyperlinkTextOption =
                    createOption(name, "VisitedHyperlink", 200, 139, 255);
            FooterFillOption =
                    createOption(name, "FooterFillOption", 170, 170, 170);
            SelectionCursorOption =
                    createOption(name, "SelectionCursorOption", 19, 157, 235);
            SelectionCursorFillOption =
                    createOption(name, "SelectionCursorFillOption", 19, 157, 235);
        }
        else if (YOTA_FS_SEPIA.equals(name)) {
            WallpaperOption =
                    new ZLStringOption("Colors", name + ":Wallpaper", "wallpapers/sepia.jpg");
            BackgroundOption =
                    createOption(name, "Background", 255, 255, 255);
            SelectionBackgroundOption =
                    createOption(name, "SelectionBackground", 82, 131, 194);
            SelectionForegroundOption =
                    createOption(name, "SelectionForeground", 255, 255, 220);
            HighlightingOption =
                    createOption(name, "Highlighting", 255, 192, 128);
            RegularTextOption =
                    createOption(name, "Text", 0, 0, 0);
            HyperlinkTextOption =
                    createOption(name, "Hyperlink", 60, 139, 255);
            VisitedHyperlinkTextOption =
                    createOption(name, "VisitedHyperlink", 200, 139, 255);
            FooterFillOption =
                    createOption(name, "FooterFillOption", 170, 170, 170);
            SelectionCursorOption =
                    createOption(name, "SelectionCursorOption", 19, 157, 235);
            SelectionCursorFillOption =
                    createOption(name, "SelectionCursorFillOption", 19, 157, 235);
        }
        else if (YOTA_BS_WHITE.equals(name)) {
            WallpaperOption =
                    new ZLStringOption("Colors", name + ":Wallpaper", "");
            BackgroundOption =
                    createOption(name, "Background", 255, 255, 255);
            SelectionBackgroundOption =
                    createOption(name, "SelectionBackground", 0, 0, 0);
            SelectionForegroundOption =
                    createOption(name, "SelectionForeground", 255, 255, 255);
            HighlightingOption =
                    createOption(name, "Highlighting", 0, 0, 0);
            RegularTextOption =
                    createOption(name, "Text", 0, 0, 0);
            HyperlinkTextOption =
                    createOption(name, "Hyperlink", 0, 0, 0);
            VisitedHyperlinkTextOption =
                    createOption(name, "VisitedHyperlink", 0, 0, 0);
            FooterFillOption =
                    createOption(name, "FooterFillOption", 0, 0, 0);
            SelectionCursorOption =
                    createOption(name, "SelectionCursorOption", 0, 0, 0);
            SelectionCursorFillOption =
                    createOption(name, "SelectionCursorFillOption", 0, 0, 0);
        }
        else if (YOTA_BS_BLACK.equals(name)) {
            WallpaperOption =
                    new ZLStringOption("Colors", name + ":Wallpaper", "");
            BackgroundOption =
                    createOption(name, "Background", 0, 0, 0);
            SelectionBackgroundOption =
                    createOption(name, "SelectionBackground", 255, 255, 255);
            SelectionForegroundOption =
                    createOption(name, "SelectionForeground", 0, 0, 0);
            HighlightingOption =
                    createOption(name, "Highlighting", 255, 255, 255);
            RegularTextOption =
                    createOption(name, "Text", 255, 255, 255);
            HyperlinkTextOption =
                    createOption(name, "Hyperlink", 0, 0, 0);
            VisitedHyperlinkTextOption =
                    createOption(name, "VisitedHyperlink", 0, 0, 0);
            FooterFillOption =
                    createOption(name, "FooterFillOption", 0, 0, 0);
            SelectionCursorOption =
                    createOption(name, "SelectionCursorOption", 255, 255, 255);
            SelectionCursorFillOption =
                    createOption(name, "SelectionCursorFillOption", 255, 255, 255);
        }
        else {
            WallpaperOption =
                    new ZLStringOption("Colors", name + ":Wallpaper", "wallpapers/sepia.jpg");
            BackgroundOption =
                    createOption(name, "Background", 255, 255, 255);
            SelectionBackgroundOption =
                    createOption(name, "SelectionBackground", 82, 131, 194);
            SelectionForegroundOption =
                    createOption(name, "SelectionForeground", 255, 255, 220);
            HighlightingOption =
                    createOption(name, "Highlighting", 255, 192, 128);
            RegularTextOption =
                    createOption(name, "Text", 0, 0, 0);
            HyperlinkTextOption =
                    createOption(name, "Hyperlink", 60, 139, 255);
            VisitedHyperlinkTextOption =
                    createOption(name, "VisitedHyperlink", 200, 139, 255);
            FooterFillOption =
                    createOption(name, "FooterFillOption", 170, 170, 170);
            SelectionCursorOption =
                    createOption(name, "SelectionCursorOption", 19, 157, 235);
            SelectionCursorFillOption =
                    createOption(name, "SelectionCursorFillOption", 19, 157, 235);
        }
	}
}
