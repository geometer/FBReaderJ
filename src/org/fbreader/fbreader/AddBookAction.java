package org.fbreader.fbreader;

import org.fbreader.collection.BookList;
import org.fbreader.description.BookDescription;
import org.zlibrary.core.dialogs.ZLDialogManager;

class AddBookAction extends FBAction {
	AddBookAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isVisible() {
		return fbreader().getMode() != FBReader.ViewMode.FOOTNOTE;
	}

	public void run() {
		FBFileHandler handler = new FBFileHandler();
		if (ZLDialogManager.getInstance().runSelectionDialog("addFileDialog", handler)) {
			BookDescription description = handler.getDescription();
			if ((description != null) && fbreader().runBookInfoDialog(description.getFileName())) {
				new BookList().addFileName(description.getFileName());
				fbreader().setMode(FBReader.ViewMode.BOOK_TEXT);
			}
			
		/*	if (description != null) {
				fbreader().openBook(description);
				fbreader().setMode(FBReader.ViewMode.BOOK_TEXT);
				fbreader().refreshWindow();
			}
		*/	
		}
	}
}
