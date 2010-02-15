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

package org.geometerplus.zlibrary.text.view;

public class ZLTextControlElement extends ZLTextElement {
	private final static ZLTextControlElement[] myStartElements = new ZLTextControlElement[256];
	private final static ZLTextControlElement[] myEndElements = new ZLTextControlElement[256];

	static ZLTextControlElement get(byte kind, boolean isStart) {
		ZLTextControlElement[] elements = isStart ? myStartElements : myEndElements;
		ZLTextControlElement element = elements[kind & 0xFF];
		if (element == null) {
			element = new ZLTextControlElement(kind, isStart);
			elements[kind & 0xFF] = element;
		}
		return element;
	}

	public final byte Kind;
	public final boolean IsStart;

	protected ZLTextControlElement(byte kind, boolean isStart) {
		Kind = kind;
		IsStart = isStart;
	}
}
