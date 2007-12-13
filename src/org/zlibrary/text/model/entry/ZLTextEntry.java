package org.zlibrary.text.model.entry;

public interface ZLTextEntry extends ZLTextParagraphEntry {
	char[] getData();
	int getDataOffset();
	int getDataLength();
}
