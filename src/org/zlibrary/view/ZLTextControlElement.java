package org.zlibrary.view;

import org.zlibrary.model.entry.ZLTextParagraphEntry;
import org.zlibrary.model.entry.ZLTextControlEntry;

public interface ZLTextControlElement extends ZLTextElement {
	
	Kind getKind();
	ZLTextControlEntry getEntry();
	byte getTextKind();
	boolean isStart();
}


