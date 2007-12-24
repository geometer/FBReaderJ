package org.fbreader.fbreader;

import java.io.File;
import java.io.IOException;

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
	static enum ViewMode {
		UNDEFINED,
		BOOK_TEXT,
		FOOTNOTE,
		CONTENTS,
		BOOKMARKS,
		BOOK_COLLECTION,
		RECENT_BOOKS,
	};

	final static String HELP_FILE_NAME = "data/help/MiniHelp.ru.fb2";
	private final ZLStringOption myBookNameOption =
		//new ZLStringOption(ZLOption.STATE_CATEGORY, "State", "Book", HELP_FILE_NAME);
		new ZLStringOption(ZLOption.STATE_CATEGORY, "State", "Book", "/test.fb2");

	private final ZLKeyBindings myBindings0 = new ZLKeyBindings("Keys");
	private final ZLKeyBindings myBindings90 = new ZLKeyBindings("Keys90");
	private final ZLKeyBindings myBindings180 = new ZLKeyBindings("Keys180");
	private final ZLKeyBindings myBindings270 = new ZLKeyBindings("Keys270");

	private ViewMode myMode = ViewMode.UNDEFINED;
	private ViewMode myPreviousMode = ViewMode.BOOK_TEXT;

	private final BookTextView myBookTextView;
	private final ContentsView myContentsView;

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
		addAction(ActionCode.LARGE_SCROLL_FORWARD, new DummyAction(this));
		addAction(ActionCode.LARGE_SCROLL_BACKWARD, new DummyAction(this));
		addAction(ActionCode.SMALL_SCROLL_FORWARD, new ScrollAction(this, 1));
		addAction(ActionCode.SMALL_SCROLL_BACKWARD, new ScrollAction(this, -1));
		addAction(ActionCode.MOUSE_SCROLL_FORWARD, new DummyAction(this));
		addAction(ActionCode.MOUSE_SCROLL_BACKWARD, new DummyAction(this));
		addAction(ActionCode.FINGER_TAP_SCROLL_FORWARD, new DummyAction(this));
		addAction(ActionCode.FINGER_TAP_SCROLL_BACKWARD, new DummyAction(this));
		addAction(ActionCode.CANCEL, new CancelAction(this));
		addAction(ActionCode.SHOW_HIDE_POSITION_INDICATOR, new DummyAction(this));
		addAction(ActionCode.OPEN_PREVIOUS_BOOK, new DummyAction(this));
		addAction(ActionCode.SHOW_HELP, new ShowHelpAction(this));
		addAction(ActionCode.GOTO_NEXT_TOC_SECTION, new DummyAction(this));
		addAction(ActionCode.GOTO_PREVIOUS_TOC_SECTION, new DummyAction(this));
		addAction(ActionCode.COPY_SELECTED_TEXT_TO_CLIPBOARD, new DummyAction(this));
		addAction(ActionCode.OPEN_SELECTED_TEXT_IN_DICTIONARY, new DummyAction(this));
		addAction(ActionCode.CLEAR_SELECTION, new DummyAction(this));
		
		addToolbarButton(ActionCode.SHOW_COLLECTION, "books");
		addToolbarButton(ActionCode.SHOW_LAST_BOOKS, "history");
		addToolbarButton(ActionCode.ADD_BOOK, "addbook");
		getToolbar().addSeparator();
		addToolbarButton(ActionCode.SCROLL_TO_HOME, "home");
		addToolbarButton(ActionCode.UNDO, "leftarrow");
		addToolbarButton(ActionCode.REDO, "rightarrow");
		getToolbar().addSeparator();
		addToolbarButton(ActionCode.SHOW_CONTENTS, "contents");
		getToolbar().addSeparator();
		addToolbarButton(ActionCode.SEARCH, "find");
		addToolbarButton(ActionCode.FIND_NEXT, "findnext");
		addToolbarButton(ActionCode.FIND_PREVIOUS, "findprev");
		getToolbar().addSeparator();
		addToolbarButton(ActionCode.SHOW_BOOK_INFO, "bookinfo");
		addToolbarButton(ActionCode.SHOW_OPTIONS, "settings");
		getToolbar().addSeparator();
		addToolbarButton(ActionCode.ROTATE_SCREEN, "rotatescreen");
		//if (ShowHelpIconOption.value()) {
			getToolbar().addSeparator();
			addToolbarButton(ActionCode.SHOW_HELP, "help");
		//}

		getMenubar().addItem(ActionCode.SHOW_BOOK_INFO, "bookInfo");
		getMenubar().addItem(ActionCode.SHOW_CONTENTS, "toc");

		Menu librarySubmenu = getMenubar().addSubmenu("library");
		librarySubmenu.addItem(ActionCode.SHOW_COLLECTION, "open");
		librarySubmenu.addItem(ActionCode.OPEN_PREVIOUS_BOOK, "previous");
		librarySubmenu.addItem(ActionCode.SHOW_LAST_BOOKS, "recent");
		librarySubmenu.addItem(ActionCode.ADD_BOOK, "addBook");
		librarySubmenu.addItem(ActionCode.SHOW_HELP, "about");

		Menu navigationSubmenu = getMenubar().addSubmenu("navigate");
		navigationSubmenu.addItem(ActionCode.SCROLL_TO_HOME, "gotoStartOfDocument");
		navigationSubmenu.addItem(ActionCode.SCROLL_TO_START_OF_TEXT, "gotoStartOfSection");
		navigationSubmenu.addItem(ActionCode.SCROLL_TO_END_OF_TEXT, "gotoEndOfSection");
		navigationSubmenu.addItem(ActionCode.GOTO_NEXT_TOC_SECTION, "gotoNextTOCItem");
		navigationSubmenu.addItem(ActionCode.GOTO_PREVIOUS_TOC_SECTION, "gotoPreviousTOCItem");
		navigationSubmenu.addItem(ActionCode.UNDO, "goBack");
		navigationSubmenu.addItem(ActionCode.REDO, "goForward");

		Menu selectionSubmenu = getMenubar().addSubmenu("selection");
		selectionSubmenu.addItem(ActionCode.COPY_SELECTED_TEXT_TO_CLIPBOARD, "clipboard");
		selectionSubmenu.addItem(ActionCode.OPEN_SELECTED_TEXT_IN_DICTIONARY, "dictionary");
		selectionSubmenu.addItem(ActionCode.CLEAR_SELECTION, "clear");

		Menu findSubmenu = getMenubar().addSubmenu("search");
		findSubmenu.addItem(ActionCode.SEARCH, "find");
		findSubmenu.addItem(ActionCode.FIND_NEXT, "next");
		findSubmenu.addItem(ActionCode.FIND_PREVIOUS, "previous");

		Menu viewSubmenu = getMenubar().addSubmenu("view");
		// MSS: these three actions can have a checkbox next to them
		viewSubmenu.addItem(ActionCode.ROTATE_SCREEN, "rotate");
		viewSubmenu.addItem(ActionCode.TOGGLE_FULLSCREEN, "fullScreen");
		viewSubmenu.addItem(ActionCode.SHOW_HIDE_POSITION_INDICATOR, "toggleIndicator");

		getMenubar().addItem(ActionCode.SHOW_OPTIONS, "settings");
		getMenubar().addItem(ActionCode.QUIT, "close");

		myBookTextView = new BookTextView(this, getContext());
		myContentsView = new ContentsView(this, getContext());

		String fileName = null;
		if (args.length > 0) {
			try {
				fileName = new File(args[0]).getCanonicalPath();
			} catch (IOException e) {
			}
		}
		if (!openBook(fileName)) {
			openBook(null);
		}
	}

	boolean openBook(String fileName) {
		if (fileName == null) {
			fileName = myBookNameOption.getValue();
		}
		BookModel model = new BookModel(fileName);
		if (!new FB2Reader().readBook(model)) {
			return false;
		}
		myBookNameOption.setValue(fileName);
		myBookTextView.setModel(model.getBookTextModel(), model.getFileName());
		myContentsView.setModel(model.getContentsModel());
		setMode(ViewMode.BOOK_TEXT);
		return true;
	}

	private final void addToolbarButton(int code, String name) {
		getToolbar().addButton(code, name);
	}

	public ZLKeyBindings keyBindings() {
		return myBindings0;
	}
	
	private ZLKeyBindings keyBindings(ZLViewWidget.Angle angle) {
		switch (angle) {
			case DEGREES0:
			default:
				return myBindings0;
			case DEGREES90:
				return myBindings90;
			case DEGREES180:
				return myBindings180;
			case DEGREES270:
				return myBindings270;
		}
	}

	ZLTextView getTextView() {
		return (ZLTextView)getCurrentView();
	}

	ViewMode getMode() {
		return myMode;
	}

	void setMode(ViewMode mode) {
		if (mode == myMode) {
			return;
		}

		myPreviousMode = myMode;
		myMode = mode;

		switch (mode) {
			case BOOK_TEXT:
				setView(myBookTextView);
				break;
			case CONTENTS:
				setView(myContentsView);
				break;
			default:
				break;
		}
	}

	void restorePreviousMode() {
		setMode(myPreviousMode);
		myPreviousMode = ViewMode.BOOK_TEXT;
	}

	BookTextView getBookTextView() {
		return myBookTextView;
	}
}
