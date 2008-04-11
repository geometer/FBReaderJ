package org.geometerplus.zlibrary.text.view.impl;

import org.geometerplus.zlibrary.text.model.*;

final class ZLTextTreeParagraphCursor extends ZLTextParagraphCursor {
	ZLTextTreeParagraphCursor(ZLTextTreeModel model, int index) {
		super(model, index);	
	}	

	public boolean isLast() {
		final ZLTextTreeModel model = (ZLTextTreeModel)myModel;
		if (myIndex + 1 == model.getParagraphsNumber()) {
			return true;
		}
		ZLTextTreeParagraph current = model.getTreeParagraph(myIndex);
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
		final ZLTextTreeParagraph parent = model.getTreeParagraph(myIndex).getParent();
		int index = myIndex - 1;
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

		if (myIndex + 1 == model.getParagraphsNumber()) {
			return null;
		}
		ZLTextTreeParagraph current = model.getTreeParagraph(myIndex);
		if (current.hasChildren() && current.isOpen()) {
			return cursor(model, myIndex + 1);
		}

		ZLTextTreeParagraph parent = current.getParent();
		while ((parent != null) && (current.isLastChild())) {
			current = parent;
			parent = current.getParent();
		}
		if (parent != null) {
			int index = myIndex + 1;
			while (model.getTreeParagraph(index).getParent() != parent) {
				++index;
			}
			return cursor(model, index);
		}
		return null;
	}
}
