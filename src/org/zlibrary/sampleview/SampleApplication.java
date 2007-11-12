package org.zlibrary.sampleview;

import org.zlibrary.core.application.*;


public class SampleApplication extends ZLApplication {
	public SampleApplication() {
		super("Sample");
		setView(new SampleView(this, getContext()));
	}

	public ZLKeyBindings keyBindings() {
		return null;
	}
}
