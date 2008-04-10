package org.zlibrary.core.optionEntries;

import java.util.ArrayList;
import org.zlibrary.core.util.*;

import org.zlibrary.core.dialogs.ZLOptionEntry;
import org.zlibrary.core.options.ZLBooleanOption;

public class ZLToggleBooleanOptionEntry extends ZLSimpleBooleanOptionEntry {
	private final ArrayList/*<ZLOptionEntry>*/ myDependentEntries = new ArrayList();
	
	public ZLToggleBooleanOptionEntry(ZLBooleanOption option) {
		super(option);
	}

	public void addDependentEntry(ZLOptionEntry dependent) {
		myDependentEntries.add(dependent);
	}
	
	public void onStateChanged(boolean state) {
		final int size = myDependentEntries.size();
		for (int i = 0; i < size; i++) {
			((ZLOptionEntry)myDependentEntries.get(i)).setVisible(state);
		}
	}

	public void onReset() {
		onStateChanged(initialState());
	}
}
