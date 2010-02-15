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

public abstract class ZLOptionView {
	protected final String myName;
	protected final ZLOptionEntry myOption;
	private boolean myInitialized;

	protected ZLOptionView(String name, ZLOptionEntry option) {
		myName = name;
		myOption = option;
		myInitialized = false;
		myOption.setView(this);
	}

	protected abstract void reset();

	public final void setActive(boolean active) {
		if (myInitialized) {
			_setActive(active);
		}
	}

	protected abstract void _setActive(boolean active);

	public final void setVisible(boolean visible) {
		if (visible) {
			if (!myInitialized) {
				createItem();
				myInitialized = true;
			}
			setActive(myOption.isActive());
			show();
		} else {
			if (myInitialized) {
				hide();
			}
		}
	}

	protected abstract void hide();
	protected abstract void show();

	public final void onAccept() {
		if (myInitialized) {
			_onAccept();
		}
	}

	protected abstract void _onAccept();

	protected abstract void createItem();
}
