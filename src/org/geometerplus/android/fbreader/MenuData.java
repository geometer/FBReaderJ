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
import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.android.fbreader.api.MenuNode;
import org.geometerplus.android.util.DeviceType;

public abstract class MenuData {
	private static List<MenuNode> ourNodes;
	private static final Map<String,ZLIntegerOption> ourNodeOptions =
		new HashMap<String,ZLIntegerOption>();
	private static final Map<String,Integer> ourDefaultValues =
			new HashMap<String,Integer>();
	
	private static void addNode(MenuNode m) {
		if (!ourDefaultValues.containsKey(code(m))) {
			ourDefaultValues.put(code(m), ourDefaultValues.size());
		}
		ourNodes.add(m);
	}

	private static synchronized List<MenuNode> allTopLevelNodes() {
		if (ourNodes == null) {
			ourNodes = new ArrayList<MenuNode>();
			addNode(new MenuNode.Item(ActionCode.SHOW_LIBRARY, R.drawable.ic_menu_library));
			if (DeviceType.Instance() == DeviceType.YOTA_PHONE) {
				addNode(new MenuNode.Item(ActionCode.YOTA_SWITCH_TO_BACK_SCREEN, R.drawable.ic_menu_p2b));
				//ourNodes.add(new MenuNode.Item(ActionCode.YOTA_SWITCH_TO_FRONT_SCREEN, R.drawable.ic_menu_p2b));
			}
			addNode(new MenuNode.Item(ActionCode.SHOW_NETWORK_LIBRARY, R.drawable.ic_menu_networklibrary));
			addNode(new MenuNode.Item(ActionCode.SHOW_TOC, R.drawable.ic_menu_toc));
			addNode(new MenuNode.Item(ActionCode.SHOW_BOOKMARKS, R.drawable.ic_menu_bookmarks));
			addNode(new MenuNode.Item(ActionCode.SWITCH_TO_NIGHT_PROFILE, R.drawable.ic_menu_night));
			addNode(new MenuNode.Item(ActionCode.SWITCH_TO_DAY_PROFILE, R.drawable.ic_menu_day));
			addNode(new MenuNode.Item(ActionCode.SEARCH, R.drawable.ic_menu_search));
			addNode(new MenuNode.Item(ActionCode.SHARE_BOOK));
			addNode(new MenuNode.Item(ActionCode.SHOW_PREFERENCES));
			addNode(new MenuNode.Item(ActionCode.SHOW_BOOK_INFO));
			final MenuNode.Submenu orientations = new MenuNode.Submenu("screenOrientation");
			orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_SYSTEM));
			orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_SENSOR));
			orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT));
			orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE));
			if (ZLibrary.Instance().supportsAllOrientations()) {
				orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT));
				orientations.Children.add(new MenuNode.Item(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE));
			}
			addNode(orientations);
			addNode(new MenuNode.Item(ActionCode.INCREASE_FONT));
			addNode(new MenuNode.Item(ActionCode.DECREASE_FONT));
			addNode(new MenuNode.Item(ActionCode.INSTALL_PLUGINS));
			addNode(new MenuNode.Item(ActionCode.OPEN_WEB_HELP));
			addNode(new MenuNode.Item(ActionCode.OPEN_START_SCREEN));
			ourNodes = Collections.unmodifiableList(ourNodes);
		}
		return ourNodes;
	}

	private static String code(MenuNode node) {
		final String code = node.Code;
		if ("day".equals(code) || "night".equals(code)) {
			return "dayNight";
		}
		if ("increaseFont".equals(code) || "decreaseFont".equals(code)) {
			return "changeFontSize";
		}
		return code;
	}
	
	private static class MenuComparator implements Comparator<MenuNode> {
		@Override
		public int compare(MenuNode lhs, MenuNode rhs) {
			return nodeOption(code(lhs)).getValue() - nodeOption(code(rhs)).getValue();
		}
	}

	public static ArrayList<String> enabledCodes() {
		final List<MenuNode> allNodes = new ArrayList<MenuNode>(allTopLevelNodes());
		Collections.<MenuNode>sort(allNodes, new MenuComparator());
		final ArrayList<String> codes = new ArrayList<String>();
		for (MenuNode m : allNodes) {
			if (m.Code.equals("night") || m.Code.equals("decreaseFont")) {
				continue; //duplicate nodes
			}
			codes.add(code(m));
		}
		return codes;
	}
	
	public static ArrayList<String> disabledCodes() {
		final List<MenuNode> allNodes = allTopLevelNodes();
		final ArrayList<String> codes = new ArrayList<String>();
		for (MenuNode node : allNodes) {
			int v = nodeOption(code(node)).getValue();
			if (v < 0) {
				codes.add(code(node));
			}
		}
		return codes;
	}
	
	public static int iconId(String code) {
		final List<MenuNode> allNodes = allTopLevelNodes();
		for (MenuNode node : allNodes) {
			final String c = code(node);
			if (node instanceof MenuNode.Item && code.equals(c) && (((MenuNode.Item) node)).IconId != null) {
				return (((MenuNode.Item) node)).IconId;
			}
		}
		return -1;
	}

	public static synchronized List<MenuNode> topLevelNodes() {
		final List<MenuNode> allNodes = new ArrayList<MenuNode>(allTopLevelNodes());
		Collections.<MenuNode>sort(allNodes, new MenuComparator());
		final List<MenuNode> activeNodes = new ArrayList<MenuNode>(allNodes.size());
		for (MenuNode m : allNodes) {
			int v = nodeOption(code(m)).getValue();
			if (v >= 0) {
				activeNodes.add(m);
			}
		}
		return activeNodes;
	}

	public static ZLIntegerOption nodeOption(String code) {
		synchronized (ourNodeOptions) {
			ZLIntegerOption option = ourNodeOptions.get(code);
			if (option == null) {
				option = new ZLIntegerOption("Menu", code, ourDefaultValues.get(code));
				ourNodeOptions.put(code, option);
			}
			return option;
		}
	}
}
