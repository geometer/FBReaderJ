package org.zlibrary.text.model.impl;

import java.util.ArrayList;

import org.zlibrary.text.model.ZLTextTreeModel;
import org.zlibrary.text.model.ZLTextTreeParagraph;

public class ZLTextTreeModelImpl extends ZLTextModelImpl implements ZLTextTreeModel {
	private final ArrayList<ZLTextTreeParagraphImpl> myParagraphs =
		new ArrayList<ZLTextTreeParagraphImpl>();
	private final ZLTextTreeParagraphImpl myRoot;
	
	public ZLTextTreeModelImpl() {
		super(4096);
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
		final ArrayList<ZLTextTreeParagraphImpl> paragraphs = myParagraphs;
		paragraphs.get(paragraphs.size() - 1).addEntry();
	}

	public ZLTextTreeParagraph createParagraph(ZLTextTreeParagraph parent) {
		createParagraph();
		if (parent == null) {
			parent = myRoot;
		}
		ZLTextTreeParagraphImpl tp = new ZLTextTreeParagraphImpl(parent, this);
		myParagraphs.add(tp);
		return tp;
	}
	
	public void removeParagraph(int index) {
		ZLTextTreeParagraph p = getParagraph(index);
		p.removeFromParent();
		myParagraphs.remove(index);
	}
}
