package org.zlibrary.view;

import org.zlibrary.model.ZLTextModel;
import org.zlibrary.model.ZLTextParagraph;

public interface ZLTextParagraphCursor {

	boolean isFirst();
	boolean isLast();
	boolean isEndOfSection();
	
	int getParagraphLength();
	int index();

	ZLTextParagraphCursor previous();
	ZLTextParagraphCursor next();

	ZLTextElement getElement(int index);
	ZLTextParagraph getParagraph();
}
