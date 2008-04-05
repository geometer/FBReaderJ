package org.fbreader.encodingOption;

import java.util.ArrayList;

import org.zlibrary.core.dialogs.ZLComboOptionEntry;

public class EncodingSetEntry extends ZLComboOptionEntry {
	private EncodingEntry myEncodingEntry;
	
	public EncodingSetEntry(EncodingEntry encodingEntry) {
		myEncodingEntry = encodingEntry;
	}

	public ArrayList getValues() {
		return myEncodingEntry.mySetNames;
	}

	public String initialValue() {
		return myEncodingEntry.myInitialSetName;
	}

	public void onAccept(String value) {}

	public void onValueSelected(int index) {
		myEncodingEntry.myInitialSetName = (String) getValues().get(index);
		myEncodingEntry.resetView();
	}

}
