package org.fbreader.bookmodel;

import java.util.HashMap;
import java.util.Map;

import org.zlibrary.text.model.ZLTextTreeModel;
import org.zlibrary.text.model.ZLTextTreeParagraph;
import org.zlibrary.text.model.impl.ZLTextTreeModelImpl;

public class ContentsModel extends ZLTextTreeModelImpl implements ZLTextTreeModel{
	private Map<ZLTextTreeParagraph, Integer> myReferenceByParagraph = 
		new HashMap<ZLTextTreeParagraph, Integer>();
	
	public int getReference(ZLTextTreeParagraph paragraph) {
		return myReferenceByParagraph.containsKey(paragraph) ? 
				myReferenceByParagraph.get(paragraph) : -1;
	}
	
	public void setReference(ZLTextTreeParagraph paragraph, int reference) {
		myReferenceByParagraph.put(paragraph, reference);
	}
	
}
