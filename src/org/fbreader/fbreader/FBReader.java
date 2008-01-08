package org.fbreader.fbreader;

import java.io.*;
import org.zlibrary.core.io.*;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.application.ZLKeyBindings;
import org.zlibrary.core.view.ZLViewWidget;
import org.zlibrary.core.options.ZLOption;
import org.zlibrary.core.options.ZLStringOption;

import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.view.ZLTextView;

import org.fbreader.bookmodel.BookModel;
import org.fbreader.formats.fb2.FB2Reader;

public final class FBReader extends ZLApplication {
	static interface ViewMode {
		int UNDEFINED = 0;
		int BOOK_TEXT = 1 << 0;
		int FOOTNOTE = 1 << 1;
		int CONTENTS = 1 << 2;
		int BOOKMARKS = 1 << 3;
		int BOOK_COLLECTION = 1 << 4;
		int RECENT_BOOKS = 1 << 5;
	};

	public final ScrollingOptions LargeScrollingOptions =
		new ScrollingOptions("LargeScrolling", 250, ZLTextView.ScrollingMode.NO_OVERLAPPING);
	public final ScrollingOptions SmallScrollingOptions =
		new ScrollingOptions("SmallScrolling", 50, ZLTextView.ScrollingMode.SCROLL_LINES);
	public final ScrollingOptions MouseScrollingOptions =
		new ScrollingOptions("MouseScrolling", 0, ZLTextView.ScrollingMode.SCROLL_LINES);
	public final ScrollingOptions FingerTapScrollingOptions =
		new ScrollingOptions("FingerTapScrolling", 0, ZLTextView.ScrollingMode.NO_OVERLAPPING);

	final static String HELP_FILE_NAME = "data/help/MiniHelp.ru.fb2";
	private final ZLStringOption myBookNameOption =
		//new ZLStringOption(ZLOption.STATE_CATEGORY, "State", "Book", HELP_FILE_NAME);
		new ZLStringOption(ZLOption.STATE_CATEGORY, "State", "Book", "/test.fb2");

	private final ZLKeyBindings myBindings0 = new ZLKeyBindings("Keys");
	private final ZLKeyBindings myBindings90 = new ZLKeyBindings("Keys90");
	private final ZLKeyBindings myBindings180 = new ZLKeyBindings("Keys180");
	private final ZLKeyBindings myBindings270 = new ZLKeyBindings("Keys270");

	private int myMode = ViewMode.UNDEFINED;
	private int myPreviousMode = ViewMode.BOOK_TEXT;

	private final BookTextView myBookTextView;
	private final ContentsView myContentsView;
	private final FootnoteView myFootnoteView;

	private BookModel myBookModel;

	public FBReader() {
		this(new String[0]);
	}

