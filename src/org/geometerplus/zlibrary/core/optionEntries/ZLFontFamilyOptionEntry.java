package org.geometerplus.zlibrary.core.optionEntries;

import java.util.ArrayList;

import org.geometerplus.zlibrary.core.dialogs.ZLComboOptionEntry;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;

public class ZLFontFamilyOptionEntry extends ZLComboOptionEntry {
	private ZLStringOption myOption;
	private final ZLPaintContext myContext;
	
	public ZLFontFamilyOptionEntry(ZLStringOption option, final ZLPaintContext context) {
		myOption = option;
		myContext = context;
		String value = option.getValue();
		if (value != null && !value.equals("")) {
			option.setValue(myContext.realFontFamilyName(value));
		}	
	}

	public ArrayList getValues() {
		return (ArrayList) myContext.fontFamilies();
	}

	public String initialValue() {
		return myOption.getValue();
	}

	public void onAccept(String value) {
		myOption.setValue(value);
	}

}
