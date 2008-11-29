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

import java.io.*;
import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.application.*;
import org.geometerplus.zlibrary.core.dialogs.ZLDialogManager;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;

import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.collection.BookCollection;
import org.geometerplus.fbreader.description.BookDescription;

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

	public final ZLBooleanOption QuitOnCancelOption =
		new ZLBooleanOption(ZLOption.CONFIG_CATEGORY, "Options", "QuitOnCancel", true);
	
	public final ZLBooleanOption UseSeparateBindingsOption = 
		new ZLBooleanOption(ZLOption.CONFIG_CATEGORY, "KeysOptions", "UseSeparateBindings", false);

	public final ScrollingOptions TouchScrollingOptions =
		new ScrollingOptions("TouchScrolling", ZLTextView.ScrollingMode.NO_OVERLAPPING);
	public final ScrollingOptions TrackballScrollingOptions =
		new ScrollingOptions("TrackballScrolling", ZLTextView.ScrollingMode.SCROLL_LINES);
	
	private String myHelpFileName;
	
	String getHelpFileName() {
		if (myHelpFileName == null) {
			myHelpFileName = ZLibrary.JAR_DATA_PREFIX + "data/help/MiniHelp." + Locale.getDefault().getLanguage() + ".fb2";
			InputStream testStream = null;
			try {
				testStream = ZLibrary.Instance().getInputStream(myHelpFileName);
				testStream.close();
			} catch (Exception e) {
			}
			if (testStream == null) {
				myHelpFileName = ZLibrary.JAR_DATA_PREFIX + "data/help/MiniHelp.en.fb2";
			}
		}
		return myHelpFileName;
	}

	private final ZLStringOption myBookNameOption =
		new ZLStringOption(ZLOption.STATE_CATEGORY, "State", "Book", "");

	private final ZLKeyBindings myBindings0 = new ZLKeyBindings("Keys");
	private final ZLKeyBindings myBindings90 = new ZLKeyBindings("Keys90");
	private final ZLKeyBindings myBindings180 = new ZLKeyBindings("Keys180");
	private final ZLKeyBindings myBindings270 = new ZLKeyBindings("Keys270");

	private int myMode = ViewMode.UNDEFINED;
	private int myPreviousMode = ViewMode.BOOK_TEXT;

	public final BookTextView BookTextView;
	public final ContentsView ContentsView;
	public final FootnoteView FootnoteView;
	public final CollectionView CollectionView;
	public final RecentBooksView RecentBooksView;

	private BookModel myBookModel;
	private final String myArg0;

	public FBReader() {
		this(new String[0]);
	}

	public FBReader(String[] args) {
		myArg0 = (args.length > 0) ? args[0] : null;
		addAction(ActionCode.TOGGLE_FULLSCREEN, new ZLApplication.FullscreenAction(true));
		addAction(ActionCode.FULLSCREEN_ON, new ZLApplication.FullscreenAction(false));
		addAction(ActionCode.QUIT, new QuitAction(this));
		addAction(ActionCode.SHOW_HELP, new ShowHelpAction(this));
		addAction(ActionCode.ROTATE_SCREEN, new ZLApplication.RotationAction());

		addAction(ActionCode.UNDO, new UndoAction(this));
		addAction(ActionCode.REDO, new RedoAction(this));

		addAction(ActionCode.INCREASE_FONT, new ChangeFontSizeAction(this, +2));
		addAction(ActionCode.DECREASE_FONT, new ChangeFontSizeAction(this, -2));

		addAction(ActionCode.SHOW_COLLECTION, new SetModeAction(this, ViewMode.BOOK_COLLECTION, ViewMode.BOOK_TEXT | ViewMode.CONTENTS | ViewMode.RECENT_BOOKS));
		addAction(ActionCode.SHOW_LAST_BOOKS, new SetModeAction(this, ViewMode.RECENT_BOOKS, ViewMode.BOOK_TEXT | ViewMode.CONTENTS));
		addAction(ActionCode.SHOW_OPTIONS, new ShowOptionsDialogAction(this));
		addAction(ActionCode.SHOW_CONTENTS, new ShowContentsAction(this));
		addAction(ActionCode.SHOW_BOOK_INFO, new ShowBookInfoDialogAction(this));
		addAction(ActionCode.ADD_BOOK, new AddBookAction(this));
		
		addAction(ActionCode.SEARCH, new SearchAction(this));
		addAction(ActionCode.FIND_NEXT, new FindNextAction(this));
		addAction(ActionCode.FIND_PREVIOUS, new FindPreviousAction(this));
		
		addAction(ActionCode.SCROLL_TO_HOME, new ScrollToHomeAction(this));
		addAction(ActionCode.SCROLL_TO_START_OF_TEXT, new DummyAction(this));
		addAction(ActionCode.SCROLL_TO_END_OF_TEXT, new DummyAction(this));
		addAction(ActionCode.TOUCH_SCROLL_FORWARD, new ScrollingAction(this, TouchScrollingOptions, true));
		addAction(ActionCode.TOUCH_SCROLL_BACKWARD, new ScrollingAction(this, TouchScrollingOptions, false));
		addAction(ActionCode.TRACKBALL_SCROLL_FORWARD, new ScrollingAction(this, TrackballScrollingOptions, true));
		addAction(ActionCode.TRACKBALL_SCROLL_BACKWARD, new ScrollingAction(this, TrackballScrollingOptions, false));
		addAction(ActionCode.CANCEL, new CancelAction(this));
		addAction(ActionCode.SHOW_HIDE_POSITION_INDICATOR, new ToggleIndicatorAction(this));
		addAction(ActionCode.OPEN_PREVIOUS_BOOK, new OpenPreviousBookAction(this));
		addAction(ActionCode.SHOW_HELP, new ShowHelpAction(this));
		addAction(ActionCode.GOTO_NEXT_TOC_SECTION, new DummyAction(this));
		addAction(ActionCode.GOTO_PREVIOUS_TOC_SECTION, new DummyAction(this));
		//addAction(ActionCode.COPY_SELECTED_TEXT_TO_CLIPBOARD, new DummyAction(this));
		//addAction(ActionCode.OPEN_SELECTED_TEXT_IN_DICTIONARY, new DummyAction(this));
		//addAction(ActionCode.CLEAR_SELECTION, new DummyAction(this));

		ZLPaintContext context = ZLibrary.Instance().getPaintContext();
		BookTextView = new BookTextView(this, context);
		ContentsView = new ContentsView(this, context);
		FootnoteView = new FootnoteView(this, context);
		CollectionView = new CollectionView(this, context);
		RecentBooksView = new RecentBooksView(this, context);

		setMode(ViewMode.BOOK_TEXT);
	}
		
	public void initWindow() {
		super.initWindow();
		refreshWindow();
		String fileName = null;
		if (myArg0 != null) {
			try {
				fileName = new File(myArg0).getCanonicalPath();
			} catch (IOException e) {
			}
		}
		BookDescription description = BookDescription.getDescription(fileName);   
		if (description == null) {
			description = BookDescription.getDescription(myBookNameOption.getValue());
		}
		if (description == null) {
			description = BookDescription.getDescription(getHelpFileName());
		}
		openBook(description);
		refreshWindow();
	}
	
	public void openBook(BookDescription bookDescription) {
		OpenBookRunnable runnable = new OpenBookRunnable(bookDescription);
		ZLDialogManager.getInstance().wait("loadingBook", runnable);
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
				setView(BookTextView);
				break;
			case ViewMode.CONTENTS:
				ContentsView.gotoReference();
				setView(ContentsView);
				break;
			case ViewMode.FOOTNOTE:
				setView(FootnoteView);
				break;
			case ViewMode.RECENT_BOOKS:
				RecentBooksView.rebuild();
				setView(RecentBooksView);
				break;
			case ViewMode.BOOK_COLLECTION:
				Runnable action = new Runnable() {
					public void run() {
						CollectionView.updateModel();
						if (myBookModel != null) {
							CollectionView.selectBook(myBookModel.Description);
						}
						setView(CollectionView);
					}
				};
				ZLDialogManager.getInstance().wait("loadingBookList", action);
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
			BookModel.Label label = myBookModel.getLabel(id);
			if ((label != null) && (label.Model != null)) {
		//		if (label.Model == myBookModel.BookTextModel) {
				if (label.ModelIndex != -1) {	
					BookTextView.gotoParagraphSafe(label.ModelIndex, label.ParagraphIndex);
				} else {
					FootnoteView.setModel(label.Model);
					setMode(ViewMode.FOOTNOTE);
					FootnoteView.gotoParagraph(label.ParagraphIndex, false);
				}
				setHyperlinkCursor(false);
				refreshWindow();
			}
		}
	}

	public void clearTextCaches() {
		BookTextView.clearCaches();
		ContentsView.clearCaches();
		FootnoteView.clearCaches();
		CollectionView.clearCaches();
		RecentBooksView.clearCaches();
	}
	
	void openBookInternal(BookDescription description) {
		clearTextCaches();

		if (description != null) {
			BookTextView.saveState();
			BookTextView.setModels(null, "");
			BookTextView.setContentsModel(null);
			ContentsView.setModel(null);

			myBookModel = null;
			//android.os.Debug.startMethodTracing("/data/anr/loading");
			myBookModel = new BookModel(description);
			//android.os.Debug.stopMethodTracing();
			final String fileName = description.FileName;
			myBookNameOption.setValue(fileName);
			ZLTextHyphenator.getInstance().load(description.getLanguage());
	//		BookTextView.setModel(myBookModel.BookTextModel, fileName);
			BookTextView.setModels(myBookModel.getBookTextModels(), fileName);
			BookTextView.setCaption(description.getTitle());
			BookTextView.setContentsModel(myBookModel.ContentsModel);
			FootnoteView.setModel(null);
			FootnoteView.setCaption(description.getTitle());
			ContentsView.setModel(myBookModel.ContentsModel);
			ContentsView.setCaption(description.getTitle());
			RecentBooksView.lastBooks().addBook(fileName);
		}
		resetWindowCaption();
		refreshWindow();
	}
	
	void showBookTextView() {
		setMode(ViewMode.BOOK_TEXT);
	}
	
	private class OpenBookRunnable implements Runnable {
		private	BookDescription myDescription;

		public OpenBookRunnable(BookDescription description) { 
			myDescription = description; 
		}
		
		public void run() { 
			openBookInternal(myDescription); 
		}
	}

	@Override
	public void openFile(String fileName) {
		BookDescription description = BookDescription.getDescription(fileName);
		if (description != null) {
			openBook(description);
			refreshWindow();
		}
	}

	protected void onQuit() {
		if (BookTextView != null) {
			BookTextView.saveState();
		}
	}
}
