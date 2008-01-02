package org.fbreader.fbreader;

import org.zlibrary.text.view.ZLTextView;

class ScrollingAction extends FBAction {
	private final ScrollingOptions myOptions;
	private final boolean myForward;

	ScrollingAction(FBReader fbreader, ScrollingOptions options, boolean forward) {
		super(fbreader);
		myOptions = options;
		myForward = forward;
	}
		
	public boolean isEnabled() {
		// TODO: implement
		return true;
	}

	public void run() {
		// TODO: use delay option
		int mode = myOptions.ModeOption.getValue();
		int value = 0;
		switch (mode) {
			case ZLTextView.ScrollingMode.KEEP_LINES:
				value = myOptions.LinesToKeepOption.getValue();
				break;
			case ZLTextView.ScrollingMode.SCROLL_LINES:
				value = myOptions.LinesToScrollOption.getValue();
				break;
			case ZLTextView.ScrollingMode.SCROLL_PERCENTAGE:
				value = myOptions.PercentToScrollOption.getValue();
				break;
		}
		fbreader().getTextView().scrollPage(myForward, mode, value);
		fbreader().refreshWindow();
	}		
}
