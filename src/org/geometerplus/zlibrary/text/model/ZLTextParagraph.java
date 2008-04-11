package org.geometerplus.zlibrary.text.model;

import org.geometerplus.zlibrary.text.model.impl.ZLImageEntry;

public interface ZLTextParagraph {
	interface Entry {
		byte TEXT = 1;
		byte IMAGE = 2;
		byte CONTROL = 3;
		byte FORCED_CONTROL = 4;
		byte FIXED_HSPACE = 5;
	}

	interface EntryIterator {
		byte getType();

		char[] getTextData();
		int getTextOffset();
		int getTextLength();

		byte getControlKind();
		boolean getControlIsStart();
		boolean getControlIsHyperlink();
		String getHyperlinkControlLabel();

		ZLImageEntry getImageEntry();

		short getFixedHSpaceLength();

		boolean hasNext();
		void next();
	}

	public EntryIterator iterator();

	interface Kind {
		byte TEXT_PARAGRAPH = 0;
		byte TREE_PARAGRAPH = 1;
		byte EMPTY_LINE_PARAGRAPH = 2;
		byte BEFORE_SKIP_PARAGRAPH = 3;
		byte AFTER_SKIP_PARAGRAPH = 4;
		byte END_OF_SECTION_PARAGRAPH = 5;
		byte END_OF_TEXT_PARAGRAPH = 6;
	};

	byte getKind();
}
