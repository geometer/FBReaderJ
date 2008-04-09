package org.zlibrary.core.optionEntries;

import java.util.ArrayList;
import java.util.HashMap;

import org.zlibrary.core.options.ZLColorOption;
import org.zlibrary.core.util.*;

import org.zlibrary.core.dialogs.ZLColorOptionEntry;
import org.zlibrary.core.dialogs.ZLComboOptionEntry;
import org.zlibrary.core.dialogs.ZLOptionEntry;

public class ZLColorOptionBuilder {
	private ZLColorOptionsData myData;
	
	public ZLColorOptionBuilder() {
		myData = new ZLColorOptionsData();
		myData.myComboEntry = new ZLColorComboOptionEntry(myData);
		myData.myColorEntry = new ZLMultiColorOptionEntry(myData);
	}

	public void addOption(final String name, ZLColorOption option) {
		myData.myOptionNames.add(name);
		myData.myCurrentColors.put(name, option.getValue());
		myData.myOptions.put(name, option);
	}

	public void setInitial(final String name) {
		myData.myCurrentOptionName = name;
		myData.myPreviousOptionName = name;
	}

	public ZLOptionEntry comboEntry() {
		return myData.myComboEntry;
	}

	public ZLOptionEntry colorEntry() {
		return myData.myColorEntry;
	}
	
	private static class ZLMultiColorOptionEntry extends ZLColorOptionEntry {
		private ZLColorOptionsData myData;
		
		public ZLMultiColorOptionEntry(ZLColorOptionsData data) {
			myData = data;
		}

		public ZLColor getColor() {
			Object color = myData.myCurrentColors.get(myData.myCurrentOptionName);
			return (color != null) ? (ZLColor)color : initialColor();
		}

		public ZLColor initialColor() {
			return ((ZLColorOption)myData.myOptions.get(myData.myCurrentOptionName)).getValue();
		}

		public void onAccept(ZLColor color) {
			onReset(color);
			final ArrayList optionNames = myData.myOptionNames;
			final HashMap options = myData.myOptions;
			final HashMap colors = myData.myCurrentColors;
			final int len = optionNames.size();
			for (int i = 0; i < len; i++) {
				Object name = optionNames.get(i);
				((ZLColorOption)options.get(name)).setValue((ZLColor)colors.get(name));
			}
		}

		public void onReset(ZLColor color) {
			myData.myCurrentColors.put(myData.myPreviousOptionName, color);
		}
	}

	private static class ZLColorComboOptionEntry extends ZLComboOptionEntry {
		private ZLColorOptionsData myData;
		
		public ZLColorComboOptionEntry(ZLColorOptionsData data) {
			myData = data;
		}
		
		public ArrayList getValues() {
			return myData.myOptionNames;
		}

		public String initialValue() {
			return myData.myCurrentOptionName;
		}

		public void onAccept(String value) {}

		public void onReset() {
			myData.myCurrentColors.clear();
		}

		public void onValueSelected(int index) {
			myData.myCurrentOptionName = (String)getValues().get(index);
			myData.myColorEntry.resetView();
			myData.myPreviousOptionName = myData.myCurrentOptionName;
		}
	}

	private static class ZLColorOptionsData {
		private ZLComboOptionEntry myComboEntry;
		private ZLColorOptionEntry myColorEntry;
		private String myCurrentOptionName;
		private String myPreviousOptionName;
		private final ArrayList/*<String>*/ myOptionNames = new ArrayList();
		private final HashMap/*<String,ZLColor>*/ myCurrentColors = new HashMap();
		private final HashMap/*<String,ZLColorOption>*/ myOptions = new HashMap();
	}		
}
