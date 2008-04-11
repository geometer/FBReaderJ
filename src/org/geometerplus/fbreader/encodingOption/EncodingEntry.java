package org.geometerplus.fbreader.encodingOption;

import java.util.ArrayList;
import java.util.HashMap;

import org.geometerplus.fbreader.encoding.ZLEncodingCollection;
import org.geometerplus.fbreader.encoding.ZLEncodingConverterInfo;
import org.geometerplus.fbreader.encoding.ZLEncodingSet;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.dialogs.ZLComboOptionEntry;

public class EncodingEntry extends ZLComboOptionEntry {
	private static final String AUTO = "auto";
	private static ArrayList/*<String>*/ AUTO_ENCODING;
	final ArrayList/*<std::string>*/ mySetNames = new ArrayList();
	private final HashMap/*<String, ArrayList<String>>*/ myValues = new HashMap();
	private final HashMap/*<String,String>*/ myInitialValues = new HashMap();
	private final HashMap/*<String,String>*/ myValueByName = new HashMap();
	private ZLStringOption myEncodingOption;
	String myInitialSetName = "";
	
	public EncodingEntry(ZLStringOption encodingOption) {
		myEncodingOption = encodingOption;
		final String value = myEncodingOption.getValue();
		if (AUTO.equals(value)) {
			myInitialSetName = value;
			myInitialValues.put(value, value);
			setActive(false);
			return;
		}

		final ArrayList/*<ZLEncodingSet>*/ sets = ZLEncodingCollection.instance().sets();
		for (int i = 0; i < sets.size(); i++) {
			ZLEncodingSet es = (ZLEncodingSet) sets.get(i);
			final ArrayList/*<ZLEncodingConverterInfo>*/ infos = es.infos();
			mySetNames.add(es.name());
			ArrayList/*<String>*/ names = (ArrayList) myValues.get(es.name());
			for (int j = 0; j < infos.size(); j++) {
				ZLEncodingConverterInfo eci = (ZLEncodingConverterInfo) infos.get(j);
				if (eci.name().equals(value)) {
					myInitialSetName = es.name();
					myInitialValues.put(myInitialSetName, eci.visibleName());
				}
				names.add(eci.visibleName());
				myValueByName.put(eci.visibleName(), eci.name());
			}
		}

		if (myInitialSetName.length() == 0) {
			myInitialSetName = (String) mySetNames.get(0);
		}
	}
	
	public ArrayList getValues() {
		if (AUTO.equals(initialValue())) {
			if (AUTO_ENCODING == null) {
				AUTO_ENCODING = new ArrayList();
				AUTO_ENCODING.add(AUTO);
			}
			return AUTO_ENCODING;
		}
		return (ArrayList) myValues.get(myInitialSetName);
	}

	public String initialValue() {
		if ( myInitialValues.get(myInitialSetName) == null) {
			myInitialValues.put(myInitialSetName, ((ArrayList) myValues.get(myInitialSetName)).get(0));
		}
		return (String) myInitialValues.get(myInitialSetName);
	}

	public void onAccept(String value) {
		if (!AUTO.equals(initialValue())) {
			myEncodingOption.setValue((String) myValueByName.get(value));
		}
	}

	public void onValueSelected(int index) {
		myInitialValues.put(myInitialSetName, getValues().get(index));
	}

}
