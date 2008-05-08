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

package org.geometerplus.zlibrary.text.model;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.image.ZLImageMap;

abstract class ZLTextModelImpl implements ZLTextModel {
	private final ArrayList myEntries = new ArrayList();
	final static int INITIAL_CAPACITY = 1024;
	private int[] myStartEntryIndices = new int[INITIAL_CAPACITY];
	private int[] myStartEntryOffsets = new int[INITIAL_CAPACITY];
	private int[] myParagraphLengths = new int[INITIAL_CAPACITY];
	int myParagraphsNumber;

	private final ArrayList myData = new ArrayList(1024);
	private final ArrayList myMarks = new ArrayList();
	private final int myDataBlockSize;
	private char[] myCurrentDataBlock;
	private int myBlockOffset;

	final class EntryIteratorImpl implements ZLTextParagraph.EntryIterator {
		private int myCounter;
		private int myLength;
		private byte myType;

		int myDataIndex;
		int myDataOffset;

		private char[] myTextData;
		private int myTextOffset;
		private int myTextLength;

		private byte myControlKind;
		private boolean myControlIsStart;
		private boolean myControlIsHyperlink;
		private String myHyperlinkControlLabel;

		private ZLImageEntry myImageEntry;

		private short myFixedHSpaceLength;

		EntryIteratorImpl(int index) {
			myLength = myParagraphLengths[index];
			myDataIndex = myStartEntryIndices[index];
			myDataOffset = myStartEntryOffsets[index];
		}

		void reset(int index) {
			myCounter = 0;
			myLength = myParagraphLengths[index];
			myDataIndex = myStartEntryIndices[index];
			myDataOffset = myStartEntryOffsets[index];
		}

		public byte getType() {
			return myType;
		}

		public char[] getTextData() {
			return myTextData;
		}
		public int getTextOffset() {
			return myTextOffset;
		}
		public int getTextLength() {
			return myTextLength;
		}

		public byte getControlKind() {
			return myControlKind;
		}
		public boolean getControlIsStart() {
			return myControlIsStart;
		}
		public boolean getControlIsHyperlink() {
			return myControlIsHyperlink;
		}
		public String getHyperlinkControlLabel() {
			return myHyperlinkControlLabel;
		}

		public ZLImageEntry getImageEntry() {
			return myImageEntry;
		}

		public short getFixedHSpaceLength() {
			return myFixedHSpaceLength;
		}

		public boolean hasNext() {
			return myCounter < myLength;
		}	

		public void next() {
			int dataOffset = myDataOffset;
			if (dataOffset == myDataBlockSize) {
				++myDataIndex;
				dataOffset = 0;
			}
			char[] data = (char[])myData.get(myDataIndex);
			byte type = (byte)data[dataOffset];
			if (type == 0) {
				data = (char[])myData.get(++myDataIndex);
				dataOffset = 0;
				type = (byte)data[0];
			}
			myType = type;
			++dataOffset;
			switch (type) {
				case ZLTextParagraph.Entry.TEXT:
					myTextLength =
						((int)data[dataOffset++] << 16) +
						(int)data[dataOffset++];
					myTextData = data;
					myTextOffset = dataOffset;
					dataOffset += myTextLength;
					break;
				case ZLTextParagraph.Entry.CONTROL:
				{
					short kind = (short)data[dataOffset++];
					myControlKind = (byte)kind;
					myControlIsStart = (kind & 0x0100) == 0x0100;
					if ((kind & 0x0200) == 0x0200) {
						myControlIsHyperlink = true;
						short labelLength = (short)data[dataOffset++];
						myHyperlinkControlLabel = new String(data, dataOffset, labelLength);
						dataOffset += labelLength;
					} else {
						myControlIsHyperlink = false;
					}
					break;
				}
				case ZLTextParagraph.Entry.IMAGE:
				{
					final int entryAddress =
						((int)data[dataOffset++] << 16) +
						(int)data[dataOffset++];
					myImageEntry = (ZLImageEntry)myEntries.get(entryAddress);
					break;
				}
				case ZLTextParagraph.Entry.FIXED_HSPACE:
					myFixedHSpaceLength = (short)data[dataOffset++];
					break;
				case ZLTextParagraph.Entry.FORCED_CONTROL:
				{
					final int entryAddress =
						((int)data[dataOffset++] << 16) +
						(int)data[dataOffset++];
					//entry = myEntries.get((int)code);
					break;
				}
			}
			++myCounter;
			myDataOffset = dataOffset;
		}
	}

