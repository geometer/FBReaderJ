package org.geometerplus.fbreader.fbreader;

import org.geometerplus.fbreader.collection.BookCollection;

class ShowBookInfoDialogAction extends FBAction {
	ShowBookInfoDialogAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isVisible() {
		return fbreader().getMode() == FBReader.ViewMode.BOOK_TEXT;
	}

	public void run() {
		final BookCollection collection = fbreader().getCollectionView().getCollection();
		final String fileName = fbreader().getBookTextView().getFileName();
		Runnable action = new Runnable() {
			public void run() {
				fbreader().openFile(fileName);
				collection.rebuild(false);
				fbreader().refreshWindow();
			}
		};
		new BookInfoDialog(collection, fileName, action).getDialog().run();
	}
}
