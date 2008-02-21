package org.zlibrary.text.model;

import java.util.*;
import org.zlibrary.core.util.*;

public interface ZLTextTreeParagraph extends ZLTextParagraph {
	boolean isOpen();
	void open(boolean o);
	void openTree();

	int getDepth();

	ZLTextTreeParagraph getParent();

	boolean hasChildren();
	boolean isLastChild();
	ArrayList getChildren();
	int getFullSize();	

	void removeFromParent();
}
