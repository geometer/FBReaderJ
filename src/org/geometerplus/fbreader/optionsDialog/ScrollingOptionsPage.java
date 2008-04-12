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

package org.geometerplus.fbreader.optionsDialog;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.fbreader.fbreader.*;
import org.geometerplus.zlibrary.core.dialogs.*;
import org.geometerplus.zlibrary.core.optionEntries.*;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.ZLTextView;

public class ScrollingOptionsPage {
	private ScrollingEntries myLargeScrollingEntries;
	private ScrollingEntries mySmallScrollingEntries;
	private ScrollingEntries myMouseScrollingEntries;
	private ScrollingEntries myTapScrollingEntries;

	private final boolean myIsMousePresented =
		new ZLBooleanOption(ZLOption.EMPTY, ZLOption.PLATFORM_GROUP, "MousePresented", false).getValue();
	private final boolean myHasTouchScreen =
		new ZLBooleanOption(ZLOption.EMPTY, ZLOption.PLATFORM_GROUP, "TouchScreenPresented", true).getValue();
	
	public ScrollingOptionsPage(ZLDialogContent dialogTab, FBReader fbreader) {
		final String optionsForKey = "optionsFor";
		ZLComboOptionEntry mainEntry = new ScrollingTypeEntry(dialogTab.getResource(optionsForKey), fbreader);
		dialogTab.addOption(optionsForKey, mainEntry);

		final ZLResource modeResource = dialogTab.getResource("mode");
		ScrollingModeEntry.ourNoOverlappingString = modeResource.getResource("noOverlapping").getValue();
		ScrollingModeEntry.ourKeepLinesString = modeResource.getResource("keepLines").getValue();
		ScrollingModeEntry.ourScrollLinesString = modeResource.getResource("scrollLines").getValue();
		ScrollingModeEntry.ourScrollPercentageString = modeResource.getResource("scrollPercentage").getValue();
		ScrollingModeEntry.ourDisableString = modeResource.getResource("disable").getValue();

		myLargeScrollingEntries = new ScrollingEntries(fbreader, fbreader.LargeScrollingOptions);
		mySmallScrollingEntries = new ScrollingEntries(fbreader, fbreader.SmallScrollingOptions);

		if (myIsMousePresented) {
			myMouseScrollingEntries = new ScrollingEntries(fbreader, fbreader.MouseScrollingOptions);
		}
		if (myHasTouchScreen) {
			myTapScrollingEntries = new ScrollingEntries(fbreader, fbreader.FingerTapScrollingOptions);
		}

		mainEntry.onStringValueSelected(mainEntry.initialValue());

		myLargeScrollingEntries.connect(dialogTab);
		mySmallScrollingEntries.connect(dialogTab);
		if (myIsMousePresented) {
			myMouseScrollingEntries.connect(dialogTab);
		}
		if (myHasTouchScreen) {
			myTapScrollingEntries.connect(dialogTab);
		}
	}

	
	private static class ScrollingEntries {
		private ZLBooleanOptionEntry myFingerOnlyEntry;
		private final ZLSpinOptionEntry myDelayEntry;
		private final ZLComboOptionEntry myModeEntry;
		private final ZLSpinOptionEntry myLinesToKeepEntry;
		private final ZLSpinOptionEntry myLinesToScrollEntry;
		private final ZLSpinOptionEntry myPercentToScrollEntry;

		public ScrollingEntries(FBReader fbreader, ScrollingOptions options) {
			final boolean isTapOption = fbreader.FingerTapScrollingOptions.equals(options);
			final boolean isFingerTapDetectionSupported = 
				new ZLBooleanOption(ZLOption.EMPTY, ZLOption.PLATFORM_GROUP, "FingerTapDetectable", false).getValue();
			if (isTapOption && isFingerTapDetectionSupported) {
				myFingerOnlyEntry = new ZLSimpleBooleanOptionEntry(fbreader.TapScrollingOnFingerOnlyOption);
			}
			
			myDelayEntry = new ZLSimpleSpinOptionEntry(options.DelayOption, 50);
			myModeEntry = new ScrollingModeEntry(fbreader, this, options.ModeOption, isTapOption);
			myLinesToKeepEntry = new ZLSimpleSpinOptionEntry(options.LinesToKeepOption, 1);
			myLinesToScrollEntry = new ZLSimpleSpinOptionEntry(options.LinesToScrollOption, 1);
			myPercentToScrollEntry = new ZLSimpleSpinOptionEntry(options.PercentToScrollOption, 5);
			myModeEntry.onStringValueSelected(myModeEntry.initialValue());
		}
		
		void connect(ZLDialogContent dialogTab) {
			//dialogTab.addOption("delay", myDelayEntry);
			dialogTab.addOption("mode", myModeEntry);
			dialogTab.addOption("linesToKeep", myLinesToKeepEntry);
			dialogTab.addOption("linesToScroll", myLinesToScrollEntry);
			dialogTab.addOption("percentToScroll", myPercentToScrollEntry);
			if (myFingerOnlyEntry != null) {
				dialogTab.addOption("fingerOnly", myFingerOnlyEntry);
			}
		}
		
