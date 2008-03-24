package org.fbreader.fbreader;

class SearchAction extends FBAction {
	SearchAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isVisible() {
		return fbreader().getTextView() != null;
	}

	public void run() {
		fbreader().getTextView().search("!", true, true, false, false);
	}
}
