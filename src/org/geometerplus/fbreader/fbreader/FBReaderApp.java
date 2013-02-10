/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.filesystem.*;
import org.geometerplus.zlibrary.core.application.*;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.util.ZLColor;

import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;
import org.geometerplus.zlibrary.text.view.*;

import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.bookmodel.*;

public final class FBReaderApp extends ZLApplication {
	public final ZLBooleanOption AllowScreenBrightnessAdjustmentOption =
		new ZLBooleanOption("LookNFeel", "AllowScreenBrightnessAdjustment", true);
	public final ZLStringOption TextSearchPatternOption =
		new ZLStringOption("TextSearch", "Pattern", "");

	public final ZLBooleanOption UseSeparateBindingsOption =
		new ZLBooleanOption("KeysOptions", "UseSeparateBindings", false);

	public final ZLBooleanOption EnableDoubleTapOption =
		new ZLBooleanOption("Options", "EnableDoubleTap", false);
	public final ZLBooleanOption NavigateAllWordsOption =
		new ZLBooleanOption("Options", "NavigateAllWords", false);

	public static enum WordTappingAction {
		doNothing, selectSingleWord, startSelecting, openDictionary
	}
	public final ZLEnumOption<WordTappingAction> WordTappingActionOption =
		new ZLEnumOption<WordTappingAction>("Options", "WordTappingAction", WordTappingAction.startSelecting);

	public final ZLColorOption ImageViewBackgroundOption =
		new ZLColorOption("Colors", "ImageViewBackground", new ZLColor(255, 255, 255));
	public final ZLEnumOption<FBView.ImageFitting> FitImagesToScreenOption =
		new ZLEnumOption<FBView.ImageFitting>("Options", "FitImagesToScreen", FBView.ImageFitting.covers);
	public static enum ImageTappingAction {
		doNothing, selectImage, openImageView
	}
	public final ZLEnumOption<ImageTappingAction> ImageTappingActionOption =
		new ZLEnumOption<ImageTappingAction>("Options", "ImageTappingAction", ImageTappingAction.openImageView);

	public final ZLIntegerRangeOption LeftMarginOption;
	public final ZLIntegerRangeOption RightMarginOption;
	public final ZLIntegerRangeOption TopMarginOption;
	public final ZLIntegerRangeOption BottomMarginOption;
	{
		final int dpi = ZLibrary.Instance().getDisplayDPI();
		final int x = ZLibrary.Instance().getPixelWidth();
		final int y = ZLibrary.Instance().getPixelHeight();
		final int horMargin = Math.min(dpi / 5, Math.min(x, y) / 30);
		LeftMarginOption = new ZLIntegerRangeOption("Options", "LeftMargin", 0, 100, horMargin);
		RightMarginOption = new ZLIntegerRangeOption("Options", "RightMargin", 0, 100, horMargin);
		TopMarginOption = new ZLIntegerRangeOption("Options", "TopMargin", 0, 100, 0);
		BottomMarginOption = new ZLIntegerRangeOption("Options", "BottomMargin", 0, 100, 4);
	}

	public final ZLIntegerRangeOption ScrollbarTypeOption =
		new ZLIntegerRangeOption("Options", "ScrollbarType", 0, 3, FBView.SCROLLBAR_SHOW_AS_FOOTER);
	public final ZLIntegerRangeOption FooterHeightOption =
		new ZLIntegerRangeOption("Options", "FooterHeight", 8, 20, 9);
	public final ZLBooleanOption FooterShowTOCMarksOption =
		new ZLBooleanOption("Options", "FooterShowTOCMarks", true);
	public final ZLBooleanOption FooterShowClockOption =
		new ZLBooleanOption("Options", "ShowClockInFooter", true);
	public final ZLBooleanOption FooterShowBatteryOption =
		new ZLBooleanOption("Options", "ShowBatteryInFooter", true);
	public final ZLBooleanOption FooterShowProgressOption =
		new ZLBooleanOption("Options", "ShowProgressInFooter", true);
	public final ZLStringOption FooterFontOption =
		new ZLStringOption("Options", "FooterFont", "Droid Sans");

	final ZLStringOption ColorProfileOption =
		new ZLStringOption("Options", "ColorProfile", ColorProfile.DAY);

