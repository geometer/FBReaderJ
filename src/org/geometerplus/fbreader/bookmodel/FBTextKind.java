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

package org.geometerplus.fbreader.bookmodel;

public interface FBTextKind {
	byte REGULAR = 0;
	byte TITLE = 1;
	byte SECTION_TITLE = 2;
	byte POEM_TITLE = 3;
	byte SUBTITLE = 4;
	byte ANNOTATION = 5;
	byte EPIGRAPH = 6;
	byte STANZA = 7;
	byte VERSE = 8;
	byte PREFORMATTED = 9;
	byte IMAGE = 10;
	//byte END_OF_SECTION = 11;
	byte CITE = 12;
	byte AUTHOR = 13;
	byte DATE = 14;
	byte INTERNAL_HYPERLINK = 15;
	byte FOOTNOTE = 16;
	byte EMPHASIS = 17;
	byte STRONG = 18;
	byte SUB = 19;
	byte SUP = 20;
	byte CODE = 21;
	byte STRIKETHROUGH = 22;
	//byte CONTENTS_TABLE_ENTRY = 23;
	//byte LIBRARY_AUTHOR_ENTRY = 24;
	//byte LIBRARY_BOOK_ENTRY = 25;
	//byte RECENT_BOOK_LIST = 26;
	byte ITALIC = 27;
	byte BOLD = 28;
	byte DEFINITION = 29;
	byte DEFINITION_DESCRIPTION = 30;
	byte H1 = 31;
	byte H2 = 32;
	byte H3 = 33;
	byte H4 = 34;
	byte H5 = 35;
	byte H6 = 36;
	byte EXTERNAL_HYPERLINK = 37;
};
