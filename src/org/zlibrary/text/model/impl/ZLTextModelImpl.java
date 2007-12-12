package org.zlibrary.text.model.impl;

import org.zlibrary.core.image.ZLImage;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.entry.ZLTextForcedControlEntry;
import org.zlibrary.text.model.entry.ZLTextParagraphEntry;

import java.util.ArrayList;
import java.util.Map;

class ZLTextModelImpl implements ZLTextModel {
	private final ArrayList<ZLTextParagraph> myParagraphs = new ArrayList<ZLTextParagraph>();

	public int getParagraphsNumber() {
		return myParagraphs.size();
	}

	public ZLTextParagraph getParagraph(int index) {
		return myParagraphs.get(index);
	}

	public void addParagraphInternal(ZLTextParagraph paragraph) {
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

	public void addText(char[] text) {
		getLastParagraph().addEntry(new ZLTextEntryImpl(text));
	}

	public void addText(ArrayList<char[]> text) {
		getLastParagraph().addEntry(new ZLTextEntryImpl(text));
	}
	
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
					sb.append("[TEXT]");
					sb.append(((ZLTextEntryImpl)entry).getData());
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
