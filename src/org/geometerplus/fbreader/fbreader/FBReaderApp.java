/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.application.*;
import org.geometerplus.zlibrary.core.dialogs.ZLDialogManager;
import org.geometerplus.zlibrary.core.options.*;

import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;
import org.geometerplus.zlibrary.text.view.ZLTextViewMode;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Bookmark;

public final class FBReaderApp extends ZLApplication {
	public final ZLBooleanOption AllowScreenBrightnessAdjustmentOption =
		new ZLBooleanOption("LookNFeel", "AllowScreenBrightnessAdjustment", true);
	public final ZLStringOption TextSearchPatternOption =
		new ZLStringOption("TextSearch", "Pattern", "");
	public final ZLStringOption BookmarkSearchPatternOption =
		new ZLStringOption("BookmarkSearch", "Pattern", "");

	public final ZLBooleanOption UseSeparateBindingsOption =
		new ZLBooleanOption("KeysOptions", "UseSeparateBindings", false);

	public final ZLIntegerRangeOption TextViewModeOption =
		new ZLIntegerRangeOption("Options", "TextViewMode", 0, 1, 0);
	public final ZLIntegerRangeOption LeftMarginOption =
		new ZLIntegerRangeOption("Options", "LeftMargin", 0, 30, 4);
	public final ZLIntegerRangeOption RightMarginOption =
		new ZLIntegerRangeOption("Options", "RightMargin", 0, 30, 4);
	public final ZLIntegerRangeOption TopMarginOption =
		new ZLIntegerRangeOption("Options", "TopMargin", 0, 30, 0);
	public final ZLIntegerRangeOption BottomMarginOption =
		new ZLIntegerRangeOption("Options", "BottomMargin", 0, 30, 4);

	public final ZLIntegerRangeOption ScrollbarTypeOption =
		new ZLIntegerRangeOption("Options", "ScrollbarType", 0, 3, FBView.SCROLLBAR_SHOW_AS_FOOTER);
	public final ZLIntegerRangeOption FooterHeightOption =
		new ZLIntegerRangeOption("Options", "FooterHeight", 8, 20, 9);
	public final ZLBooleanOption FooterShowTOCMarksOption =
		new ZLBooleanOption("Options", "FooterShowTOCMarks", true);
	public final ZLIntegerRangeOption FooterLongTapOption =
		new ZLIntegerRangeOption("Options", "FooterLongTap", 0, 1, 0/*revert*/);
	public final ZLBooleanOption FooterShowClockOption =
		new ZLBooleanOption("Options", "ShowClockInFooter", true);
	public final ZLBooleanOption FooterShowBatteryOption =
		new ZLBooleanOption("Options", "ShowBatteryInFooter", true);
	public final ZLBooleanOption FooterShowProgressOption =
		new ZLBooleanOption("Options", "ShowProgressInFooter", true);
	public final ZLBooleanOption FooterIsSensitiveOption =
		new ZLBooleanOption("Options", "FooterIsSensitive", false);
	public final ZLStringOption FooterFontOption =
		new ZLStringOption("Options", "FooterFont", "Droid Sans");

	final ZLBooleanOption SelectionEnabledOption =
		new ZLBooleanOption("Options", "IsSelectionEnabled", true);

	final ZLStringOption ColorProfileOption =
		new ZLStringOption("Options", "ColorProfile", ColorProfile.DAY);

	private final ZLKeyBindings myBindings = new ZLKeyBindings("Keys");

	public final FBView BookTextView;
	final FBView FootnoteView;

	public BookModel Model;

	private final String myArg0;

