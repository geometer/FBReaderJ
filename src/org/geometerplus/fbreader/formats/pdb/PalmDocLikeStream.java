/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

abstract class PalmDocLikeStream extends PdbStream {
	protected int myMaxRecordIndex;
	protected int myRecordIndex;
	protected interface CompressionType {
		int NONE = 1;
		int DOC = 2;
		int HUFFDIC = 17480;
	}
	protected int myCompressionType;

	private final long myFileSize;

	PalmDocLikeStream(ZLFile file) throws IOException {
		super(file);
		myFileSize = file.size();
	}

	protected final boolean fillBuffer() {
		while (myBufferOffset == myBufferLength) {
			if (myRecordIndex + 1 > myMaxRecordIndex) {
				return false;
			}
			++myRecordIndex;
			final int currentOffset = myHeader.Offsets[myRecordIndex];

			try {
				myBase.skip(currentOffset - myBase.offset());
				final int nextOffset =
					(myRecordIndex + 1 < myHeader.Offsets.length) ?
						myHeader.Offsets[myRecordIndex + 1] :
						(int)myFileSize;
				if (nextOffset < currentOffset) {
					return false;
				}
				final short recordSize = (short)Math.min(nextOffset - currentOffset, myBuffer.length);

				switch (myCompressionType) {
					case CompressionType.NONE:
						myBase.read(myBuffer, 0, recordSize);
						myBufferLength = recordSize;
						break;
					case CompressionType.DOC:
						myBufferLength = (short)DocDecompressor.decompress(myBase, myBuffer, recordSize);
						break;
					//case CompressionType.HUFFDIC:
					//	myBufferLength = (short)HuffdicDecompressor.decompress(myBase, myBuffer, recordSize);
					//	break;
					default:
						// Unsupported compression type
						return false;
				}
			} catch (IOException e) {
				return false;
			}
			myBufferOffset = 0;
		}
		
		return true;
	}
}
