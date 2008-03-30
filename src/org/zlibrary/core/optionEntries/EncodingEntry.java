package org.zlibrary.core.optionEntries;

import java.util.ArrayList;
import java.util.HashMap;

import org.zlibrary.core.dialogs.ZLComboOptionEntry;
import org.zlibrary.core.options.ZLStringOption;

public class EncodingEntry /*extends ZLComboOptionEntry*/ {
	/*public EncodingEntry(ZLStringOption encodingOption) {
		String value = myEncodingOption.getValue();
		if (value == AUTO) {
			myInitialSetName = value;
			myInitialValues.put(value, value);
			setActive(false);
			return;
		}

		ArrayList sets = ZLEncodingCollection.instance().sets();
		for (std::vector<shared_ptr<ZLEncodingSet> >::const_iterator it = sets.begin(); it != sets.end(); ++it) {
			const std::vector<ZLEncodingConverterInfoPtr> &infos = (*it)->infos();
			mySetNames.push_back((*it)->name());
			std::vector<std::string> &names = myValues[(*it)->name()];
			for (std::vector<ZLEncodingConverterInfoPtr>::const_iterator jt = infos.begin(); jt != infos.end(); ++jt) {
				if ((*jt)->name() == value) {
					myInitialSetName = (*it)->name();
					myInitialValues[myInitialSetName] = (*jt)->visibleName();
				}
				names.push_back((*jt)->visibleName());
				myValueByName[(*jt)->visibleName()] = (*jt)->name();
			}
		}

		if (myInitialSetName.empty()) {
			myInitialSetName = mySetNames[0];
		}
	}

	public	String initialValue() {
		
	}
	public	ArrayList values();
	public	void onAccept(String value);
	public	void onValueSelected(int index);

	private ArrayList mySetNames;
	private	HashMap/*<String,ArrayList<String> > myValues;
	private HashMap<String,String> myInitialValues;
	private	HashMap<String,String> myValueByName;
	private	ZLStringOption myEncodingOption;
	private	String myInitialSetName;*/
}
