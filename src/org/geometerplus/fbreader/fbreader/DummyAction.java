package org.geometerplus.fbreader.fbreader;

public class DummyAction extends FBAction {
	final boolean myIsEnabled;

	DummyAction(FBReaderApp fbreader, boolean enabled) {
		super(fbreader);
		myIsEnabled = enabled;
	}

	public boolean isEnabled() {
		return myIsEnabled;
	}

	public void run() {
	}
}
