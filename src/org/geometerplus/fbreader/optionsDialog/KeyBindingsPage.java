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

import java.util.ArrayList;

import org.geometerplus.zlibrary.core.application.*;
import org.geometerplus.zlibrary.core.dialogs.*;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.optionEntries.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;

import org.geometerplus.fbreader.fbreader.*;

public class KeyBindingsPage {

	public KeyBindingsPage(FBReader fbreader, ZLDialogContent dialogTab) {
		if (new ZLBooleanOption(ZLOption.EMPTY, ZLOption.PLATFORM_GROUP, "FullKeyboardControlSupported", false).getValue()) {
			dialogTab.addOption("grabSystemKeys", new KeyboardControlEntry(fbreader));
		}
		MultiKeyOptionEntry keyEntry = new MultiKeyOptionEntry(dialogTab.getResource("action"), fbreader);
		OrientationEntry orientationEntry = new OrientationEntry(keyEntry);
		ZLBooleanOptionEntry useSeparateBindingsEntry = new UseSeparateOptionsEntry(fbreader, keyEntry, orientationEntry);
		dialogTab.addOption("separate", useSeparateBindingsEntry);
		dialogTab.addOption("orientation", orientationEntry);
		dialogTab.addOption("action", keyEntry);//?
		ZLOptionEntry exitOnCancelEntry = new ZLSimpleBooleanOptionEntry(fbreader.QuitOnCancelOption);
		keyEntry.setExitOnCancelEntry(exitOnCancelEntry);
		dialogTab.addOption("quitOnCancel", exitOnCancelEntry);
		exitOnCancelEntry.setVisible(false);
		useSeparateBindingsEntry.onStateChanged(useSeparateBindingsEntry.initialState());
		//dialogTab.addOption("keyDelay", new ZLSimpleSpinOptionEntry(fbreader.KeyDelayOption, 50));
	}

	private static class KeyboardControlEntry extends ZLSimpleBooleanOptionEntry {
		private FBReader myFBReader;
		public KeyboardControlEntry(FBReader fbreader) {
			super(fbreader.KeyboardControlOption);
			myFBReader = fbreader;
		}

		public void onStateChanged(boolean state) {
			super.onStateChanged(state);
			myFBReader.grabAllKeys(state);
		}
	}

	private static class SingleKeyOptionEntry extends ZLSimpleKeyOptionEntry {
		private final CodeIndexBimap myBimap;

		public SingleKeyOptionEntry(final CodeIndexBimap bimap, ZLKeyBindings bindings) {
			super(bindings);
			myBimap = bimap;
		}

		public CodeIndexBimap codeIndexBimap() {
			return myBimap;
		}
	}

	private static class MultiKeyOptionEntry extends ZLKeyOptionEntry {
		private final ZLResource myResource;
		private ZLSimpleKeyOptionEntry.CodeIndexBimap myBimap;
		private SingleKeyOptionEntry myEntry0;
		private SingleKeyOptionEntry myEntry90;
		private SingleKeyOptionEntry myEntry180;
		private SingleKeyOptionEntry myEntry270;
		private SingleKeyOptionEntry myCurrentEntry;
		private ZLOptionEntry myExitOnCancelEntry;

		public MultiKeyOptionEntry(final ZLResource resource, FBReader fbreader) {
			super();
			myResource = resource;
			myBimap = new ZLSimpleKeyOptionEntry.CodeIndexBimap();
			myEntry0 = new SingleKeyOptionEntry(myBimap, fbreader.keyBindings(ZLViewWidget.Angle.DEGREES0));
			myEntry90 = new SingleKeyOptionEntry(myBimap, fbreader.keyBindings(ZLViewWidget.Angle.DEGREES90));
			myEntry180 = new SingleKeyOptionEntry(myBimap, fbreader.keyBindings(ZLViewWidget.Angle.DEGREES180));
			myEntry270 = new SingleKeyOptionEntry(myBimap, fbreader.keyBindings(ZLViewWidget.Angle.DEGREES270));
			myCurrentEntry = myEntry0;
			myExitOnCancelEntry = null;

			addAction(ZLApplication.NoAction);

			// switch view
			addAction(ActionCode.SHOW_COLLECTION);
			addAction(ActionCode.SHOW_LAST_BOOKS);
			addAction(ActionCode.OPEN_PREVIOUS_BOOK);
			addAction(ActionCode.SHOW_CONTENTS);

			// navigation
			addAction(ActionCode.SCROLL_TO_HOME);
			addAction(ActionCode.SCROLL_TO_START_OF_TEXT);
			addAction(ActionCode.SCROLL_TO_END_OF_TEXT);
			addAction(ActionCode.GOTO_NEXT_TOC_SECTION);
			addAction(ActionCode.GOTO_PREVIOUS_TOC_SECTION);
			addAction(ActionCode.UNDO);
			addAction(ActionCode.REDO);

			// selection
			//addAction(ActionCode.COPY_SELECTED_TEXT_TO_CLIPBOARD);
			//addAction(ActionCode.OPEN_SELECTED_TEXT_IN_DICTIONARY);
			//addAction(ActionCode.CLEAR_SELECTION);

			// search
			addAction(ActionCode.SEARCH);
			addAction(ActionCode.FIND_PREVIOUS);
			addAction(ActionCode.FIND_NEXT);

			// look
			addAction(ActionCode.INCREASE_FONT);
			addAction(ActionCode.DECREASE_FONT);
			addAction(ActionCode.SHOW_HIDE_POSITION_INDICATOR);
			addAction(ActionCode.TOGGLE_FULLSCREEN);
			addAction(ActionCode.FULLSCREEN_ON);
			addAction(ActionCode.ROTATE_SCREEN);

			// dialogs
			addAction(ActionCode.SHOW_OPTIONS);
			addAction(ActionCode.SHOW_BOOK_INFO);
			addAction(ActionCode.ADD_BOOK);

			// quit
			addAction(ActionCode.CANCEL);
			addAction(ActionCode.QUIT);
		}

