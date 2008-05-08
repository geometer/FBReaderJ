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

package org.geometerplus.zlibrary.text.model;

import org.geometerplus.zlibrary.core.util.ZLArrayUtils;

public final class ZLTextPlainModel extends ZLTextModelImpl {
	private byte[] myParagraphKinds = new byte[INITIAL_CAPACITY];

	public ZLTextPlainModel(int dataBlockSize) {
		super(dataBlockSize);
	}

	public int getParagraphsNumber() {
		return myParagraphsNumber;
	}

	public ZLTextParagraph getParagraph(int index) {
		final byte kind = myParagraphKinds[index];
		return (kind == ZLTextParagraph.Kind.TEXT_PARAGRAPH) ?
			new ZLTextParagraphImpl(this, index) :
			new ZLTextSpecialParagraphImpl(kind, this, index);
	}

	void extend() {
		super.extend();
		final int size = myParagraphKinds.length;
		myParagraphKinds = ZLArrayUtils.createCopy(myParagraphKinds, size, size << 1);
	}

	public void createParagraph(byte kind) {
		final int index = myParagraphsNumber;
		createParagraph();
		myParagraphKinds[index] = kind;
	}
}
