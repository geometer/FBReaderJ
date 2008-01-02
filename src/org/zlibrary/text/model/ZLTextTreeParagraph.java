package org.zlibrary.text.model;

public interface ZLTextTreeParagraph extends ZLTextParagraph {
	boolean isOpen();
	void open(boolean o);
	void openTree();

	int getDepth();

	ZLTextTreeParagraph getParent();

	boolean hasChildren();
	boolean isLastChild();
	int getFullSize();	

	void removeFromParent();
}
