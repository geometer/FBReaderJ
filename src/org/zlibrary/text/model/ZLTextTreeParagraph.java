package org.zlibrary.text.model;

import java.util.List;

public interface ZLTextTreeParagraph extends ZLTextParagraph {
	boolean isOpen();
	void open(boolean o);
	void openTree();
	int getDepth();
	ZLTextTreeParagraph getParent();
	List<ZLTextTreeParagraph> getChildren();
	int getFullSize();	
	void removeFromParent();
}
