package org.fbreader.fbreader;

import org.zlibrary.core.options.ZLBooleanOption;

class ToggleIndicatorAction extends FBAction {
	ToggleIndicatorAction(FBReader fbreader) {
		super(fbreader);
	}

	public void run() {
		ZLBooleanOption option = ((FBIndicatorInfo)fbreader().getTextView().getIndicatorInfo()).ShowOption;
		option.setValue(!option.getValue());
		fbreader().refreshWindow();
	}
}
