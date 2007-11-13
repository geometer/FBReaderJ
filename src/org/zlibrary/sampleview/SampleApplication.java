package org.zlibrary.sampleview;

import org.zlibrary.core.application.*;


public class SampleApplication extends ZLApplication {
	public SampleApplication(String fileName) {
		super("Sample");
		SampleView view = new SampleView(this, getContext());
		view.setModel(fileName);
		setView(view);
	}

	public ZLKeyBindings keyBindings() {
		return null;
	}
}
