package org.zlibrary.core.dialogs;

import java.util.*;
import org.zlibrary.core.util.*;

public abstract class ZLKeyOptionEntry extends ZLOptionEntry {
	private final ArrayList/*<String>*/ myActionNames = new ArrayList();
	
	public ZLKeyOptionEntry() {}
	
	public int getKind() {
		return ZLOptionKind.KEY;
	}

	public final void addActionName(String actionName) {
		myActionNames.add(actionName);
	}
	
	public final ArrayList getActionNames() {
		return myActionNames;
	}
	
	public abstract void onAccept();
	
	public abstract int actionIndex(String key);
	
	public abstract void onValueChanged(String key, int index);
	
	public abstract void onKeySelected(String key);
}
