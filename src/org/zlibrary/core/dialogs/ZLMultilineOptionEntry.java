package org.zlibrary.core.dialogs;

public abstract class ZLMultilineOptionEntry extends ZLTextOptionEntry {
	protected ZLMultilineOptionEntry() {}
	
	public int getKind() {
		return ZLOptionKind.MULTILINE;
	}
}
