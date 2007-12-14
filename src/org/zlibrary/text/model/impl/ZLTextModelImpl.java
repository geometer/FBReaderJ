package org.zlibrary.text.model.impl;

import org.zlibrary.core.util.ZLTextBuffer;
import org.zlibrary.core.image.ZLImage;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;

import java.util.ArrayList;
import java.util.Map;

abstract class ZLTextModelImpl implements ZLTextModel {
	private final ArrayList<ZLTextParagraph> myParagraphs = new ArrayList<ZLTextParagraph>();

	private static final int DATA_BLOCK_SIZE = 51200;
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

	private void addEntry(ZLTextParagraph.Entry entry) {
		final ArrayList<ZLTextParagraph> paragraphs = myParagraphs;
		paragraphs.get(paragraphs.size() - 1).addEntry(entry);
	}

	public void addControl(byte textKind, boolean isStart) {
		addEntry(EntryPool.getControlEntry(textKind, isStart));
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
		addEntry(new ZLTextEntryImpl(block, blockOffset, length));
		myBlockOffset += length;
	}
	
	public void addControl(ZLTextForcedControlEntry entry) {
		addEntry(entry);
	}
	
	public void addHyperlinkControl(byte textKind, String label) {
		addEntry(new ZLTextHyperlinkControlEntry(textKind, label));
	}
	
	public void addImage(String id, Map<String,ZLImage> imageMap, short vOffset) {
		addEntry(new ZLImageEntry(id, imageMap, vOffset));
	}
	
	public void addFixedHSpace(short length) {
		addEntry(new ZLTextFixedHSpaceEntry(length));
	}	

	public String dump() {
		StringBuilder sb = new StringBuilder();
		for (ZLTextParagraph paragraph: myParagraphs) {
			sb.append("[PARAGRAPH]\n");
			for (ZLTextParagraph.Entry entry: paragraph.getEntries()) {
				if (entry instanceof ZLTextEntryImpl) {
					ZLTextEntryImpl textEntry = (ZLTextEntryImpl)entry;
					sb.append("[TEXT]");
					sb.append(textEntry.getData(), textEntry.getDataOffset(), textEntry.getDataLength());
					sb.append("[/TEXT]");
				} else if (entry instanceof ZLTextControlEntry) {
					ZLTextControlEntry controlEntry = (ZLTextControlEntry)entry;
					if (controlEntry.IsStart)
						sb.append("[CONTROL "+controlEntry.Kind+"]");
					else
						sb.append("[/CONTROL "+controlEntry.Kind+"]");					
				}
			}
			sb.append("[/PARAGRAPH]\n");
		}
		return sb.toString();
	}
}
