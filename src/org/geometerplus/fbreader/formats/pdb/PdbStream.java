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

package org.geometerplus.fbreader.formats.pdb;

import java.io.IOException;
import java.io.InputStream;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.util.ZLInputStreamWithOffset;

public abstract class PdbStream extends InputStream {
	protected final ZLInputStreamWithOffset myBase;
	public PdbHeader myHeader;
	protected byte[] myBuffer;

	protected short myBufferLength;
	protected short myBufferOffset;

	public PdbStream(ZLFile file) throws IOException {
		myBase = new ZLInputStreamWithOffset(file.getInputStream());

		myHeader = new PdbHeader(myBase);

		myBase.skip(myHeader.Offsets[0] - myHeader.length());

		myBufferLength = 0;
		myBufferOffset = 0;
	}
	
	public int read() {
		if (!fillBuffer()) {
			return -1;
		}
		return myBuffer[myBufferOffset++];
	}

	public int read(byte[] buffer, int offset, int maxSize) {
		int realSize = 0;
		while (realSize < maxSize) {
			if (!fillBuffer()) {
				break;
			}
			int size = Math.min(maxSize - realSize, myBufferLength - myBufferOffset);
			if (size > 0) {
				if (buffer != null) {
					System.arraycopy(myBuffer, myBufferOffset, buffer, offset + realSize, size);
				}
				realSize += size;
				myBufferOffset += size;
			}
		}
		return (realSize > 0) ? realSize : -1;
	}
	
	public void close() throws IOException {
		if (myBase != null) {
			myBase.close();
		}
		if (myBuffer != null) {
			myBuffer = null;
		}
	}

	public void skip(int offset) throws IOException {
		if (offset > 0) {
			read(null, 0, offset);
		} else {
			throw new IOException("Cannot skip: " + offset + " bytes");
		}
	}

	protected abstract boolean fillBuffer();
}
