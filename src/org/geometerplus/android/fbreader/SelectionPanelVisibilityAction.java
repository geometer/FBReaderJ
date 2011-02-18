package org.geometerplus.android.fbreader;

import org.geometerplus.fbreader.fbreader.FBAction;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;

public class SelectionPanelVisibilityAction extends FBAction {
	private final FBReader myActivity;

	SelectionPanelVisibilityAction(FBReader activity, FBReaderApp fbreader) {
		super(fbreader);
		myActivity = activity;
	}

	public boolean isVisible() {
		return Reader.Model != null;
	}

	public void run() {
		final FBView fbview = Reader.getTextView();
		boolean show = fbview.isSelectionModeActive() && !fbview.isNowSelecting();
		int selectionStartY = 0, selectionEndY = 0; 
		if (show) {
			selectionStartY = fbview.getSelectionStartY();
			selectionEndY = fbview.getSelectionEndY();
		}
		myActivity.onShowSelectionPanel(show, selectionStartY, selectionEndY);
	}
}
