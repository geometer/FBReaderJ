package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.application.ZLApplication;

abstract class FBAction extends ZLApplication.ZLAction {
	private final FBReader myFBReader;

	FBAction(FBReader fbreader) {
		myFBReader = fbreader;
	}
	
	FBReader fbreader() {
		return myFBReader;
	}
}
