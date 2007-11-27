package org.fbreader.fbreader;

import org.zlibrary.core.application.FullscreenAction;
import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.application.menu.Menu;
import org.zlibrary.core.application.ZLKeyBindings;
import org.zlibrary.core.resources.ZLResourceKey;
import org.zlibrary.text.view.ZLTextView;
import org.zlibrary.text.view.impl.ZLTextViewImpl;

public class FBReader extends ZLApplication {
	public FBReader() {
		this(new String[0]);
	}

	public FBReader(String[] args) {
		addAction(ActionCode.TOGGLE_FULLSCREEN.getCode(), new FullscreenAction(this, true));
		addAction(ActionCode.QUIT.getCode(), new FBReaderActions.QuitAction(this));
		addAction(ActionCode.SHOW_HELP.getCode(), new FBReaderActions.ShowHelpAction(this));
		
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

		getMenubar().addItem(ActionCode.SHOW_BOOK_INFO.ordinal(), new ZLResourceKey("bookInfo"));
		getMenubar().addItem(ActionCode.SHOW_CONTENTS.ordinal(), new ZLResourceKey("toc"));

		Menu librarySubmenu = getMenubar().addSubmenu(new ZLResourceKey("library"));
		librarySubmenu.addItem(ActionCode.SHOW_COLLECTION.ordinal(), new ZLResourceKey("open"));
		librarySubmenu.addItem(ActionCode.OPEN_PREVIOUS_BOOK.ordinal(), new ZLResourceKey("previous"));
		librarySubmenu.addItem(ActionCode.SHOW_LAST_BOOKS.ordinal(), new ZLResourceKey("recent"));
		librarySubmenu.addItem(ActionCode.ADD_BOOK.ordinal(), new ZLResourceKey("addBook"));
		librarySubmenu.addItem(ActionCode.SHOW_HELP.ordinal(), new ZLResourceKey("about"));

		Menu navigationSubmenu = getMenubar().addSubmenu(new ZLResourceKey("navigate"));
		navigationSubmenu.addItem(ActionCode.SCROLL_TO_HOME.ordinal(), new ZLResourceKey("gotoStartOfDocument"));
		navigationSubmenu.addItem(ActionCode.SCROLL_TO_START_OF_TEXT.ordinal(), new ZLResourceKey("gotoStartOfSection"));
		navigationSubmenu.addItem(ActionCode.SCROLL_TO_END_OF_TEXT.ordinal(), new ZLResourceKey("gotoEndOfSection"));
		navigationSubmenu.addItem(ActionCode.GOTO_NEXT_TOC_SECTION.ordinal(), new ZLResourceKey("gotoNextTOCItem"));
		navigationSubmenu.addItem(ActionCode.GOTO_PREVIOUS_TOC_SECTION.ordinal(), new ZLResourceKey("gotoPreviousTOCItem"));
		navigationSubmenu.addItem(ActionCode.UNDO.ordinal(), new ZLResourceKey("goBack"));
		navigationSubmenu.addItem(ActionCode.REDO.ordinal(), new ZLResourceKey("goForward"));

		Menu selectionSubmenu = getMenubar().addSubmenu(new ZLResourceKey("selection"));
		selectionSubmenu.addItem(ActionCode.COPY_SELECTED_TEXT_TO_CLIPBOARD.ordinal(), new ZLResourceKey("clipboard"));
		selectionSubmenu.addItem(ActionCode.OPEN_SELECTED_TEXT_IN_DICTIONARY.ordinal(), new ZLResourceKey("dictionary"));
		selectionSubmenu.addItem(ActionCode.CLEAR_SELECTION.ordinal(), new ZLResourceKey("clear"));

		Menu findSubmenu = getMenubar().addSubmenu(new ZLResourceKey("search"));
		findSubmenu.addItem(ActionCode.SEARCH.ordinal(), new ZLResourceKey("find"));
		findSubmenu.addItem(ActionCode.FIND_NEXT.ordinal(), new ZLResourceKey("next"));
		findSubmenu.addItem(ActionCode.FIND_PREVIOUS.ordinal(), new ZLResourceKey("previous"));

		Menu viewSubmenu = getMenubar().addSubmenu(new ZLResourceKey("view"));
		// MSS: these three actions can have a checkbox next to them
		viewSubmenu.addItem(ActionCode.ROTATE_SCREEN.ordinal(), new ZLResourceKey("rotate"));
		viewSubmenu.addItem(ActionCode.TOGGLE_FULLSCREEN.ordinal(), new ZLResourceKey("fullScreen"));
		viewSubmenu.addItem(ActionCode.SHOW_HIDE_POSITION_INDICATOR.ordinal(), new ZLResourceKey("toggleIndicator"));

		getMenubar().addItem(ActionCode.SHOW_OPTIONS.ordinal(), new ZLResourceKey("settings"));
		getMenubar().addItem(ActionCode.QUIT.ordinal(), new ZLResourceKey("close"));

		ZLTextView view = new ZLTextViewImpl(this, getContext());
		view.setModel((args.length > 0) ? args[0] : "data/help/MiniHelp.ru.fb2");
		setView(view);
	}

	private final void addToolbarButton(ActionCode code, String name) {
		getToolbar().addButton(code.ordinal(), new ZLResourceKey(name));
	}

	public ZLKeyBindings keyBindings() {
		return null;
	}
}
