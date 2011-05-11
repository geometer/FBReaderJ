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

import java.util.*;

import org.geometerplus.zlibrary.core.application.ZLApplication;

final class ZLTextSelectionModel {
	final static class BoundElement extends ZLTextPosition {
		boolean Exists;
		int ParagraphIndex;
		int ElementIndex;
		int CharIndex;

		void copyFrom(BoundElement original) {
			Exists = original.Exists;
			ParagraphIndex = original.ParagraphIndex;
			ElementIndex = original.ElementIndex;
			CharIndex = original.CharIndex;
		}

		public boolean equalsTo(BoundElement be) {
			return
				(Exists == be.Exists) &&
				(ParagraphIndex == be.ParagraphIndex) &&
				(ElementIndex == be.ElementIndex) &&
				(CharIndex == be.CharIndex);
		}

		public final int getParagraphIndex() {
			return ParagraphIndex;
		}

		public final int getElementIndex() {
			return ElementIndex;
		}

		public final int getCharIndex() {
			return CharIndex;
		}
	};

	final static class Range {
		final BoundElement Left = new BoundElement();
		final BoundElement Right = new BoundElement();

		Range(BoundElement left, BoundElement right) {
			Left.copyFrom(left);
			Right.copyFrom(right);
		}
	};
}
