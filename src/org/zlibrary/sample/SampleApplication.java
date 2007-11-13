package org.zlibrary.sample;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.application.ZLKeyBindings;

public class SampleApplication extends ZLApplication {
	public SampleApplication() {
		super("Sample");
		setView(new SampleView(this, getContext()));
	}

	public ZLKeyBindings keyBindings() {
		return null;
	}
}
