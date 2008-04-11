package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;

class ChangeFontSizeAction extends FBAction {
	ChangeFontSizeAction(FBReader fbreader, int delta) {
		super(fbreader);
		myDelta = delta;
	}

	public void run() {
		ZLIntegerRangeOption option =
			ZLTextStyleCollection.getInstance().getBaseStyle().FontSizeOption;
		option.setValue(option.getValue() + myDelta);
		fbreader().clearTextCaches();
		fbreader().refreshWindow();
	}

	private int myDelta;
}
