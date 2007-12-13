package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.ZLTextAlignmentType;

class ZLTextForcedControlEntryImpl implements ZLTextForcedControlEntry {
	private final static int SUPPORTS_LEFT_INDENT = 1;
	private final static int SUPPORTS_RIGHT_INDENT = 2;
	private final static int SUPPORTS_ALIGNMENT_TYPE = 4;
	
	private int myMask;
	private short myLeftIndent;
	private short myRightIndent;
	private byte myAlignmentType;
		
	ZLTextForcedControlEntryImpl() {
		myMask = 0;
	}
	
	public boolean isLeftIndentSupported() {
		return (myMask & SUPPORTS_LEFT_INDENT) == SUPPORTS_LEFT_INDENT;
	}
	
	public short getLeftIndent() {
		return myLeftIndent;
	};
	
	public void setLeftIndent(short leftIndent) {
		myMask |= SUPPORTS_LEFT_INDENT;
		myLeftIndent = leftIndent;
	}
	
	public boolean isRightIndentSupported() {
		return (myMask & SUPPORTS_RIGHT_INDENT) == SUPPORTS_RIGHT_INDENT;
	}
	
	public short getRightIndent() {
		return myRightIndent;
	}
	
	public void setRightIndent(short rightIndent) {
		myMask |= SUPPORTS_RIGHT_INDENT;
		myRightIndent = rightIndent;
	}

	public boolean isAlignmentTypeSupported() {
		return (myMask & SUPPORTS_ALIGNMENT_TYPE) == SUPPORTS_ALIGNMENT_TYPE;
	}
	
	public byte getAlignmentType() {
		return myAlignmentType;
	}
	
	public void setAlignmentType(byte alignmentType) {
		myMask |= SUPPORTS_ALIGNMENT_TYPE;
		myAlignmentType = alignmentType;
	}
}