	ZLTextModelImpl(int dataBlockSize) {
		myDataBlockSize = dataBlockSize;
	}

	void extend() {
		final int size = myStartEntryIndices.length;
		myStartEntryIndices = ZLArrayUtils.createCopy(myStartEntryIndices, size, size << 1);
		myStartEntryOffsets = ZLArrayUtils.createCopy(myStartEntryOffsets, size, size << 1);
		myParagraphLengths = ZLArrayUtils.createCopy(myParagraphLengths, size, size << 1);
	}

	final void createParagraph() {
		final int index = myParagraphsNumber++;
		int[] startEntryIndices = myStartEntryIndices;
		if (index == startEntryIndices.length) {
			extend();
			startEntryIndices = myStartEntryIndices;
		}
		final int dataSize = myData.size();
		startEntryIndices[index] = (dataSize == 0) ? 0 : (dataSize - 1);
		myStartEntryOffsets[index] = myBlockOffset;
		myParagraphLengths[index] = 0;
	}

	private final char[] getDataBlock(int minimumLength) {
		char[] block = myCurrentDataBlock;
		if ((block == null) || (minimumLength > block.length - myBlockOffset)) {
			int blockSize = myDataBlockSize;
			if (minimumLength > blockSize) {
				blockSize = minimumLength;
			}
			block = new char[blockSize];
			myData.add(block);
			myCurrentDataBlock = block;
			myBlockOffset = 0;
		}
		return block;
	}

	public final void addControl(byte textKind, boolean isStart) {
		final char[] block = getDataBlock(2);
		++myParagraphLengths[myParagraphsNumber - 1];
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.CONTROL;
		short kind = textKind;
		if (isStart) {
			kind += 0x0100;
		}
		block[myBlockOffset++] = (char)kind;
	}

	public final void addText(char[] text) {
		addText(text, 0, text.length);
	}

	public final void addText(ZLTextBuffer buffer) {
		addText(buffer.getData(), 0, buffer.getLength());
	}

	public final void addText(char[] text, int offset, int length) {
		char[] block = getDataBlock(3 + length);
		++myParagraphLengths[myParagraphsNumber - 1];
		int blockOffset = myBlockOffset;
		block[blockOffset++] = (char)ZLTextParagraph.Entry.TEXT;
		block[blockOffset++] = (char)(length >> 16);
		block[blockOffset++] = (char)length;
		System.arraycopy(text, offset, block, blockOffset, length);
		myBlockOffset = blockOffset + length;
	}
	
	
	public final void addControl(ZLTextForcedControlEntry entry) {
		final char[] block = getDataBlock(3);
		++myParagraphLengths[myParagraphsNumber - 1];
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.FORCED_CONTROL;
		final int entryAddress = myEntries.size();
		block[myBlockOffset++] = (char)(entryAddress >> 16);
		block[myBlockOffset++] = (char)entryAddress;
		myEntries.add(entry);
	}
	
	
	public final void addHyperlinkControl(byte textKind, String label) {
		final short labelLength = (short)label.length();
		final char[] block = getDataBlock(3 + labelLength);
		++myParagraphLengths[myParagraphsNumber - 1];
		int blockOffset = myBlockOffset;
		block[blockOffset++] = (char)ZLTextParagraph.Entry.CONTROL;
		block[blockOffset++] = (char)(0x0300 + textKind);
		block[blockOffset++] = (char)labelLength;
		label.getChars(0, labelLength, block, blockOffset);
		myBlockOffset = blockOffset + labelLength;
	}
	
	public final void addImage(String id, ZLImageMap imageMap, short vOffset) {
		final char[] block = getDataBlock(3);
		++myParagraphLengths[myParagraphsNumber - 1];
		final ArrayList entries = myEntries;
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.IMAGE;
		final int entryAddress = myEntries.size();
		block[myBlockOffset++] = (char)(entryAddress >> 16);
		block[myBlockOffset++] = (char)entryAddress;
		entries.add(new ZLImageEntry(imageMap, id, vOffset));
	}
	
	
	public final void addFixedHSpace(short length) {
		final char[] block = getDataBlock(2);
		++myParagraphLengths[myParagraphsNumber - 1];
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.FIXED_HSPACE;
		block[myBlockOffset++] = (char)length;
	}	

