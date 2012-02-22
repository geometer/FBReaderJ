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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLSingleImage;
import org.geometerplus.zlibrary.core.util.MimeType;

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
		final short mimeLength = (short)data[offset++];
		final String mime = new String(data, offset, mimeLength);
		offset += mimeLength;

		switch (kind) {
			case ZLSingleImage.Kind.BASE64_ENCODED_IMAGE:
			case ZLSingleImage.Kind.REGULAR_IMAGE:
			{
				final int dataSize = (int)data[offset] + (((int)data[offset + 1]) << 16);
				offset += 2;
				final String path = myStorage.fileName(index);
				return new ZLFileImage(
					MimeType.get(mime), ZLFile.createFileByPath(path), offset * 2, dataSize * 2
				);
			}
			case ZLSingleImage.Kind.FILE_IMAGE:
			{
				final int fileOffset = (int)data[offset] + (((int)data[offset + 1]) << 16);
				offset += 2;
				final int fileSize = (int)data[offset] + (((int)data[offset + 1]) << 16);
				offset += 2;
				final short pathLength = (short)data[offset++];
				final String path = new String(data, offset, pathLength);
				offset += pathLength;
				return new ZLFileImage(
					MimeType.get(mime), ZLFile.createFileByPath(path), fileOffset, fileSize
				);
			}
		}

		return null;
	}
}
