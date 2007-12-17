package org.fbreader.fbreader;

class RedoAction extends FBAction {
	RedoAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isVisible() {
		return fbreader().getMode() == FBReader.ViewMode.BOOK_TEXT;
	}

	public boolean isEnabled() {
		return false;
		//return isVisible();
			// && fbreader().bookTextView().canRedoPageMove();
	}

	public void run() {
	}
}
