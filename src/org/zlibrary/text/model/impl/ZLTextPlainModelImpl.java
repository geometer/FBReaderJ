package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextPlainModel;

class ZLTextPlainModelImpl extends ZLTextModelImpl implements ZLTextPlainModel {
	
	public void createParagraph(ZLTextParagraph.Kind kind) {
		ZLTextParagraph paragraph = (kind == ZLTextParagraph.Kind.TEXT_PARAGRAPH) ? new ZLTextParagraphImpl() : new ZLTextSpecialParagraphImpl(kind);
		addParagraphInternal(paragraph);	
	}
}