	public FBReader(String[] args) {
		addAction(ActionCode.TOGGLE_FULLSCREEN, new ZLApplication.FullscreenAction(this, true));
		addAction(ActionCode.FULLSCREEN_ON, new ZLApplication.FullscreenAction(this, false));
		addAction(ActionCode.QUIT, new QuitAction(this));
		addAction(ActionCode.SHOW_HELP, new ShowHelpAction(this));
		addAction(ActionCode.ROTATE_SCREEN, new ZLApplication.RotationAction(this));

		addAction(ActionCode.UNDO, new UndoAction(this));
		addAction(ActionCode.REDO, new RedoAction(this));

		addAction(ActionCode.INCREASE_FONT, new ChangeFontSizeAction(this, +2));
		addAction(ActionCode.DECREASE_FONT, new ChangeFontSizeAction(this, -2));

		addAction(ActionCode.SHOW_COLLECTION, new DummyAction(this));
		addAction(ActionCode.SHOW_LAST_BOOKS, new DummyAction(this));
		addAction(ActionCode.SHOW_OPTIONS, new DummyAction(this));
		addAction(ActionCode.SHOW_CONTENTS, new ShowContentsAction(this));
		addAction(ActionCode.SHOW_BOOK_INFO, new DummyAction(this));
		addAction(ActionCode.ADD_BOOK, new AddBookAction(this));
		addAction(ActionCode.SEARCH, new DummyAction(this));
		addAction(ActionCode.FIND_NEXT, new DummyAction(this));
		addAction(ActionCode.FIND_PREVIOUS, new DummyAction(this));
		addAction(ActionCode.SCROLL_TO_HOME, new ScrollToHomeAction(this));
		addAction(ActionCode.SCROLL_TO_START_OF_TEXT, new DummyAction(this));
		addAction(ActionCode.SCROLL_TO_END_OF_TEXT, new DummyAction(this));
		addAction(ActionCode.LARGE_SCROLL_FORWARD, new ScrollingAction(this, LargeScrollingOptions, true));
		addAction(ActionCode.LARGE_SCROLL_BACKWARD, new ScrollingAction(this, LargeScrollingOptions, false));
		addAction(ActionCode.SMALL_SCROLL_FORWARD, new ScrollingAction(this, SmallScrollingOptions, true));
		addAction(ActionCode.SMALL_SCROLL_BACKWARD, new ScrollingAction(this, SmallScrollingOptions, false));
		addAction(ActionCode.MOUSE_SCROLL_FORWARD, new ScrollingAction(this, MouseScrollingOptions, true));
		addAction(ActionCode.MOUSE_SCROLL_BACKWARD, new ScrollingAction(this, MouseScrollingOptions, false));
		addAction(ActionCode.FINGER_TAP_SCROLL_FORWARD, new ScrollingAction(this, FingerTapScrollingOptions, true));
		addAction(ActionCode.FINGER_TAP_SCROLL_BACKWARD, new ScrollingAction(this, FingerTapScrollingOptions, false));
		addAction(ActionCode.CANCEL, new CancelAction(this));
		addAction(ActionCode.SHOW_HIDE_POSITION_INDICATOR, new DummyAction(this));
		addAction(ActionCode.OPEN_PREVIOUS_BOOK, new DummyAction(this));
		addAction(ActionCode.SHOW_HELP, new ShowHelpAction(this));
		addAction(ActionCode.GOTO_NEXT_TOC_SECTION, new DummyAction(this));
		addAction(ActionCode.GOTO_PREVIOUS_TOC_SECTION, new DummyAction(this));
		addAction(ActionCode.COPY_SELECTED_TEXT_TO_CLIPBOARD, new DummyAction(this));
		addAction(ActionCode.OPEN_SELECTED_TEXT_IN_DICTIONARY, new DummyAction(this));
		addAction(ActionCode.CLEAR_SELECTION, new DummyAction(this));

		myBookTextView = new BookTextView(this, getContext());
		myContentsView = new ContentsView(this, getContext());
		myFootnoteView = new FootnoteView(this, getContext());

		String fileName = null;
		if (args.length > 0) {
			try {
				fileName = new File(args[0]).getCanonicalPath();
			} catch (IOException e) {
			}
		}
		if (!openBook(fileName)) {
			openBook(HELP_FILE_NAME);
		}
		refreshWindow();
	}

	boolean openBook(String fileName) {
		if (fileName == null) {
			fileName = myBookNameOption.getValue();
		}
		myBookModel = new BookModel(fileName);
		if (!new FB2Reader(myBookModel).read()) {
			myBookModel = null;
			return false;
		}
		myBookNameOption.setValue(fileName);
		myBookTextView.setModel(myBookModel.getBookTextModel(), myBookModel.getFileName());
		myContentsView.setModel(myBookModel.getContentsModel());
		setMode(ViewMode.BOOK_TEXT);
		return true;
	}

	public ZLKeyBindings keyBindings() {
		return myBindings0;
	}
	
	private ZLKeyBindings keyBindings(int angle) {
		switch (angle) {
			case ZLViewWidget.Angle.DEGREES0:
			default:
				return myBindings0;
			case ZLViewWidget.Angle.DEGREES90:
				return myBindings90;
			case ZLViewWidget.Angle.DEGREES180:
				return myBindings180;
			case ZLViewWidget.Angle.DEGREES270:
				return myBindings270;
		}
	}

	ZLTextView getTextView() {
		return (ZLTextView)getCurrentView();
	}

	int getMode() {
		return myMode;
	}

	void setMode(int mode) {
		if (mode == myMode) {
			return;
		}

		myPreviousMode = myMode;
		myMode = mode;

		switch (mode) {
			case ViewMode.BOOK_TEXT:
				setView(myBookTextView);
				break;
			case ViewMode.CONTENTS:
				setView(myContentsView);
				break;
			case ViewMode.FOOTNOTE:
				setView(myFootnoteView);
				break;
			default:
				break;
		}
	}

	void restorePreviousMode() {
		setMode(myPreviousMode);
		myPreviousMode = ViewMode.BOOK_TEXT;
	}

	void tryOpenFootnote(String id) {
		if (myBookModel != null) {
			ZLTextModel footnoteModel = myBookModel.getFootnoteModel(id);
			if (footnoteModel != null) {
				myFootnoteView.setModel(footnoteModel);
				setMode(ViewMode.FOOTNOTE);
			}
		}
	}

	BookTextView getBookTextView() {
		return myBookTextView;
	}

	void clearTextCaches() {
		myBookTextView.clearCaches();
		myContentsView.clearCaches();
	}
}
