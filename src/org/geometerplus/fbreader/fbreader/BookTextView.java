/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.impl.*;

public class BookTextView extends FBView {
	private static final String BUFFER_SIZE = "UndoBufferSize";
	private static final String POSITION_IN_BUFFER = "PositionInBuffer";
	private static final String PARAGRAPH_PREFIX = "Paragraph_";
	private static final String WORD_PREFIX = "Word_";
	private static final String CHAR_PREFIX = "Char_";

	private static final int MAX_UNDO_STACK_SIZE = 20;
	
	private ZLIntegerOption myParagraphIndexOption;
	private ZLIntegerOption myWordIndexOption;
	private ZLIntegerOption myCharIndexOption;
	private ZLTextModel myContentsModel;

	private ArrayList myPositionStack = new ArrayList();
	private int myCurrentPointInStack;

	private String myFileName;

	public ZLBooleanOption ShowTOCMarksOption;

	public final ZLBooleanOption OpenInBrowserOption =
		new ZLBooleanOption(ZLOption.CONFIG_CATEGORY, "Web Browser", "Enabled", true);
	
	BookTextView(FBReader fbreader, ZLPaintContext context) {
		super(fbreader, context);
		ShowTOCMarksOption = new ZLBooleanOption(ZLOption.LOOK_AND_FEEL_CATEGORY, "Indicator", "ShowTOCMarks", false);
	}
	
	public void setContentsModel(ZLTextModel contentsModel) {
		myContentsModel = contentsModel;
	}

	public void setModel(ZLTextModel model, String fileName) {
		super.setModel(model);
		myFileName = fileName;

		myPositionStack.clear();

		final int stackSize = new ZLIntegerRangeOption(ZLOption.STATE_CATEGORY, fileName, BUFFER_SIZE, 0, MAX_UNDO_STACK_SIZE, 0).getValue();
		myCurrentPointInStack = new ZLIntegerRangeOption(ZLOption.STATE_CATEGORY, fileName, POSITION_IN_BUFFER, 0, (stackSize == 0) ? 0 : (stackSize - 1), 0).getValue();

		for (int i = 0; i < stackSize; ++i) {
			myPositionStack.add(new Position(
				new ZLIntegerOption(ZLOption.STATE_CATEGORY, fileName, PARAGRAPH_PREFIX + i, 0).getValue(),
				new ZLIntegerOption(ZLOption.STATE_CATEGORY, fileName, WORD_PREFIX + i, 0).getValue(),
				new ZLIntegerOption(ZLOption.STATE_CATEGORY, fileName, CHAR_PREFIX + i, 0).getValue()
			));
		}

		if ((model != null) && (!myPositionStack.isEmpty())) {
			gotoPosition((Position)myPositionStack.get(myCurrentPointInStack));
		}
	}

	protected synchronized void preparePaintInfo() {
		super.preparePaintInfo();
		if (myPositionStack.isEmpty()) {
			myPositionStack.add(new Position(StartCursor));
		} else {
			((Position)myPositionStack.get(myCurrentPointInStack)).set(StartCursor);
		}
	}

	void scrollToHome() {
		final ZLTextWordCursor cursor = StartCursor;
		if (!cursor.isNull() && cursor.isStartOfParagraph() && cursor.getParagraphCursor().Index == 0) {
			return;
		}
		final Position position = new Position(cursor);
		gotoParagraph(0, false);
		gotoPosition(0, 0, 0);
		preparePaintInfo();
		if (!position.equalsToCursor(StartCursor)) {
			savePosition(position);
		}
		Application.refreshWindow();
	}

	void gotoParagraphSafe(int paragraphIndex) {
		preparePaintInfo();
		final ZLTextWordCursor cursor = StartCursor;
		if (cursor != null) {
			final Position position = new Position(cursor);
			gotoParagraph(paragraphIndex, false);
			preparePaintInfo();
			if (!position.equalsToCursor(StartCursor)) {
				savePosition(position);
			}
		}
	}

	public boolean _onStylusPress(int x, int y) {
		ZLTextElementArea area = getElementByCoordinates(x, y);
		if (area != null) {
			ZLTextElement element = area.Element;
			if ((element instanceof ZLTextImageElement) ||
					(element instanceof ZLTextWord)) {
				final ZLTextWordCursor cursor = new ZLTextWordCursor(StartCursor);
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
							if (OpenInBrowserOption.getValue()) {
								ZLibrary.getInstance().openInBrowser(id);
							}
							return true;
						case FBTextKind.FOOTNOTE:
						case FBTextKind.INTERNAL_HYPERLINK:
							((FBReader)Application).tryOpenFootnote(id);
							return true;
					}
				}
			}
		}
		return false;
	}
	
	public String getFileName() {
		return this.myFileName;
	}
	
	protected void savePosition(Position position) {
		if (myPositionStack.isEmpty()) {
			preparePaintInfo();
		}
		Position currentPosition =
			(Position)myPositionStack.get(myCurrentPointInStack);
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

	public void saveState() {
		if (getModel() == null) {
			return;
		}

		final String group = getFileName();
		
		new ZLIntegerOption(ZLOption.STATE_CATEGORY, group, BUFFER_SIZE, 0).setValue(myPositionStack.size());
		new ZLIntegerOption(ZLOption.STATE_CATEGORY, group, POSITION_IN_BUFFER, 0).setValue(myCurrentPointInStack);

		for (int i = 0; i < myPositionStack.size(); ++i) {
			Position position = (Position)myPositionStack.get(i);
			new ZLIntegerOption(ZLOption.STATE_CATEGORY, group, PARAGRAPH_PREFIX + i, 0).setValue(position.ParagraphIndex);
			new ZLIntegerOption(ZLOption.STATE_CATEGORY, group, WORD_PREFIX + i, 0).setValue(position.WordIndex);
			new ZLIntegerOption(ZLOption.STATE_CATEGORY, group, CHAR_PREFIX + i, 0).setValue(position.CharIndex);
		}
	}

	boolean canUndoPageMove() {
		return myCurrentPointInStack > 0;
	}

	void undoPageMove() {
		gotoPosition((Position)myPositionStack.get(--myCurrentPointInStack));
		((FBReader)Application).refreshWindow();
	}

	boolean canRedoPageMove() {
		return myCurrentPointInStack < myPositionStack.size() - 1;
	}

	void redoPageMove() {
		gotoPosition((Position)myPositionStack.get(++myCurrentPointInStack));
		((FBReader)Application).refreshWindow();
	}
}
