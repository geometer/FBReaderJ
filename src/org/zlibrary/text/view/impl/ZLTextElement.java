package org.zlibrary.text.view.impl;

class ZLTextElement {
	
	enum Kind {
		WORD_ELEMENT,
		CONTROL_ELEMENT,
	};
	
	public Kind getKind() {
		return Kind.WORD_ELEMENT;
	}
}
