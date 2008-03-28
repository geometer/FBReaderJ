package org.zlibrary.core.optionEntries;

import java.util.ArrayList;

import org.zlibrary.core.dialogs.ZLComboOptionEntry;
import org.zlibrary.core.options.ZLStringOption;
import org.zlibrary.core.view.ZLPaintContext;

public class ZLFontFamilyOptionEntry extends ZLComboOptionEntry {
	private ZLStringOption myOption;
	private final ZLPaintContext myContext;
	
	public ZLFontFamilyOptionEntry(ZLStringOption option, final ZLPaintContext context) {
		myOption = option;
		myContext = context;
	/*	String value = option.getValue();
		if (value != null && !value.equals("")) {
			option.setValue(myContext.realFontFamilyName(value));
		}
	*/	
	}

	public ArrayList getValues() {
		// TODO Auto-generated method stub
	//	return myContext.fontFamilies();
		ArrayList list = new ArrayList();
		list.add("font");
		return list;
	}

	public String initialValue() {
		return myOption.getValue();
	}

	public void onAccept(String value) {
		myOption.setValue(value);
	}

}
