package org.zlibrary.text.model.impl;

import java.util.*;
import org.zlibrary.core.util.*;
import org.zlibrary.text.model.ZLTextTreeParagraph;

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
			System.out.print("here->"+parent.isOpen());
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
