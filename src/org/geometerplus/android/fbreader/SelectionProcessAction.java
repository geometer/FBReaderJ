package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.fbreader.FBAction;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

public abstract class SelectionProcessAction extends FBAction {
	FBReader myActivity;

	SelectionProcessAction(FBReader activity, FBReaderApp fbreader) {
		super(fbreader);
		myActivity = activity;
	}

	public boolean isVisible() {
		return !Reader.getTextView().isSelectionEmpty();
	}
}