	public FBReaderApp(String arg) {
		myArg0 = arg;

		addAction(ActionCode.INCREASE_FONT, new ChangeFontSizeAction(this, +2));
		addAction(ActionCode.DECREASE_FONT, new ChangeFontSizeAction(this, -2));
		addAction(ActionCode.ROTATE, new RotateAction(this));

		addAction(ActionCode.FIND_NEXT, new FindNextAction(this));
		addAction(ActionCode.FIND_PREVIOUS, new FindPreviousAction(this));
		addAction(ActionCode.CLEAR_FIND_RESULTS, new ClearFindResultsAction(this));

		addAction(ActionCode.SET_TEXT_VIEW_MODE_VISIT_HYPERLINKS, new SwitchTextViewModeAction(this, ZLTextViewMode.MODE_VISIT_HYPERLINKS));
		addAction(ActionCode.SET_TEXT_VIEW_MODE_VISIT_ALL_WORDS, new SwitchTextViewModeAction(this, ZLTextViewMode.MODE_VISIT_ALL_WORDS));

		addAction(ActionCode.VOLUME_KEY_SCROLL_FORWARD, new VolumeKeyScrollingAction(this, true));
		addAction(ActionCode.VOLUME_KEY_SCROLL_BACKWARD, new VolumeKeyScrollingAction(this, false));

		addAction(ActionCode.CANCEL, new CancelAction(this));
		//addAction(ActionCode.COPY_SELECTED_TEXT_TO_CLIPBOARD, new DummyAction(this));
		//addAction(ActionCode.OPEN_SELECTED_TEXT_IN_DICTIONARY, new DummyAction(this));
		//addAction(ActionCode.CLEAR_SELECTION, new DummyAction(this));

		addAction(ActionCode.SWITCH_TO_DAY_PROFILE, new SwitchProfileAction(this, ColorProfile.DAY));
		addAction(ActionCode.SWITCH_TO_NIGHT_PROFILE, new SwitchProfileAction(this, ColorProfile.NIGHT));

		BookTextView = new FBView(this);
		FootnoteView = new FBView(this);

		setView(BookTextView);
	}

	public void initWindow() {
		super.initWindow();
		ZLDialogManager.Instance().wait("loadingBook", new Runnable() {
			public void run() {
				Book book = createBookForFile(ZLFile.createFileByPath(myArg0));
				if (book == null) {
					book = Library.getRecentBook();
				}
				if ((book == null) || !book.File.exists()) {
					book = Book.getByFile(Library.getHelpFile());
				}
				openBookInternal(book, null);
			}
		});
	}

	public void openBook(final Book book, final Bookmark bookmark) {
		if (bookmark == null & book.File.getPath().equals(Model.Book.File.getPath())) {
			return;
		}
		ZLDialogManager.Instance().wait("loadingBook", new Runnable() {
			public void run() {
				openBookInternal(book, bookmark);
			}
		});
	}

	private ColorProfile myColorProfile;

	public ColorProfile getColorProfile() {
		if (myColorProfile == null) {
			myColorProfile = ColorProfile.get(getColorProfileName());
		}
		return myColorProfile;
	}

	public String getColorProfileName() {
		return ColorProfileOption.getValue();
	}

	public void setColorProfileName(String name) {
		ColorProfileOption.setValue(name);
		myColorProfile = null;
	}

	public ZLKeyBindings keyBindings() {
		return myBindings;
	}

	public FBView getTextView() {
		return (FBView)getCurrentView();
	}

	public void tryOpenFootnote(String id) {
		if (Model != null) {
			BookModel.Label label = Model.getLabel(id);
			if (label != null) {
				if (label.ModelId == null) {
					BookTextView.gotoPosition(label.ParagraphIndex, 0, 0);
				} else {
					FootnoteView.setModel(Model.getFootnoteModel(label.ModelId));
					setView(FootnoteView);
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
		if (book != null) {
			onViewChanged();

			if (Model != null) {
				Model.Book.storePosition(BookTextView.getStartCursor());
			}
			BookTextView.setModel(null);
			FootnoteView.setModel(null);
			clearTextCaches();

			Model = null;
			System.gc();
			System.gc();
			Model = BookModel.createModel(book);
			if (Model != null) {
				ZLTextHyphenator.Instance().load(book.getLanguage());
				BookTextView.setModel(Model.BookTextModel);
				BookTextView.gotoPosition(book.getStoredPosition());
				if (bookmark == null) {
					setView(BookTextView);
				} else {
					gotoBookmark(bookmark);
				}
				Library.addBookToRecentList(book);
			}
		}
		repaintView();
	}

	public void gotoBookmark(Bookmark bookmark) {
		final String modelId = bookmark.getModelId();
		if (modelId == null) {
			BookTextView.gotoPosition(bookmark);
			setView(BookTextView);
		} else {
			FootnoteView.setModel(Model.getFootnoteModel(modelId));
			FootnoteView.gotoPosition(bookmark);
			setView(FootnoteView);
		}
		repaintView();
	}

	public void showBookTextView() {
		setView(BookTextView);
	}

	private Book createBookForFile(ZLFile file) {
		if (file == null) {
			return null;
		}
		Book book = Book.getByFile(file);
		if (book != null) {
			book.insertIntoBookList();
			return book;
		}
		if (file.isArchive()) {
			for (ZLFile child : file.children()) {
				book = Book.getByFile(child);
				if (book != null) {
					book.insertIntoBookList();
					return book;
				}
			}
		}
		return null;
	}

	@Override
	public void openFile(ZLFile file) {
		final Book book = createBookForFile(file);
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
