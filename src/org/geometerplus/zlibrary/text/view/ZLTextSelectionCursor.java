/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.library.ZLibrary;

public enum ZLTextSelectionCursor {
	None,
	Left,
	Right;

	private static int ourHeight;
	private static int ourWidth;
	private static int ourAccent;

	private static void init() {
		if (ourHeight == 0) {
			final int dpi = ZLibrary.Instance().getDisplayDPI();
			ourAccent = dpi / 12;
			ourWidth = dpi / 6;
			ourHeight = dpi / 4;
		}
	}

	static int getHeight() {
		init();
		return ourHeight;
	}

	static int getWidth() {
		init();
		return ourWidth;
	}

	static int getAccent() {
		init();
		return ourAccent;
	}
}