		public int actionIndex(String key) {
			return myCurrentEntry.actionIndex(key);
		}

		public void onAccept() {
			myEntry0.onAccept();
			myEntry90.onAccept();
			myEntry180.onAccept();
			myEntry270.onAccept();
		}

		public void onReset() {
			myEntry0.onReset();
			myEntry90.onReset();
			myEntry180.onReset();
			myEntry270.onReset();
		}

		public void onKeySelected(String key) {
			if (myExitOnCancelEntry != null) {
				myExitOnCancelEntry.setVisible(ActionCode.CANCEL.equals(myBimap.codeByIndex(myCurrentEntry.actionIndex(key))));
			}
		}

		public void onValueChanged(String key, int index) {
			myCurrentEntry.onValueChanged(key, index);
			if (myExitOnCancelEntry != null) {
				myExitOnCancelEntry.setVisible(ActionCode.CANCEL.equals(myBimap.codeByIndex(index)));
			}
		}

		public void setOrientation(int angle) {
			switch (angle) {
				case ZLViewWidget.Angle.DEGREES0:
					myCurrentEntry = myEntry0;
					break;
				case ZLViewWidget.Angle.DEGREES90:
					myCurrentEntry = myEntry90;
					break;
				case ZLViewWidget.Angle.DEGREES180:
					myCurrentEntry = myEntry180;
					break;
				case ZLViewWidget.Angle.DEGREES270:
					myCurrentEntry = myEntry270;
					break;
			}
			resetView();
		}

		public void setExitOnCancelEntry(ZLOptionEntry exitOnCancelEntry) {
			myExitOnCancelEntry = exitOnCancelEntry;
		}

		private void addAction(final String actionId) {
			myBimap.insert(actionId);
			addActionName(myResource.getResource(actionId).getValue());
		}		
	}

	private static class OrientationEntry extends ZLComboOptionEntry {
		private MultiKeyOptionEntry myKeyEntry;
		private static final ArrayList VALUES = new ArrayList();

		public OrientationEntry(MultiKeyOptionEntry keyEntry) {
			myKeyEntry = keyEntry;
		}

		public ArrayList getValues() {
			if (VALUES.size() == 0) {
				VALUES.add("0 Degrees");
				VALUES.add("90 Degrees Counterclockwise");
				VALUES.add("180 Degrees");
				VALUES.add("90 Degrees Clockwise");
			}
			return VALUES;
		}

		public String initialValue() {
			return (String) getValues().get(0);
		}

		public void onAccept(String value) {}

		public void onValueSelected(int index) {
			final int angles [] = {ZLViewWidget.Angle.DEGREES0, ZLViewWidget.Angle.DEGREES90,
					ZLViewWidget.Angle.DEGREES180, ZLViewWidget.Angle.DEGREES270};
			myKeyEntry.setOrientation(angles[index]);
		}
	}

	private static class UseSeparateOptionsEntry extends ZLSimpleBooleanOptionEntry {
		private ZLOptionEntry myKeyEntry;
		private	OrientationEntry myOrientationEntry;

		public UseSeparateOptionsEntry(FBReader fbreader, ZLOptionEntry keyEntry, OrientationEntry orientationEntry) {
			super(fbreader.UseSeparateBindingsOption);
			myKeyEntry = keyEntry;
			myOrientationEntry = orientationEntry;
		}

		public void onStateChanged(boolean state) {
			super.onStateChanged(state);
			myOrientationEntry.setVisible(state);
			myKeyEntry.resetView();
		}
	}
}
