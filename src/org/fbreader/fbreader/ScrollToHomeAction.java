package org.fbreader.fbreader;

class ScrollToHomeAction extends FBAction {
	ScrollToHomeAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isVisible() {
		return fbreader().getMode() == FBReader.ViewMode.BOOK_TEXT;
	}

	public boolean isEnabled() {
		return false;
		//return isVisible();
	}

	public void run() {
		//
	}
}
