/*
 * Copyright (C) 2011 Geometer Plus <contact@geometerplus.com>
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


public class ZLTextNativeModel implements ZLTextModel {

	public int findParagraphByTextLength(int length) {
		return 0;
	}

	public ZLTextMark getFirstMark() {
		return null;
	}

	public String getId() {
		return null;
	}

	public String getLanguage() {
		return null;
	}

	public ZLTextMark getLastMark() {
		return null;
	}

	public List<ZLTextMark> getMarks() {
		return null;
	}

	public ZLTextMark getNextMark(ZLTextMark position) {
		return null;
	}

	public ZLTextParagraph getParagraph(int index) {
		return null;
	}

	public int getParagraphsNumber() {
		return 0;
	}

	public ZLTextMark getPreviousMark(ZLTextMark position) {
		return null;
	}

	public int getTextLength(int index) {
		return 0;
	}

	public void removeAllMarks() {
	}

	public int search(String text, int startIndex, int endIndex, boolean ignoreCase) {
		return 0;
	}
}
