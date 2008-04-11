package org.geometerplus.fbreader.fbreader;

import java.util.ArrayList;

import org.geometerplus.fbreader.option.FBOptions;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.dialogs.ZLComboOptionEntry;

class SearchPatternEntry extends ZLComboOptionEntry {
	SearchAction myAction;
	ArrayList /*String*/ myValues = new ArrayList();
	
	public SearchPatternEntry(SearchAction action) {
		super(true);
		myAction = action;
	}
	
	public void onAccept(final String value) {
		String v = value;
//		ZLStringUtil.stripWhiteSpaces(v);
		if (v != "" && v != (String) getValues().get(0)) {
			myAction.SearchPatternOption.setValue(v);
			int index = 1;
			for (int i = 0; (index < 6) && (i < myValues.size()); i++) {
				if (!myValues.get(i).equals(v)) {
					(new ZLStringOption(FBOptions.SEARCH_CATEGORY, SearchAction.SEARCH, SearchAction.PATTERN + index, "")).
						setValue((String) myValues.get(i));
					index++;
				}
			}
		}
	}

	public ArrayList getValues() {
		if (myValues.isEmpty()) {
			myValues.add(myAction.SearchPatternOption.getValue());	
			for (int i = 1; i < 6; i++) {
				String value = (new ZLStringOption(FBOptions.SEARCH_CATEGORY, 
					SearchAction.SEARCH, SearchAction.PATTERN + i, "")).getValue();
				if (value != "") {
					myValues.add(value);
				}
			}
		}
		return myValues;
	}

	public String initialValue() {
		return (String) getValues().get(0);
	}
}
