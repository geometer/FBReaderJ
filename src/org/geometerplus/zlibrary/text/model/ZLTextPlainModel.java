/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import org.geometerplus.zlibrary.core.fonts.FontManager;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.util.*;

public final class ZLTextPlainModel implements ZLTextModel, ZLTextStyleEntry.Feature {
	private final String myId;
	private final String myLanguage;

	private int[] myStartEntryIndices;
	private int[] myStartEntryOffsets;
	private int[] myParagraphLengths;
	private int[] myTextSizes;
	private byte[] myParagraphKinds;

	private int myParagraphsNumber;

	private final CachedCharStorage myStorage;
	private final Map<String,ZLImage> myImageMap;

	private ArrayList<ZLTextMark> myMarks;

	private final FontManager myFontManager;

	final class EntryIteratorImpl implements ZLTextParagraph.EntryIterator {
		private int myCounter;
		private int myLength;
		private byte myType;

		int myDataIndex;
		int myDataOffset;

		// TextEntry data
		private char[] myTextData;
		private int myTextOffset;
		private int myTextLength;

		// ControlEntry data
		private byte myControlKind;
		private boolean myControlIsStart;
		// HyperlinkControlEntry data
		private byte myHyperlinkType;
		private String myHyperlinkId;

		// ImageEntry
		private ZLImageEntry myImageEntry;

		// VideoEntry
		private ZLVideoEntry myVideoEntry;

		// ExtensionEntry
		private ExtensionEntry myExtensionEntry;

		// StyleEntry
		private ZLTextStyleEntry myStyleEntry;

		// FixedHSpaceEntry data
		private short myFixedHSpaceLength;

		EntryIteratorImpl(int index) {
			reset(index);
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

		public ZLVideoEntry getVideoEntry() {
			return myVideoEntry;
		}

		public ExtensionEntry getExtensionEntry() {
			return myExtensionEntry;
		}

		public ZLTextStyleEntry getStyleEntry() {
			return myStyleEntry;
		}

		public short getFixedHSpaceLength() {
			return myFixedHSpaceLength;
		}

		public boolean next() {
			if (myCounter >= myLength) {
				return false;
			}

			int dataOffset = myDataOffset;
			char[] data = myStorage.block(myDataIndex);
			if (data == null) {
				return false;
			}
			if (dataOffset >= data.length) {
				data = myStorage.block(++myDataIndex);
				if (data == null) {
					return false;
				}
				dataOffset = 0;
			}
			short first = (short)data[dataOffset];
			byte type = (byte)first;
			if (type == 0) {
				data = myStorage.block(++myDataIndex);
				if (data == null) {
					return false;
				}
				dataOffset = 0;
				first = (short)data[0];
				type = (byte)first;
			}
			myType = type;
			++dataOffset;
			switch (type) {
				case ZLTextParagraph.Entry.TEXT:
				{
					int textLength = (int)data[dataOffset++];
					textLength += (((int)data[dataOffset++]) << 16);
					textLength = Math.min(textLength, data.length - dataOffset);
					myTextLength = textLength;
					myTextData = data;
					myTextOffset = dataOffset;
					dataOffset += textLength;
					break;
				}
				case ZLTextParagraph.Entry.CONTROL:
				{
					short kind = (short)data[dataOffset++];
					myControlKind = (byte)kind;
					myControlIsStart = (kind & 0x0100) == 0x0100;
					myHyperlinkType = 0;
					break;
				}
				case ZLTextParagraph.Entry.HYPERLINK_CONTROL:
				{
					final short kind = (short)data[dataOffset++];
					myControlKind = (byte)kind;
					myControlIsStart = true;
					myHyperlinkType = (byte)(kind >> 8);
					final short labelLength = (short)data[dataOffset++];
					myHyperlinkId = new String(data, dataOffset, labelLength);
					dataOffset += labelLength;
					break;
				}
				case ZLTextParagraph.Entry.IMAGE:
				{
					final short vOffset = (short)data[dataOffset++];
					final short len = (short)data[dataOffset++];
					final String id = new String(data, dataOffset, len);
					dataOffset += len;
					final boolean isCover = data[dataOffset++] != 0;
					myImageEntry = new ZLImageEntry(myImageMap, id, vOffset, isCover);
					break;
				}
				case ZLTextParagraph.Entry.FIXED_HSPACE:
					myFixedHSpaceLength = (short)data[dataOffset++];
					break;
				case ZLTextParagraph.Entry.STYLE_CSS:
				case ZLTextParagraph.Entry.STYLE_OTHER:
				{
					final short depth = (short)((first >> 8) & 0xFF);
					final ZLTextStyleEntry entry =
						type == ZLTextParagraph.Entry.STYLE_CSS
							? new ZLTextCSSStyleEntry(depth)
							: new ZLTextOtherStyleEntry();

					final short mask = (short)data[dataOffset++];
					for (int i = 0; i < NUMBER_OF_LENGTHS; ++i) {
						if (ZLTextStyleEntry.isFeatureSupported(mask, i)) {
							final short size = (short)data[dataOffset++];
							final byte unit = (byte)data[dataOffset++];
							entry.setLength(i, size, unit);
						}
					}
					if (ZLTextStyleEntry.isFeatureSupported(mask, ALIGNMENT_TYPE) ||
						ZLTextStyleEntry.isFeatureSupported(mask, NON_LENGTH_VERTICAL_ALIGN)) {
						final short value = (short)data[dataOffset++];
						if (ZLTextStyleEntry.isFeatureSupported(mask, ALIGNMENT_TYPE)) {
							entry.setAlignmentType((byte)(value & 0xFF));
						}
						if (ZLTextStyleEntry.isFeatureSupported(mask, NON_LENGTH_VERTICAL_ALIGN)) {
							entry.setVerticalAlignCode((byte)((value >> 8) & 0xFF));
						}
					}
					if (ZLTextStyleEntry.isFeatureSupported(mask, FONT_FAMILY)) {
						entry.setFontFamilies(myFontManager, (short)data[dataOffset++]);
					}
					if (ZLTextStyleEntry.isFeatureSupported(mask, FONT_STYLE_MODIFIER)) {
						final short value = (short)data[dataOffset++];
						entry.setFontModifiers((byte)(value & 0xFF), (byte)((value >> 8) & 0xFF));
					}

					myStyleEntry = entry;
				}
				case ZLTextParagraph.Entry.STYLE_CLOSE:
					// No data
					break;
				case ZLTextParagraph.Entry.RESET_BIDI:
					// No data
					break;
				case ZLTextParagraph.Entry.AUDIO:
					// No data
					break;
				case ZLTextParagraph.Entry.VIDEO:
				{
					myVideoEntry = new ZLVideoEntry();
					final short mapSize = (short)data[dataOffset++];
					for (short i = 0; i < mapSize; ++i) {
						short len = (short)data[dataOffset++];
						final String mime = new String(data, dataOffset, len);
						dataOffset += len;
						len = (short)data[dataOffset++];
						final String src = new String(data, dataOffset, len);
						dataOffset += len;
						myVideoEntry.addSource(mime, src);
					}
					break;
				}
				case ZLTextParagraph.Entry.EXTENSION:
				{
					final short kindLength = (short)data[dataOffset++];
					final String kind = new String(data, dataOffset, kindLength);
					dataOffset += kindLength;

					final Map<String,String> map = new HashMap<String,String>();
					final short dataSize = (short)((first >> 8) & 0xFF);
					for (short i = 0; i < dataSize; ++i) {
						final short keyLength = (short)data[dataOffset++];
						final String key = new String(data, dataOffset, keyLength);
						dataOffset += keyLength;
						final short valueLength = (short)data[dataOffset++];
						map.put(key, new String(data, dataOffset, valueLength));
						dataOffset += valueLength;
					}
					myExtensionEntry = new ExtensionEntry(kind, map);
					break;
				}
			}
			++myCounter;
			myDataOffset = dataOffset;
			return true;
		}
	}

