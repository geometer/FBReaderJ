package org.zlibrary.core.dialogs;

import java.util.ArrayList;
import org.zlibrary.core.util.*;

public abstract class ZLOrderOptionEntry extends ZLOptionEntry {
	private final ArrayList/*<String>*/ myValues = new ArrayList();
	
	public ZLOrderOptionEntry() {}
	
	public int getKind() {
		return ZLOptionKind.ORDER;
	}

	public ArrayList getValues() {
		return myValues;
	}
}
