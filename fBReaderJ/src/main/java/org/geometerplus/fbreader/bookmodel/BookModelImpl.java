/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.bookmodel;

import java.util.HashMap;

import org.geometerplus.zlibrary.core.image.*;

import org.geometerplus.zlibrary.text.model.*;

import org.geometerplus.fbreader.book.Book;

abstract class BookModelImpl extends BookModel {
	protected CharStorage myInternalHyperlinks;
	protected final HashMap<String,ZLImage> myImageMap = new HashMap<String,ZLImage>();
	protected final HashMap<String,ZLTextModel> myFootnotes = new HashMap<String,ZLTextModel>();

	BookModelImpl(Book book) {
		super(book);
	}

	@Override
	protected Label getLabelInternal(String id) {
		final int len = id.length();
		final int size = myInternalHyperlinks.size();

		for (int i = 0; i < size; ++i) {
			final char[] block = myInternalHyperlinks.block(i);
			for (int offset = 0; offset < block.length; ) {
				final int labelLength = (int)block[offset++];
				if (labelLength == 0) {
					break;
				}
				final int idLength = (int)block[offset + labelLength];
				if ((labelLength != len) || !id.equals(new String(block, offset, labelLength))) {
					offset += labelLength + idLength + 3;
					continue;
				}
				offset += labelLength + 1;
				final String modelId = (idLength > 0) ? new String(block, offset, idLength) : null;
				offset += idLength;
				final int paragraphNumber = (int)block[offset] + (((int)block[offset + 1]) << 16);
				return new Label(modelId, paragraphNumber);
			}
		}
		return null;
	}

	public void addImage(String id, ZLImage image) {
		myImageMap.put(id, image);
	}
}
