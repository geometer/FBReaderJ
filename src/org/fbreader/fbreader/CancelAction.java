package org.fbreader.fbreader;

class CancelAction extends FBAction {
	CancelAction(FBReader fbreader) {
		super(fbreader);
	}

	public void run() {
		if (fbreader().getMode() != FBReader.ViewMode.BOOK_TEXT) {
			fbreader().restorePreviousMode();
		} else if (fbreader().isFullscreen()) {
			fbreader().setFullscreen(false);
		//} else if (fbreader().QuitOnCancelOption.value()) {
		//	fbreader().quit();
		}
	}
}
