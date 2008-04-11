package org.geometerplus.fbreader.fbreader;

import org.geometerplus.fbreader.collection.*;
import org.geometerplus.fbreader.description.BookDescription;
import org.geometerplus.zlibrary.core.dialogs.ZLDialogManager;

class AddBookAction extends FBAction {
	AddBookAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isVisible() {
		return fbreader().getMode() != FBReader.ViewMode.FOOTNOTE;
	}

	public void run() {
		final FBFileHandler handler = new FBFileHandler();
		Runnable actionOnAccept = new Runnable() {
			public void run() {
				final BookDescription description = handler.getDescription();
				if (description == null) {
					return;
				}
				final BookCollection collection = fbreader().getCollectionView().getCollection();
				final String fileName = description.getFileName();
				Runnable action = new Runnable() {
					public void run() {
						fbreader().openFile(fileName);
						collection.rebuild(false);
						new BookList().addFileName(fileName);
						fbreader().setMode(FBReader.ViewMode.BOOK_TEXT);
						fbreader().refreshWindow();
					}
				};
				new BookInfoDialog(collection, fileName, action).getDialog().run();
			}
		};
		ZLDialogManager.getInstance().runSelectionDialog("addFileDialog", handler, actionOnAccept);
	}
}
