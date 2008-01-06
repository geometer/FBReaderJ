package org.zlibrary.text.model.impl;

import org.zlibrary.core.util.ZLIntArray;
import org.zlibrary.core.util.ZLTextBuffer;
import org.zlibrary.core.image.ZLImageMap;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;

import java.util.*;

abstract class ZLTextModelImpl implements ZLTextModel {
	private final ArrayList<ZLImageEntry> myEntries = new ArrayList<ZLImageEntry>();
	private final ZLIntArray myStartEntryIndices = new ZLIntArray(1024);
	private final ZLIntArray myStartEntryOffsets = new ZLIntArray(1024);
	private final ZLIntArray myParagraphLengths = new ZLIntArray(1024);

	private final ArrayList<char[]> myData = new ArrayList<char[]>(1024);
	private final int myDataBlockSize;
	private char[] myCurrentDataBlock;
	private int myBlockOffset;

	final class EntryIteratorImpl implements ZLTextParagraph.EntryIterator {
		private int myCounter;
		private final int myLength;
		private byte myType;

		int myDataIndex;
		int myDataOffset;

		private char[] myTextData;
		private int myTextOffset;
		private int myTextLength;

		private byte myControlKind;
		private boolean myControlIsStart;
		private boolean myControlIsHyperlink;

		private ZLImageEntry myImageEntry;

		EntryIteratorImpl(int index) {
			myLength = myParagraphLengths.get(index);
			myDataIndex = myStartEntryIndices.get(index);
			myDataOffset = myStartEntryOffsets.get(index);
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

		public ZLImageEntry getImageEntry() {
			return myImageEntry;
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
			char[] data = myData.get(myDataIndex);
			byte type = (byte)data[dataOffset];
			if (type == 0) {
				data = myData.get(++myDataIndex);
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
						dataOffset += labelLength;
					} else {
						myControlIsHyperlink = false;
					}
					break;
				}
				case ZLTextParagraph.Entry.IMAGE:
					myImageEntry = myEntries.get((int)data[dataOffset++]);
					break;
				case ZLTextParagraph.Entry.FIXED_HSPACE:
				case ZLTextParagraph.Entry.FORCED_CONTROL:
					//entry = myEntries.get((int)code);
					break;
			}
			++myCounter;
			myDataOffset = dataOffset;
		}
	}

	ZLTextModelImpl(int dataBlockSize) {
		myDataBlockSize = dataBlockSize;
	}

	void createParagraph() {
		final int dataSize = myData.size();
		myStartEntryIndices.add((dataSize == 0) ? 0 : (dataSize - 1));
		myStartEntryOffsets.add(myBlockOffset);
		myParagraphLengths.add(0);
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
		myParagraphLengths.incrementLast();
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
		myParagraphLengths.incrementLast();
		int blockOffset = myBlockOffset;
		block[blockOffset++] = (char)ZLTextParagraph.Entry.TEXT;
		block[blockOffset++] = (char)(length >> 16);
		block[blockOffset++] = (char)length;
		System.arraycopy(text, offset, block, blockOffset, length);
		myBlockOffset = blockOffset + length;
	}
	
	/*
	public final void addControl(ZLTextForcedControlEntry entry) {
		final char[] block = getDataBlock(3);
		myParagraphLengths.incrementLast();
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.FORCED_CONTROL;
		final int entryAddress = myEntries.size();
		block[myBlockOffset++] = (char)(entryAddress >> 16);
		block[myBlockOffset++] = (char)entryAddress;
		myEntries.add(entry);
	}
	*/
	
	public final void addHyperlinkControl(byte textKind, String label) {
		final short labelLength = (short)label.length();
		final char[] block = getDataBlock(3 + labelLength);
		myParagraphLengths.incrementLast();
		int blockOffset = myBlockOffset;
		block[blockOffset++] = (char)ZLTextParagraph.Entry.CONTROL;
		block[blockOffset++] = (char)(0x0300 + textKind);
		block[blockOffset++] = (char)labelLength;
		label.getChars(0, labelLength, block, blockOffset);
		myBlockOffset = blockOffset + labelLength;
		//addEntry(code + myEntries.size());
		//myEntries.add(new ZLTextHyperlinkControlEntry(textKind, label));
	}
	
	public final void addImage(String id, ZLImageMap imageMap, short vOffset) {
		final char[] block = getDataBlock(2);
		myParagraphLengths.incrementLast();
		final ArrayList<ZLImageEntry> entries = myEntries;
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.IMAGE;
		block[myBlockOffset++] = (char)entries.size();
		entries.add(new ZLImageEntry(imageMap, id, vOffset));
	}
	
	/*
	public final void addFixedHSpace(short length) {
		final char[] block = getDataBlock(3);
		myParagraphLengths.incrementLast();
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.FIXED_HSPACE;
		final int entryAddress = myEntries.size();
		block[myBlockOffset++] = (char)(entryAddress >> 16);
		block[myBlockOffset++] = (char)entryAddress;
		myEntries.add(new ZLTextFixedHSpaceEntry(length));
	}	
	*/
}
