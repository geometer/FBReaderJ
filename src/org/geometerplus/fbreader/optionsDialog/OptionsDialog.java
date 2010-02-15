/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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
import org.geometerplus.zlibrary.core.optionEntries.*;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.style.*;

import org.geometerplus.fbreader.fbreader.*;
import org.geometerplus.fbreader.formats.PluginCollection;

public class OptionsDialog {
	private ZLOptionsDialog myDialog;
	
	public OptionsDialog(FBReader fbreader) {
		myDialog = ZLDialogManager.Instance().createOptionsDialog("OptionsDialog", null, new OptionsApplyRunnable(fbreader), true);

		ZLDialogContent marginTab = myDialog.createTab("Margins");
		marginTab.addOptions(
			"left", new ZLSimpleSpinOptionEntry(fbreader.LeftMarginOption, 1),
			"right", new ZLSimpleSpinOptionEntry(fbreader.RightMarginOption, 1)
		);
		marginTab.addOptions(
			"top", new ZLSimpleSpinOptionEntry(fbreader.TopMarginOption, 1),
			"bottom", new ZLSimpleSpinOptionEntry(fbreader.BottomMarginOption, 1)
		);
		
		new FormatOptionsPage(myDialog.createTab("Format"));
			
		new StyleOptionsPage(myDialog.createTab("Styles"), ZLibrary.Instance().getPaintContext());
		
		final ZLDialogContent colorsTab = myDialog.createTab("Colors");
		final String colorKey = "colorFor";
		final ZLResource resource = colorsTab.getResource(colorKey);
		final ZLColorOptionBuilder builder = new ZLColorOptionBuilder();
		final String BACKGROUND = resource.getResource("background").getValue();
		final ColorProfile profile = fbreader.getColorProfile();
		builder.addOption(BACKGROUND, profile.BackgroundOption);
		builder.addOption(resource.getResource("highlighting").getValue(), profile.HighlightingOption);
		builder.addOption(resource.getResource("text").getValue(), profile.RegularTextOption);
		builder.addOption(resource.getResource("hyperlink").getValue(), profile.HyperlinkTextOption);
		builder.setInitial(BACKGROUND);
		colorsTab.addOption(colorKey, builder.comboEntry());
		colorsTab.addOption("", builder.colorEntry());
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
			myFBReader.clearTextCaches();
			myFBReader.repaintView();
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
