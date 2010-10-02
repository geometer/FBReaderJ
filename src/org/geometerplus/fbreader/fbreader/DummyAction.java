package org.geometerplus.fbreader.fbreader;

public class DummyAction extends FBAction {
	final boolean myEnabled;
	DummyAction(FBReader fbreader, boolean enabled) {
		super(fbreader);
		myEnabled = enabled;
	}

	public boolean isEnabled() {
		return myEnabled;
	}

	public void run() {
	}
}
