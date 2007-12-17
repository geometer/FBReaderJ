package org.fbreader.fbreader;

class ShowContentsAction extends FBAction {
	ShowContentsAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isVisible() {
		switch (fbreader().getMode()) {
			case BOOK_TEXT:
			case FOOTNOTE:
				return true;
			default:
				return false;
		}
	}

	public void run() {
		fbreader().setMode(FBReader.ViewMode.CONTENTS);
	}
}