	public ZLTextMark getFirstMark() {
		return myMarks.size() == 0 ? new ZLTextMark() : (ZLTextMark) myMarks.get(0);
	}
	
	public ZLTextMark getLastMark() {
		return myMarks.size() == 0 ? new ZLTextMark() : (ZLTextMark) myMarks.get(myMarks.size() - 1);
	}

	public ZLTextMark getNextMark(ZLTextMark position) {
		ZLTextMark mark = null;
		for (int i = 0; i < myMarks.size(); i++) {
			ZLTextMark current = (ZLTextMark) myMarks.get(i);
			if (current.compareTo(position) > 0) {
				if ((mark == null) || (mark.compareTo(current) > 0)) {
					mark = current;
				}
			}
		}
		return (mark != null) ? mark : new ZLTextMark();	
	}

	public ZLTextMark getPreviousMark(ZLTextMark position) {
		ZLTextMark mark = null;
		for (int i = 0; i < myMarks.size(); i++) {
			ZLTextMark current = (ZLTextMark) myMarks.get(i);
			if (current.compareTo(position) < 0) {
				if ((mark == null) || (mark.compareTo(current) < 0)) {
					mark = current;
				}
			}
		}
		return (mark != null) ? mark : new ZLTextMark();
	}

	public final void search(final String text, int startIndex, int endIndex, boolean ignoreCase) {
		ZLSearchPattern pattern = new ZLSearchPattern(text, ignoreCase);
		myMarks.clear();
		if (startIndex > myParagraphsNumber) {
                	startIndex = myParagraphsNumber;				
		}
		if (endIndex > myParagraphsNumber) {
			endIndex = myParagraphsNumber;
		}				
		int index = startIndex;
		for (EntryIteratorImpl it = new EntryIteratorImpl(index); index < endIndex; it.reset(++index)) {
			int offset = 0;
			while (it.hasNext()) {
				it.next();
				if (it.getType() == ZLTextParagraph.Entry.TEXT) {
					char[] textData = it.getTextData();
					int textOffset = it.getTextOffset();
					int textLength = it.getTextLength();
					for (int pos = ZLSearchUtil.find(textData, textOffset, textLength, pattern); pos != -1; 
						pos = ZLSearchUtil.find(textData, textOffset, textLength, pattern, pos + 1)) {
						myMarks.add(new ZLTextMark(index, offset + pos, pattern.getLength()));
					}
					offset += textLength;						
				}				
			} 
		}
	}

	public ArrayList getMarks() {
		return myMarks;
	}	

	protected void clear() {
		myParagraphsNumber = 0;
		myEntries.clear();
		myData.clear();
		myMarks.clear();
		myCurrentDataBlock = null;
		myBlockOffset = 0;
	}

	public void removeAllMarks() {
		myMarks.clear();
	}

	public void selectParagraph(int index) {
		if (index < myParagraphsNumber) {
			myMarks.add(new ZLTextMark(index, 0, getParagraphTextLength(index)));
		}
	}

	public int getParagraphTextLength(int index) {
		int textLength = 0;
		int dataIndex = myStartEntryIndices[index];
		int dataOffset = myStartEntryOffsets[index];
		char[] data = (char[])myData.get(dataIndex);

		for (int len = myParagraphLengths[index]; len > 0; --len) {
			if (dataOffset == myDataBlockSize) {
				data = (char[])myData.get(++dataIndex);
				dataOffset = 0;
			}
			byte type = (byte)data[dataOffset];
			if (type == 0) {
				data = (char[])myData.get(++dataIndex);
				dataOffset = 0;
				type = (byte)data[0];
			}
			++dataOffset;
			switch (type) {
				case ZLTextParagraph.Entry.TEXT:
					int entryLength =
						((int)data[dataOffset++] << 16) +
						(int)data[dataOffset++];
					dataOffset += entryLength;
					textLength += entryLength;
					break;
				case ZLTextParagraph.Entry.CONTROL:
				{
					if ((data[dataOffset++] & 0x0200) == 0x0200) {
						dataOffset += (short)data[dataOffset++];
					}
					break;
				}
				case ZLTextParagraph.Entry.IMAGE:
					dataOffset += 2;
					break;
				case ZLTextParagraph.Entry.FIXED_HSPACE:
					++dataOffset;
					break;
				case ZLTextParagraph.Entry.FORCED_CONTROL:
					dataOffset += 2;
					break;
			}
		}

		return textLength;
	}
}
