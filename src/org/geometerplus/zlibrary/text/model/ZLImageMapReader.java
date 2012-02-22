/*
 * Copyright (C) 2011-2012 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.image.ZLImage;

class ZLImageMapReader {
	private final CachedCharStorageRO myStorage;

	public ZLImageMapReader(String directoryName, String fileExtension, int blocksNumber) {
		myStorage = new CachedCharStorageRO(directoryName, fileExtension, blocksNumber);
	}

	public ZLImage readImage(int index, int offset) {
		char[] data = myStorage.block(index);
		while (offset == data.length || data[offset] == '\000') {
			data = myStorage.block(++index);
			offset = 0;
		}
		final byte kind = (byte)data[offset];
		final boolean multi = ((byte)(data[offset] >> 8)) != 0;
		if (multi) {
			return readMultiImage(index, offset + 1, data);
		} else {
			return readSingleImage(index, offset + 1, data, kind);
		}
	}

	private ZLImage readMultiImage(int index, int offset, char[] data) {
		// TODO: implement
		return null;
	}

	private ZLImage readSingleImage(int index, int offset, char[] data, byte kind) {
		// TODO: implement
		return null;
	}
}
