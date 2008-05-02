/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

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
		
		System.out.println("sets size = " + sets.size());
		
		for (int i = 0; i < sets.size(); i++) {
			ZLEncodingSet es = (ZLEncodingSet) sets.get(i);
			final ArrayList/*<ZLEncodingConverterInfo>*/ infos = es.infos();
	//		System.out.println(es.name());
			mySetNames.add(es.name());
			ArrayList/*<String>*/ names = new ArrayList();
			for (int j = 0; j < infos.size(); j++) {
				ZLEncodingConverterInfo eci = (ZLEncodingConverterInfo) infos.get(j);
				if (eci.name().equals(value)) {
					myInitialSetName = es.name();
					myInitialValues.put(myInitialSetName, eci.visibleName());
				}
				names.add(eci.visibleName());
				myValueByName.put(eci.visibleName(), eci.name());
			}
			myValues.put(es.name(), names);
		}
		//TODO:
		if (myInitialSetName.length() == 0 && !mySetNames.isEmpty()) {
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
