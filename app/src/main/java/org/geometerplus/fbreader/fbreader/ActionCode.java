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

public interface ActionCode {
	String SHOW_LIBRARY = "library";
	String SHOW_PREFERENCES = "preferences";
	String SHOW_BOOK_INFO = "bookInfo";
	String SHOW_TOC = "toc";
	String SHOW_BOOKMARKS = "bookmarks";
	String SHOW_NETWORK_LIBRARY = "networkLibrary";

	String SWITCH_TO_NIGHT_PROFILE = "night";
	String SWITCH_TO_DAY_PROFILE = "day";

	String SHARE_BOOK = "shareBook";

	String SEARCH = "search";
	String FIND_PREVIOUS = "findPrevious";
	String FIND_NEXT = "findNext";
	String CLEAR_FIND_RESULTS = "clearFindResults";

	String SET_TEXT_VIEW_MODE_VISIT_HYPERLINKS = "hyperlinksOnlyMode";
	String SET_TEXT_VIEW_MODE_VISIT_ALL_WORDS = "dictionaryMode";

	String TURN_PAGE_BACK = "previousPage";
	String TURN_PAGE_FORWARD = "nextPage";

	String MOVE_CURSOR_UP = "moveCursorUp";
	String MOVE_CURSOR_DOWN = "moveCursorDown";
	String MOVE_CURSOR_LEFT = "moveCursorLeft";
	String MOVE_CURSOR_RIGHT = "moveCursorRight";

	String VOLUME_KEY_SCROLL_FORWARD = "volumeKeyScrollForward";
	String VOLUME_KEY_SCROLL_BACK = "volumeKeyScrollBackward";
	String SHOW_MENU = "menu";
	String SHOW_NAVIGATION = "navigate";

	String GO_BACK = "goBack";
	String EXIT = "exit";
	String SHOW_CANCEL_MENU = "cancelMenu";

	String SET_SCREEN_ORIENTATION_SYSTEM = "screenOrientationSystem";
	String SET_SCREEN_ORIENTATION_SENSOR = "screenOrientationSensor";
	String SET_SCREEN_ORIENTATION_PORTRAIT = "screenOrientationPortrait";
	String SET_SCREEN_ORIENTATION_LANDSCAPE = "screenOrientationLandscape";
	String SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT = "screenOrientationReversePortrait";
	String SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE = "screenOrientationReverseLandscape";

	String INCREASE_FONT = "increaseFont";
	String DECREASE_FONT = "decreaseFont";

	String DISPLAY_BOOK_POPUP = "displayBookPopup";
	String PROCESS_HYPERLINK = "processHyperlink";

	String SELECTION_SHOW_PANEL = "selectionShowPanel";
	String SELECTION_HIDE_PANEL = "selectionHidePanel";
	String SELECTION_CLEAR = "selectionClear";
	String SELECTION_COPY_TO_CLIPBOARD = "selectionCopyToClipboard";
	String SELECTION_SHARE = "selectionShare";
	String SELECTION_TRANSLATE = "selectionTranslate";
	String SELECTION_BOOKMARK = "selectionBookmark";

	String OPEN_VIDEO = "video";

	String HIDE_TOAST = "hideToast";
	String OPEN_START_SCREEN = "openStartScreen";
	String OPEN_WEB_HELP = "help";
	String INSTALL_PLUGINS = "plugins";
}
