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
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.fbreader.ActionCode;

import org.geometerplus.android.fbreader.api.MenuNode;
import org.geometerplus.android.util.DeviceType;

public abstract class MenuData {
	private static List<MenuNode> ourNodes;
	private static final Map<String,ZLBooleanOption> ourNodeOptions =
		new HashMap<String,ZLBooleanOption>();

	private static synchronized List<MenuNode> allTopLevelNodes() {
		if (ourNodes == null) {
			ourNodes = new ArrayList<MenuNode>();
			ourNodes.add(new MenuNode.Item(ActionCode.SHOW_LIBRARY, R.drawable.ic_menu_library));
			if (DeviceType.Instance() == DeviceType.YOTA_PHONE) {
				ourNodes.add(new MenuNode.Item(ActionCode.YOTA_SWITCH_TO_BACK_SCREEN, R.drawable.ic_menu_p2b));
				//ourNodes.add(new MenuNode.Item(ActionCode.YOTA_SWITCH_TO_FRONT_SCREEN, R.drawable.ic_menu_p2b));
			}
			ourNodes.add(new MenuNode.Item(ActionCode.SHOW_NETWORK_LIBRARY, R.drawable.ic_menu_networklibrary));
			ourNodes.add(new MenuNode.Item(ActionCode.SHOW_TOC, R.drawable.ic_menu_toc));
			ourNodes.add(new MenuNode.Item(ActionCode.SHOW_BOOKMARKS, R.drawable.ic_menu_bookmarks));
			ourNodes.add(new MenuNode.Item(ActionCode.SWITCH_TO_NIGHT_PROFILE, R.drawable.ic_menu_night));
			ourNodes.add(new MenuNode.Item(ActionCode.SWITCH_TO_DAY_PROFILE, R.drawable.ic_menu_day));
			ourNodes.add(new MenuNode.Item(ActionCode.SEARCH, R.drawable.ic_menu_search));
			ourNodes.add(new MenuNode.Item(ActionCode.SHARE_BOOK));
			ourNodes.add(new MenuNode.Item(ActionCode.SHOW_PREFERENCES));
			ourNodes.add(new MenuNode.Item(ActionCode.SHOW_BOOK_INFO));
			final MenuNode.Submenu orientations = new MenuNode.Submenu("screenOrientation");
			orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_SYSTEM));
			orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_SENSOR));
			orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT));
			orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE));
			if (ZLibrary.Instance().supportsAllOrientations()) {
				orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT));
				orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE));
			}
			ourNodes.add(orientations);
			ourNodes.add(new MenuNode.Item(ActionCode.INCREASE_FONT));
			ourNodes.add(new MenuNode.Item(ActionCode.DECREASE_FONT));
			ourNodes.add(new MenuNode.Item(ActionCode.INSTALL_PLUGINS));
			ourNodes.add(new MenuNode.Item(ActionCode.OPEN_WEB_HELP));
			ourNodes.add(new MenuNode.Item(ActionCode.OPEN_START_SCREEN));
			ourNodes = Collections.unmodifiableList(ourNodes);
		}
		return ourNodes;
	}

	private static String code(MenuNode node) {
		final String code = node.Code;
		if ("day".equals(code) || "night".equals(code)) {
			return "dayNight";
		}
		return code;
	}

	public static List<String> allCodes() {
		final List<MenuNode> allNodes = allTopLevelNodes();
		final List<String> codes = new ArrayList<String>(allNodes.size());
		for (MenuNode node : allNodes) {
			final String c = code(node);
			if (codes.isEmpty() || !c.equals(codes.get(codes.size() - 1))) {
				codes.add(c);
			}
		}
		return codes;
	}

	public static synchronized List<MenuNode> topLevelNodes() {
		final List<MenuNode> allNodes = allTopLevelNodes();
		final List<MenuNode> activeNodes = new ArrayList<MenuNode>(allNodes.size());
		for (MenuNode node : allNodes) {
			if (nodeOption(code(node)).getValue()) {
				activeNodes.add(node);
			}
		}
		return activeNodes;
	}

	public static ZLBooleanOption nodeOption(String code) {
		synchronized (ourNodeOptions) {
			ZLBooleanOption option = ourNodeOptions.get(code);
			if (option == null) {
				option = new ZLBooleanOption("Menu", code, true);
				ourNodeOptions.put(code, option);
			}
			return option;
		}
	}
}
