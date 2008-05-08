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

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

public class ZLTextTreeModel extends ZLTextModelImpl {
	private final ArrayList myParagraphs = new ArrayList();
	private final ZLTextTreeParagraphImpl myRoot;
	
	public ZLTextTreeModel() {
		super(4096);
		myRoot = new ZLTextTreeParagraphImpl(null, this);
		myRoot.open(true);
	}
	
	public final int getParagraphsNumber() {
		return myParagraphs.size();
	}

	public final ZLTextParagraph getParagraph(int index) {
		final ArrayList paragraphs = myParagraphs;
		if (index >= paragraphs.size()) {
			index = paragraphs.size() - 1;
		}
		return (ZLTextParagraph)paragraphs.get(index);
	}

	public final ZLTextTreeParagraph getTreeParagraph(int index) {
		final ArrayList paragraphs = myParagraphs;
		if (index >= paragraphs.size()) {
			index = paragraphs.size() - 1;
		}
		return (ZLTextTreeParagraph)paragraphs.get(index);
	}

	public final ZLTextTreeParagraph createParagraph(ZLTextTreeParagraph parent) {
		createParagraph();
		if (parent == null) {
			parent = myRoot;
		}
		ZLTextTreeParagraphImpl tp = new ZLTextTreeParagraphImpl(parent, this);
		myParagraphs.add(tp);
		return tp;
	}
	
	public final void removeParagraph(int index) {
		ZLTextTreeParagraph p = getTreeParagraph(index);
		p.removeFromParent();
		myParagraphs.remove(index);
	}

	public final void clear() {
		super.clear();
		myParagraphs.clear();
	}
}
