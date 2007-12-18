package org.zlibrary.text.model.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.zlibrary.text.model.ZLTextTreeParagraph;

final class ZLTextTreeParagraphImpl extends ZLTextParagraphImpl implements ZLTextTreeParagraph {
	private boolean myIsOpen;
	private	int myDepth;
	private	ZLTextTreeParagraph myParent;
	private	ArrayList<ZLTextTreeParagraph> myChildren = null;
	
	ZLTextTreeParagraphImpl(ZLTextTreeParagraph parent, ZLTextModelImpl model) {
		super(model);
		myParent = parent;
		if (parent != null) {
			((ZLTextTreeParagraphImpl)parent).addChild(this);
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

	/*
	public List<ZLTextTreeParagraph> children() {
		return (myChildren != null) ?
			Collections.unmodifiableList(myChildren) :
			Collections.<ZLTextTreeParagraph>emptyList();
	}
	*/

	public ZLTextTreeParagraph getLastChild() {
		if (!hasChildren()) {
			return null;
		}
		return myChildren.get(myChildren.size() - 1);
	}
	
	public int getFullSize() {
		int size = 1;
		if (myChildren != null) {
			for (ZLTextTreeParagraph child : myChildren) {
				size += child.getFullSize();
			}
		}
		return size;
	}

	public void removeFromParent() {		
		if (myParent != null) {
			((ZLTextTreeParagraphImpl)myParent).myChildren.remove(this);
		}
	}

	private void addChild(ZLTextTreeParagraph child) {
		if (myChildren == null) {
			myChildren = new ArrayList<ZLTextTreeParagraph>();
		}
		myChildren.add(child);
	}
}
