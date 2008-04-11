package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.text.view.ZLTextView;

class FindPreviousAction extends FBAction {
	FindPreviousAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isEnabled() {
		ZLTextView view = fbreader().getTextView();
		return (view != null) && view.canFindPrevious();
	}

	public void run() {
		fbreader().getTextView().findPrevious();
	}
}