	public final ZLBooleanOption ShowLibraryInCancelMenuOption =
		new ZLBooleanOption("CancelMenu", "library", true);
	public final ZLBooleanOption ShowNetworkLibraryInCancelMenuOption =
		new ZLBooleanOption("CancelMenu", "networkLibrary", true);
	public final ZLBooleanOption ShowPreviousBookInCancelMenuOption =
		new ZLBooleanOption("CancelMenu", "previousBook", false);
	public final ZLBooleanOption ShowPositionsInCancelMenuOption =
		new ZLBooleanOption("CancelMenu", "positions", true);

	private final ZLKeyBindings myBindings = new ZLKeyBindings("Keys");

	public final FBView BookTextView;
	public final FBView FootnoteView;

	public volatile BookModel Model;

	private ZLTextPosition myJumpEndPosition;
	private Date myJumpTimeStamp;

	public final IBookCollection Collection;

	public FBReaderApp(IBookCollection collection) {
		Collection = collection;

		addAction(ActionCode.INCREASE_FONT, new ChangeFontSizeAction(this, +2));
		addAction(ActionCode.DECREASE_FONT, new ChangeFontSizeAction(this, -2));

		addAction(ActionCode.FIND_NEXT, new FindNextAction(this));
		addAction(ActionCode.FIND_PREVIOUS, new FindPreviousAction(this));
		addAction(ActionCode.CLEAR_FIND_RESULTS, new ClearFindResultsAction(this));

		addAction(ActionCode.SELECTION_CLEAR, new SelectionClearAction(this));

		addAction(ActionCode.TURN_PAGE_FORWARD, new TurnPageAction(this, true));
		addAction(ActionCode.TURN_PAGE_BACK, new TurnPageAction(this, false));

		addAction(ActionCode.MOVE_CURSOR_UP, new MoveCursorAction(this, FBView.Direction.up));
		addAction(ActionCode.MOVE_CURSOR_DOWN, new MoveCursorAction(this, FBView.Direction.down));
		addAction(ActionCode.MOVE_CURSOR_LEFT, new MoveCursorAction(this, FBView.Direction.rightToLeft));
		addAction(ActionCode.MOVE_CURSOR_RIGHT, new MoveCursorAction(this, FBView.Direction.leftToRight));

		addAction(ActionCode.VOLUME_KEY_SCROLL_FORWARD, new VolumeKeyTurnPageAction(this, true));
		addAction(ActionCode.VOLUME_KEY_SCROLL_BACK, new VolumeKeyTurnPageAction(this, false));

		addAction(ActionCode.SWITCH_TO_DAY_PROFILE, new SwitchProfileAction(this, ColorProfile.DAY));
		addAction(ActionCode.SWITCH_TO_NIGHT_PROFILE, new SwitchProfileAction(this, ColorProfile.NIGHT));

		addAction(ActionCode.EXIT, new ExitAction(this));

		BookTextView = new FBView(this);
		FootnoteView = new FBView(this);

		setView(BookTextView);
	}

	public void openBook(final Book book, final Bookmark bookmark, final Runnable postAction) {
		if (book != null || Model == null) {
			runWithMessage("loadingBook", new Runnable() {
				public void run() {
					openBookInternal(book, bookmark, false);
				}
			}, postAction);
		}
	}

