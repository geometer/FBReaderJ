package org.zlibrary.model.impl;

import org.zlibrary.model.ZLTextParagraph;
import org.zlibrary.model.ZLTextPlainModel;

class ZLTextPlainModelImpl extends ZLTextModelImpl implements ZLTextPlainModel {
	
	public void createParagraph(ZLTextParagraph.Kind kind) {
		ZLTextParagraph paragraph = (kind == ZLTextParagraph.Kind.TEXT_PARAGRAPH) ? new ZLTextParagraphImpl() : new ZLTextSpecialParagraphImpl(kind);
		addParagraphInternal(paragraph);	
	}
}
