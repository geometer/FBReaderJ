package org.fbreader.fbreader;

public enum ActionCode {
	// please, don't change these numbers
	// add new action id's at end of this enumeration
	NONE(0),// = 0,
	SHOW_COLLECTION(1),// = 1,
	SHOW_OPTIONS(2),// = 2,
	UNDO(3),// = 3,
	REDO(4),// = 4,
	SHOW_CONTENTS(5),// = 5,
	SEARCH(6),// = 6,
	FIND_PREVIOUS(7),// = 7,
	FIND_NEXT(8),// = 8,
	LARGE_SCROLL_FORWARD(9),// = 9,
	LARGE_SCROLL_BACKWARD(10),// = 10,
	SMALL_SCROLL_FORWARD(11),// = 11,
	SMALL_SCROLL_BACKWARD(12),// = 12,
	MOUSE_SCROLL_FORWARD(13),// = 13,
	MOUSE_SCROLL_BACKWARD(14),// = 14,
	SCROLL_TO_HOME(15),// = 15,
	SCROLL_TO_START_OF_TEXT(16),// = 16,
	SCROLL_TO_END_OF_TEXT(17),// = 17,
	CANCEL(18),// = 18,
	INCREASE_FONT(19),// = 19,
	DECREASE_FONT(20),// = 20,
	SHOW_HIDE_POSITION_INDICATOR(21),// = 21,
	TOGGLE_FULLSCREEN(22),// = 22,
	FULLSCREEN_ON(23),// = 23,
	ADD_BOOK(24),// = 24,
	SHOW_BOOK_INFO(25),// = 25,
	SHOW_HELP(26),// = 26,
	ROTATE_SCREEN(27),// = 27,
	SHOW_LAST_BOOKS(28),// = 28,
	QUIT(29),// = 29,
	OPEN_PREVIOUS_BOOK(30),// = 30,
	FINGER_TAP_SCROLL_FORWARD(31),// = 31,
	FINGER_TAP_SCROLL_BACKWARD(32),// = 32,
	GOTO_NEXT_TOC_SECTION(33),// = 33,
	GOTO_PREVIOUS_TOC_SECTION(34),// = 34,
	COPY_SELECTED_TEXT_TO_CLIPBOARD(35),// = 35,
	CLEAR_SELECTION(36),// = 36,
	OPEN_SELECTED_TEXT_IN_DICTIONARY(37);// = 37,
	
	private final int myCode;
	
	ActionCode(int code) {
		myCode = code;
	}
	
	public int getCode() {
		return myCode;
	}
};
