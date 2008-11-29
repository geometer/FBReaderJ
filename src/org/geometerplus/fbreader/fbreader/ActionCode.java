/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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
	String SHOW_COLLECTION = "showLibrary";
	String SHOW_OPTIONS = "preferences";
	String UNDO = "undo";
	String REDO = "redo";
	String SHOW_CONTENTS = "toc";
	String SEARCH = "search";
	String FIND_PREVIOUS = "findPrevious";
	String FIND_NEXT = "findNext";
	String TOUCH_SCROLL_FORWARD = "touchScrollForward";
	String TOUCH_SCROLL_BACKWARD = "touchScrollBackward";
	String TRACKBALL_SCROLL_FORWARD = "trackballScrollForward";
	String TRACKBALL_SCROLL_BACKWARD = "trackballScrollBackward";
	String SCROLL_TO_HOME = "gotoHome";
	String SCROLL_TO_START_OF_TEXT = "gotoSectionStart";
	String SCROLL_TO_END_OF_TEXT = "gotoSectionEnd";
	String CANCEL = "cancel";
	String INCREASE_FONT = "increaseFont";
	String DECREASE_FONT = "decreaseFont";
	String SHOW_HIDE_POSITION_INDICATOR = "toggleIndicator";
	String TOGGLE_FULLSCREEN = "toggleFullscreen";
	String FULLSCREEN_ON = "onFullscreen";
	String ADD_BOOK = "addBook";
	String SHOW_BOOK_INFO = "bookInfo";
	String SHOW_HELP = "showHelp";
	String ROTATE_SCREEN = "rotate";
	String SHOW_LAST_BOOKS = "showRecent";
	String QUIT = "quit";
	String OPEN_PREVIOUS_BOOK = "previousBook";
	String GOTO_NEXT_TOC_SECTION = "nextTOCSection";
	String GOTO_PREVIOUS_TOC_SECTION = "previousTOCSection";
	String COPY_SELECTED_TEXT_TO_CLIPBOARD = "copyToClipboard";
	String CLEAR_SELECTION = "clearSelection";
	String OPEN_SELECTED_TEXT_IN_DICTIONARY = "openInDictionary";
};
