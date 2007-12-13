package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.ZLTextParagraph;

public interface ZLTextForcedControlEntry extends ZLTextParagraph.Entry {
	boolean isLeftIndentSupported();
	short getLeftIndent();
	void setLeftIndent(short leftIndent);

	boolean isRightIndentSupported();
	short getRightIndent();
	void setRightIndent(short rightIndent);

	boolean isAlignmentTypeSupported();
	byte getAlignmentType();
	void setAlignmentType(byte alignmentType);
}
