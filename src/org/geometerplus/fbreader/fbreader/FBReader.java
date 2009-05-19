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

import java.io.*;
import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.application.*;
import org.geometerplus.zlibrary.core.dialogs.ZLDialogManager;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;

import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Bookmark;

public final class FBReader extends ZLApplication {
	static interface ViewMode {
		int UNDEFINED = 0;
		int BOOK_TEXT = 1 << 0;
		int FOOTNOTE = 1 << 1;
	};

	public final ZLStringOption BookSearchPatternOption =
		new ZLStringOption("BookSearch", "Pattern", "");
	public final ZLStringOption TextSearchPatternOption =
		new ZLStringOption("TextSearch", "Pattern", "");
	public final ZLStringOption BookmarkSearchPatternOption =
		new ZLStringOption("BookmarkSearch", "Pattern", "");

	public final ZLBooleanOption UseSeparateBindingsOption = 
		new ZLBooleanOption("KeysOptions", "UseSeparateBindings", false);

	public final ZLIntegerRangeOption LeftMarginOption =
		new ZLIntegerRangeOption("Options", "LeftMargin", 0, 1000, 4);
	public final ZLIntegerRangeOption RightMarginOption =
		new ZLIntegerRangeOption("Options", "RightMargin", 0, 1000, 4);
	public final ZLIntegerRangeOption TopMarginOption =
		new ZLIntegerRangeOption("Options", "TopMargin", 0, 1000, 0);
	public final ZLIntegerRangeOption BottomMarginOption =
		new ZLIntegerRangeOption("Options", "BottomMargin", 0, 1000, 4);

	final ZLBooleanOption SelectionEnabledOption =
		new ZLBooleanOption("Options", "IsSelectionEnabled", true);

	private final ZLKeyBindings myBindings0 = new ZLKeyBindings("Keys");
	private final ZLKeyBindings myBindings90 = new ZLKeyBindings("Keys90");
	private final ZLKeyBindings myBindings180 = new ZLKeyBindings("Keys180");
	private final ZLKeyBindings myBindings270 = new ZLKeyBindings("Keys270");

	private int myMode = ViewMode.UNDEFINED;
	private int myPreviousMode = ViewMode.BOOK_TEXT;

	public final FBView BookTextView;
	final FBView FootnoteView;

	public BookModel Model;

	private final String myArg0;

	public FBReader(String[] args) {
		myArg0 = (args.length > 0) ? args[0] : null;
		addAction(ActionCode.QUIT, new QuitAction(this));
		addAction(ActionCode.ROTATE_SCREEN, new ZLApplication.RotationAction());

		addAction(ActionCode.INCREASE_FONT, new ChangeFontSizeAction(this, +2));
		addAction(ActionCode.DECREASE_FONT, new ChangeFontSizeAction(this, -2));

		addAction(ActionCode.SHOW_LIBRARY, new ShowLibraryAction(this));
		addAction(ActionCode.SHOW_OPTIONS, new ShowOptionsDialogAction(this));
		addAction(ActionCode.SHOW_PREFERENCES, new PreferencesAction(this));
		addAction(ActionCode.SHOW_BOOK_INFO, new BookInfoAction(this));
		addAction(ActionCode.SHOW_CONTENTS, new ShowTOCAction(this));
		addAction(ActionCode.SHOW_BOOKMARKS, new ShowBookmarksAction(this));
		
		addAction(ActionCode.SEARCH, new SearchAction(this));
		addAction(ActionCode.FIND_NEXT, new FindNextAction(this));
		addAction(ActionCode.FIND_PREVIOUS, new FindPreviousAction(this));
		addAction(ActionCode.CLEAR_FIND_RESULTS, new ClearFindResultsAction(this));
		
		addAction(ActionCode.SCROLL_TO_HOME, new ScrollToHomeAction(this));
		//addAction(ActionCode.SCROLL_TO_START_OF_TEXT, new DummyAction(this));
		//addAction(ActionCode.SCROLL_TO_END_OF_TEXT, new DummyAction(this));
		addAction(ActionCode.VOLUME_KEY_SCROLL_FORWARD, new VolumeKeyScrollingAction(this, true));
		addAction(ActionCode.VOLUME_KEY_SCROLL_BACKWARD, new VolumeKeyScrollingAction(this, false));
		addAction(ActionCode.TRACKBALL_SCROLL_FORWARD, new TrackballScrollingAction(this, true));
		addAction(ActionCode.TRACKBALL_SCROLL_BACKWARD, new TrackballScrollingAction(this, false));
		addAction(ActionCode.CANCEL, new CancelAction(this));
		//addAction(ActionCode.GOTO_NEXT_TOC_SECTION, new DummyAction(this));
		//addAction(ActionCode.GOTO_PREVIOUS_TOC_SECTION, new DummyAction(this));
		//addAction(ActionCode.COPY_SELECTED_TEXT_TO_CLIPBOARD, new DummyAction(this));
		//addAction(ActionCode.OPEN_SELECTED_TEXT_IN_DICTIONARY, new DummyAction(this));
		//addAction(ActionCode.CLEAR_SELECTION, new DummyAction(this));

		BookTextView = new FBView(this);
		FootnoteView = new FBView(this);

		setMode(ViewMode.BOOK_TEXT);
	}
		
