package org.zlibrary.core.dialogs;

public abstract class ZLChoiceOptionEntry extends ZLOptionEntry {
	protected ZLChoiceOptionEntry() {}
	
	public int getKind() {
		return ZLOptionKind.CHOICE;
	}

	public abstract String getText(int index);
	
	public abstract int choiceNumber();
	
	public abstract int initialCheckedIndex();
	
	public abstract void onAccept(int index);
}