	public void reloadBook() {
		if (Model != null && Model.Book != null) {
			runWithMessage("loadingBook", new Runnable() {
				public void run() {
					openBookInternal(Model.Book, null, true);
				}
			}, null);
		}
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
			myJumpEndPosition = null;
			myJumpTimeStamp = null;
			BookModel.Label label = Model.getLabel(id);
			if (label != null) {
				if (label.ModelId == null) {
					if (getTextView() == BookTextView) {
						addInvisibleBookmark();
						myJumpEndPosition = new ZLTextFixedPosition(label.ParagraphIndex, 0, 0);
						myJumpTimeStamp = new Date();
					}
					BookTextView.gotoPosition(label.ParagraphIndex, 0, 0);
					setView(BookTextView);
				} else {
					FootnoteView.setModel(Model.getFootnoteModel(label.ModelId));
					setView(FootnoteView);
					FootnoteView.gotoPosition(label.ParagraphIndex, 0, 0);
				}
				getViewWidget().repaint();
			}
		}
	}

	public void clearTextCaches() {
		BookTextView.clearCaches();
		FootnoteView.clearCaches();
	}

	synchronized void openBookInternal(Book book, Bookmark bookmark, boolean force) {
		if (book == null) {
			book = Collection.getRecentBook(0);
			if (book == null || !book.File.exists()) {
				book = Collection.getBookByFile(BookUtil.getHelpFile());
			}
			if (book == null) {
				return;
			}
		}
		if (!force && Model != null && book.equals(Model.Book)) {
			if (bookmark != null) {
				gotoBookmark(bookmark);
			}
			return;
		}

		onViewChanged();

		storePosition();
		BookTextView.setModel(null);
		FootnoteView.setModel(null);
		clearTextCaches();

		Model = null;
		System.gc();
		System.gc();
		try {
			Model = BookModel.createModel(book);
			Collection.saveBook(book, false);
			ZLTextHyphenator.Instance().load(book.getLanguage());
			BookTextView.setModel(Model.getTextModel());
			BookTextView.gotoPosition(Collection.getStoredPosition(book.getId()));
			if (bookmark == null) {
				setView(BookTextView);
			} else {
				gotoBookmark(bookmark);
			}
			Collection.addBookToRecentList(book);
			final StringBuilder title = new StringBuilder(book.getTitle());
			if (!book.authors().isEmpty()) {
				boolean first = true;
				for (Author a : book.authors()) {
					title.append(first ? " (" : ", ");
					title.append(a.DisplayName);
					first = false;
				}
				title.append(")");
			}
			setTitle(title.toString());
		} catch (BookReadingException e) {
			processException(e);
		}

		getViewWidget().reset();
		getViewWidget().repaint();
	}

	public boolean jumpBack() {
		try {
			if (getTextView() != BookTextView) {
				showBookTextView();
				return true;
			}

			if (myJumpEndPosition == null || myJumpTimeStamp == null) {
				return false;
			}
			// more than 2 minutes ago
			if (myJumpTimeStamp.getTime() + 2 * 60 * 1000 < new Date().getTime()) {
				return false;
			}
			if (!myJumpEndPosition.equals(BookTextView.getStartCursor())) {
				return false;
			}

			final List<Bookmark> bookmarks = Collection.invisibleBookmarks(Model.Book);
			if (bookmarks.isEmpty()) {
				return false;
			}
			final Bookmark b = bookmarks.get(0);
			Collection.deleteBookmark(b);
			gotoBookmark(b);
			return true;
		} finally {
			myJumpEndPosition = null;
			myJumpTimeStamp = null;
		}
	}

	private void gotoBookmark(Bookmark bookmark) {
		final String modelId = bookmark.ModelId;
		if (modelId == null) {
			addInvisibleBookmark();
			BookTextView.gotoPosition(bookmark);
			setView(BookTextView);
		} else {
			FootnoteView.setModel(Model.getFootnoteModel(modelId));
			FootnoteView.gotoPosition(bookmark);
			setView(FootnoteView);
		}
		getViewWidget().repaint();
	}

	public void showBookTextView() {
		setView(BookTextView);
	}

	public void onWindowClosing() {
		storePosition();
	}

	public void storePosition() {
		if (Model != null && Model.Book != null && BookTextView != null) {
			Collection.storePosition(Model.Book.getId(), BookTextView.getStartCursor());
		}
	}

	static enum CancelActionType {
		library,
		networkLibrary,
		previousBook,
		returnTo,
		close
	}

	public static class CancelActionDescription {
		final CancelActionType Type;
		public final String Title;
		public final String Summary;

		CancelActionDescription(CancelActionType type, String summary) {
			final ZLResource resource = ZLResource.resource("cancelMenu");
			Type = type;
			Title = resource.getResource(type.toString()).getValue();
			Summary = summary;
		}
	}

	private static class BookmarkDescription extends CancelActionDescription {
		final Bookmark Bookmark;

		BookmarkDescription(Bookmark b) {
			super(CancelActionType.returnTo, b.getText());
			Bookmark = b;
		}
	}

	private final ArrayList<CancelActionDescription> myCancelActionsList =
		new ArrayList<CancelActionDescription>();

	public List<CancelActionDescription> getCancelActionsList() {
		myCancelActionsList.clear();
		if (ShowLibraryInCancelMenuOption.getValue()) {
			myCancelActionsList.add(new CancelActionDescription(
				CancelActionType.library, null
			));
		}
		if (ShowNetworkLibraryInCancelMenuOption.getValue()) {
			myCancelActionsList.add(new CancelActionDescription(
				CancelActionType.networkLibrary, null
			));
		}
		if (ShowPreviousBookInCancelMenuOption.getValue()) {
			final Book previousBook = Collection.getRecentBook(1);
			if (previousBook != null) {
				myCancelActionsList.add(new CancelActionDescription(
					CancelActionType.previousBook, previousBook.getTitle()
				));
			}
		}
		if (ShowPositionsInCancelMenuOption.getValue()) {
			if (Model != null && Model.Book != null) {
				for (Bookmark bookmark : Collection.invisibleBookmarks(Model.Book)) {
					myCancelActionsList.add(new BookmarkDescription(bookmark));
				}
			}
		}
		myCancelActionsList.add(new CancelActionDescription(
			CancelActionType.close, null
		));
		return myCancelActionsList;
	}

	public void runCancelAction(int index) {
		if (index < 0 || index >= myCancelActionsList.size()) {
			return;
		}

		final CancelActionDescription description = myCancelActionsList.get(index);
		switch (description.Type) {
			case library:
				runAction(ActionCode.SHOW_LIBRARY);
				break;
			case networkLibrary:
				runAction(ActionCode.SHOW_NETWORK_LIBRARY);
				break;
			case previousBook:
				openBook(Collection.getRecentBook(1), null, null);
				break;
			case returnTo:
			{
				final Bookmark b = ((BookmarkDescription)description).Bookmark;
				Collection.deleteBookmark(b);
				gotoBookmark(b);
				break;
			}
			case close:
				closeWindow();
				break;
		}
	}

	private synchronized void updateInvisibleBookmarksList(Bookmark b) {
		if (Model != null && Model.Book != null && b != null) {
			for (Bookmark bm : Collection.invisibleBookmarks(Model.Book)) {
				if (b.equals(bm)) {
					Collection.deleteBookmark(bm);
				}
			}
			Collection.saveBookmark(b);
			final List<Bookmark> bookmarks = Collection.invisibleBookmarks(Model.Book);
			for (int i = 3; i < bookmarks.size(); ++i) {
				Collection.deleteBookmark(bookmarks.get(i));
			}
		}
	}

	public void addInvisibleBookmark(ZLTextWordCursor cursor) {
		if (cursor != null && Model != null && Model.Book != null && getTextView() == BookTextView) {
			updateInvisibleBookmarksList(new Bookmark(
				Model.Book,
				getTextView().getModel().getId(),
				cursor,
				6,
				false
			));
		}
	}

	public void addInvisibleBookmark() {
		if (Model.Book != null && getTextView() == BookTextView) {
			updateInvisibleBookmarksList(createBookmark(6, false));
		}
	}

	public Bookmark createBookmark(int maxLength, boolean visible) {
		final FBView view = getTextView();
		final ZLTextWordCursor cursor = view.getStartCursor();

		if (cursor.isNull()) {
			return null;
		}

		return new Bookmark(
			Model.Book,
			view.getModel().getId(),
			cursor,
			maxLength,
			visible
		);
	}

	public TOCTree getCurrentTOCElement() {
		final ZLTextWordCursor cursor = BookTextView.getStartCursor();
		if (Model == null || cursor == null) {
			return null;
		}

		int index = cursor.getParagraphIndex();
		if (cursor.isEndOfParagraph()) {
			++index;
		}
		TOCTree treeToSelect = null;
		for (TOCTree tree : Model.TOCTree) {
			final TOCTree.Reference reference = tree.getReference();
			if (reference == null) {
				continue;
			}
			if (reference.ParagraphIndex > index) {
				break;
			}
			treeToSelect = tree;
		}
		return treeToSelect;
	}
}
