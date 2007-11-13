package org.fbreader.fbreader;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.application.ZLKeyBindings;
import org.zlibrary.text.view.ZLTextView;

public class FBReader extends ZLApplication {
	public FBReader(String fileName) {
		super("Sample");
		ZLTextView view = new ZLTextView(this, getContext());
		view.setModel(fileName);
		setView(view);
	}

	public ZLKeyBindings keyBindings() {
		return null;
	}
}
