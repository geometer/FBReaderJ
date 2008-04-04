package org.zlibrary.text.view;

import java.util.ArrayList;
import org.zlibrary.core.util.*;

import org.zlibrary.core.dialogs.ZLComboOptionEntry;
import org.zlibrary.core.options.ZLIntegerOption;
import org.zlibrary.core.resources.ZLResource;


public class ZLTextLineSpaceOptionEntry extends ZLComboOptionEntry {
	private static final String KEY_UNCHANGED = "unchanged";
	private static final ArrayList/*<std::string>*/ ourAllValues = new ArrayList();
	private static final ArrayList/*<std::string>*/ ourAllValuesPlusBase = new ArrayList();
	private final ZLResource myResource;
	private ZLIntegerOption myOption;
	private boolean myAllowBase;
	
	public ZLTextLineSpaceOptionEntry(ZLIntegerOption option, final ZLResource resource,
			boolean allowBase) {
		myAllowBase = allowBase;
		myResource = resource;
		myOption = option;
		if (ourAllValuesPlusBase.size() == 0) {
			for (int i = 5; i <= 20; ++i) {
				ourAllValues.add("" + (char)(i / 10 + '0') + '.' + (char)(i % 10 + '0'));
			}
			ourAllValuesPlusBase.add(myResource.getResource(KEY_UNCHANGED).getValue());
			ourAllValuesPlusBase.addAll(ourAllValues);
		}
	}
		
	public ArrayList getValues() {
		return myAllowBase ? ourAllValuesPlusBase : ourAllValues;
	}

	public String initialValue() {
		final int value = myOption.getValue();
		if (value == -1) {
			return (String) ourAllValuesPlusBase.get(0);
		}
		final int index = Math.max(0, Math.min(15, (value + 5) / 10 - 5));
		return (String) ourAllValues.get(index);
	}

	public void onAccept(String value) {
		if (ourAllValuesPlusBase.get(0).equals(value)) {
			myOption.setValue(-1);
		} else {
			for (int i = 5; i <= 20; ++i) {
				if (ourAllValues.get(i - 5).equals(value)) {
					myOption.setValue(10 * i);
					break;
				}
			}
		}
	}

}
