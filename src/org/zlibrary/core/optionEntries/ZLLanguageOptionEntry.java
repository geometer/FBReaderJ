package org.zlibrary.core.optionEntries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.zlibrary.core.dialogs.ZLComboOptionEntry;
import org.zlibrary.core.language.ZLLanguageList;
import org.zlibrary.core.options.ZLIntegerOption;
import org.zlibrary.core.options.ZLStringOption;

public class ZLLanguageOptionEntry extends ZLComboOptionEntry {
	public ZLLanguageOptionEntry(ZLStringOption languageOption, ArrayList/*<String>*/ languageCodes) {
		myLanguageOption = languageOption;
		String initialCode = myLanguageOption.getValue();
		for (Iterator it = languageCodes.iterator(); it.hasNext(); ) {
			String itstr = (String)it.next();
			String name = ZLLanguageList.languageName(itstr);
			myValuesToCodes.put(name, itstr);
			if (initialCode.equals(itstr)) {
				myInitialValue = name;
			}
		}
		for (Iterator it = myValuesToCodes.keySet().iterator(); it.hasNext(); ) {
			myValues.add(it.next());
		}
		String otherCode = "other";
		String otherName = ZLLanguageList.languageName(otherCode);
		myValues.add(otherName);
		myValuesToCodes.put(otherName,otherCode);
		if (myInitialValue.length() == 0) {
			myInitialValue = otherName;
		}

	}

	public	String initialValue() {
		return myInitialValue;
	}
	
	public	ArrayList/*<String>*/ getValues() {
		return myValues;
	}
	
	public	void onAccept(String value) {
		myLanguageOption.setValue((String)myValuesToCodes.get(value));
	}

	private ArrayList/*<String>*/ myValues;
	private	HashMap/*<String,String>*/ myValuesToCodes;
	private	String myInitialValue;
	private	ZLStringOption myLanguageOption;
}
