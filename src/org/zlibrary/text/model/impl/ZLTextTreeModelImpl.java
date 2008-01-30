package org.zlibrary.text.model.impl;

import java.util.ArrayList;

import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextTreeModel;
import org.zlibrary.text.model.ZLTextTreeParagraph;

public class ZLTextTreeModelImpl extends ZLTextModelImpl implements ZLTextTreeModel {
	private final ArrayList myParagraphs = new ArrayList();
	private final ZLTextTreeParagraphImpl myRoot;
	
	public ZLTextTreeModelImpl() {
		super(4096);
		myRoot = new ZLTextTreeParagraphImpl(null, this);
		myRoot.open(true);
	}
	
	public final int getParagraphsNumber() {
		return myParagraphs.size();
	}

	public final ZLTextParagraph getParagraph(int index) {
		return (ZLTextParagraph)myParagraphs.get(index);
	}

	public final ZLTextTreeParagraph getTreeParagraph(int index) {
		return (ZLTextTreeParagraph)myParagraphs.get(index);
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
}
