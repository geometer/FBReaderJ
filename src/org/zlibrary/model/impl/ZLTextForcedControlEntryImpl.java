package org.zlibrary.model.impl;

import org.zlibrary.model.ZLTextAlignmentType;
import org.zlibrary.model.entry.ZLTextForcedControlEntry;

class ZLTextForcedControlEntryImpl implements  ZLTextForcedControlEntry {
    private final static int SUPPORT_LEFT_INDENT = 1;
    private final static int SUPPORT_RIGHT_INDENT = 2;
    private final static int SUPPORT_ALIGNMENT_TYPE = 4;
	
	private int myMask;
    private short myLeftIndent;
    private short myRightIndent;   
	private ZLTextAlignmentType myAlignmentType;
    	
	ZLTextForcedControlEntryImpl() {
		myMask = 0;
	}
	
    /*	ZLTextForcedControlEntry::ZLTextForcedControlEntry(char *address) {
		myMask = *address;
		memcpy(&myLeftIndent, address + 1, sizeof(short));
		memcpy(&myRightIndent, address + 1 + sizeof(short), sizeof(short));
		myAlignmentType = (ZLTextAlignmentType)*(address + 1 + 2 * sizeof(short));
	}*/

	
	ZLTextForcedControlEntryImpl(short[] address) {
		myMask = address[0];
		myLeftIndent = (short)address[1];
		myRightIndent = (short)address[2];
		//myAlignmentType = (ZLTextAlignmentType)(address[3]);		
	}
    
	public boolean leftIndentSupported() {
		int a = myMask & SUPPORT_LEFT_INDENT;
		return (a != 0);
	}
	
	public short leftIndent() {
		return myLeftIndent;
	};
	
	public void setLeftIndent(short leftIndent) {
		myLeftIndent = leftIndent; myMask |= SUPPORT_LEFT_INDENT;
	}
    
	//todo  
	public boolean rightIndentSupported() {
		int a = myMask & SUPPORT_RIGHT_INDENT; 
		return (a != 0);	
	}
	
	public short rightIndent() {
		return myRightIndent;
	}
	
	public void setRightIndent(short rightIndent) {
		myRightIndent = rightIndent; myMask |= SUPPORT_RIGHT_INDENT;
	}

	//todo
	public boolean alignmentTypeSupported() {
		int a = myMask & SUPPORT_ALIGNMENT_TYPE;
		return (a != 0);	
	}
	
	public ZLTextAlignmentType alignmentType() {
		return myAlignmentType;
	}
	
	public void setAlignmentType(ZLTextAlignmentType alignmentType) {
		myAlignmentType = alignmentType; myMask |= SUPPORT_ALIGNMENT_TYPE;
	}
}
