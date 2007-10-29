package org.zlibrary.model;

import org.zlibrary.model.entry.ZLTextParagraphEntry;

public interface ZLTextForcedControlEntry extends ZLTextParagraphEntry{
    boolean leftIndentSupported();
	
	short leftIndent();
	
	void setLeftIndent(short leftIndent);
   
	boolean rightIndentSupported();
	
	short rightIndent();
	
	void setRightIndent(short rightIndent);

	boolean alignmentTypeSupported();
	
	ZLTextAlignmentType alignmentType();
	
	void setAlignmentType(ZLTextAlignmentType alignmentType);

}
