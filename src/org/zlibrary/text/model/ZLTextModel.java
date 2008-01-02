package org.zlibrary.text.model;

import org.zlibrary.core.util.ZLTextBuffer;
import org.zlibrary.core.image.ZLImageMap;

import org.zlibrary.text.model.impl.ZLTextForcedControlEntry;

public interface ZLTextModel {
	int getParagraphsNumber();
	ZLTextParagraph getParagraph(int index);

	void addControl(byte textKind, boolean isStart);
	void addText(char[] text);
	void addText(char[] text, int offset, int length);
	void addText(ZLTextBuffer buffer);

	//void addControl(ZLTextForcedControlEntry entry);
	void addHyperlinkControl(byte textKind, String label);
	void addImage(String id, ZLImageMap imageMap, short vOffset);
	//void addFixedHSpace(short length);
}
