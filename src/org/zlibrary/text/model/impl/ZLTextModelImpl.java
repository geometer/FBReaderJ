package org.zlibrary.text.model.impl;

import org.zlibrary.core.util.ZLTextBuffer;
import org.zlibrary.core.image.ZLImage;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.entry.ZLTextForcedControlEntry;
import org.zlibrary.text.model.entry.ZLTextParagraphEntry;

import java.util.ArrayList;
import java.util.Map;

abstract class ZLTextModelImpl implements ZLTextModel {
	private final ArrayList<ZLTextParagraph> myParagraphs = new ArrayList<ZLTextParagraph>();

	private static final int DATA_BLOCK_SIZE = 102400;
	private int myBlockOffset = 0;
	private boolean myBlockOverflow = true;
	private final ArrayList<char[]> myData = new ArrayList<char[]>();

	public int getParagraphsNumber() {
		return myParagraphs.size();
	}

	public ZLTextParagraph getParagraph(int index) {
		return myParagraphs.get(index);
	}

	void addParagraphInternal(ZLTextParagraph paragraph) {
		myParagraphs.add(paragraph);
	}
	
	public void removeParagraphInternal(int index) {
		myParagraphs.remove(index);
	}

	private ZLTextParagraph getLastParagraph() {
		return myParagraphs.get(myParagraphs.size() - 1);
	}

	public void addControl(byte textKind, boolean isStart) {
		getLastParagraph().addEntry(EntryPool.getControlEntry(textKind, isStart));
	}

	private final char[] getDataBlock() {
		final ArrayList<char[]> data = myData;
		if (!myBlockOverflow && (myBlockOffset < DATA_BLOCK_SIZE)) {
			return data.get(data.size() - 1);
		}
		char[] block = new char[DATA_BLOCK_SIZE];
		data.add(block);
		myBlockOffset = 0;
		myBlockOverflow = false;
		return block;
	}

	public void addText(char[] text) {
		addText(text, text.length);
	}

	public void addText(ZLTextBuffer buffer) {
		addText(buffer.getData(), buffer.getLength());
	}

	private void addText(char[] text, int length) {
		if (length > DATA_BLOCK_SIZE) {
			length = DATA_BLOCK_SIZE;
		}
		if (length > DATA_BLOCK_SIZE - myBlockOffset) {
			myBlockOverflow = true;
		}
		final char[] block = getDataBlock();
		final int blockOffset = myBlockOffset;
		System.arraycopy(text, 0, block, blockOffset, length);
		getLastParagraph().addEntry(new ZLTextEntryImpl(block, blockOffset, length));
		myBlockOffset += length;
	}

	/*
	public void addText(ArrayList<char[]> text) {
		int index = 0;
		final int arraySize = text.size();
		while (index < arraySize) {
			final char[] block = getDataBlock();
			int blockOffset = myBlockOffset;
			for (; index < arraySize; ++index) {
				final char[] part = text.get(index);
				final int length = (part.length < DATA_BLOCK_SIZE) ? part.length : DATA_BLOCK_SIZE;
				if (length > DATA_BLOCK_SIZE - blockOffset) {
					myBlockOverflow = true;
					break;
				}
				System.arraycopy(part, 0, block, blockOffset, length);
				blockOffset += length;
			}
			getLastParagraph().addEntry(new ZLTextEntryImpl(block, myBlockOffset, blockOffset - myBlockOffset));
			myBlockOffset = blockOffset;
		}
	}
	*/
	
	public void addControl(ZLTextForcedControlEntry entry) {
		getLastParagraph().addEntry(entry);
	}
	
	public void addHyperlinkControl(byte textKind, String label) {
		getLastParagraph().addEntry(new ZLTextHyperlinkControlEntryImpl(textKind, label));
	}
	
	public void addImage(String id, Map<String, ZLImage> imageMap, short vOffset) {
		getLastParagraph().addEntry(new ZLImageEntryImpl(id, imageMap, vOffset));
	}
	
	public void addFixedHSpace(byte length) {
		getLastParagraph().addEntry(new ZLTextFixedHSpaceEntryImpl(length));
	}	

	public String dump() {
		StringBuilder sb = new StringBuilder();
		for (ZLTextParagraph paragraph: myParagraphs) {
			sb.append("[PARAGRAPH]\n");
			for (ZLTextParagraphEntry entry: paragraph.getEntries()) {
				if (entry instanceof ZLTextEntryImpl) {
					ZLTextEntryImpl textEntry = (ZLTextEntryImpl)entry;
					sb.append("[TEXT]");
					sb.append(textEntry.getData(), textEntry.getDataOffset(), textEntry.getDataLength());
					sb.append("[/TEXT]");
				} else if (entry instanceof ZLTextControlEntryImpl) {
					ZLTextControlEntryImpl entryControl = (ZLTextControlEntryImpl)entry;
					if (entryControl.isStart())
						sb.append("[CONTROL "+entryControl.getKind()+"]");
					else
						sb.append("[/CONTROL "+entryControl.getKind()+"]");					
				}
			}
			sb.append("[/PARAGRAPH]\n");
		}
		return sb.toString();
	}
}
