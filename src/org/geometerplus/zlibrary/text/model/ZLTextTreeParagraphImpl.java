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

final class ZLTextTreeParagraphImpl extends ZLTextParagraphImpl implements ZLTextTreeParagraph {
	private boolean myIsOpen;
	private	int myDepth;
	private	ZLTextTreeParagraphImpl myParent;
	private	ArrayList myChildren = null;
	
	ZLTextTreeParagraphImpl(ZLTextTreeParagraph parent, ZLTextModelImpl model) {
		super(model);
		myParent = (ZLTextTreeParagraphImpl)parent;
		if (parent != null) {
			myParent.addChild(this);
			myDepth = parent.getDepth() + 1;
		}
	}
	
	public byte getKind() {
		return Kind.TREE_PARAGRAPH;
	}
	
	public	boolean isOpen() {
		return myIsOpen;
	}
	
	public	void open(boolean o) {
		myIsOpen = o;
	}
	
	public void openTree() {
		ZLTextTreeParagraph parent = myParent;
		while (parent != null) {
			parent.open(true);
			parent = parent.getParent();
		}	
	}
	
	public int getDepth() {
		return myDepth;
	}
	
	public ZLTextTreeParagraph getParent() {
		return myParent;
	}

	public boolean hasChildren() {
		return (myChildren != null) && !myChildren.isEmpty();
	}

	public int childNumber() {
		return (myChildren != null) ? myChildren.size() : 0;
	}

	public boolean isLastChild() {
		if (myParent == null) {
			return false;
		}
		ArrayList siblings = myParent.myChildren;
		return this == siblings.get(siblings.size() - 1);
	}
	
	public int getFullSize() {
		int size = 1;
		final ArrayList children = myChildren;
		if (children != null) {
			final int length = children.size();
			for (int i = 0; i < length; ++i) {
				size += ((ZLTextTreeParagraph)children.get(i)).getFullSize();
			}
		}
		return size;
	}

	public void removeFromParent() {		
		if (myParent != null) {
			myParent.myChildren.remove(this);
		}
	}
	
	private void addChild(ZLTextTreeParagraph child) {
		if (myChildren == null) {
			myChildren = new ArrayList();
		}
		myChildren.add(child);
	}
}
