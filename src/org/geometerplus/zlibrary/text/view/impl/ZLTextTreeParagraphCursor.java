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

import org.geometerplus.zlibrary.text.model.*;

final class ZLTextTreeParagraphCursor extends ZLTextParagraphCursor {
	ZLTextTreeParagraphCursor(ZLTextTreeModel model, int index) {
		super(model, index);	
	}	

	public boolean isLast() {
		final ZLTextTreeModel model = (ZLTextTreeModel)myModel;
		if (Index + 1 == model.getParagraphsNumber()) {
			return true;
		}
		ZLTextTreeParagraph current = model.getTreeParagraph(Index);
		if (current.isOpen() && current.hasChildren()) {
			return false;
		}
		ZLTextTreeParagraph parent = current.getParent();
		while (parent != null) {
			if (!current.isLastChild()) {
				return false;
			}
			current = parent;
			parent = current.getParent();
		}
		return true;
	}

	public ZLTextParagraphCursor previous() {
		if (isFirst()) {
			return null;
		}

		final ZLTextTreeModel model = (ZLTextTreeModel)myModel;
		final ZLTextTreeParagraph parent = model.getTreeParagraph(Index).getParent();
		int index = Index - 1;
		ZLTextTreeParagraph newTreeParagraph = model.getTreeParagraph(index);
		if (newTreeParagraph != parent) {
			ZLTextTreeParagraph lastNotOpen = newTreeParagraph;
			for (ZLTextTreeParagraph p = newTreeParagraph.getParent(); p != parent; p = p.getParent()) {
				if (!p.isOpen()) {
					lastNotOpen = p;
				}
			}
			while (model.getParagraph(index) != lastNotOpen) {
				--index;
			}
		}
		return cursor(model, index);
	}

	public ZLTextParagraphCursor next() {
		final ZLTextTreeModel model = (ZLTextTreeModel)myModel;

		if (Index + 1 == model.getParagraphsNumber()) {
			return null;
		}
		ZLTextTreeParagraph current = model.getTreeParagraph(Index);
		if (current.hasChildren() && current.isOpen()) {
			return cursor(model, Index + 1);
		}

		ZLTextTreeParagraph parent = current.getParent();
		while ((parent != null) && (current.isLastChild())) {
			current = parent;
			parent = current.getParent();
		}
		if (parent != null) {
			int index = Index + 1;
			while (model.getTreeParagraph(index).getParent() != parent) {
				++index;
			}
			return cursor(model, index);
		}
		return null;
	}
}
