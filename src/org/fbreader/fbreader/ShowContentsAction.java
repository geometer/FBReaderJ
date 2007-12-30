package org.fbreader.fbreader;

class ShowContentsAction extends FBAction {
	ShowContentsAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isVisible() {
		switch (fbreader().getMode()) {
			case FBReader.ViewMode.BOOK_TEXT:
			case FBReader.ViewMode.FOOTNOTE:
				return true;
			default:
				return false;
		}
	}

	public void run() {
		fbreader().setMode(FBReader.ViewMode.CONTENTS);
	}
}
