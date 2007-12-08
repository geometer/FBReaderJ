package org.fbreader.fbreader;

import org.zlibrary.core.options.ZLIntegerRangeOption;
import org.zlibrary.text.view.style.ZLTextStyleCollection;

class ChangeFontSizeAction extends FBAction {
	ChangeFontSizeAction(FBReader fbreader, int delta) {
		super(fbreader);
		myDelta = delta;
	}

	public void run() {
		ZLIntegerRangeOption option =
			ZLTextStyleCollection.getInstance().getBaseStyle().FontSizeOption;
		option.setValue(option.getValue() + myDelta);
		//fbreader().clearTextCaches();
		fbreader().refreshWindow();
	}

	private int myDelta;
}
