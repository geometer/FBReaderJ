package org.fbreader.fbreader;

class DummyAction extends FBAction {
	DummyAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isVisible() {
		return true;
	}

	public boolean isEnabled() {
		return false;
	}

	public void run() {
	}
}
