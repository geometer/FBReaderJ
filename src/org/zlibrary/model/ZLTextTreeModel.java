package org.zlibrary.model;

public interface ZLTextTreeModel extends ZLTextModel {
	ZLTextTreeParagraph createParagraph(ZLTextTreeParagraph parent);
	void removeParagraph(int index);
	
	//void search(String text, int startIndex, int endIndex, boolean ignoreCase);
	//void selectParagraph(int index);
}
