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

package org.geometerplus.zlibrary.text.model;

public interface ZLTextParagraph {
	interface Entry {
		byte TEXT = 1;
		byte IMAGE = 2;
		byte CONTROL = 3;
		byte FORCED_CONTROL = 4;
		byte FIXED_HSPACE = 5;
	}

	interface EntryIterator {
		byte getType();

		char[] getTextData();
		int getTextOffset();
		int getTextLength();

		byte getControlKind();
		boolean getControlIsStart();

		byte getHyperlinkType();
		String getHyperlinkId();

		ZLImageEntry getImageEntry();

		short getFixedHSpaceLength();

		boolean hasNext();
		void next();
	}

	public EntryIterator iterator();

	interface Kind {
		byte TEXT_PARAGRAPH = 0;
		byte EMPTY_LINE_PARAGRAPH = 1;
		byte BEFORE_SKIP_PARAGRAPH = 2;
		byte AFTER_SKIP_PARAGRAPH = 3;
		byte END_OF_SECTION_PARAGRAPH = 4;
		byte END_OF_TEXT_PARAGRAPH = 5;
	};

	byte getKind();
}
