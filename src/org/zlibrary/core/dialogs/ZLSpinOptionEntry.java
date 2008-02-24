package org.zlibrary.core.dialogs;

public abstract class ZLSpinOptionEntry extends ZLOptionEntry {
	protected ZLSpinOptionEntry() {}
	
	public int getKind() {
		return ZLOptionKind.SPIN;
	}

	public abstract int initialValue();
	
	public abstract int minValue();
	
	public abstract int maxValue() ;
	
	public abstract int step();
	
	public abstract void onAccept(int value);
}
