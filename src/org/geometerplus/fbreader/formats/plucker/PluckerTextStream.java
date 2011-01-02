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

import java.io.IOException;

import org.geometerplus.fbreader.formats.pdb.PdbStream;
import org.geometerplus.fbreader.formats.pdb.PdbUtil;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

public class PluckerTextStream extends PdbStream {
	private short myCompressionVersion;
	private	byte[] myFullBuffer;
	private	int myRecordIndex;

	public PluckerTextStream(ZLFile file) throws IOException {
		super(file);
		myFullBuffer = null;
	}
	
	public int read() {
		return 0;
	}
	
	public boolean open() throws IOException {
		//if (!super.open()) {
		//	return false;
		//}

		myCompressionVersion = (short) PdbUtil.readShort(myBase);

		myBuffer = new byte[65536];
		myFullBuffer = new byte[65536];

		myRecordIndex = 0;

		return true;
	}
	
	public	void close() throws IOException {
		if (myFullBuffer != null) {
			myFullBuffer = null;
		}
		super.close();
	}

	protected boolean fillBuffer() {
		while (myBufferOffset == myBufferLength) {
			if (myRecordIndex + 1 > myHeader.Offsets.length - 1) {
				return false;
			}
			++myRecordIndex;
			int currentOffset = myHeader.Offsets[myRecordIndex];
			//if (currentOffset < myBase.offset()) {
			//	return false;
			//}
			//((PdbStream)myBase).seek(currentOffset, true);
			int nextOffset =
				(myRecordIndex + 1 < myHeader.Offsets.length) ?
						myHeader.Offsets[myRecordIndex + 1] : 0;//myBase.sizeOfOpened();
			if (nextOffset < currentOffset) {
				return false;
			}
			try {
				processRecord(nextOffset - currentOffset);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	private void processRecord(int recordSize) throws IOException {
		myBase.skip(2);

		int paragraphs = PdbUtil.readShort(myBase);
		int size = PdbUtil.readShort(myBase);
		
		char type = (char)myBase.read(); 
		if (type > 1) { // this record is not text record
			return;
		}

		myBase.skip(1);

		final int[] pars = new int[paragraphs];
		for (int i = 0; i < paragraphs; ++i) {
			pars[i] = PdbUtil.readShort(myBase);
			myBase.skip(2);
		}

		boolean doProcess = false;
		if (type == 0) {
			doProcess = myBase.read(myFullBuffer, 0, size) == size;
		} else if (myCompressionVersion == 1) {
			//doProcess =
				//DocDecompressor().decompress(myBase, myFullBuffer, recordSize - 8 - 4 * paragraphs, size) == size;
		} else if (myCompressionVersion == 2) {
			myBase.skip(2);
			//doProcess =
				//ZLZDecompressor(recordSize - 10 - 4 * paragraphs).decompress(myBase, myFullBuffer, size) == size;
		}
		if (doProcess) {
			myBufferLength = 0;
			myBufferOffset = 0;

			int start = 0;
			int end = 0;

			for (int i = 0; i < paragraphs; ++i) {
				start = end;
				end = start + pars[i];
				if (end > myFullBuffer[size]) {
					break;
				}
				processTextParagraph(myFullBuffer.toString().toCharArray(), start, end);
			}
		}
	}
	
	private	void processTextParagraph(char[] data, int start, int end) {
		int textStart = start;
		boolean functionFlag = false;
		for (int ptr = start; ptr < end; ++ptr) {
			if (data[ptr] == 0) {
				functionFlag = true;
				if (ptr != textStart) {
					//memcpy(myBuffer + myBufferLength, textStart, ptr - textStart);
					myBufferLength += ptr - textStart;
				}
			} else if (functionFlag) {
				int paramCounter = (data[ptr]) % 8;
				if (end - ptr > paramCounter + 1) {
					ptr += paramCounter;
				} else {
					ptr = end - 1;
				}
				functionFlag = false;
				textStart = ptr + 1;
			}
		}
		if (end != textStart) {
			//memcpy(myBuffer + myBufferLength, textStart, end - textStart);
			myBufferLength += end - textStart;
		}
	}
}
