package org.zlibrary.core.optionEntries;

import org.zlibrary.core.dialogs.ZLSpinOptionEntry;
import org.zlibrary.core.options.ZLIntegerRangeOption;

public class ZLSimpleSpinOptionEntry extends ZLSpinOptionEntry {
	private int myStep;
	private ZLIntegerRangeOption myOption;
	
	public ZLSimpleSpinOptionEntry(ZLIntegerRangeOption option, int step) {
		myStep = step;
		myOption = option;
	}
	
	public int getStep() {
		return myStep;
	}

	public int initialValue() {
		return myOption.getValue();
	}

	public int maxValue() {
		return myOption.getMaxValue();
	}

	public int minValue() {
		return myOption.getMinValue();
	}

	public void onAccept(int value) {
		myOption.setValue(value);
	}
}
