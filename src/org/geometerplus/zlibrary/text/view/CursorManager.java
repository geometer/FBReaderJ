/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.lang.ref.WeakReference;
import java.util.*;

import org.geometerplus.zlibrary.text.model.ZLTextModel;

final class CursorManager {
	private final ZLTextModel myModel;
	final ExtensionElementManager ExtensionManager;

	CursorManager(ZLTextModel model, ExtensionElementManager extManager) {
		myModel = model;
		ExtensionManager = extManager;
	}

	private final HashMap<Integer,WeakReference<ZLTextParagraphCursor>> myMap =
		new HashMap<Integer,WeakReference<ZLTextParagraphCursor>>();

	ZLTextParagraphCursor cursor(int index) {
		final WeakReference<ZLTextParagraphCursor> ref = myMap.get(index);
		ZLTextParagraphCursor result = ref != null ? ref.get() : null;
		if (result == null) {
			result = new ZLTextParagraphCursor(this, myModel, index);
			myMap.put(index, new WeakReference<ZLTextParagraphCursor>(result));
		}
		return result;
	}

	void clear() {
		myMap.clear();
	}
}
