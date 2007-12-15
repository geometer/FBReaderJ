package org.zlibrary.text.model.impl;

import org.zlibrary.core.util.ZLTextBuffer;
import org.zlibrary.core.image.ZLImage;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;

import java.util.*;

abstract class ZLTextModelImpl implements ZLTextModel {
	private final ArrayList<ZLTextParagraph.Entry> myEntries = new ArrayList<ZLTextParagraph.Entry>();
	private int[] myStartEntryInfos = new int[1024];

	private static final int DATA_BLOCK_SIZE = 65536;
	private int myBlockOffset = 0;
	private final ArrayList<char[]> myData = new ArrayList<char[]>();
	{
		myData.add(new char[DATA_BLOCK_SIZE]);
	}

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

		EntryIteratorImpl(int index, int length) {
			myLength = length;
			final int address = myStartEntryInfos[index];
			myDataIndex = address >> 16;
			myDataOffset = address & 0xFFFF;
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

		public boolean hasNext() {
			return myCounter < myLength;
		}	

		public ZLTextParagraph.Entry next() {
			if (myCounter == myLength) {
				throw new NoSuchElementException();
			}
			ZLTextParagraph.Entry entry = null;
			if (myDataOffset == DATA_BLOCK_SIZE) {
				++myDataIndex;
				myDataOffset = 0;
			}
			char[] data = myData.get(myDataIndex);
			myType = (byte)data[myDataOffset];
			if (myType == 0) {
				data = myData.get(++myDataIndex);
				myDataOffset = 0;
				myType = (byte)data[0];
			}
			++myDataOffset;
			switch (myType) {
				case ZLTextParagraph.Entry.TEXT:
					myTextLength = ((int)data[myDataOffset++] << 16) + (int)data[myDataOffset++];
					if (myDataOffset + myTextLength <= DATA_BLOCK_SIZE) {
						myTextData = data;
						myTextOffset = myDataOffset;
						myDataOffset += myTextLength;
					} else {
						myTextData = myData.get(++myDataIndex);
						myTextOffset = 0;
						myDataOffset = myTextLength;
					}
					break;
				case ZLTextParagraph.Entry.CONTROL:
				{
					short kind = (short)data[myDataOffset++];
					myControlKind = (byte)kind;
					myControlIsStart = (kind & 0x0100) == 0x0100;
					if ((kind & 0x0200) == 0x0200) {
						myControlIsHyperlink = true;
						short labelLength = (short)data[myDataOffset++];
						myDataOffset += labelLength;
					} else {
						myControlIsHyperlink = false;
					}
					break;
				}
				case ZLTextParagraph.Entry.IMAGE:
				{
					int address = ((int)data[myDataOffset++] << 16) + (int)data[myDataOffset++];
					entry = myEntries.get(address);
					break;
				}
				case ZLTextParagraph.Entry.FIXED_HSPACE:
				case ZLTextParagraph.Entry.FORCED_CONTROL:
					//entry = myEntries.get((int)code);
					break;
			}
			++myCounter;
			return entry;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	abstract void increaseLastParagraphSize();

	void onParagraphCreation() {
		final int index = getParagraphsNumber();
		if (myStartEntryInfos.length == index) {
			int entryInfos[] = new int[index * 2];
			System.arraycopy(myStartEntryInfos, 0, entryInfos, 0, index);
			myStartEntryInfos = entryInfos;
		}
		myStartEntryInfos[index] = (myData.size() - 1 << 16) + (myBlockOffset & 0xFFFF);
	}

	public void addControl(byte textKind, boolean isStart) {
		final char[] block = getDataBlock(2);
		increaseLastParagraphSize();
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.CONTROL;
		short kind = textKind;
		if (isStart) {
			kind += 0x0100;
		}
		block[myBlockOffset++] = (char)kind;
	}

	private final char[] getDataBlock(int minimumLength) {
		final ArrayList<char[]> data = myData;
		if (minimumLength <= DATA_BLOCK_SIZE - myBlockOffset) {
			return data.get(data.size() - 1);
		}
		char[] block = new char[DATA_BLOCK_SIZE];
		data.add(block);
		myBlockOffset = 0;
		return block;
	}

	public void addText(char[] text) {
		addText(text, 0, text.length);
	}

	public void addText(ZLTextBuffer buffer) {
		addText(buffer.getData(), 0, buffer.getLength());
	}

	public void addText(char[] text, int offset, int length) {
		if (length > DATA_BLOCK_SIZE) {
			length = DATA_BLOCK_SIZE;
		}
		char[] block = getDataBlock(3);
		increaseLastParagraphSize();
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.TEXT;
		block[myBlockOffset++] = (char)(length >> 16);
		block[myBlockOffset++] = (char)length;
		block = getDataBlock(length);
		System.arraycopy(text, offset, block, myBlockOffset, length);
		myBlockOffset += length;
	}
	
	public void addControl(ZLTextForcedControlEntry entry) {
		final char[] block = getDataBlock(3);
		increaseLastParagraphSize();
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.FORCED_CONTROL;
		final int entryAddress = myEntries.size();
		block[myBlockOffset++] = (char)(entryAddress >> 16);
		block[myBlockOffset++] = (char)entryAddress;
		myEntries.add(entry);
	}
	
	public void addHyperlinkControl(byte textKind, String label) {
		final short labelLength = (short)label.length();
		final char[] block = getDataBlock(3 + labelLength);
		increaseLastParagraphSize();
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.CONTROL;
		block[myBlockOffset++] = (char)(0x0300 + textKind);
		block[myBlockOffset++] = (char)labelLength;
		label.getChars(0, labelLength, block, myBlockOffset);
		myBlockOffset += labelLength;
		//addEntry(code + myEntries.size());
		//myEntries.add(new ZLTextHyperlinkControlEntry(textKind, label));
	}
	
	public void addImage(String id, Map<String,ZLImage> imageMap, short vOffset) {
		final char[] block = getDataBlock(3);
		increaseLastParagraphSize();
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.IMAGE;
		final int entryAddress = myEntries.size();
		block[myBlockOffset++] = (char)(entryAddress >> 16);
		block[myBlockOffset++] = (char)entryAddress;
		myEntries.add(new ZLImageEntry(id, imageMap, vOffset));
	}
	
	public void addFixedHSpace(short length) {
		final char[] block = getDataBlock(3);
		increaseLastParagraphSize();
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.FIXED_HSPACE;
		final int entryAddress = myEntries.size();
		block[myBlockOffset++] = (char)(entryAddress >> 16);
		block[myBlockOffset++] = (char)entryAddress;
		myEntries.add(new ZLTextFixedHSpaceEntry(length));
	}	

	public String dump() {
		StringBuilder sb = new StringBuilder();
		final int len = getParagraphsNumber();
		for (int i = 0; i < len; ++i) {
			ZLTextParagraph paragraph = getParagraph(i);
			sb.append("[PARAGRAPH]\n");
			for (ZLTextParagraph.EntryIterator it = paragraph.iterator(); it.hasNext(); ) {
				it.next();
				switch (it.getType()) {
					case ZLTextParagraph.Entry.TEXT:
						sb.append("[TEXT]");
						sb.append(it.getTextData(), it.getTextOffset(), it.getTextLength());
						sb.append("[/TEXT]");
						break;
					case ZLTextParagraph.Entry.CONTROL:
						if (it.getControlIsStart())
							sb.append("[CONTROL "+it.getControlKind()+"]");
						else
							sb.append("[/CONTROL "+it.getControlKind()+"]");					
						break;
				}
			}
			sb.append("[/PARAGRAPH]\n");
		}
		return sb.toString();
	}
}
