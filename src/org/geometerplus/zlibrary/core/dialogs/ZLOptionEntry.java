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

package org.geometerplus.zlibrary.core.dialogs;

public abstract class ZLOptionEntry {
	private ZLOptionView myView;
	private	boolean myIsVisible;
	private	boolean myIsActive;

	public ZLOptionEntry() {
		myIsVisible = true;
		myIsActive = true;
	}

	public abstract int getKind();

	public final void setView(ZLOptionView view) {
		myView = view;
	}

	public final void resetView() {
		if (myView != null) {
			myView.reset();
		}
	}

	public final boolean isVisible() {
		return myIsVisible;
	}

	public final boolean isActive() {
		return myIsActive;
	}

	public void setVisible(boolean visible) {
		myIsVisible = visible;
		if (myView != null) {
			myView.setVisible(visible);
		}
	}

	public void setActive(boolean active) {
		myIsActive = active;
		if (myView != null) {
			myView.setActive(active);
		}
	}
}
