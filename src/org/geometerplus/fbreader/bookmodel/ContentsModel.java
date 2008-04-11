package org.geometerplus.fbreader.bookmodel;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.text.model.ZLTextTreeModel;
import org.geometerplus.zlibrary.text.model.ZLTextTreeParagraph;
import org.geometerplus.zlibrary.text.model.impl.ZLTextTreeModelImpl;

public class ContentsModel extends ZLTextTreeModelImpl implements ZLTextTreeModel{
	private final HashMap myReferenceByParagraph = new HashMap();
	
	public int getReference(ZLTextTreeParagraph paragraph) {
		Integer num = (Integer)myReferenceByParagraph.get(paragraph);
		return (num != null) ? num.intValue() : -1;
	}
	
	public void setReference(ZLTextTreeParagraph paragraph, int reference) {
		myReferenceByParagraph.put(paragraph, reference);
	}
}
