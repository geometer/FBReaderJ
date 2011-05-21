package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.fbreader.FBAction;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;

public class SelectionPanelVisibilityAction extends FBAction {
	private final FBReader myActivity;
	private final boolean myShow;

	SelectionPanelVisibilityAction(FBReader activity, FBReaderApp fbreader, boolean show) {
		super(fbreader);
		myActivity = activity;
		myShow = show;
	}

	public void run() {
		final FBView fbview = Reader.getTextView();
		int selectionStartY = 0, selectionEndY = 0; 
		if (myShow) {
			selectionStartY = fbview.getSelectionStartY();
			selectionEndY = fbview.getSelectionEndY();
		}
		myActivity.onShowSelectionPanel(myShow, selectionStartY, selectionEndY);
	}
}
