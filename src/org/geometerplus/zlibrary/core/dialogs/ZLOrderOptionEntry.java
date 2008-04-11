package org.geometerplus.zlibrary.core.dialogs;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

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
