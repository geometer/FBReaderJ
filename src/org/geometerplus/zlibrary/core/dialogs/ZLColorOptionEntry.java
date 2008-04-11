package org.geometerplus.zlibrary.core.dialogs;

import org.geometerplus.zlibrary.core.util.ZLColor;

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
