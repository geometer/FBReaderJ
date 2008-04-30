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

package org.geometerplus.zlibrary.text.view.impl;

import org.geometerplus.zlibrary.core.application.ZLApplication;

class ZLTextSelectionModel {
	private final ZLTextViewImpl myView;
	private final ZLApplication myApplication;

	private boolean myIsActive;
	private boolean myIsEmpty = true;
	private boolean myDoUpdate;
	private boolean myTextIsUpToDate = true;

	ZLTextSelectionModel(ZLTextViewImpl view, ZLApplication application) {
		myView = view;
		myApplication = application;
	}

	void activate(int x, int y) {
		// TODO: implement
	}

	boolean extendTo(int x, int y) {
		// TODO: implement
		return false;
	}

  void update() {
		// TODO: implement
	}

  void deactivate() {
		// TODO: implement
	}

  void clear() {
		// TODO: implement
	}

  String getText() {
		// TODO: implement
		return null;
	}

  boolean isEmpty() {
		// TODO: implement
		return false;
	}

	class BoundElement {
		boolean Exists;
		int ParagraphNumber;
		int TextElementNumber;
		int CharNumber;

		//bool operator == (const BoundElement &element) const;
		//bool operator != (const BoundElement &element) const;
	};
}
