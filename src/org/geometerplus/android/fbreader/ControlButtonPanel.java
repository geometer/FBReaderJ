/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.geometerplus.zlibrary.core.application.ZLApplication;

import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

abstract class ControlButtonPanel implements ZLApplication.ButtonPanel {
	public final FBReaderApp Reader;
	public ZLTextWordCursor StartPosition;

	private boolean myVisible;

	protected ControlPanel myControlPanel;

	ControlButtonPanel(FBReaderApp fbReader) {
		Reader = fbReader;
		fbReader.registerButtonPanel(this);
	}

	public final void hide() {
		hide(false);
	}

	public void updateStates() {
	}

	public final boolean hasControlPanel() {
		return myControlPanel != null;
	}

	private final void removeControlPanel() {
		if (myControlPanel != null) {
			ViewGroup root = (ViewGroup)myControlPanel.getParent();
			myControlPanel.hide(false);
			root.removeView(myControlPanel);
			myControlPanel = null;
		}
	}

	public static void removeControlPanels(ZLApplication application) {
		for (ZLApplication.ButtonPanel panel : application.buttonPanels()) {
			((ControlButtonPanel)panel).removeControlPanel();
		}
	}

	public static void restoreVisibilities(ZLApplication application) {
		for (ZLApplication.ButtonPanel panel : application.buttonPanels()) {
			final ControlButtonPanel p = (ControlButtonPanel)panel;
			p.setVisibility(p.myVisible);
		}
	}

	public static void saveVisibilities(ZLApplication application) {
		for (ZLApplication.ButtonPanel panel : application.buttonPanels()) {
			final ControlButtonPanel p = (ControlButtonPanel)panel;
			p.myVisible = p.getVisibility();
		}
	}

	public static void hideAllPendingNotify(ZLApplication application) {
		for (ZLApplication.ButtonPanel panel : application.buttonPanels()) {
			final ControlButtonPanel p = (ControlButtonPanel)panel;
			if (p.myControlPanel != null && p.getVisibility()) {
				p.myControlPanel.hide(false);
			}
		}
	}

	public final boolean getVisibility() {
		if (myControlPanel != null) {
			return myControlPanel.getVisibility() == View.VISIBLE;
		}
		return false;
	}

	private void setVisibility(boolean visible) {
		if (visible) {
			show(false);
		} else {
			hide(false);
		}
	}

	private void hideOthers() {
		for (ZLApplication.ButtonPanel panel : Reader.buttonPanels()) {
			if (panel != this) {
				((ControlButtonPanel)panel).hide(false);
			}
		}
	}

	public final void show(boolean animate) {
		if (myControlPanel != null && !getVisibility()) {
			hideOthers();
			onShow();
			myControlPanel.show(animate);
		}
	}

	public final void initPosition() {
		if (StartPosition == null) {
			StartPosition = new ZLTextWordCursor(Reader.getTextView().getStartCursor());
		}
	}

	public final void storePosition() {
		if (StartPosition != null &&
			!StartPosition.equals(Reader.getTextView().getStartCursor())) {
			Reader.addInvisibleBookmark(StartPosition);
		}
	}

	public final void hide(boolean animate) {
		if (myControlPanel != null && getVisibility()) {
			onHide();
			myControlPanel.hide(animate);
		}
	}

	public abstract void createControlPanel(FBReader activity, RelativeLayout root);

	// callback methods
	public void onShow() {}
	public void onHide() {}
}
