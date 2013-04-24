package org.geometerplus.android.fbreader;

import java.util.ArrayList;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.ui.android.R;

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
		return root;
	}
}
