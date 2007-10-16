package org.zlibrary.view;

import org.zlibrary.model.ZLTextParagraph;

public interface ZLTextElement {
	
	enum Kind {
		WORD_ELEMENT,
		CONTROL_ELEMENT,
	};
	
	abstract Kind getKind();

}
