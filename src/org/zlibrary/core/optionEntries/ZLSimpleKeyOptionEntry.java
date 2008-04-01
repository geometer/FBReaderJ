package org.zlibrary.core.optionEntries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.application.ZLKeyBindings;
import org.zlibrary.core.dialogs.ZLKeyOptionEntry;
import org.zlibrary.core.util.*;

public abstract class ZLSimpleKeyOptionEntry extends ZLKeyOptionEntry {
	private ZLKeyBindings myBindings;
	private final HashMap/*<std::string,std::string>*/ myChangedCodes = new HashMap();
	
	public ZLSimpleKeyOptionEntry(ZLKeyBindings bindings) {
		super();
		myBindings = bindings;
	}
	
	public int actionIndex(String key) {
		String code = (String) myChangedCodes.get(key);
		return codeIndexBimap().indexByCode((code != null) ? code : myBindings.getBinding(key));
	}

	public void onAccept() {
		for (Iterator it = myChangedCodes.entrySet().iterator(); it.hasNext(); ) {
			Entry entry = (Entry) it.next();
			myBindings.bindKey((String) entry.getKey(), (String) entry.getValue());
		}
		myBindings.saveCustomBindings();
	}

	public void onKeySelected(String key) {}

	public void onValueChanged(String key, int index) {
		myChangedCodes.put(key, codeIndexBimap().codeByIndex(index));
	}
	
	public abstract CodeIndexBimap codeIndexBimap();
	
	public static class CodeIndexBimap {
		private final ArrayList/*<String>*/ CodeByIndex = new ArrayList();
		private final HashMap/*<std::string,int>*/ IndexByCode = new HashMap();
			
		public void insert(final String code) {
			IndexByCode.put(code, CodeByIndex.size());
			CodeByIndex.add(code);
		}
			
		public int indexByCode(final String code) {
			return (Integer) IndexByCode.get(code); 
		}
		
		public String codeByIndex(int index) {
			if ((index < 0) || (index >= (int) CodeByIndex.size())) {
				return ZLApplication.NoAction;
			}
			return (String) CodeByIndex.get(index);
		}
	}

}
