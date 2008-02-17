package org.fbreader.fbreader;

import java.util.ArrayList;

import org.fbreader.bookmodel.FBTextKind;
import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.core.options.ZLIntegerOption;
import org.zlibrary.core.options.ZLOption;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.view.impl.ZLTextControlElement;
import org.zlibrary.text.view.impl.ZLTextElement;
import org.zlibrary.text.view.impl.ZLTextElementArea;
import org.zlibrary.text.view.impl.ZLTextHyperlinkControlElement;
import org.zlibrary.text.view.impl.ZLTextImageElement;
import org.zlibrary.text.view.impl.ZLTextWord;
import org.zlibrary.text.view.impl.ZLTextWordCursor;

class BookTextView extends FBView {
	private static final String PARAGRAPH_OPTION_NAME = "Paragraph";
	private static final String WORD_OPTION_NAME = "Word";
	private static final String CHAR_OPTION_NAME = "Char";
	private static final String BUFFER_SIZE = "UndoBufferSize";
	private static final String POSITION_IN_BUFFER = "PositionInBuffer";
	private static final String BUFFER_PARAGRAPH_PREFIX = "Paragraph_";
	private static final String BUFFER_WORD_PREFIX = "Word_";

	
	private ZLIntegerOption myParagraphNumberOption;
	private ZLIntegerOption myWordNumberOption;
	private ZLIntegerOption myCharNumberOption;
	private ZLTextModel myContentsModel;
    
	//todo deque
	private ArrayList/*<Position>*/ myPositionStack = new ArrayList();
	private int myCurrentPointInStack;
	private int myMaxStackSize;
    private boolean myLockUndoStackChanges;
    private String myFileName;

	
	BookTextView(FBReader fbreader, ZLPaintContext context) {
		super(fbreader, context);
	}
	
	public void setContentsModel(ZLTextModel contentsModel) {
		myContentsModel = contentsModel;
	}

	public void setModel(ZLTextModel model, String fileName) {
		super.setModel(model);
		myParagraphNumberOption = new ZLIntegerOption(ZLOption.STATE_CATEGORY, fileName, "Paragraph", 0);
		myWordNumberOption = new ZLIntegerOption(ZLOption.STATE_CATEGORY, fileName, "Word", 0);
		myCharNumberOption = new ZLIntegerOption(ZLOption.STATE_CATEGORY, fileName, "Char", 0);
		gotoPosition(myParagraphNumberOption.getValue(), myWordNumberOption.getValue(), myCharNumberOption.getValue());
	}

	protected void preparePaintInfo() {
		super.preparePaintInfo();
		final ZLTextWordCursor cursor = getStartCursor();
		if (!cursor.isNull()) {
			myParagraphNumberOption.setValue(cursor.getParagraphCursor().getIndex());
			myWordNumberOption.setValue(cursor.getWordNumber());
			myCharNumberOption.setValue(cursor.getCharNumber());
		}
	}

	void scrollToHome() {
		final ZLTextWordCursor cursor = getStartCursor();
		if (!cursor.isNull() && cursor.isStartOfParagraph() && cursor.getParagraphCursor().getIndex() == 0) {
			return;
		}
	  //gotoParagraph(0, false);
		gotoPosition(0, 0, 0);
	  getApplication().refreshWindow();
	}

	public boolean onStylusPress(int x, int y) {
		if (super.onStylusPress(x, y)) {
			return true;
		}

		ZLTextElementArea area = getElementByCoordinates(x, y);
		if (area != null) {
			ZLTextElement element = area.Element;
			if ((element instanceof ZLTextImageElement) ||
					(element instanceof ZLTextWord)) {
				final ZLTextWordCursor cursor = new ZLTextWordCursor(getStartCursor());
				cursor.moveToParagraph(area.ParagraphNumber);
				cursor.moveToParagraphStart();
				final int elementNumber = area.TextElementNumber;
				byte hyperlinkKind = FBTextKind.REGULAR;
				String id = null;
				for (int i = 0; i < elementNumber; ++i) {
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
							ZLibrary.getInstance().openInBrowser(id);
							return true;
						case FBTextKind.FOOTNOTE:
						case FBTextKind.INTERNAL_HYPERLINK:
							getFBReader().tryOpenFootnote(id);
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
	
	public void saveState() {
		final ZLTextWordCursor cursor = getStartCursor();
		final String group = getFileName();

		if (!cursor.isNull()) {
			new ZLIntegerOption(ZLOption.STATE_CATEGORY, group, PARAGRAPH_OPTION_NAME, 0).setValue(cursor.getParagraphCursor().getIndex());
			new ZLIntegerOption(ZLOption.STATE_CATEGORY, group, WORD_OPTION_NAME, 0).setValue(cursor.getWordNumber());
			new ZLIntegerOption(ZLOption.STATE_CATEGORY, group, CHAR_OPTION_NAME, 0).setValue(cursor.getCharNumber());
			new ZLIntegerOption(ZLOption.STATE_CATEGORY, group, BUFFER_SIZE, 0).setValue(myPositionStack.size());
			new ZLIntegerOption(ZLOption.STATE_CATEGORY, group, POSITION_IN_BUFFER, 0).setValue(myCurrentPointInStack);

			for (int i = 0; i < myPositionStack.size(); ++i) {
				String bufferParagraph = BUFFER_PARAGRAPH_PREFIX;
				String bufferWord = BUFFER_WORD_PREFIX;
				bufferParagraph += i;
				bufferWord += i;
				new ZLIntegerOption(ZLOption.STATE_CATEGORY, group, bufferParagraph, -1).setValue(((Position)myPositionStack.get(i)).getFirst());
				new ZLIntegerOption(ZLOption.STATE_CATEGORY, group, bufferWord, -1).setValue(((Position)myPositionStack.get(i)).getSecond());
			}
		}
	}
	
	private class Position {
		private int first;
		private int second;
		
		public Position() {}
		
		public Position(int first, int second) {
			this.first = first;
			this.second = second;
		}
		
		public int getFirst() {
			return first;
		}
		
		public int getSecond() {
			return second;
		}
	}
}
