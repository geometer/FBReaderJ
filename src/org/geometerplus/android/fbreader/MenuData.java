/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.ui.android.R;

public abstract class MenuData {
	public static MenuNode.Submenu getRoot() {
		final MenuNode.Submenu root = new MenuNode.Submenu("root");
		root.Children.add(new MenuNode.Item(ActionCode.SHOW_LIBRARY, R.drawable.ic_menu_library));
		root.Children.add(new MenuNode.Item(ActionCode.SHOW_NETWORK_LIBRARY, R.drawable.ic_menu_networklibrary));
		root.Children.add(new MenuNode.Item(ActionCode.SHOW_TOC, R.drawable.ic_menu_toc));
		root.Children.add(new MenuNode.Item(ActionCode.SHOW_BOOKMARKS, R.drawable.ic_menu_bookmarks));
		root.Children.add(new MenuNode.Item(ActionCode.SWITCH_TO_NIGHT_PROFILE, R.drawable.ic_menu_night));
		root.Children.add(new MenuNode.Item(ActionCode.SWITCH_TO_DAY_PROFILE, R.drawable.ic_menu_day));
		root.Children.add(new MenuNode.Item(ActionCode.SEARCH, R.drawable.ic_menu_search));
		root.Children.add(new MenuNode.Item(ActionCode.SHARE_BOOK));
		root.Children.add(new MenuNode.Item(ActionCode.SHOW_PREFERENCES));
		root.Children.add(new MenuNode.Item(ActionCode.SHOW_BOOK_INFO));
		MenuNode.Submenu orient = new MenuNode.Submenu("screenOrientation");
		orient.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_SYSTEM));
		orient.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_SENSOR));
		orient.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT));
		orient.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE));
		if (ZLibrary.Instance().supportsAllOrientations()) {
			orient.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT));
			orient.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE));
		}
		root.Children.add(orient);
		root.Children.add(new MenuNode.Item(ActionCode.INCREASE_FONT));
		root.Children.add(new MenuNode.Item(ActionCode.DECREASE_FONT));
		root.Children.add(new MenuNode.Item(ActionCode.INSTALL_PLUGINS));
		root.Children.add(new MenuNode.Item(ActionCode.OPEN_WEB_HELP));
		return root;
	}
}
