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

public interface ActionCode {
	String SHOW_LIBRARY = "library";
	String SHOW_PREFERENCES = "preferences";
	String SHOW_BOOK_INFO = "bookInfo";
	String SHOW_CONTENTS = "toc";
	String SHOW_BOOKMARKS = "bookmarks";
	String SHOW_NETWORK_LIBRARY = "networkLibrary";

	String SWITCH_TO_NIGHT_PROFILE = "night";
	String SWITCH_TO_DAY_PROFILE = "day";

	String SEARCH = "search";
	String FIND_PREVIOUS = "findPrevious";
	String FIND_NEXT = "findNext";
	String CLEAR_FIND_RESULTS = "clearFindResults";

	String VOLUME_KEY_SCROLL_FORWARD = "volumeKeyScrollForward";
	String VOLUME_KEY_SCROLL_BACKWARD = "volumeKeyScrollBackward";
	String TRACKBALL_SCROLL_FORWARD = "trackballScrollForward";
	String TRACKBALL_SCROLL_BACKWARD = "trackballScrollBackward";
	String SHOW_NAVIGATION = "navigate";
	String CANCEL = "cancel";
	String ROTATE = "rotate";
	String INCREASE_FONT = "increaseFont";
	String DECREASE_FONT = "decreaseFont";
	String TOGGLE_FULLSCREEN = "toggleFullscreen";
	String FULLSCREEN_ON = "onFullscreen";
	String QUIT = "quit";
	String COPY_SELECTED_TEXT_TO_CLIPBOARD = "copyToClipboard";
	String CLEAR_SELECTION = "clearSelection";
	String OPEN_SELECTED_TEXT_IN_DICTIONARY = "openInDictionary";
	String FOLLOW_HYPERLINK = "followHyperlink";
};
