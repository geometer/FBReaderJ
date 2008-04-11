package org.geometerplus.fbreader.fbreader;

class RedoAction extends FBAction {
	RedoAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isVisible() {
		return fbreader().getMode() == FBReader.ViewMode.BOOK_TEXT;
	}

	public boolean isEnabled() {
		return isVisible() && fbreader().getBookTextView().canRedoPageMove();
	}

	public void run() {
		fbreader().getBookTextView().redoPageMove();
	}
}
