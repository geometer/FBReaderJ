package org.fbreader.fbreader;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.application.ZLKeyBindings;
import org.zlibrary.core.resources.ZLResourceKey;
import org.zlibrary.core.view.ZLViewWidget;
import org.zlibrary.core.options.ZLOption;
import org.zlibrary.core.options.ZLStringOption;

import org.zlibrary.text.view.ZLTextView;
import org.zlibrary.text.view.impl.ZLTextViewImpl;

public final class FBReader extends ZLApplication {
	private final ZLKeyBindings myBindings0 = new ZLKeyBindings("Keys");
	private final ZLKeyBindings myBindings90 = new ZLKeyBindings("Keys90");
	private final ZLKeyBindings myBindings180 = new ZLKeyBindings("Keys180");
	private final ZLKeyBindings myBindings270 = new ZLKeyBindings("Keys270");

	public FBReader() {
		this(new String[0]);
	}

	public FBReader(String[] args) {
		addAction(ActionCode.TOGGLE_FULLSCREEN, new ZLApplication.FullscreenAction(this, true));
		addAction(ActionCode.FULLSCREEN_ON, new ZLApplication.FullscreenAction(this, false));
		addAction(ActionCode.QUIT, new QuitAction(this));
		addAction(ActionCode.SHOW_HELP, new ShowHelpAction(this));
		addAction(ActionCode.ROTATE_SCREEN, new ZLApplication.RotationAction(this));

		addAction(ActionCode.UNDO, new ScrollAction(this, -1));
		addAction(ActionCode.REDO, new ScrollAction(this, 1));

		addAction(ActionCode.INCREASE_FONT, new ChangeFontSizeAction(this, +2));
		addAction(ActionCode.DECREASE_FONT, new ChangeFontSizeAction(this, -2));

		addAction(ActionCode.SHOW_COLLECTION, new DummyAction(this));
		addAction(ActionCode.SHOW_LAST_BOOKS, new DummyAction(this));
		addAction(ActionCode.SHOW_OPTIONS, new DummyAction(this));
		addAction(ActionCode.SHOW_CONTENTS, new DummyAction(this));
		addAction(ActionCode.SHOW_BOOK_INFO, new DummyAction(this));
		addAction(ActionCode.ADD_BOOK, new DummyAction(this));
		addAction(ActionCode.SEARCH, new DummyAction(this));
		addAction(ActionCode.FIND_NEXT, new DummyAction(this));
		addAction(ActionCode.FIND_PREVIOUS, new DummyAction(this));
		addAction(ActionCode.SCROLL_TO_HOME, new DummyAction(this));
		addAction(ActionCode.SCROLL_TO_START_OF_TEXT, new DummyAction(this));
		addAction(ActionCode.SCROLL_TO_END_OF_TEXT, new DummyAction(this));
		addAction(ActionCode.LARGE_SCROLL_FORWARD, new DummyAction(this));
		addAction(ActionCode.LARGE_SCROLL_BACKWARD, new DummyAction(this));
		addAction(ActionCode.SMALL_SCROLL_FORWARD, new DummyAction(this));
		addAction(ActionCode.SMALL_SCROLL_BACKWARD, new DummyAction(this));
		addAction(ActionCode.MOUSE_SCROLL_FORWARD, new DummyAction(this));
		addAction(ActionCode.MOUSE_SCROLL_BACKWARD, new DummyAction(this));
		addAction(ActionCode.FINGER_TAP_SCROLL_FORWARD, new DummyAction(this));
		addAction(ActionCode.FINGER_TAP_SCROLL_BACKWARD, new DummyAction(this));
		addAction(ActionCode.CANCEL, new DummyAction(this));
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

		getMenubar().addItem(ActionCode.SHOW_BOOK_INFO, new ZLResourceKey("bookInfo"));
		getMenubar().addItem(ActionCode.SHOW_CONTENTS, new ZLResourceKey("toc"));

		Menu librarySubmenu = getMenubar().addSubmenu(new ZLResourceKey("library"));
		librarySubmenu.addItem(ActionCode.SHOW_COLLECTION, new ZLResourceKey("open"));
		librarySubmenu.addItem(ActionCode.OPEN_PREVIOUS_BOOK, new ZLResourceKey("previous"));
		librarySubmenu.addItem(ActionCode.SHOW_LAST_BOOKS, new ZLResourceKey("recent"));
		librarySubmenu.addItem(ActionCode.ADD_BOOK, new ZLResourceKey("addBook"));
		librarySubmenu.addItem(ActionCode.SHOW_HELP, new ZLResourceKey("about"));

		Menu navigationSubmenu = getMenubar().addSubmenu(new ZLResourceKey("navigate"));
		navigationSubmenu.addItem(ActionCode.SCROLL_TO_HOME, new ZLResourceKey("gotoStartOfDocument"));
		navigationSubmenu.addItem(ActionCode.SCROLL_TO_START_OF_TEXT, new ZLResourceKey("gotoStartOfSection"));
		navigationSubmenu.addItem(ActionCode.SCROLL_TO_END_OF_TEXT, new ZLResourceKey("gotoEndOfSection"));
		navigationSubmenu.addItem(ActionCode.GOTO_NEXT_TOC_SECTION, new ZLResourceKey("gotoNextTOCItem"));
		navigationSubmenu.addItem(ActionCode.GOTO_PREVIOUS_TOC_SECTION, new ZLResourceKey("gotoPreviousTOCItem"));
		navigationSubmenu.addItem(ActionCode.UNDO, new ZLResourceKey("goBack"));
		navigationSubmenu.addItem(ActionCode.REDO, new ZLResourceKey("goForward"));

		Menu selectionSubmenu = getMenubar().addSubmenu(new ZLResourceKey("selection"));
		selectionSubmenu.addItem(ActionCode.COPY_SELECTED_TEXT_TO_CLIPBOARD, new ZLResourceKey("clipboard"));
		selectionSubmenu.addItem(ActionCode.OPEN_SELECTED_TEXT_IN_DICTIONARY, new ZLResourceKey("dictionary"));
		selectionSubmenu.addItem(ActionCode.CLEAR_SELECTION, new ZLResourceKey("clear"));

		Menu findSubmenu = getMenubar().addSubmenu(new ZLResourceKey("search"));
		findSubmenu.addItem(ActionCode.SEARCH, new ZLResourceKey("find"));
		findSubmenu.addItem(ActionCode.FIND_NEXT, new ZLResourceKey("next"));
		findSubmenu.addItem(ActionCode.FIND_PREVIOUS, new ZLResourceKey("previous"));

		Menu viewSubmenu = getMenubar().addSubmenu(new ZLResourceKey("view"));
		// MSS: these three actions can have a checkbox next to them
		viewSubmenu.addItem(ActionCode.ROTATE_SCREEN, new ZLResourceKey("rotate"));
		viewSubmenu.addItem(ActionCode.TOGGLE_FULLSCREEN, new ZLResourceKey("fullScreen"));
		viewSubmenu.addItem(ActionCode.SHOW_HIDE_POSITION_INDICATOR, new ZLResourceKey("toggleIndicator"));

		getMenubar().addItem(ActionCode.SHOW_OPTIONS, new ZLResourceKey("settings"));
		getMenubar().addItem(ActionCode.QUIT, new ZLResourceKey("close"));

		ZLTextView view = new ZLTextViewImpl(this, getContext());
		ZLStringOption bookNameOption = new ZLStringOption(ZLOption.STATE_CATEGORY, "State", "Book", "data/help/MiniHelp.ru.fb2");
		if (args.length > 0) {
			bookNameOption.setValue(args[0]);
		}
		view.setModel(bookNameOption.getValue());
//		view.setModel((args.length > 0) ? args[0] : "test/data/fb2/subtitle.fb2");
		setView(view);
	}

	private final void addToolbarButton(int code, String name) {
		getToolbar().addButton(code, new ZLResourceKey(name));
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
}
