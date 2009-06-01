/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

public final class ZLTextPlainModel implements ZLTextModel {
	private final String myId;

	private int[] myStartEntryIndices;
	private int[] myStartEntryOffsets;
	private int[] myParagraphLengths;
	private byte[] myParagraphKinds;

	private final ArrayList myEntries = new ArrayList();

	int myParagraphsNumber;

	private final CharStorage myStorage;
	private final ArrayList<ZLTextMark> myMarks = new ArrayList<ZLTextMark>();
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
		private byte myHyperlinkType;
		private String myHyperlinkId;

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
		public byte getHyperlinkType() {
			return myHyperlinkType;
		}
		public String getHyperlinkId() {
			return myHyperlinkId;
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
			char[] data = myStorage.block(myDataIndex);
			if (dataOffset == data.length) {
				data = myStorage.block(++myDataIndex);
				dataOffset = 0;
			}
			byte type = (byte)data[dataOffset];
			if (type == 0) {
				data = myStorage.block(++myDataIndex);
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
					myHyperlinkType = (byte)(kind >> 9);
					if (myHyperlinkType != 0) {
						short labelLength = (short)data[dataOffset++];
						myHyperlinkId = new String(data, dataOffset, labelLength);
						dataOffset += labelLength;
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

	public ZLTextPlainModel(String id, int arraySize, int dataBlockSize, String directoryName, String extension) {
		myId = id;
		myStartEntryIndices = new int[arraySize];
		myStartEntryOffsets = new int[arraySize];
		myParagraphLengths = new int[arraySize];
		myParagraphKinds = new byte[arraySize];
		myStorage = new CachedCharStorage(dataBlockSize, directoryName, extension);
	}

	public String getId() {
		return myId;
	}

	private void extend() {
		final int size = myStartEntryIndices.length;
		myStartEntryIndices = ZLArrayUtils.createCopy(myStartEntryIndices, size, size << 1);
		myStartEntryOffsets = ZLArrayUtils.createCopy(myStartEntryOffsets, size, size << 1);
		myParagraphLengths = ZLArrayUtils.createCopy(myParagraphLengths, size, size << 1);
		myParagraphKinds = ZLArrayUtils.createCopy(myParagraphKinds, size, size << 1);
	}

	public void createParagraph(byte kind) {
		final int index = myParagraphsNumber++;
		int[] startEntryIndices = myStartEntryIndices;
		if (index == startEntryIndices.length) {
			extend();
			startEntryIndices = myStartEntryIndices;
		}
		final int dataSize = myStorage.size();
		startEntryIndices[index] = (dataSize == 0) ? 0 : (dataSize - 1);
		myStartEntryOffsets[index] = myBlockOffset;
		myParagraphLengths[index] = 0;
		myParagraphKinds[index] = kind;
	}

	private char[] getDataBlock(int minimumLength) {
		char[] block = myCurrentDataBlock;
		if ((block == null) || (minimumLength > block.length - myBlockOffset)) {
			if (block != null) {
				myStorage.freezeLastBlock();
			}
			block = myStorage.createNewBlock(minimumLength);
			myCurrentDataBlock = block;
			myBlockOffset = 0;
		}
		return block;
	}

	public void addControl(byte textKind, boolean isStart) {
		final char[] block = getDataBlock(2);
		++myParagraphLengths[myParagraphsNumber - 1];
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.CONTROL;
		short kind = textKind;
		if (isStart) {
			kind += 0x0100;
		}
		block[myBlockOffset++] = (char)kind;
	}

	public void addText(char[] text) {
		addText(text, 0, text.length);
	}

	public void addText(char[] text, int offset, int length) {
		char[] block = getDataBlock(3 + length);
		++myParagraphLengths[myParagraphsNumber - 1];
		int blockOffset = myBlockOffset;
		block[blockOffset++] = (char)ZLTextParagraph.Entry.TEXT;
		block[blockOffset++] = (char)(length >> 16);
		block[blockOffset++] = (char)length;
		System.arraycopy(text, offset, block, blockOffset, length);
		myBlockOffset = blockOffset + length;
	}
	
	
	public void addControl(ZLTextForcedControlEntry entry) {
		final char[] block = getDataBlock(3);
		++myParagraphLengths[myParagraphsNumber - 1];
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.FORCED_CONTROL;
		final int entryAddress = myEntries.size();
		block[myBlockOffset++] = (char)(entryAddress >> 16);
		block[myBlockOffset++] = (char)entryAddress;
		myEntries.add(entry);
	}
	
	
	public void addHyperlinkControl(byte textKind, byte hyperlinkType, String label) {
		final short labelLength = (short)label.length();
		final char[] block = getDataBlock(3 + labelLength);
		++myParagraphLengths[myParagraphsNumber - 1];
		int blockOffset = myBlockOffset;
		block[blockOffset++] = (char)ZLTextParagraph.Entry.CONTROL;
		block[blockOffset++] = (char)((hyperlinkType << 9) + 0x0100 + textKind);
		block[blockOffset++] = (char)labelLength;
		label.getChars(0, labelLength, block, blockOffset);
		myBlockOffset = blockOffset + labelLength;
	}
	
	public void addImage(String id, ZLImageMap imageMap, short vOffset) {
		final char[] block = getDataBlock(3);
		++myParagraphLengths[myParagraphsNumber - 1];
		final ArrayList entries = myEntries;
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.IMAGE;
		final int entryAddress = myEntries.size();
		block[myBlockOffset++] = (char)(entryAddress >> 16);
		block[myBlockOffset++] = (char)entryAddress;
		entries.add(new ZLImageEntry(imageMap, id, vOffset));
	}
	
	
	public void addFixedHSpace(short length) {
		final char[] block = getDataBlock(2);
		++myParagraphLengths[myParagraphsNumber - 1];
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.FIXED_HSPACE;
		block[myBlockOffset++] = (char)length;
	}	

	public ZLTextMark getFirstMark() {
		return myMarks.isEmpty() ? null : myMarks.get(0);
	}
	
	public ZLTextMark getLastMark() {
		return myMarks.isEmpty() ? null : myMarks.get(myMarks.size() - 1);
	}

	public ZLTextMark getNextMark(ZLTextMark position) {
		if (position == null) {
			return null;
		}

		ZLTextMark mark = null;
		for (ZLTextMark current : myMarks) {
			if (current.compareTo(position) >= 0) {
				if ((mark == null) || (mark.compareTo(current) > 0)) {
					mark = current;
				}
			}
		}
		return mark;
	}

	public ZLTextMark getPreviousMark(ZLTextMark position) {
		if (position == null) {
			return null;
		}

		ZLTextMark mark = null;
		for (ZLTextMark current : myMarks) {
			if (current.compareTo(position) < 0) {
				if ((mark == null) || (mark.compareTo(current) < 0)) {
					mark = current;
				}
			}
		}
		return mark;
	}

	public int search(final String text, int startIndex, int endIndex, boolean ignoreCase) {
		int count = 0;
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
						++count;
					}
					offset += textLength;						
				}				
			} 
		}
		return count;
	}

	public ArrayList getMarks() {
		return myMarks;
	}	

	public void removeAllMarks() {
		myMarks.clear();
	}

	public int getParagraphsNumber() {
		return myParagraphsNumber;
	}

	public ZLTextParagraph getParagraph(int index) {
		final byte kind = myParagraphKinds[index];
		return (kind == ZLTextParagraph.Kind.TEXT_PARAGRAPH) ?
			new ZLTextParagraphImpl(this, index) :
			new ZLTextSpecialParagraphImpl(kind, this, index);
	}

	public int getParagraphTextLength(int index) {
		int textLength = 0;
		int dataIndex = myStartEntryIndices[index];
		int dataOffset = myStartEntryOffsets[index];
		char[] data = myStorage.block(dataIndex);

		for (int len = myParagraphLengths[index]; len > 0; --len) {
			if (dataOffset == data.length) {
				data = myStorage.block(++dataIndex);
				dataOffset = 0;
			}
			byte type = (byte)data[dataOffset];
			if (type == 0) {
				data = myStorage.block(++dataIndex);
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
