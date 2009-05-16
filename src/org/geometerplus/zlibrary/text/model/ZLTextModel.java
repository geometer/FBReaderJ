/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

import java.util.ArrayList;

import org.geometerplus.zlibrary.core.image.ZLImageMap;

public interface ZLTextModel {
	String getId();

	int getParagraphsNumber();
	ZLTextParagraph getParagraph(int index);

	void createParagraph(byte kind);

	void addControl(byte textKind, boolean isStart);
	void addText(char[] text);
	void addText(char[] text, int offset, int length);

	void addControl(ZLTextForcedControlEntry entry);
	void addHyperlinkControl(byte textKind, String label);
	void addImage(String id, ZLImageMap imageMap, short vOffset);
	void addFixedHSpace(short length);

	void removeAllMarks();
	ZLTextMark getFirstMark();
	ZLTextMark getLastMark();
	ZLTextMark getNextMark(ZLTextMark position);
	ZLTextMark getPreviousMark(ZLTextMark position);

	ArrayList getMarks();

	int getParagraphTextLength(int index);
	
	int search(final String text, int startIndex, int endIndex, boolean ignoreCase);
}
