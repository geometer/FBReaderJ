package org.zlibrary.text.view;

import java.util.ArrayList;

import org.zlibrary.core.dialogs.ZLComboOptionEntry;
import org.zlibrary.core.options.ZLIntegerOption;
import org.zlibrary.core.resources.ZLResource;

public class ZLTextAlignmentOptionEntry extends ZLComboOptionEntry {
	private static final String KEY_LEFT = "left";
	private static final String KEY_RIGHT = "right";
	private static final String KEY_CENTER = "center";
	private static final String KEY_JUSTIFY = "justify";
	private static final String KEY_UNCHANGED = "unchanged";
	
	private static final ArrayList/*<String>*/ ourValues4 = new ArrayList();
	private static final ArrayList/*<String>*/ ourValues5 = new ArrayList();
	private	final ZLResource myResource;
	private	ZLIntegerOption myOption;
	private	boolean myAllowUndefined;
	
	private ArrayList values4() {
		if (ourValues4.size() == 0) {
			ourValues4.add(myResource.getResource(KEY_LEFT).getValue());
			ourValues4.add(myResource.getResource(KEY_RIGHT).getValue());
			ourValues4.add(myResource.getResource(KEY_CENTER).getValue());
			ourValues4.add(myResource.getResource(KEY_JUSTIFY).getValue());
		}
		return ourValues4;
	}
	
	private ArrayList values5() {
		if (ourValues5.size() == 0) {
			ourValues5.add(myResource.getResource(KEY_UNCHANGED).getValue());
			ourValues5.add(myResource.getResource(KEY_LEFT).getValue());
			ourValues5.add(myResource.getResource(KEY_RIGHT).getValue());
			ourValues5.add(myResource.getResource(KEY_CENTER).getValue());
			ourValues5.add(myResource.getResource(KEY_JUSTIFY).getValue());
		}
		return ourValues5;
	}
		
	public ZLTextAlignmentOptionEntry(ZLIntegerOption option, final ZLResource resource,
			boolean allowUndefined) {
		myAllowUndefined = allowUndefined;
		myOption = option;
		myResource = resource;
	}	
		
	public ArrayList getValues() {
		return myAllowUndefined ? values5() : values4();
	}

	public String initialValue() {
		int value = myOption.getValue();
		if (value >= values5().size()) {
			value = 0;
		}
		return (String) values5().get(value);
	}

	public void onAccept(String value) {
		for (int i = 0; i < values5().size(); ++i) {
			if (values5().get(i).equals(value)) {
				myOption.setValue(i);
				break;
			}
		}
	}

}
