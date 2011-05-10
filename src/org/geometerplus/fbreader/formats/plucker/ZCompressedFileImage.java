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
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLSingleImage;
import org.geometerplus.zlibrary.core.util.MimeType;

public class ZCompressedFileImage extends ZLSingleImage {
	private final ZLFile myFile;
	private final int myOffset;
	private final int myCompressedSize;
	
	public ZCompressedFileImage(MimeType mimeType, final ZLFile file, final int offset, final int compressedSize) {
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
			
			final ArrayList<byte[]> data = new ArrayList<byte[]>();
			byte[] buffer;
			int sizeOfBufferData;

			stream.skip(myOffset);
			byte [] targetBuffer = new byte[myCompressedSize];
			stream.read(targetBuffer, 0, myCompressedSize);
			Inflater decompressor = new Inflater();
			decompressor.setInput(targetBuffer, 0, myCompressedSize);
			do {
				buffer = new byte[4096];
				sizeOfBufferData = decompressor.inflate(buffer);
				data.add(buffer);
			} while (sizeOfBufferData == 4096);
			decompressor.end();
			final int dataSizeMinus1 = data.size() - 1;
			buffer = new byte[dataSizeMinus1 * 4096 + sizeOfBufferData];
			for (int i = 0; i < dataSizeMinus1; ++i) {
				System.arraycopy(data.get(i), 0, buffer, i * 4096, 4096);
			}
			System.arraycopy(data.get(dataSizeMinus1), 0, buffer, dataSizeMinus1 * 4096, sizeOfBufferData);
			return new ByteArrayInputStream(buffer);
		} catch (IOException e) {
			return null;
		} catch (DataFormatException e) {
			return null;
		}
	}
}
