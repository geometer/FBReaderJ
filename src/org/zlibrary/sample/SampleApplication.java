package org.zlibrary.sample;

import org.zlibrary.core.application.ZLApplication;

public class SampleApplication extends ZLApplication {
	public SampleApplication() {
		super("Sample");
		setView(new SampleView(this, getContext()));
	}
}
