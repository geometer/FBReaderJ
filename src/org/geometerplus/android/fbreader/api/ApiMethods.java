/*
 * This code is in the public domain.
 */

package org.geometerplus.android.fbreader.api;

interface ApiMethods {
	// fbreader information
	int GET_FBREADER_VERSION = 1;

	// library information

	// network library information

	// bookmarks information

	// preferences
	int GET_OPTION_GROUPS = 401;
	int GET_OPTION_NAMES = 402;
	int GET_OPTION_VALUE = 403;
	int SET_OPTION_VALUE = 404;

	// book information
	int GET_BOOK_LANGUAGE = 501;
	int GET_BOOK_TITLE = 502;
	int GET_BOOK_AUTHORS = 503;
	int GET_BOOK_TAGS = 504;
	int GET_BOOK_FILE_NAME = 505;
	int GET_BOOK_HASH = 506;

	// text information
	int GET_PARAGRAPHS_NUMBER = 601;
	int GET_ELEMENTS_NUMBER = 602;
	int GET_PARAGRAPH_TEXT = 603;

	// page information
	int GET_PAGE_START = 701;
	int GET_PAGE_END = 702;
	int IS_PAGE_END_OF_TEXT = 703;
	int IS_PAGE_END_OF_SECTION = 704;

	// view management
	int SET_PAGE_START = 801;
	int HIGHLIGHT_AREA = 802;
	int CLEAR_HIGHLIGHTING = 803;
}
