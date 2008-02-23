package org.fbreader.fbreader;

import java.io.*;
import java.util.*;
import org.zlibrary.core.util.*;

import org.fbreader.bookmodel.BookModel;
import org.fbreader.description.BookDescription;
import org.fbreader.formats.fb2.FB2Reader;
import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.application.ZLKeyBindings;
import org.zlibrary.core.dialogs.ZLDialogManager;
import org.zlibrary.core.library.ZLibrary;
import org.zlibrary.core.options.*;
import org.zlibrary.core.runnable.ZLRunnable;
import org.zlibrary.core.view.ZLViewWidget;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.view.ZLTextView;
import org.zlibrary.text.hyphenation.ZLTextHyphenator;

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

	private String myHelpFileName;
	
	private BookModel myModel;
	
	private static final String OPTIONS = "Options";
	private static final String SEARCH = "Search";
	private static final String STATE = "State";
	private static final String BOOK = "Book";

	private static final String LARGE_SCROLLING = "LargeScrolling";
	private static final String SMALL_SCROLLING = "SmallScrolling";
	private static final String MOUSE_SCROLLING = "MouseScrolling";
	private static final String FINGER_TAP_SCROLLING = "FingerTapScrolling";

	private static final String DELAY = "ScrollingDelay";
	private static final String MODE = "Mode";
	private static final String LINES_TO_KEEP = "LinesToKeep";
	private static final String LINES_TO_SCROLL = "LinesToScroll";
	private static final String PERCENT_TO_SCROLL = "PercentToScroll";

	
	
	String getHelpFileName() {
		if (myHelpFileName == null) {
			myHelpFileName = ZLibrary.JAR_DATA_PREFIX + "data/help/MiniHelp." + Locale.getDefault().getLanguage() + ".fb2";
			InputStream testStream = null;
			try {
				testStream = ZLibrary.getInstance().getInputStream(myHelpFileName);
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
		//new ZLStringOption(ZLOption.STATE_CATEGORY, "State", "Book", "");
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
	private final CollectionView myCollectionView;
	private final RecentBooksView myRecentBooksView;

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

		addAction(ActionCode.SHOW_COLLECTION, new SetModeAction(this, ViewMode.BOOK_COLLECTION, ViewMode.BOOK_TEXT | ViewMode.CONTENTS | ViewMode.RECENT_BOOKS));
		addAction(ActionCode.SHOW_LAST_BOOKS, new SetModeAction(this, ViewMode.RECENT_BOOKS, ViewMode.BOOK_TEXT | ViewMode.CONTENTS));
		addAction(ActionCode.SHOW_OPTIONS, new ShowOptionsDialogAction(this));
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
		myCollectionView = new CollectionView(this, getContext());
		myRecentBooksView = new RecentBooksView(this, getContext());
		
		String fileName = null;
		if (args.length > 0) {
			try {
				fileName = new File(args[0]).getCanonicalPath();
			} catch (IOException e) {
			}
		}
		if (!openBook(fileName)) {
			openBook(getHelpFileName());
		}
		refreshWindow();
	}

	boolean openBook(String fileName) {
//		System.err.println("openBook");
//		System.out.println("try open " + fileName);
		if (fileName == null) {
			fileName = myBookNameOption.getValue();
		}
		myBookModel = new BookModel(fileName);
		//android.os.Debug.startMethodTracing("/tmp/openBook2");
		if (!new FB2Reader(myBookModel).read()) {
			myBookModel = null;
			return false;
		}
		//android.os.Debug.stopMethodTracing();
		myBookNameOption.setValue(fileName);
		myBookTextView.setModel(myBookModel.getBookTextModel(), myBookModel.getFileName());
		myContentsView.setModel(myBookModel.getContentsModel());
		setMode(ViewMode.BOOK_TEXT);
		ZLTextHyphenator.getInstance().load("ru");
		return true;
	}
	
	
	public void openBook(BookDescription bookDescription) {
		OpenBookRunnable runnable = new OpenBookRunnable(this, bookDescription);
		runnable.run();
		//ZLDialogManager.getInstance().wait(new ZLResourceKey("loadingBook"), runnable);
		resetWindowCaption();
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
			case ViewMode.RECENT_BOOKS:
				setView(myRecentBooksView);
				break;
			case ViewMode.BOOK_COLLECTION:
				setView(myCollectionView);
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

	public void clearTextCaches() {
		myBookTextView.clearCaches();
		myContentsView.clearCaches();
	}
	
	void openBookInternal(BookDescription description) {
		if (description != null) {
			BookTextView bookTextView = myBookTextView;
			ContentsView contentsView = myContentsView;
			FootnoteView footnoteView = myFootnoteView;
			RecentBooksView recentBooksView = myRecentBooksView;

			bookTextView.saveState();
			bookTextView.setModel(null, "");
			bookTextView.setContentsModel(null);
			contentsView.setModel(null, "");
			if (myModel != null) {
				myModel = null;
			}
			myModel = new BookModel(description);
			new ZLStringOption(ZLOption.STATE_CATEGORY, STATE, BOOK, "").setValue(myModel.getFileName());
			ZLTextHyphenator.getInstance().load(description.getLanguage());
			bookTextView.setModel(myModel.getBookTextModel(), description.getFileName());
			bookTextView.setCaption(description.getTitle());
			bookTextView.setContentsModel(myModel.getContentsModel());
			footnoteView.setModel(null, "");
			footnoteView.setCaption(description.getTitle());
			contentsView.setModel(myModel.getContentsModel(), description.getFileName());
			contentsView.setCaption(description.getTitle());
			recentBooksView.lastBooks().addBook(description.getFileName());
		}
	}
	
	public void showBookTextView() {
		setMode(ViewMode.BOOK_TEXT);
	}

	public CollectionView getCollectionView() {
		return myCollectionView;
	}
	
	private class OpenBookRunnable implements ZLRunnable {
		private FBReader myReader;
		private	BookDescription myDescription;

		public OpenBookRunnable(FBReader reader, BookDescription description) { 
			myReader = reader;
			myDescription = description; 
		}
		
		public void run() { 
			myReader.openBookInternal(myDescription); 
		}

	};

}
