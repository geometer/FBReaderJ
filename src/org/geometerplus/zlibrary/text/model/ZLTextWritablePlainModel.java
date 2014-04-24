/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

import java.util.Map;

import org.geometerplus.zlibrary.core.fonts.FontManager;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.util.*;

public final class ZLTextWritablePlainModel extends ZLTextPlainModel implements ZLTextWritableModel {
	private char[] myCurrentDataBlock;
	private int myBlockOffset;

	public ZLTextWritablePlainModel(String id, String language, int arraySize, int dataBlockSize, String directoryName, String extension, Map<String,ZLImage> imageMap, FontManager fontManager) {
		super(
			id, language,
			new int[arraySize], new int[arraySize],
			new int[arraySize], new int[arraySize],
			new byte[arraySize],
			new CachedCharStorage(dataBlockSize, directoryName, extension),
			imageMap,
			fontManager
		);
	}

	private void extend() {
		final int size = myStartEntryIndices.length;
		myStartEntryIndices = ZLArrayUtils.createCopy(myStartEntryIndices, size, size << 1);
		myStartEntryOffsets = ZLArrayUtils.createCopy(myStartEntryOffsets, size, size << 1);
		myParagraphLengths = ZLArrayUtils.createCopy(myParagraphLengths, size, size << 1);
		myTextSizes = ZLArrayUtils.createCopy(myTextSizes, size, size << 1);
		myParagraphKinds = ZLArrayUtils.createCopy(myParagraphKinds, size, size << 1);
	}

	public void createParagraph(byte kind) {
		final int index = myParagraphsNumber++;
		int[] startEntryIndices = myStartEntryIndices;
		if (index == startEntryIndices.length) {
			extend();
			startEntryIndices = myStartEntryIndices;
		}
		if (index > 0) {
			myTextSizes[index] = myTextSizes[index - 1];
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

	public void addText(char[] text) {
		addText(text, 0, text.length);
	}

	public void addText(char[] text, int offset, int length) {
		char[] block = getDataBlock(3 + length);
		++myParagraphLengths[myParagraphsNumber - 1];
		int blockOffset = myBlockOffset;
		block[blockOffset++] = (char)ZLTextParagraph.Entry.TEXT;
		block[blockOffset++] = (char)length;
		block[blockOffset++] = (char)(length >> 16);
		System.arraycopy(text, offset, block, blockOffset, length);
		myBlockOffset = blockOffset + length;
		myTextSizes[myParagraphsNumber - 1] += length;
	}

	public void addImage(String id, short vOffset, boolean isCover) {
		final int len = id.length();
		final char[] block = getDataBlock(4 + len);
		++myParagraphLengths[myParagraphsNumber - 1];
		int blockOffset = myBlockOffset;
		block[blockOffset++] = (char)ZLTextParagraph.Entry.IMAGE;
		block[blockOffset++] = (char)vOffset;
		block[blockOffset++] = (char)len;
		id.getChars(0, len, block, blockOffset);
		blockOffset += len;
		block[blockOffset++] = (char)(isCover ? 1 : 0);
		myBlockOffset = blockOffset;
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

	public void addHyperlinkControl(byte textKind, byte hyperlinkType, String label) {
		final short labelLength = (short)label.length();
		final char[] block = getDataBlock(3 + labelLength);
		++myParagraphLengths[myParagraphsNumber - 1];
		int blockOffset = myBlockOffset;
		block[blockOffset++] = (char)ZLTextParagraph.Entry.HYPERLINK_CONTROL;
		block[blockOffset++] = (char)((hyperlinkType << 8) + textKind);
		block[blockOffset++] = (char)labelLength;
		label.getChars(0, labelLength, block, blockOffset);
		myBlockOffset = blockOffset + labelLength;
	}

	public void addStyleEntry(ZLTextStyleEntry entry) {
		// TODO: implement
		/*
		int len = 2;
		for (int mask = entry.getMask(); mask != 0; mask >>= 1) {
			len += mask & 1;
		}
		final char[] block = getDataBlock(len);
		++myParagraphLengths[myParagraphsNumber - 1];
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.STYLE; // CSS or OTHER
		block[myBlockOffset++] = (char)entry.getMask();
		if (entry.isLeftIndentSupported()) {
			block[myBlockOffset++] = (char)entry.getLeftIndent();
		}
		if (entry.isRightIndentSupported()) {
			block[myBlockOffset++] = (char)entry.getRightIndent();
		}
		if (entry.isAlignmentTypeSupported()) {
			block[myBlockOffset++] = (char)entry.getAlignmentType();
		}
		*/
	}

	public void addFixedHSpace(short length) {
		final char[] block = getDataBlock(2);
		++myParagraphLengths[myParagraphsNumber - 1];
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.FIXED_HSPACE;
		block[myBlockOffset++] = (char)length;
	}

	public void addBidiReset() {
		final char[] block = getDataBlock(1);
		++myParagraphLengths[myParagraphsNumber - 1];
		block[myBlockOffset++] = (char)ZLTextParagraph.Entry.RESET_BIDI;
	}

	public void stopReading() {
		/*
		if (myCurrentDataBlock != null) {
			myStorage.freezeLastBlock();
			myCurrentDataBlock = null;
		}
		final int size = myParagraphsNumber;
		myStartEntryIndices = ZLArrayUtils.createCopy(myStartEntryIndices, size, size);
		myStartEntryOffsets = ZLArrayUtils.createCopy(myStartEntryOffsets, size, size);
		myParagraphLengths = ZLArrayUtils.createCopy(myParagraphLengths, size, size);
		myParagraphKinds = ZLArrayUtils.createCopy(myParagraphKinds, size, size);
		*/
	}
}
