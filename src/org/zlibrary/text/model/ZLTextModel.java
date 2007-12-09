package org.zlibrary.text.model;

import java.util.ArrayList;
import java.util.Map;

import org.zlibrary.core.image.ZLImage;
import org.zlibrary.text.model.entry.ZLTextForcedControlEntry;

public interface ZLTextModel {
    
    int getParagraphsNumber();
    ZLTextParagraph getParagraph(int index);

    void addParagraphInternal(ZLTextParagraph paragraph);
    void removeParagraphInternal(int index);

    void addControl(byte textKind, boolean isStart);
    void addText(char[] text);
    void addText(ArrayList<char[]> text);


	void addControl(ZLTextForcedControlEntry entry);
	void addHyperlinkControl(byte textKind, String label);
	void addImage(String id, Map<String, ZLImage> imageMap, short vOffset);
	void addFixedHSpace(byte length);

    String dump();    
}
