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

package org.geometerplus.fbreader.fbreader;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.config.ZLConfig;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.impl.*;

public class BookTextView extends FBView {
	private static final String BUFFER_SIZE = "UndoBufferSize";
	private static final String POSITION_IN_BUFFER = "PositionInBuffer";
	private static final String PARAGRAPH_PREFIX = "Paragraph_";
	private static final String WORD_PREFIX = "Word_";
	private static final String CHAR_PREFIX = "Char_";
//	private static final String MODEL_PREFIX = "Model_";

	private static final int MAX_UNDO_STACK_SIZE = 20;
	
	private ZLIntegerOption myParagraphIndexOption;
	private ZLIntegerOption myWordIndexOption;
	private ZLIntegerOption myCharIndexOption;

	private ArrayList<ZLTextPosition> myPositionStack = new ArrayList<ZLTextPosition>();
	private int myCurrentPointInStack;

	private String myFileName;

	BookTextView(ZLPaintContext context) {
		super(context);
	}
	
	public void setModel(ZLTextModel model, String fileName) {
		super.setModel(model);

		myFileName = fileName;

		myPositionStack.clear();

		final int stackSize = new ZLIntegerRangeOption(fileName, BUFFER_SIZE, 0, MAX_UNDO_STACK_SIZE, 0).getValue();
		myCurrentPointInStack = new ZLIntegerRangeOption(fileName, POSITION_IN_BUFFER, 0, (stackSize == 0) ? 0 : (stackSize - 1), 0).getValue();

		if (model != null) {
			final ZLIntegerOption option = new ZLIntegerOption(fileName, "", 0);
			for (int i = 0; i < stackSize; ++i) {
				option.changeName(PARAGRAPH_PREFIX + i);
				int paragraphIndex = option.getValue();
				option.changeName(WORD_PREFIX + i);
				final int wordIndex = option.getValue();
				option.changeName(CHAR_PREFIX + i);
				final int charIndex = option.getValue();
				myPositionStack.add(new ZLTextPosition(paragraphIndex, wordIndex, charIndex));
			}
			if (!myPositionStack.isEmpty()) {
				gotoPosition(myPositionStack.get(myCurrentPointInStack));
			}
		}
	}

	protected void onPaintInfoPrepared() {
		if (myPositionStack.isEmpty()) {
			myPositionStack.add(new ZLTextPosition(getStartCursor()));
		} else {
			myPositionStack.get(myCurrentPointInStack).set(getStartCursor());
		}
	}

	void scrollToHome() {
		final ZLTextWordCursor cursor = getStartCursor();
		if (!cursor.isNull() && cursor.isStartOfParagraph() && cursor.getParagraphCursor().Index == 0) {
			return;
		}
		final ZLTextPosition position = new ZLTextPosition(cursor);
		gotoParagraph(0, false);
		gotoPosition(0, 0, 0);
		preparePaintInfo();
		savePosition(position, getStartCursor());
		ZLApplication.Instance().repaintView();
	}

	public void gotoParagraphSafe(int paragraphIndex) {
		preparePaintInfo();
		final ZLTextWordCursor cursor = getStartCursor();
		if (!cursor.isNull()) {
			final ZLTextPosition position = new ZLTextPosition(cursor);
			gotoParagraph(paragraphIndex, false);
			preparePaintInfo();
			savePosition(position, getStartCursor());
		}
	}
	
	public boolean onStylusPress(int x, int y) {
		ZLTextElementArea area = getElementByCoordinates(x, y);
		if (area != null) {
			ZLTextElement element = area.Element;
			if ((element instanceof ZLTextImageElement) ||
					(element instanceof ZLTextWord)) {
				final ZLTextWordCursor cursor = new ZLTextWordCursor(getStartCursor());
				cursor.moveToParagraph(area.ParagraphIndex);
				cursor.moveToParagraphStart();
				final int elementIndex = area.TextElementIndex;
				byte hyperlinkKind = FBTextKind.REGULAR;
				String id = null;
				for (int i = 0; i < elementIndex; ++i) {
					ZLTextElement e = cursor.getElement();
					if (e instanceof ZLTextControlElement) {
						if (e instanceof ZLTextHyperlinkControlElement) {
							final ZLTextHyperlinkControlElement control = (ZLTextHyperlinkControlElement)e;
							hyperlinkKind = control.Kind;
							id = control.Label;
						} else {
							final ZLTextControlElement control = (ZLTextControlElement)e;
							if (!control.IsStart && (control.Kind == hyperlinkKind)) {
								hyperlinkKind = FBTextKind.REGULAR;
								id = null;
							}
						}
					}
					cursor.nextWord();
				}
				if (id != null) {
					switch (hyperlinkKind) {
						case FBTextKind.EXTERNAL_HYPERLINK:
							ZLibrary.Instance().openInBrowser(id);
							return true;
						case FBTextKind.FOOTNOTE:
						case FBTextKind.INTERNAL_HYPERLINK:
							((FBReader)ZLApplication.Instance()).tryOpenFootnote(id);
							return true;
					}
				}
			}
		}

		return super.onStylusPress(x, y);
	}
	
	public String getFileName() {
		return this.myFileName;
	}
	
	protected void savePosition(ZLTextPosition position) {
		if (myPositionStack.isEmpty()) {
			preparePaintInfo();
		}
		final ZLTextPosition currentPosition = myPositionStack.get(myCurrentPointInStack);
		while (myPositionStack.size() > myCurrentPointInStack) {
			myPositionStack.remove(myPositionStack.size() - 1);
		}
		myPositionStack.add(position);
		myPositionStack.add(currentPosition);
		while (myPositionStack.size() >= MAX_UNDO_STACK_SIZE) {
			myPositionStack.remove(0);
		}
		myCurrentPointInStack = myPositionStack.size() - 1;
	}

	void saveState() {
		new Thread(new Runnable() {
			public void run() {
				ZLConfig.Instance().executeAsATransaction(new Runnable() {
					public void run() {
						saveStateInternal();
					}
				});
			}
		}).start();
	}

	private void saveStateInternal() {
		if (getModel() == null) {
			return;
		}

		final String group = getFileName();
		
		new ZLIntegerOption(group, BUFFER_SIZE, 0).setValue(myPositionStack.size());
		new ZLIntegerOption(group, POSITION_IN_BUFFER, 0).setValue(myCurrentPointInStack);

		final ZLIntegerOption option = new ZLIntegerOption(group, "", 0);
		for (int i = 0; i < myPositionStack.size(); ++i) {
			final ZLTextPosition position = myPositionStack.get(i);
			option.changeName(PARAGRAPH_PREFIX + i);
			option.setValue(position.ParagraphIndex);
			option.changeName(CHAR_PREFIX + i);
			option.setValue(position.CharIndex);
			option.changeName(WORD_PREFIX + i);
			option.setValue(position.WordIndex);
		}
	}

	boolean canUndoPageMove() {
		return myCurrentPointInStack > 0;
	}

	void undoPageMove() {
		gotoPosition(myPositionStack.get(--myCurrentPointInStack));
		ZLApplication.Instance().repaintView();
	}

	boolean canRedoPageMove() {
		return myCurrentPointInStack < myPositionStack.size() - 1;
	}

	void redoPageMove() {
		gotoPosition(myPositionStack.get(++myCurrentPointInStack));
		ZLApplication.Instance().repaintView();
	}
}
