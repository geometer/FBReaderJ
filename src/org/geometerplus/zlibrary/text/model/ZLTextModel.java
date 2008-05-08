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

package org.geometerplus.zlibrary.text.model;

import java.util.ArrayList;

import org.geometerplus.zlibrary.core.util.ZLTextBuffer;
import org.geometerplus.zlibrary.core.image.ZLImageMap;

public interface ZLTextModel {
	int getParagraphsNumber();
	ZLTextParagraph getParagraph(int index);

	void addControl(byte textKind, boolean isStart);
	void addText(char[] text);
	void addText(char[] text, int offset, int length);
	void addText(ZLTextBuffer buffer);

	void addControl(ZLTextForcedControlEntry entry);
	void addHyperlinkControl(byte textKind, String label);
	void addImage(String id, ZLImageMap imageMap, short vOffset);
	void addFixedHSpace(short length);

	void selectParagraph(int index);
	void removeAllMarks();
	ZLTextMark getFirstMark();
	ZLTextMark getLastMark();
	ZLTextMark getNextMark(ZLTextMark position);
	ZLTextMark getPreviousMark(ZLTextMark position);

	ArrayList getMarks();

	int getParagraphTextLength(int index);
	
	void search(final String text, int startIndex, int endIndex, boolean ignoreCase);
}
