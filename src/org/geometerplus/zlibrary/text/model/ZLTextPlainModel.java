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

package org.geometerplus.zlibrary.text.model;

import java.util.*;

import org.geometerplus.zlibrary.core.util.*;
import org.geometerplus.zlibrary.core.image.ZLImageMap;

public class ZLTextPlainModel implements ZLTextModel {
	protected final String myId;
	private final String myLanguage;

	protected int[] myStartEntryIndices;
	protected int[] myStartEntryOffsets;
	protected int[] myParagraphLengths;
	protected int[] myTextSizes;
	protected byte[] myParagraphKinds;

	protected int myParagraphsNumber;

	protected final CharStorage myStorage;
	private ArrayList<ZLTextMark> myMarks;

	protected final ZLImageMap myImageMap;

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
		private ZLTextForcedControlEntry myForcedControlEntry;

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

		public ZLTextForcedControlEntry getForcedControlEntry() {
			return myForcedControlEntry;
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
					final short vOffset = (short)data[dataOffset++];
					final short len = (short)data[dataOffset++];
					final String id = new String(data, dataOffset, len);
					dataOffset += len;
					myImageEntry = new ZLImageEntry(myImageMap, id, vOffset);
					break;
				}
				case ZLTextParagraph.Entry.FIXED_HSPACE:
					myFixedHSpaceLength = (short)data[dataOffset++];
					break;
				case ZLTextParagraph.Entry.FORCED_CONTROL:
				{
					final int mask = (int)data[dataOffset++];
					final ZLTextForcedControlEntry entry = new ZLTextForcedControlEntry();
					if ((mask & ZLTextForcedControlEntry.SUPPORTS_LEFT_INDENT) ==
								ZLTextForcedControlEntry.SUPPORTS_LEFT_INDENT) {
						entry.setLeftIndent((short)data[dataOffset++]);
					}
					if ((mask & ZLTextForcedControlEntry.SUPPORTS_RIGHT_INDENT) ==
								ZLTextForcedControlEntry.SUPPORTS_RIGHT_INDENT) {
						entry.setRightIndent((short)data[dataOffset++]);
					}
					if ((mask & ZLTextForcedControlEntry.SUPPORTS_ALIGNMENT_TYPE) ==
								ZLTextForcedControlEntry.SUPPORTS_ALIGNMENT_TYPE) {
						entry.setAlignmentType((byte)data[dataOffset++]);
					}
					myForcedControlEntry = entry;
				}
			}
			++myCounter;
			myDataOffset = dataOffset;
		}
	}

	protected ZLTextPlainModel(String id, String language, int arraySize, int dataBlockSize, String directoryName, String extension, ZLImageMap imageMap) {
		myId = id;
		myLanguage = language;
		myStartEntryIndices = new int[arraySize];
		myStartEntryOffsets = new int[arraySize];
		myParagraphLengths = new int[arraySize];
		myTextSizes = new int[arraySize];
		myParagraphKinds = new byte[arraySize];
		myStorage = new CachedCharStorage(dataBlockSize, directoryName, extension);
		myImageMap = imageMap;
	}

	public final String getId() {
		return myId;
	}

	public final String getLanguage() {
		return myLanguage;
	}

	public final ZLTextMark getFirstMark() {
		return ((myMarks == null) || myMarks.isEmpty()) ? null : myMarks.get(0);
	}
	
	public final ZLTextMark getLastMark() {
		return ((myMarks == null) || myMarks.isEmpty()) ? null : myMarks.get(myMarks.size() - 1);
	}

	public final ZLTextMark getNextMark(ZLTextMark position) {
		if ((position == null) || (myMarks == null)) {
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

	public final ZLTextMark getPreviousMark(ZLTextMark position) {
		if ((position == null) || (myMarks == null)) {
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

	public final int search(final String text, int startIndex, int endIndex, boolean ignoreCase) {
		int count = 0;
		ZLSearchPattern pattern = new ZLSearchPattern(text, ignoreCase);
		myMarks = new ArrayList<ZLTextMark>();
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

	public final List<ZLTextMark> getMarks() {
		return (myMarks != null) ? myMarks : Collections.<ZLTextMark>emptyList();
	}	

	public final void removeAllMarks() {
		myMarks = null;
	}

	public final int getParagraphsNumber() {
		return myParagraphsNumber;
	}

	public final ZLTextParagraph getParagraph(int index) {
		final byte kind = myParagraphKinds[index];
		return (kind == ZLTextParagraph.Kind.TEXT_PARAGRAPH) ?
			new ZLTextParagraphImpl(this, index) :
			new ZLTextSpecialParagraphImpl(kind, this, index);
	}

	public final int getTextLength(int index) {
		return myTextSizes[Math.max(Math.min(index, myParagraphsNumber - 1), 0)];
	}

	private static int binarySearch(int[] array, int length, int value) {
		int lowIndex = 0;
		int highIndex = length - 1;

		while (lowIndex <= highIndex) {
			int midIndex = (lowIndex + highIndex) >>> 1;
			int midValue = array[midIndex];
			if (midValue > value) {
				highIndex = midIndex - 1;
			} else if (midValue < value) {
				lowIndex = midIndex + 1;
			} else {
				return midIndex;
			}
		}
		return -lowIndex - 1;
	}

	public final int findParagraphByTextLength(int length) {
		int index = binarySearch(myTextSizes, myParagraphsNumber, length);
		if (index >= 0) {
			return index;
		}
		return Math.min(-index - 1, myParagraphsNumber - 1);
	}
}