	public ZLTextPlainModel(
		String id,
		String language,
		int paragraphsNumber,
		int[] entryIndices,
		int[] entryOffsets,
		int[] paragraphLengths,
		int[] textSizes,
		byte[] paragraphKinds,
		String directoryName,
		String fileExtension,
		int blocksNumber,
		Map<String,ZLImage> imageMap,
		FontManager fontManager
	) {
		myId = id;
		myLanguage = language;
		myParagraphsNumber = paragraphsNumber;
		myStartEntryIndices = entryIndices;
		myStartEntryOffsets = entryOffsets;
		myParagraphLengths = paragraphLengths;
		myTextSizes = textSizes;
		myParagraphKinds = paragraphKinds;
		myStorage = new CachedCharStorage(directoryName, fileExtension, blocksNumber);
		myImageMap = imageMap;
		myFontManager = fontManager;
	}

	public final String getId() {
		return myId;
	}

	public final String getLanguage() {
		return myLanguage;
	}

	public final ZLTextMark getFirstMark() {
		return (myMarks == null || myMarks.isEmpty()) ? null : myMarks.get(0);
	}

	public final ZLTextMark getLastMark() {
		return (myMarks == null || myMarks.isEmpty()) ? null : myMarks.get(myMarks.size() - 1);
	}

	public final ZLTextMark getNextMark(ZLTextMark position) {
		if (position == null || myMarks == null) {
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
		final EntryIteratorImpl it = new EntryIteratorImpl(index);
		while (true) {
			int offset = 0;
			while (it.next()) {
				if (it.getType() == ZLTextParagraph.Entry.TEXT) {
					char[] textData = it.getTextData();
					int textOffset = it.getTextOffset();
					int textLength = it.getTextLength();
					for (ZLSearchUtil.Result res = ZLSearchUtil.find(textData, textOffset, textLength, pattern); res != null;
						res = ZLSearchUtil.find(textData, textOffset, textLength, pattern, res.Start + 1)) {
						myMarks.add(new ZLTextMark(index, offset + res.Start, res.Length));
						++count;
					}
					offset += textLength;
				}
			}
			if (++index >= endIndex) {
				break;
			}
			it.reset(index);
		}
		return count;
	}

	public final List<ZLTextMark> getMarks() {
		return myMarks != null ? myMarks : Collections.<ZLTextMark>emptyList();
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
		if (myTextSizes.length == 0) {
			return 0;
		}
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
