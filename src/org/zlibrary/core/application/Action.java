package org.zlibrary.core.application;

abstract public class Action {
	public boolean isVisible() {
		return true;
	}

	public boolean isEnabled() {
		return isVisible();
	}
	
	public void checkAndRun() {
		if (isEnabled()) {
			run();
		}
	}
	
	public boolean useKeyDelay() {
		return true;
	}
	
	abstract protected void run();
}
