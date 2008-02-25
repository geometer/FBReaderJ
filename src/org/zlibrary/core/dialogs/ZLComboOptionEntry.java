package org.zlibrary.core.dialogs;

import java.util.*;
import org.zlibrary.core.util.*;

public abstract class ZLComboOptionEntry extends ZLOptionEntry {
	private final boolean myEditable;
	
	protected ZLComboOptionEntry() {
		myEditable = false;
	}
	
	protected ZLComboOptionEntry(boolean editable) {
		myEditable = editable;
	}
	
	public int getKind() {
		return ZLOptionKind.COMBO;
	}
	
	public void onValueSelected(int index) {}
	
	public final void onStringValueSelected(String value) {
		int index = getValues().indexOf(value);
		if (index != -1) {
			onValueSelected(index);
		}
	}
	
	public boolean useOnValueEdited() {
		return false;
	}
	
	public void onValueEdited(String value) {}
	
	public final boolean isEditable() {
		return myEditable;
	}
	
	public abstract String initialValue();
	
	public abstract ArrayList/*<String>*/ getValues();
	
	public abstract void onAccept(String value);
}
