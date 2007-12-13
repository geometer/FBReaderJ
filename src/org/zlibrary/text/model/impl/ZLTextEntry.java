package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.ZLTextParagraph;

public interface ZLTextEntry extends ZLTextParagraph.Entry {
	char[] getData();
	int getDataOffset();
	int getDataLength();
}
