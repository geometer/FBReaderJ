package org.zlibrary.core.dialogs;

import org.zlibrary.core.util.ZLColor;

public abstract class ZLColorOptionEntry extends ZLOptionEntry {
	protected ZLColorOptionEntry() {}
	
	public int getKind() {
		return ZLOptionKind.COLOR;
	}
	
	public void onReset(ZLColor color) {}
	
	public abstract ZLColor initialColor();
	
	public abstract ZLColor getColor();
	
	public abstract void onAccept(ZLColor color);
}
