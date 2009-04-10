/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.dialogs.*;
import org.geometerplus.zlibrary.core.language.ZLLanguageList;
import org.geometerplus.zlibrary.core.optionEntries.*;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;
import org.geometerplus.zlibrary.text.view.style.*;

import org.geometerplus.fbreader.encoding.ZLEncodingCollection;
import org.geometerplus.fbreader.encodingOption.*;
import org.geometerplus.fbreader.collection.BookCollection;
import org.geometerplus.fbreader.fbreader.*;
import org.geometerplus.fbreader.formats.PluginCollection;

public class OptionsDialog {
	private ZLOptionsDialog myDialog;
	
	public OptionsDialog(FBReader fbreader) {
		final ZLTextBaseStyle baseStyle = ZLTextStyleCollection.getInstance().baseStyle();
		myDialog = ZLDialogManager.getInstance().createOptionsDialog("OptionsDialog", null, new OptionsApplyRunnable(fbreader), true);

		final ZLDialogContent encodingTab = myDialog.createTab("Language");
		encodingTab.addOption("autoDetect", new ZLSimpleBooleanOptionEntry(PluginCollection.instance().LanguageAutoDetectOption));
		encodingTab.addOption("defaultLanguage", new ZLLanguageOptionEntry(PluginCollection.instance().DefaultLanguageOption, ZLLanguageList.languageCodes()));
//		EncodingEntry encodingEntry = new EncodingEntry(PluginCollection.instance().DefaultEncodingOption);
//		EncodingSetEntry encodingSetEntry = new EncodingSetEntry(encodingEntry);
//		encodingTab.addOption("defaultEncodingSet", encodingSetEntry);
//		encodingTab.addOption("defaultEncoding", encodingEntry);

		new ScrollingOptionsPage(myDialog.createTab("Scrolling"), fbreader);
				
		final ZLDialogContent selectionTab = myDialog.createTab("Selection");
		selectionTab.addOption("enableSelection", FBView.selectionOption());
		
		ZLDialogContent marginTab = myDialog.createTab("Margins");
		marginTab.addOptions(
			"left", new ZLSimpleSpinOptionEntry(FBView.getLeftMarginOption(), 1),
			"right", new ZLSimpleSpinOptionEntry(FBView.getRightMarginOption(), 1)
		);
		marginTab.addOptions(
			"top", new ZLSimpleSpinOptionEntry(FBView.getTopMarginOption(), 1),
			"bottom", new ZLSimpleSpinOptionEntry(FBView.getBottomMarginOption(), 1)
		);
		
		new FormatOptionsPage(myDialog.createTab("Format"));
			
		new StyleOptionsPage(myDialog.createTab("Styles"), ZLibrary.Instance().getPaintContext());
		
		final ZLDialogContent rotationTab = myDialog.createTab("Rotation");
		rotationTab.addOption("direction", new RotationTypeEntry(rotationTab.getResource("direction"), fbreader.RotationAngleOption));
		
		final ZLDialogContent colorsTab = myDialog.createTab("Colors");
		final String colorKey = "colorFor";
		final ZLResource resource = colorsTab.getResource(colorKey);
		final ZLColorOptionBuilder builder = new ZLColorOptionBuilder();
		final String BACKGROUND = resource.getResource("background").getValue();
		builder.addOption(BACKGROUND, baseStyle.BackgroundColorOption);
		//builder.addOption(resource.getResource("selectionBackground").getValue(), baseStyle.SelectionBackgroundColorOption);
		builder.addOption(resource.getResource("text").getValue(), baseStyle.RegularTextColorOption);
		builder.addOption(resource.getResource("internalLink").getValue(), baseStyle.InternalHyperlinkTextColorOption);
		builder.addOption(resource.getResource("externalLink").getValue(), baseStyle.ExternalHyperlinkTextColorOption);
		builder.addOption(resource.getResource("highlighted").getValue(), baseStyle.HighlightedTextColorOption);
		builder.setInitial(BACKGROUND);
		colorsTab.addOption(colorKey, builder.comboEntry());
		colorsTab.addOption("", builder.colorEntry());
		
		new KeyBindingsPage(fbreader, myDialog.createTab("Keys"));
	}
	
	public ZLOptionsDialog getDialog() {
		return myDialog;
	}
	
	private static class OptionsApplyRunnable implements Runnable {
		private final FBReader myFBReader;
		
		public OptionsApplyRunnable(FBReader fbreader) {
			myFBReader = fbreader;
		}
		
		public void run() {
			myFBReader.grabAllKeys(myFBReader.KeyboardControlOption.getValue());
			myFBReader.clearTextCaches();
			myFBReader.refreshWindow();
		}
	}
	
	private static class RotationTypeEntry extends ZLChoiceOptionEntry {
		private final ZLResource myResource;
		private ZLIntegerOption myAngleOption;
		
		public RotationTypeEntry(ZLResource resource, ZLIntegerOption angleOption) {
			myAngleOption = angleOption;
			myResource = resource;
		}
		
		public int choiceNumber() {
			return 5;
		}

		public String getText(int index) {
			final String keyName;
			switch (index) {
				case 1:
					keyName = "counterclockwise";
					break;
				case 2:
					keyName = "180";
					break;
				case 3:
					keyName = "clockwise";
					break;
				case 4:
					keyName = "cycle";
					break;
				default:
					keyName = "disabled";
					break;
			}
			return myResource.getResource(keyName).getValue();
		}

		public int initialCheckedIndex() {
			switch (myAngleOption.getValue()) {
			default:
				return 0;
			case ZLViewWidget.Angle.DEGREES90:
				return 1;
			case ZLViewWidget.Angle.DEGREES180:
				return 2;
			case ZLViewWidget.Angle.DEGREES270:
				return 3;
			case -1:
				return 4;
			}
		}

		public void onAccept(int index) {
			int angle = ZLViewWidget.Angle.DEGREES0;
			switch (index) {
				case 1:
					angle = ZLViewWidget.Angle.DEGREES90;
					break;
				case 2:
					angle = ZLViewWidget.Angle.DEGREES180;
					break;
				case 3:
					angle = ZLViewWidget.Angle.DEGREES270;
					break;
				case 4:
					angle = -1;
					break;
			}
			myAngleOption.setValue(angle);
		}	
	}
	
	private static class StateOptionEntry extends ZLToggleBooleanOptionEntry {
		private boolean myState;
		
		public StateOptionEntry(ZLBooleanOption option) {
			super(option);
			myState = option.getValue();
		}
	
		public void onStateChanged(boolean state) {
			myState = state;
			super.onStateChanged(state);
		}
	}

	private static class SpecialFontSizeEntry extends ZLSimpleSpinOptionEntry {
		private StateOptionEntry myFirst;
		private StateOptionEntry mySecond;
		
		public SpecialFontSizeEntry(ZLIntegerRangeOption option, int step, StateOptionEntry first, StateOptionEntry second) {
			super(option, step);
			myFirst = first;
			mySecond = second;
		}

		public void setVisible(boolean state) {
			super.setVisible(
					(myFirst.isVisible() && myFirst.myState) ||
					(mySecond.isVisible() && mySecond.myState)
			);
		}	
	}
}
