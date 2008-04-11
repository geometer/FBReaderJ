package org.geometerplus.fbreader.fbreader;

class UndoAction extends FBAction {
	UndoAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isVisible() {
		return true;
	}

	public boolean isEnabled() {
		return fbreader().getMode() != FBReader.ViewMode.BOOK_TEXT ||
					 fbreader().getBookTextView().canUndoPageMove();
	}

	public void run() {
		if (fbreader().getMode() == FBReader.ViewMode.BOOK_TEXT) {
			fbreader().getBookTextView().undoPageMove();
		} else {
			fbreader().restorePreviousMode();
		}
	}
}
