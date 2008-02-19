package org.zlibrary.core.dialogs;

public abstract class ZLTextOptionEntry extends ZLOptionEntry {
	public abstract String initialValue();
	
	public abstract void onAccept(String value);
	
	public boolean useOnValueEdited() {
		return false;
	}
	
	public void onValueEdited(String value) {
	}
}
