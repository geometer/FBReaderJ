/*
 * Copyright (C) 2009 Geometer Plus <contact@geometerplus.com>
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
	protected boolean myIsCompressed;

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
				final short recordSize = (short)(nextOffset - currentOffset);

				if (myIsCompressed) {
					myBufferLength = (short)DocDecompressor.decompress(myBase, myBuffer, recordSize);
				} else {
					myBase.read(myBuffer, 0, recordSize);
					myBufferLength = recordSize;
				}
			} catch (IOException e) {
				return false;
			}
			myBufferOffset = 0;
		}
		
		return true;
	}
}
