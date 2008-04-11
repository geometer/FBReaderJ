package org.geometerplus.zlibrary.text.model;

import java.util.ArrayList;

import org.geometerplus.zlibrary.core.util.ZLTextBuffer;
import org.geometerplus.zlibrary.core.image.ZLImageMap;

import org.geometerplus.zlibrary.text.model.impl.ZLTextForcedControlEntry;
import org.geometerplus.zlibrary.text.model.impl.ZLTextMark;

public interface ZLTextModel {
	int getParagraphsNumber();
	ZLTextParagraph getParagraph(int index);

	void addControl(byte textKind, boolean isStart);
	void addText(char[] text);
	void addText(char[] text, int offset, int length);
	void addText(ZLTextBuffer buffer);

	void addControl(ZLTextForcedControlEntry entry);
	void addHyperlinkControl(byte textKind, String label);
	void addImage(String id, ZLImageMap imageMap, short vOffset);
	void addFixedHSpace(short length);

	void selectParagraph(int index);
	void removeAllMarks();
	ZLTextMark getFirstMark();
	ZLTextMark getLastMark();
	ZLTextMark getNextMark(ZLTextMark position);
	ZLTextMark getPreviousMark(ZLTextMark position);

	ArrayList getMarks();

	int getParagraphTextLength(int index);
	
	void search(final String text, int startIndex, int endIndex, boolean ignoreCase);
}
