package org.fbreader.optionsDialog;

import java.util.ArrayList;
import org.zlibrary.core.util.*;

import org.fbreader.fbreader.FBReader;
import org.fbreader.fbreader.ScrollingOptions;
import org.zlibrary.core.dialogs.ZLBooleanOptionEntry;
import org.zlibrary.core.dialogs.ZLComboOptionEntry;
import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.dialogs.ZLSpinOptionEntry;
import org.zlibrary.core.optionEntries.ZLSimpleBooleanOptionEntry;
import org.zlibrary.core.optionEntries.ZLSimpleSpinOptionEntry;
import org.zlibrary.core.options.ZLBooleanOption;
import org.zlibrary.core.options.ZLIntegerOption;
import org.zlibrary.core.options.ZLOption;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.text.view.ZLTextView;

public class ScrollingOptionsPage {
	private ScrollingEntries myLargeScrollingEntries;
	private ScrollingEntries mySmallScrollingEntries;
	private ScrollingEntries myMouseScrollingEntries;
	private ScrollingEntries myTapScrollingEntries;
	
	public ScrollingOptionsPage(ZLDialogContent dialogTab, FBReader fbreader) {
		final String optionsForKey = "optionsFor";
		ZLComboOptionEntry mainEntry = new ScrollingTypeEntry(dialogTab.getResource(optionsForKey), fbreader, this);
		dialogTab.addOption(optionsForKey, mainEntry);

		final ZLResource modeResource = dialogTab.getResource("mode");
		ScrollingModeEntry.ourNoOverlappingString = modeResource.getResource("noOverlapping").getValue();
		ScrollingModeEntry.ourKeepLinesString = modeResource.getResource("keepLines").getValue();
		ScrollingModeEntry.ourScrollLinesString = modeResource.getResource("scrollLines").getValue();
		ScrollingModeEntry.ourScrollPercentageString = modeResource.getResource("scrollPercentage").getValue();
		ScrollingModeEntry.ourDisableString = modeResource.getResource("disable").getValue();

		myLargeScrollingEntries = new ScrollingEntries(fbreader, fbreader.LargeScrollingOptions);
		mySmallScrollingEntries = new ScrollingEntries(fbreader, fbreader.SmallScrollingOptions);

		final boolean isMousePresented = 
			new ZLBooleanOption(ZLOption.EMPTY, ZLOption.PLATFORM_GROUP, "MousePresented", false).getValue();
		final boolean hasTouchScreen = 
			new ZLBooleanOption(ZLOption.EMPTY, ZLOption.PLATFORM_GROUP, "TouchScreenPresented", false).getValue();

		if (isMousePresented) {
			myMouseScrollingEntries = new ScrollingEntries(fbreader, fbreader.MouseScrollingOptions);
		}
		if (hasTouchScreen) {
			myTapScrollingEntries = new ScrollingEntries(fbreader, fbreader.FingerTapScrollingOptions);
		}

		mainEntry.onStringValueSelected(mainEntry.initialValue());

		myLargeScrollingEntries.connect(dialogTab);
		mySmallScrollingEntries.connect(dialogTab);
		if (isMousePresented) {
			myMouseScrollingEntries.connect(dialogTab);
		}
		if (hasTouchScreen) {
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
			dialogTab.addOption("delay", myDelayEntry);
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

	
	private static class ScrollingTypeEntry extends ZLComboOptionEntry {
		private String myLargeScrollingString;
		private String mySmallScrollingString;
		private String myMouseScrollingString;
		private String myTapScrollingString;
		
		private final ZLResource myResource;
		private FBReader myFBReader;
		private ScrollingOptionsPage myPage;
		private final ArrayList/*<std.string>*/ myValues = new ArrayList();
		
		public ScrollingTypeEntry(final ZLResource resource, FBReader fbreader, ScrollingOptionsPage page) {
			myResource = resource;
			myFBReader = fbreader;
			myPage = page;
			
			myLargeScrollingString = resource.getResource("large").getValue();
			mySmallScrollingString = resource.getResource("small").getValue();
			myMouseScrollingString = resource.getResource("mouse").getValue();
			myTapScrollingString = resource.getResource("tap").getValue();

			myValues.add(myLargeScrollingString);
			myValues.add(mySmallScrollingString);

			final boolean isMousePresented = 
				new ZLBooleanOption(ZLOption.EMPTY, ZLOption.PLATFORM_GROUP, "MousePresented", false).getValue();
			final boolean hasTouchScreen = 
				new ZLBooleanOption(ZLOption.EMPTY, ZLOption.PLATFORM_GROUP, "TouchScreenPresented", false).getValue();

			if (isMousePresented) {
				myValues.add(myMouseScrollingString);
			}
			if (hasTouchScreen) {
				myValues.add(myTapScrollingString);
			}
		}
		
		public void onValueSelected(int index) {
			final String selectedValue = (String) getValues().get(index);
			myPage.myLargeScrollingEntries.show(myLargeScrollingString.equals(selectedValue));
			myPage.mySmallScrollingEntries.show(mySmallScrollingString.equals(selectedValue));

			final boolean isMousePresented = 
				new ZLBooleanOption(ZLOption.EMPTY, ZLOption.PLATFORM_GROUP, "MousePresented", false).getValue();
			final boolean hasTouchScreen = 
				new ZLBooleanOption(ZLOption.EMPTY, ZLOption.PLATFORM_GROUP, "TouchScreenPresented", false).getValue();

			if (isMousePresented) {
				myPage.myMouseScrollingEntries.show(myMouseScrollingString.equals(selectedValue));
			}
			if (hasTouchScreen) {
				myPage.myTapScrollingEntries.show(myTapScrollingString.equals(selectedValue));
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
			
		public ScrollingModeEntry(FBReader fbreader, ScrollingOptionsPage.ScrollingEntries entries,
				ZLIntegerOption option, boolean isTapOption) {
			myEntries = entries;
			myFBReader = fbreader;
			myIsTapOption = isTapOption;
			myOption = option;
			myValues.add(ourNoOverlappingString);
			myValues.add(ourKeepLinesString);
			myValues.add(ourScrollLinesString);
			myValues.add(ourScrollPercentageString);
			if (myIsTapOption) {
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
