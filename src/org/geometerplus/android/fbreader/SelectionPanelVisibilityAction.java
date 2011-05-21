package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;

public class SelectionPanelVisibilityAction extends FBAndroidAction {
	private final boolean myShow;

	SelectionPanelVisibilityAction(FBReader baseActivity, FBReaderApp fbreader, boolean show) {
		super(baseActivity, fbreader);
		myShow = show;
	}

	public void run() {
		final FBView fbview = Reader.getTextView();
		int selectionStartY = 0, selectionEndY = 0; 
		if (myShow) {
			selectionStartY = fbview.getSelectionStartY();
			selectionEndY = fbview.getSelectionEndY();
		}
		BaseActivity.onShowSelectionPanel(myShow, selectionStartY, selectionEndY);
	}
}
