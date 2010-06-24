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

import java.util.List;

public interface ZLTextModel {
	String getId();
	String getLanguage();

	int getParagraphsNumber();
	ZLTextParagraph getParagraph(int index);

	void removeAllMarks();
	ZLTextMark getFirstMark();
	ZLTextMark getLastMark();
	ZLTextMark getNextMark(ZLTextMark position);
	ZLTextMark getPreviousMark(ZLTextMark position);

	List<ZLTextMark> getMarks();

	// text length for paragraphs from 0 to index
	int getTextLength(int index);
	int findParagraphByTextLength(int length);
	
	int search(final String text, int startIndex, int endIndex, boolean ignoreCase);
}
