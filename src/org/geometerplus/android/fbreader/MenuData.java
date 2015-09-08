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
	private static final Map<String,Integer> ourDefaultValues = new HashMap<String,Integer>();
	private static final Map<String,String> ourConfigCodes = new HashMap<String,String>();
	private static final Set<String> ourAlwaysEnabledCodes = new HashSet<String>();

	private static final String CONFIG_CODE_DAY_NIGHT = "dayNight";
	private static final String CONFIG_CODE_CHANGE_FONT_SIZE = "changeFontSize";

	private enum Status {
		AlwaysEnabled,
		EnabledByDefault,
		DisabledByDefault
	}

	private static void addToplevelNode(MenuNode node) {
		addToplevelNode(node, node.Code, Status.EnabledByDefault);
	}

	private static void addToplevelNode(MenuNode node, String configCode, Status status) {
		ourConfigCodes.put(node.Code, configCode);
		if (status != Status.DisabledByDefault && !ourDefaultValues.containsKey(configCode)) {
			ourDefaultValues.put(configCode, ourDefaultValues.size());
		}
		ourNodes.add(node);
		if (status == Status.AlwaysEnabled) {
			ourAlwaysEnabledCodes.add(configCode);
		}
	}

	private static synchronized List<MenuNode> allTopLevelNodes() {
		if (ourNodes == null) {
			ourNodes = new ArrayList<MenuNode>();
			addToplevelNode(new MenuNode.Item(ActionCode.SHOW_LIBRARY, R.drawable.ic_menu_library));
			if (DeviceType.Instance() == DeviceType.YOTA_PHONE) {
				addToplevelNode(new MenuNode.Item(ActionCode.YOTA_SWITCH_TO_BACK_SCREEN, R.drawable.ic_menu_p2b));
				//addToplevelNode(new MenuNode.Item(ActionCode.YOTA_SWITCH_TO_FRONT_SCREEN, R.drawable.ic_menu_p2b));
			}
			addToplevelNode(new MenuNode.Item(ActionCode.SHOW_NETWORK_LIBRARY, R.drawable.ic_menu_networklibrary));
			addToplevelNode(new MenuNode.Item(ActionCode.SHOW_TOC, R.drawable.ic_menu_toc));
			addToplevelNode(new MenuNode.Item(ActionCode.SHOW_BOOKMARKS, R.drawable.ic_menu_bookmarks));
			addToplevelNode(
				new MenuNode.Item(ActionCode.SWITCH_TO_NIGHT_PROFILE, R.drawable.ic_menu_night),
				CONFIG_CODE_DAY_NIGHT,
				Status.EnabledByDefault
			);
			addToplevelNode(
				new MenuNode.Item(ActionCode.SWITCH_TO_DAY_PROFILE, R.drawable.ic_menu_day),
				CONFIG_CODE_DAY_NIGHT,
				Status.EnabledByDefault
			);
			addToplevelNode(new MenuNode.Item(ActionCode.SEARCH, R.drawable.ic_menu_search));
			addToplevelNode(new MenuNode.Item(ActionCode.SHARE_BOOK));
			addToplevelNode(
				new MenuNode.Item(ActionCode.SHOW_PREFERENCES),
				ActionCode.SHOW_PREFERENCES,
				Status.AlwaysEnabled
			);
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
			addToplevelNode(
				new MenuNode.Item(ActionCode.INCREASE_FONT),
				CONFIG_CODE_CHANGE_FONT_SIZE,
				Status.EnabledByDefault
			);
			addToplevelNode(
				new MenuNode.Item(ActionCode.DECREASE_FONT),
				CONFIG_CODE_CHANGE_FONT_SIZE,
				Status.EnabledByDefault
			);
			addToplevelNode(new MenuNode.Item(ActionCode.INSTALL_PLUGINS));
			addToplevelNode(new MenuNode.Item(ActionCode.OPEN_WEB_HELP));
			addToplevelNode(new MenuNode.Item(ActionCode.OPEN_START_SCREEN));
			addToplevelNode(
				new MenuNode.Item(ActionCode.GOTO_PAGE_NUMBER),
				ActionCode.GOTO_PAGE_NUMBER,
				Status.DisabledByDefault
			);
			ourNodes = Collections.unmodifiableList(ourNodes);
		}
		return ourNodes;
	}

	private static String code(MenuNode node) {
		return ourConfigCodes.get(node.Code);
	}

	private static class MenuComparator implements Comparator<MenuNode> {
		@Override
		public int compare(MenuNode lhs, MenuNode rhs) {
			return nodeOption(code(lhs)).getValue() - nodeOption(code(rhs)).getValue();
		}
	}

	private static class CodeComparator implements Comparator<String> {
		@Override
		public int compare(String lhs, String rhs) {
			return nodeOption(lhs).getValue() - nodeOption(rhs).getValue();
		}
	}

	public static ArrayList<String> enabledCodes() {
		final ArrayList<String> codes = new ArrayList<String>();
		for (MenuNode node : allTopLevelNodes()) {
			final String c = code(node);
			if (!codes.contains(c) && isCodeEnabled(c)) {
				codes.add(c);
			}
		}
		Collections.sort(codes, new CodeComparator());
		return codes;
	}

	public static ArrayList<String> disabledCodes() {
		final ArrayList<String> codes = new ArrayList<String>();
		for (MenuNode node : allTopLevelNodes()) {
			final String c = code(node);
			if (!codes.contains(c) && !isCodeEnabled(c)) {
				codes.add(c);
			}
		}
		return codes;
	}

	public static int configIconId(String itemCode) {
		final List<MenuNode> allNodes = allTopLevelNodes();
		Integer iconId = null;
		for (MenuNode node : allNodes) {
			if (node instanceof MenuNode.Item && itemCode.equals(code(node))) {
				iconId = ((MenuNode.Item)node).IconId;
				break;
			}
		}
		return iconId != null ? iconId : R.drawable.ic_menu_none;
	}

	public static synchronized List<MenuNode> topLevelNodes() {
		final List<MenuNode> allNodes = new ArrayList<MenuNode>(allTopLevelNodes());
		final List<MenuNode> activeNodes = new ArrayList<MenuNode>(allNodes.size());
		for (MenuNode node : allNodes) {
			if (isCodeEnabled(code(node))) {
				activeNodes.add(node);
			}
		}
		Collections.<MenuNode>sort(activeNodes, new MenuComparator());
		return activeNodes;
	}

	public static ZLIntegerOption nodeOption(String code) {
		synchronized (ourNodeOptions) {
			ZLIntegerOption option = ourNodeOptions.get(code);
			if (option == null) {
				final Integer defaultValue = ourDefaultValues.get(code);
				option = new ZLIntegerOption(
					"MainMenu", code, defaultValue != null ? defaultValue : -1
				);
				ourNodeOptions.put(code, option);
			}
			return option;
		}
	}

	public static boolean isCodeAlwaysEnabled(String code) {
		return ourAlwaysEnabledCodes.contains(code);
	}

	private static boolean isCodeEnabled(String code) {
		return ourAlwaysEnabledCodes.contains(code) || nodeOption(code).getValue() >= 0;
	}
}
