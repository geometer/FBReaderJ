package org.zlibrary.text.model.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.zlibrary.text.model.ZLTextTreeParagraph;

class ZLTextTreeParagraphImpl extends ZLTextParagraphImpl implements ZLTextTreeParagraph {
	private boolean myIsOpen;
	private	int myDepth;
	private	ZLTextTreeParagraph myParent;
	private	List<ZLTextTreeParagraph> myChildren = new LinkedList<ZLTextTreeParagraph>();
	
	ZLTextTreeParagraphImpl() {}
	
	ZLTextTreeParagraphImpl(ZLTextTreeParagraph parent) {
		myIsOpen = false;
		myParent = parent;
		if (parent != null) {
			((ZLTextTreeParagraphImpl)parent).addChild(this);
			myDepth = parent.getDepth() + 1;
		} else {
			myDepth = 0;
		}
	}
	
	public 	Kind kind() {
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

	public List<ZLTextTreeParagraph> getChildren() {
		return Collections.unmodifiableList(myChildren);
	}
	
	public int getFullSize() {
		int size = 1;
		for (ZLTextTreeParagraph  child: getChildren()) {
			size += child.getFullSize();
		}
		return size;
	}

	public void removeFromParent() {		
		if (myParent != null) {
			myChildren.clear();
		}
	}

	private void addChild(ZLTextTreeParagraph child) {
		this.myChildren.add(child);
	}
}
