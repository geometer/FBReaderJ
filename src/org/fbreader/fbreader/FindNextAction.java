package org.fbreader.fbreader;

import org.zlibrary.text.view.ZLTextView;

class FindNextAction extends FBAction {
	FindNextAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isEnabled() {
		ZLTextView view = fbreader().getTextView();
		return (view != null) && view.canFindNext();
	}

	public void run() {
		fbreader().getTextView().findNext();
	}
}
