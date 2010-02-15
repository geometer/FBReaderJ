/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.text.model;

public final class ZLTextForcedControlEntry {
	final static short SUPPORTS_LEFT_INDENT = 1 << 0;
	final static short SUPPORTS_RIGHT_INDENT = 1 << 1;
	final static short SUPPORTS_ALIGNMENT_TYPE = 1 << 2;
	
	private short myMask;
	private short myLeftIndent;
	private short myRightIndent;
	private byte myAlignmentType;
		
	public ZLTextForcedControlEntry() {
	}

	short getMask() {
		return myMask;
	};
	
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
