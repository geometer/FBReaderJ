package org.zlibrary.core.dialogs;

public abstract class ZLStringOptionEntry extends ZLTextOptionEntry {
	protected ZLStringOptionEntry() {
	}
	
	public int getKind() {
		return ZLOptionKind.STRING;
	}
}
