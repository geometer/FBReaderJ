/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader;

import java.util.*;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.fbreader.ActionCode;

import org.geometerplus.android.fbreader.api.MenuNode;

public abstract class MenuData {
	private static List<MenuNode> ourNodes;

	private static void addToplevelNode(MenuNode node) {
		ourNodes.add(node);
	}

	public static synchronized List<MenuNode> topLevelNodes() {
		if (ourNodes == null) {
			ourNodes = new ArrayList<MenuNode>();
			addToplevelNode(new MenuNode.Item(ActionCode.SHOW_LIBRARY, R.drawable.ic_menu_library));
			addToplevelNode(new MenuNode.Item(ActionCode.SHOW_NETWORK_LIBRARY, R.drawable.ic_menu_networklibrary));
			addToplevelNode(new MenuNode.Item(ActionCode.SHOW_TOC, R.drawable.ic_menu_toc));
			addToplevelNode(new MenuNode.Item(ActionCode.SHOW_BOOKMARKS, R.drawable.ic_menu_bookmarks));
			addToplevelNode(new MenuNode.Item(ActionCode.SWITCH_TO_NIGHT_PROFILE, R.drawable.ic_menu_night));
			addToplevelNode(new MenuNode.Item(ActionCode.SWITCH_TO_DAY_PROFILE, R.drawable.ic_menu_day));
			addToplevelNode(new MenuNode.Item(ActionCode.SEARCH, R.drawable.ic_menu_search));
			addToplevelNode(new MenuNode.Item(ActionCode.SHARE_BOOK));
			addToplevelNode(new MenuNode.Item(ActionCode.SHOW_PREFERENCES));
			addToplevelNode(new MenuNode.Item(ActionCode.SHOW_BOOK_INFO));
			final MenuNode.Submenu orientations = new MenuNode.Submenu("screenOrientation");
			orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_SYSTEM));
			orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_SENSOR));
			orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT));
			orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE));
			if (ZLibrary.Instance().supportsAllOrientations()) {
				orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT));
				orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE));
			}
			addToplevelNode(orientations);
			addToplevelNode(new MenuNode.Item(ActionCode.INCREASE_FONT));
			addToplevelNode(new MenuNode.Item(ActionCode.DECREASE_FONT));
			addToplevelNode(new MenuNode.Item(ActionCode.SHOW_NAVIGATION));
			addToplevelNode(new MenuNode.Item(ActionCode.INSTALL_PLUGINS));
			addToplevelNode(new MenuNode.Item(ActionCode.OPEN_WEB_HELP));
			addToplevelNode(new MenuNode.Item(ActionCode.OPEN_START_SCREEN));
			ourNodes = Collections.unmodifiableList(ourNodes);
		}
		return ourNodes;
	}
}
