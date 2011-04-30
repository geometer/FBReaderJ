/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.plucker;

import java.io.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLSingleImage;
import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.fbreader.formats.pdb.DocDecompressor;

public class DocCompressedFileImage extends ZLSingleImage {
	private final ZLFile myFile;
	private final int myOffset;
	private final int myCompressedSize;
	
	public DocCompressedFileImage(MimeType mimeType, final ZLFile file, final int offset, final int compressedSize) {
		super(mimeType);
		myFile = file;
		myOffset = offset;
		myCompressedSize = compressedSize;
	}

	public String getURI() {
		// TODO: implement
		return null;
	}

	@Override
	public InputStream inputStream() {
		try {
			final InputStream stream = myFile.getInputStream();
			if (stream == null) {
				return null;
			}

			stream.skip(myOffset);
			final byte[] buffer = new byte[65535];
			final int size = DocDecompressor.decompress(stream, buffer, myCompressedSize);
			if (size > 0) {
				return new ByteArrayInputStream(buffer, 0, size);
			}
		} catch (IOException e) {
		}
		return null;
	}
}
