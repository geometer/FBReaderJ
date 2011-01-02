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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import org.geometerplus.zlibrary.core.application.ZLApplication;

class ControlButtonPanel implements ZLApplication.ButtonPanel {
	private boolean myVisible;
	protected ControlPanel myControlPanel;

	private static LinkedList<ControlButtonPanel> ourPanels = new LinkedList<ControlButtonPanel>();


	public final void hide() {
		hide(false);
	}

	public void updateStates() {
		if (myControlPanel != null) {
			myControlPanel.updateStates();
		}
	}

	public final void register() {
		ZLApplication.Instance().registerButtonPanel(this);
		ourPanels.add(this);
	}

	public final boolean hasControlPanel() {
		return myControlPanel != null;
	}

	public final void setControlPanel(ControlPanel panel, RelativeLayout root, boolean fillWidth) {
		myControlPanel = panel;
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
			fillWidth ? ViewGroup.LayoutParams.FILL_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT,
			RelativeLayout.LayoutParams.WRAP_CONTENT
		);
		p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		p.addRule(RelativeLayout.CENTER_HORIZONTAL);
		myControlPanel.setVisibility(View.GONE);
		root.addView(myControlPanel, p);
	}

	private final void removeControlPanel() {
		if (myControlPanel != null) {
			ViewGroup root = (ViewGroup) myControlPanel.getParent();
			myControlPanel.hide(false);
			root.removeView(myControlPanel);
			myControlPanel = null;
		}
	}

	public static void removeControlPanels() {
		for (ControlButtonPanel panel: ourPanels) {
			panel.removeControlPanel();
		}
	}

	public static void restoreVisibilities() {
		for (ControlButtonPanel panel: ourPanels) {
			panel.setVisibility(panel.myVisible);
		}
	}

	public static void saveVisibilities() {
		for (ControlButtonPanel panel: ourPanels) {
			panel.myVisible = panel.getVisibility();
		}
	}

	public static void restoreVisibilitiesFrom(List<Boolean> buffer) {
		Iterator<Boolean> it = buffer.iterator();
		for (ControlButtonPanel panel: ourPanels) {
			panel.setVisibility(it.next());
		}
	}

	public static void saveVisibilitiesTo(List<Boolean> buffer) {
		buffer.clear();
		for (ControlButtonPanel panel: ourPanels) {
			buffer.add(panel.getVisibility());
		}
	}

	public static void hideAllPendingNotify() {
		for (ControlButtonPanel panel: ourPanels) {
			if (panel.myControlPanel != null && panel.getVisibility()) {
				panel.myControlPanel.hide(false);
			}
		}
	}


	public final boolean getVisibility() {
		if (myControlPanel != null) {
			return myControlPanel.getVisibility() == View.VISIBLE;
		}
		return false;
	}

	public final void setVisibility(boolean visible) {
		if (visible) {
			show(false);
		} else {
			hide(false);
		}
	}

	private void hideOthers() {
		for (ControlButtonPanel panel: ourPanels) {
			if (panel != this) {
				panel.hide(false);
			}
		}
	}

	public final void show(boolean animate) {
		if (myControlPanel != null && !getVisibility()) {
			myVisible = true;
			hideOthers();
			onShow();
			myControlPanel.show(animate);
		}
	}

	public final void hide(boolean animate) {
		myVisible = false;
		if (myControlPanel != null && getVisibility()) {
			onHide();
			myControlPanel.hide(animate);
		}
	}


	// callback methods
	public void onShow() {}
	public void onHide() {}
}
