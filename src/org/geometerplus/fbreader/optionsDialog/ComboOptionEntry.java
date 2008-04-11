package org.geometerplus.fbreader.optionsDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.geometerplus.zlibrary.core.dialogs.ZLComboOptionEntry;
import org.geometerplus.zlibrary.core.dialogs.ZLOptionEntry;

class ComboOptionEntry extends ZLComboOptionEntry {
	protected final OptionsPage myPage;
	protected final ArrayList /*<std::string>*/ myValues = new ArrayList();
	protected String myInitialValue;
	
	public ComboOptionEntry(final OptionsPage myPage, String myInitialValue) {
		this.myPage = myPage;
		this.myInitialValue = myInitialValue;
	}

	public ArrayList getValues() {
		return myValues;
	}

	public String initialValue() {
		return myInitialValue;
	}

	public void onAccept(String value) {}

	public void onValueSelected(int index) {
		final Object selectedValue = myValues.get(index);
		final LinkedHashMap /*<ZLOptionEntry*,std::string>*/ entries = myPage.getEntries();
/*		for (Iterator it = entries.keySet().iterator(); it.hasNext(); ) {
			ZLOptionEntry entry = (ZLOptionEntry) it.next();
			entry.setVisible(selectedValue != null && selectedValue.equals(entries.get(entry)));
			if (entry.isVisible())
				System.out.println(entry.getKind()+" "+entry.hashCode());
		} 
	*/
		for (Iterator it = entries.entrySet().iterator(); it.hasNext(); ) {
			Entry entry = (Entry) it.next();
			((ZLOptionEntry) entry.getKey()).setVisible(selectedValue != null && selectedValue.equals(entry.getValue()));
		} 
	}
	
	public void addValue(final String value) {
		myValues.add(value);
	}
}
