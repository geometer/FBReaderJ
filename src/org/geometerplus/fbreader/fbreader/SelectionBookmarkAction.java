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
		final FBView fbview = Reader.getTextView();
		new Bookmark(
			Reader.Model.Book,
			fbview.getModel().getId(),
			fbview.getSelectedText(), 
			fbview.getSelectionStartParagraphID(), 
			fbview.getSelectionStartElementID(),
			0
		).save();
	}
}
