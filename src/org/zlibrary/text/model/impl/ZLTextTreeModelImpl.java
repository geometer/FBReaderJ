package org.zlibrary.text.model.impl;

import java.util.ArrayList;

import org.zlibrary.text.model.ZLTextTreeModel;
import org.zlibrary.text.model.ZLTextTreeParagraph;

public class ZLTextTreeModelImpl extends ZLTextModelImpl implements ZLTextTreeModel {
	private final ArrayList<ZLTextTreeParagraph> myParagraphs = new ArrayList<ZLTextTreeParagraph>();
	private final ZLTextTreeParagraphImpl myRoot;
	
	public ZLTextTreeModelImpl() {
		myRoot = new ZLTextTreeParagraphImpl(null, this);
		myRoot.open(true);
	}
	
	public final int getParagraphsNumber() {
		return myParagraphs.size();
	}

	public final ZLTextTreeParagraph getParagraph(int index) {
		return myParagraphs.get(index);
	}

	void increaseLastParagraphSize() {
		((ZLTextTreeParagraphImpl)myParagraphs.get(myParagraphs.size() - 1)).addEntry();
	}

	public ZLTextTreeParagraph createParagraph(ZLTextTreeParagraph parent) {
		onParagraphCreation();
		if (parent == null) {
			parent = myRoot;
		}
		ZLTextTreeParagraph tp = new ZLTextTreeParagraphImpl(parent, this);
		myParagraphs.add(tp);
		return tp;
	}
	
	public void removeParagraph(int index) {
		ZLTextTreeParagraph p = (ZLTextTreeParagraph)this.getParagraph(index);
		p.removeFromParent();
		myParagraphs.remove(index);
	}
}
