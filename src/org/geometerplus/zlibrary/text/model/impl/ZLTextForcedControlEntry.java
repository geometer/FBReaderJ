/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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
package org.geometerplus.zlibrary.text.model.impl;

import org.geometerplus.zlibrary.text.model.ZLTextParagraph;

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