		void show(boolean visible) {
			if (myDelayEntry != null) {
				if (myFingerOnlyEntry != null) {
					myFingerOnlyEntry.setVisible(visible);
				}
				myDelayEntry.setVisible(visible);
				myModeEntry.setVisible(visible);
				if (visible) {
					((ScrollingModeEntry)myModeEntry).onMadeVisible();
				} else {
					myLinesToKeepEntry.setVisible(false);
					myLinesToScrollEntry.setVisible(false);
					myPercentToScrollEntry.setVisible(false);
				}
			}
		}
	}

	
	private class ScrollingTypeEntry extends ZLComboOptionEntry {
		private final String myLargeScrollingString;
		private final String mySmallScrollingString;
		private final String myMouseScrollingString;
		private final String myTapScrollingString;
		
		private final ZLResource myResource;
		private final FBReader myFBReader;
		private final ArrayList myValues = new ArrayList();
		
		public ScrollingTypeEntry(final ZLResource resource, FBReader fbreader) {
			myResource = resource;
			myFBReader = fbreader;
			
			myLargeScrollingString = resource.getResource("large").getValue();
			mySmallScrollingString = resource.getResource("small").getValue();
			myMouseScrollingString = resource.getResource("mouse").getValue();
			myTapScrollingString = resource.getResource("tap").getValue();

			myValues.add(myLargeScrollingString);
			myValues.add(mySmallScrollingString);

			if (myIsMousePresented) {
				myValues.add(myMouseScrollingString);
			}
			if (myHasTouchScreen) {
				myValues.add(myTapScrollingString);
			}
		}
		
		public void onValueSelected(int index) {
			final String selectedValue = (String) getValues().get(index);
			myLargeScrollingEntries.show(myLargeScrollingString.equals(selectedValue));
			mySmallScrollingEntries.show(mySmallScrollingString.equals(selectedValue));

			if (myIsMousePresented) {
				myMouseScrollingEntries.show(myMouseScrollingString.equals(selectedValue));
			}
			if (myHasTouchScreen) {
				myTapScrollingEntries.show(myTapScrollingString.equals(selectedValue));
			}
		}

		public ArrayList getValues() {
			return myValues;
		}

		public String initialValue() {
			return myLargeScrollingString;
		}

		public void onAccept(String value) {}
	}

	
	private static class ScrollingModeEntry extends ZLComboOptionEntry {
		public static String ourNoOverlappingString = "";
		public static String ourKeepLinesString = "";
		public static String ourScrollLinesString = "";
		public static String ourScrollPercentageString = "";
		public static String ourDisableString = "";
		
		private FBReader myFBReader;
		private ScrollingOptionsPage.ScrollingEntries myEntries;
		private ZLIntegerOption myOption;
		private final ArrayList/*<String>*/ myValues = new ArrayList();
		private int myCurrentIndex;
		private boolean myIsTapOption;

		private static String nameByCode(int code) {
			switch (code) {
			case ZLTextView.ScrollingMode.KEEP_LINES:
				return ourKeepLinesString;
			case ZLTextView.ScrollingMode.SCROLL_LINES:
				return ourScrollLinesString;
			case ZLTextView.ScrollingMode.SCROLL_PERCENTAGE:
				return ourScrollPercentageString;
			default:
				return ourNoOverlappingString;
			}
		}
		
		private static int codeByName(final String name) {
			if (ourKeepLinesString.equals(name)) {
				return ZLTextView.ScrollingMode.KEEP_LINES;
			}
			if (ourScrollLinesString.equals(name)) {
				return ZLTextView.ScrollingMode.SCROLL_LINES;
			}
			if (ourScrollPercentageString.equals(name)) {
				return ZLTextView.ScrollingMode.SCROLL_PERCENTAGE;
			}
			return ZLTextView.ScrollingMode.NO_OVERLAPPING;
		}
			
		public ScrollingModeEntry(FBReader fbreader, ScrollingOptionsPage.ScrollingEntries entries, ZLIntegerOption option, boolean isTapOption) {
			myEntries = entries;
			myFBReader = fbreader;
			myOption = option;
			myValues.add(ourNoOverlappingString);
			myValues.add(ourKeepLinesString);
			myValues.add(ourScrollLinesString);
			myValues.add(ourScrollPercentageString);
			myIsTapOption = isTapOption;
			if (isTapOption) {
				myValues.add(ourDisableString);
			}
		}

		
		public void onValueSelected(int index) {
			myCurrentIndex = index;
			final String selectedValue = (String) getValues().get(index);
			if (myEntries.myFingerOnlyEntry != null) {
				myEntries.myFingerOnlyEntry.setVisible(!ourDisableString.equals(selectedValue));
			}
			myEntries.myDelayEntry.setVisible(!ourDisableString.equals(selectedValue));
			myEntries.myLinesToKeepEntry.setVisible(ourKeepLinesString.equals(selectedValue));
			myEntries.myLinesToScrollEntry.setVisible(ourScrollLinesString.equals(selectedValue));
			myEntries.myPercentToScrollEntry.setVisible(ourScrollPercentageString.equals(selectedValue));
		}
			
		public void onMadeVisible() {
			onValueSelected(myCurrentIndex);
		}

		public ArrayList getValues() {
			return myValues;
		}

		public String initialValue() {
			if (myIsTapOption && !myFBReader.EnableTapScrollingOption.getValue()) {
				return ourDisableString;
			}
			return nameByCode(myOption.getValue());
		}

		public void onAccept(String value) {
			if (myIsTapOption) {
				if (ourDisableString.equals(value)) {
					myFBReader.EnableTapScrollingOption.setValue(false);
				} else {
					myFBReader.EnableTapScrollingOption.setValue(true);
					myOption.setValue(codeByName(value));
				}
			} else {
				myOption.setValue(codeByName(value));
			}
		}
	}
}
