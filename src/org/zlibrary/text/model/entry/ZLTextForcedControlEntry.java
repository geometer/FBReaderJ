package org.zlibrary.text.model.entry;

public interface ZLTextForcedControlEntry extends ZLTextParagraphEntry{
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
