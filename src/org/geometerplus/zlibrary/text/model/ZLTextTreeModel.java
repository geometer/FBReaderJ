package org.geometerplus.zlibrary.text.model;

public interface ZLTextTreeModel extends ZLTextModel {
	ZLTextTreeParagraph getTreeParagraph(int index);
	ZLTextTreeParagraph createParagraph(ZLTextTreeParagraph parent);
	void removeParagraph(int index);
	void clear();
	
	//void search(String text, int startIndex, int endIndex, boolean ignoreCase);
	//void selectParagraph(int index);
}
