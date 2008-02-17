package org.fbreader.fbreader;

class SetModeAction extends FBAction {
	SetModeAction(FBReader fbreader, int modeToSet, int visibleInModes) {
		super(fbreader);
		myModeToSet = modeToSet;
		myVisibleInModes = visibleInModes;
	}

	public boolean isVisible() {
		return (fbreader().getMode() & myVisibleInModes) != 0;
	}

	public void run() {
		fbreader().setMode(myModeToSet);
	}

	private final int myModeToSet;
	private final int myVisibleInModes;
}