	public void initWindow() {
		super.initWindow();
		ZLDialogManager.Instance().wait("loadingBook", new Runnable() {
			public void run() { 
				Book book = Book.getByFile(ZLFile.createFileByPath(myArg0));
				if (book == null) {
					book = Library.Instance().getRecentBook();
				}
				if (book == null) {
					book = Book.getByFile(Library.Instance().getHelpFile());
				}
				openBookInternal(book, null);
			}
		});
	}
	
	public void openBook(final Book book, final Bookmark bookmark) {
		ZLDialogManager.Instance().wait("loadingBook", new Runnable() {
			public void run() { 
				openBookInternal(book, bookmark); 
			}
		});
	}

	public ZLKeyBindings keyBindings() {
		return UseSeparateBindingsOption.getValue() ?
				keyBindings(myViewWidget.getRotation()) : myBindings0;
	}
	
	public ZLKeyBindings keyBindings(int angle) {
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

	public FBView getTextView() {
		return (FBView)getCurrentView();
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
				setView(BookTextView);
				break;
			case ViewMode.FOOTNOTE:
				setView(FootnoteView);
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
		if (Model != null) {
			BookModel.Label label = Model.getLabel(id);
			if ((label != null) && (label.Model != null)) {
				if (label.Model == Model.BookTextModel) {
					BookTextView.gotoPosition(label.ParagraphIndex, 0, 0);
				} else {
					FootnoteView.setModel(label.Model);
					setMode(ViewMode.FOOTNOTE);
					FootnoteView.gotoPosition(label.ParagraphIndex, 0, 0);
				}
				repaintView();
			}
		}
	}

	public void clearTextCaches() {
		BookTextView.clearCaches();
		FootnoteView.clearCaches();
	}
	
	void openBookInternal(Book book, Bookmark bookmark) {
		clearTextCaches();

		if (book != null) {
			onViewChanged();

			if (Model != null) {
				Model.Book.storePosition(BookTextView.getStartCursor());
			}
			BookTextView.setModel(null);
			FootnoteView.setModel(null);

			Model = null;
			System.gc();
			System.gc();
			Model = BookModel.createModel(book);
			if (Model != null) {
				ZLTextHyphenator.Instance().load(book.getLanguage());
				BookTextView.setModel(Model.BookTextModel);
				BookTextView.gotoPosition(book.getStoredPosition());
				if (bookmark == null) {
					setMode(ViewMode.BOOK_TEXT);
				} else {
					gotoBookmark(bookmark);
				}
				Library.Instance().addBookToRecentList(book);
			}
		}
		repaintView();
	}

	public void gotoBookmark(Bookmark bookmark) {
		final String modelId = bookmark.getModelId();
		if (modelId == null) {
			BookTextView.gotoPosition(bookmark);
			setMode(ViewMode.BOOK_TEXT);
		} else {
			FootnoteView.setModel(Model.getFootnoteModel(modelId));
			FootnoteView.gotoPosition(bookmark);
			setMode(ViewMode.FOOTNOTE);
		}
		repaintView();
	}
	
	public void showBookTextView() {
		setMode(ViewMode.BOOK_TEXT);
	}
	
	@Override
	public void openFile(final ZLFile file) {
		if (file == null) {
			return;
		}
		Book book = Book.getByFile(file);
		if ((book == null) && file.isArchive()) {
			for (ZLFile child : file.children()) {
				book = Book.getByFile(child);
				if (book != null) {
					break;
				}
			}
		}
		if (book != null) {
			openBook(book, null);
		}
	}

	public void onWindowClosing() {
		if ((Model != null) && (BookTextView != null)) {
			Model.Book.storePosition(BookTextView.getStartCursor());
		}
	}
}
