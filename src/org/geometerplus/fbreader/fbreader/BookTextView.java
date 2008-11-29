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
//	private static final String MODEL_PREFIX = "Model_";

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

	public void setModels(ArrayList/*<ZLTextModel>*/ models, String fileName) {
		myFileName = fileName;

		myPositionStack.clear();

		final int stackSize = new ZLIntegerRangeOption(ZLOption.STATE_CATEGORY, fileName, BUFFER_SIZE, 0, MAX_UNDO_STACK_SIZE, 0).getValue();
		myCurrentPointInStack = new ZLIntegerRangeOption(ZLOption.STATE_CATEGORY, fileName, POSITION_IN_BUFFER, 0, (stackSize == 0) ? 0 : (stackSize - 1), 0).getValue();

		if (models != null) {
			final ZLIntegerOption option = new ZLIntegerOption(ZLOption.STATE_CATEGORY, fileName, "", 0);
			final int size = models.size();
			for (int i = 0; i < stackSize; ++i) {
		//		option.changeName(MODEL_PREFIX + i);
		//		final int modelIndex = option.getValue();
				option.changeName(PARAGRAPH_PREFIX + i);
				int paragraphIndex = option.getValue();
				int modelIndex = -1;
				int paragraphsNumber = 0;
				while (paragraphIndex >= 0 && paragraphsNumber != 1) {
					modelIndex++;
					paragraphsNumber = modelIndex >= 0 && modelIndex < size ? 
							((ZLTextModel)models.get(modelIndex)).getParagraphsNumber() + 1 : 1;
					paragraphIndex -= paragraphsNumber;
				}
				paragraphIndex += paragraphsNumber;
				option.changeName(WORD_PREFIX + i);
				final int wordIndex = option.getValue();
				option.changeName(CHAR_PREFIX + i);
				final int charIndex = option.getValue();
				myPositionStack.add(new Position(modelIndex, paragraphIndex,
						wordIndex, charIndex));
			}
		}
		if (!myPositionStack.isEmpty()) {
			super.setModels(models, ((Position)myPositionStack.get(myCurrentPointInStack)).ModelIndex);
		} else {
			super.setModels(models, 0);
		}
		if ((getModel() != null) && (!myPositionStack.isEmpty())) {
			gotoPosition((Position)myPositionStack.get(myCurrentPointInStack));
		}
	}

	protected synchronized void preparePaintInfo() {
		super.preparePaintInfo();
		if (myPositionStack.isEmpty()) {
			myPositionStack.add(new Position(myCurrentModelIndex, StartCursor));
		} else {
			((Position)myPositionStack.get(myCurrentPointInStack)).set(StartCursor);
			((Position)myPositionStack.get(myCurrentPointInStack)).ModelIndex = myCurrentModelIndex;

		//	Position position = (Position)myPositionStack.get(myCurrentPointInStack);
		//	System.out.println("current position " + position.ModelIndex + " , " + position.ParagraphIndex);
		}
	}

	void scrollToHome() {
		final ZLTextWordCursor cursor = StartCursor;
		if (!cursor.isNull() && cursor.isStartOfParagraph() && cursor.getParagraphCursor().Index == 0
				&& myCurrentModelIndex == 0) {
			return;
		}
		final int modelIndexToCheck = myCurrentModelIndex;
		setModelIndex(0);
		final Position position = new Position(modelIndexToCheck, cursor);
		gotoParagraph(0, false);
		gotoPosition(0, 0, 0);
		preparePaintInfo();
		savePosition(position, 0, StartCursor);
		Application.refreshWindow();
	}

	//TODO: remove
	void gotoParagraphSafe(int paragraphIndex) {
//		gotoParagraphSafe(paragraphIndex, myCurrentModelIndex);
		preparePaintInfo();
		final ZLTextWordCursor cursor = StartCursor;
		if (cursor != null) {
			final Position position = new Position(myCurrentModelIndex, cursor);
			gotoParagraph(paragraphIndex, false);
			preparePaintInfo();
			savePosition(position, myCurrentModelIndex, StartCursor);
		}
	}

	public void gotoParagraphSafe(ZLTextModel model, int paragraphIndex) {
		gotoParagraphSafe(getModelList().indexOf(model), paragraphIndex);
	}
	
	void gotoParagraphSafe(int modelIndex, int paragraphIndex) {
		preparePaintInfo();
		final ZLTextWordCursor cursor = StartCursor;
		if (cursor != null) {
			final Position position = new Position(myCurrentModelIndex, cursor);
			setModelIndex(modelIndex);
			gotoParagraph(paragraphIndex, false);
			preparePaintInfo();
			savePosition(position, modelIndex, StartCursor);
		}
	}
	
	public boolean onStylusRelease(int x, int y) {
		if (super.onStylusRelease(x, y)) {
			return false;
		}

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
								ZLibrary.Instance().openInBrowser(id);
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
		
		for (Object p : myPositionStack) {
			System.out.print(((Position) p).ModelIndex + ","
					+ ((Position) p).ParagraphIndex + "; ");
		}
		System.out.println("current position " + myCurrentPointInStack);
	}

	public void saveState() {
		if (getModel() == null) {
			return;
		}

		final String group = getFileName();
		
		new ZLIntegerOption(ZLOption.STATE_CATEGORY, group, BUFFER_SIZE, 0).setValue(myPositionStack.size());
		new ZLIntegerOption(ZLOption.STATE_CATEGORY, group, POSITION_IN_BUFFER, 0).setValue(myCurrentPointInStack);

		final ZLIntegerOption option = new ZLIntegerOption(ZLOption.STATE_CATEGORY, group, "", 0);
		for (int i = 0; i < myPositionStack.size(); ++i) {
			Position position = (Position)myPositionStack.get(i);
			option.changeName(PARAGRAPH_PREFIX + i);
			int paragraphIndex = position.ParagraphIndex + position.ModelIndex;
			final ArrayList models = getModelList();
			for (int j = 0; j < position.ModelIndex; j++) {
				paragraphIndex += ((ZLTextModel)models.get(j)).getParagraphsNumber();
			}
			
			option.setValue(paragraphIndex);
			
			option.changeName(CHAR_PREFIX + i);
			option.setValue(position.CharIndex);
			
			option.changeName(WORD_PREFIX + i);
			option.setValue(position.WordIndex);
			
//			option.changeName(MODEL_PREFIX + i);
//			option.setValue(position.ModelIndex);
		}
	}

	boolean canUndoPageMove() {
		return myCurrentPointInStack > 0;
	}

	void undoPageMove() {
		gotoPosition((Position)myPositionStack.get(--myCurrentPointInStack));
		((FBReader)Application).refreshWindow();
		
		for (Object p : myPositionStack) {
			System.out.print(((Position) p).ModelIndex + ","
					+ ((Position) p).ParagraphIndex + "; ");
		}
		System.out.println("current position " + myCurrentPointInStack);
	}

	boolean canRedoPageMove() {
		return myCurrentPointInStack < myPositionStack.size() - 1;
	}

	void redoPageMove() {
		gotoPosition((Position)myPositionStack.get(++myCurrentPointInStack));
		((FBReader)Application).refreshWindow();
		
		for (Object p : myPositionStack) {
			System.out.print(((Position) p).ModelIndex + ","
					+ ((Position) p).ParagraphIndex + "; ");
		}
		System.out.println("current position " + myCurrentPointInStack);
	}

}
