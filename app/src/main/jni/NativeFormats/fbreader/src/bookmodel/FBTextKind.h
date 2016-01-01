/*
 * Copyright (C) 2004-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#ifndef __FBTEXTKIND_H__
#define __FBTEXTKIND_H__

enum FBTextKind {
	// please, don't change these numbers
	// add new text kinds at end of this enumeration
	//
	// all the values MUST be in the range 0..127
	REGULAR = 0,
	TITLE = 1,
	SECTION_TITLE = 2,
	POEM_TITLE = 3,
	SUBTITLE = 4,
	ANNOTATION = 5,
	EPIGRAPH = 6,
	STANZA = 7,
	VERSE = 8,
	PREFORMATTED = 9,
	IMAGE = 10,
	END_OF_SECTION = 11,
	CITE = 12,
	AUTHOR = 13,
	DATEKIND = 14,
	INTERNAL_HYPERLINK = 15,
	FOOTNOTE = 16,
	EMPHASIS = 17,
	STRONG = 18,
	SUB = 19,
	SUP = 20,
	CODE = 21,
	STRIKETHROUGH = 22,
	CONTENTS_TABLE_ENTRY = 23,
	//LIBRARY_AUTHOR_ENTRY = 24,
	//LIBRARY_BOOK_ENTRY = 25,
	LIBRARY_ENTRY = 25,
	//RECENT_BOOK_LIST = 26,
	ITALIC = 27,
	BOLD = 28,
	DEFINITION = 29,
	DEFINITION_DESCRIPTION = 30,
	H1 = 31,
	H2 = 32,
	H3 = 33,
	H4 = 34,
	H5 = 35,
	H6 = 36,
	EXTERNAL_HYPERLINK = 37,
	//BOOK_HYPERLINK = 38,

	XHTML_TAG_P = 51,
};

#endif /* __FBTEXTKIND_H__ */
