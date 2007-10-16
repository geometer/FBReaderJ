package org.zlibrary.view;

import org.zlibrary.model.ZLTextParagraph;

public interface ZLTextElement {
	
	public enum Kind {
		WORD_ELEMENT,
		CONTROL_ELEMENT,
	};
	
	public abstract Kind getKind();

}
