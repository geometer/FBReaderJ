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
	String SWITCH_PROFILE = "switchProfile";

	String SEARCH = "search";
	String FIND_PREVIOUS = "findPrevious";
	String FIND_NEXT = "findNext";
	String CLEAR_FIND_RESULTS = "clearFindResults";

	String PREV_PAGE = "prevPage";
	String NEXT_PAGE = "nextPage";
	String PREV_LINE = "prevLine";
	String NEXT_LINE = "nextLine";
	String PREV_LINK = "prevLink";
	String NEXT_LINK = "nextLink";

	String SHOW_NAVIGATION = "navigate";
	String BACK = "back";
	String ROTATE = "rotate";
	String INCREASE_FONT = "increaseFont";
	String DECREASE_FONT = "decreaseFont";
	String TOGGLE_FULLSCREEN = "toggleFullscreen";
	String FULLSCREEN_ON = "onFullscreen";
	String QUIT = "quit";

	String INITIATE_COPY = "initiateCopy";
	String COPY_SELECTED_TEXT_TO_CLIPBOARD = "copyToClipboard";
	String CLEAR_SELECTION = "clearSelection";
	String TRANSLATE = "translate";

	String FOLLOW_HYPERLINK = "followHyperlink";

	String TAP_ZONES = "tapZones";
	String TAP_ZONE_SELECT_ACTION = "tapZoneSelectAction";
	String TAP_ZONE_ADD = "tapZoneAdd";
	String TAP_ZONE_DELETE = "tapZoneDelete";
	String TAP_ZONES_SAVE = "tapZonesSave";
	String TAP_ZONES_CANCEL = "tapZonesCancel";

	String DEFAULT = "default";
	String NOTHING = "nothing";
};
