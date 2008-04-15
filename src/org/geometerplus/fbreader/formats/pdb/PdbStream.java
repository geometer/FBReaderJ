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

package org.geometerplus.fbreader.formats.pdb;

import java.io.IOException;
import java.io.InputStream;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

public abstract class PdbStream extends InputStream {
	protected final InputStream myBase;
	private int myOffset;
	public final PdbHeader myHeader = new PdbHeader();
	protected byte[] myBuffer;

	protected short myBufferLength;
	protected short myBufferOffset;

	public PdbStream(ZLFile file) {
		InputStream base;
		try {
			base = file.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			base = null;
		}
		myBase = base;
	}
	
	public int read(byte[] buffer,int offset, int maxSize) {
		int realSize = 0;
		while (realSize < maxSize) {
			if (!fillBuffer()) {
				break;
			}
			int size = Math.min((maxSize - realSize), (myBufferLength - myBufferOffset));
			if (size > 0) {
				System.arraycopy(myBuffer, myBufferOffset, buffer, offset + realSize, size);
				realSize += size;
				myBufferOffset += size;
			}
		}
		myOffset += realSize;
		return realSize;
	}
	/*public int read(byte[] buffer,int offset, int maxSize) {
		int realSize = 0;
		while (realSize < maxSize) {
			if (!fillBuffer()) {
				break;
			}
			int size = Math.min((maxSize - realSize), (myBufferLength - myBufferOffset));
			if (size > 0) {
				if (buffer != null) {
					for (int i = 0; i < size; i++) {
						myBuffer[myBufferOffset+i] = buffer[realSize+i]; 
					}
					//memcpy(buffer + realSize, myBuffer + myBufferOffset, size);
				}
				realSize += size;
				myBufferOffset += size;
			}
		}
		myOffset += realSize;
		return realSize;
	}*/
	
	public boolean open() throws IOException {
		if ((myBase == null) || !myHeader.read(myBase)) {
			return false;
		}

		myBase.skip(myHeader.Offsets[0] - 78 - 8 * myHeader.Offsets.length);

		myBufferLength = 0;
		myBufferOffset = 0;

		myOffset = 0;

		return true;
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
		} else if (offset < 0) {
			offset += this.offset();
			open();
			if (offset >= 0) {
				read(null, 0, offset);
			}
		}
	}
	
	public int offset() {
		return myOffset;
	}
	
	public int sizeOfOpened() {
		// TODO: implement
		return 0;
	}

	protected abstract boolean fillBuffer();
}
