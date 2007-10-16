package org.zlibrary.view;

import org.zlibrary.model.entry.ZLTextParagraphEntry;
import org.zlibrary.model.entry.ZLTextControlEntry;

public abstract class ZLTextControlElement implements ZLTextElement {
	
	private ZLTextParagraphEntry myEntry;

	public abstract Kind getKind();
	public abstract ZLTextControlEntry getEntry();
	public abstract byte getTextKind();
	public abstract boolean isStart();
}


