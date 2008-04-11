package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.options.ZLBooleanOption;

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
