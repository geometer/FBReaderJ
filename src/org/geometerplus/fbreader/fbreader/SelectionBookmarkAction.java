package org.geometerplus.fbreader.fbreader;

import org.geometerplus.fbreader.library.Bookmark;

public class SelectionBookmarkAction extends FBAction {

	SelectionBookmarkAction(FBReaderApp fbreader) {
		super(fbreader);
	}

	public boolean isVisible() {
		return !Reader.getTextView().isSelectionEmpty();
	}

	public void run() {
		Bookmark bookmark = Reader.addBookmark();
		bookmark.save();
	}
}
