/*
 * Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com>
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

import java.util.ArrayList;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

public class MenuItemData {
	public static enum MenuType {
		ACTION,
		SUBMENU
	}
	public final MenuItemData.MenuType Type;
	public final String Code;
	public final Integer IconId;
	
	private MenuItemData(MenuItemData.MenuType t, String c, Integer i) {
		Type = t;
		Code = c;
		IconId = i;
	}
	
	public final ArrayList<MenuItemData> Children = new ArrayList<MenuItemData>();
	
	public MenuItemData findByCode(String code) {
		if (code == null) {
			return null;
		}
		if (code.equals(Code)) {
			return this;
		}
		for (MenuItemData el : Children) {
			MenuItemData res = el.findByCode(code);
			if (res != null) {
				return res;
			}
		}
		return null;
	}
	
	public static MenuItemData getRoot() {
		MenuItemData root = new MenuItemData(MenuType.SUBMENU, "root", null);
		root.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.SHOW_LIBRARY, R.drawable.ic_menu_library));
		root.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.SHOW_NETWORK_LIBRARY, R.drawable.ic_menu_networklibrary));
		root.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.SHOW_TOC, R.drawable.ic_menu_toc));
		root.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.SHOW_BOOKMARKS, R.drawable.ic_menu_bookmarks));
		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
		if (zlibrary.isYotaPhone()) {
			root.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.YOTA_SWITCH_TO_BACK_SCREEN, R.drawable.ic_menu_p2b));
			root.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.YOTA_SWITCH_TO_FRONT_SCREEN, R.drawable.ic_menu_p2b));
		}
		root.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.SWITCH_TO_NIGHT_PROFILE, R.drawable.ic_menu_night));
		root.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.SWITCH_TO_DAY_PROFILE, R.drawable.ic_menu_day));
		root.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.SEARCH, R.drawable.ic_menu_search));
		root.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.SHARE_BOOK, R.drawable.ic_menu_search));
		root.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.SHOW_PREFERENCES, null));
		root.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.SHOW_BOOK_INFO, null));
		MenuItemData orient = new MenuItemData(MenuType.SUBMENU, "screenOrientation", null);
		orient.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.SET_SCREEN_ORIENTATION_SYSTEM, null));
		orient.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.SET_SCREEN_ORIENTATION_SENSOR, null));
		orient.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT, null));
		orient.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE, null));
		if (ZLibrary.Instance().supportsAllOrientations()) {
			orient.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT, null));
			orient.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE, null));
		}
		root.Children.add(orient);
		root.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.INCREASE_FONT, null));
		root.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.DECREASE_FONT, null));
		root.Children.add(new MenuItemData(MenuType.ACTION, ActionCode.OPEN_WEB_HELP, null));
		return root;
	}
}
