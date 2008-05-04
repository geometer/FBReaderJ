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

package org.geometerplus.zlibrary.core.dialogs;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.options.ZLSimpleOption;

public abstract class ZLDialog {
	protected ZLDialogContent myTab;	

	public ZLResource getResource(final String key) {
		return myTab.getResource(key);
	}

	public abstract void addButton(final String key, Runnable action);

	public void addOptionByName(final String name, ZLOptionEntry entry) {
		myTab.addOptionByName(name, entry);
	}
	
	public void addOption(final String key, ZLOptionEntry entry) {
		myTab.addOption(key, entry);
	}
	
	public void addOption(final String key, ZLSimpleOption option) {
		myTab.addOption(key, option);
	}

	public abstract void run();

	public void acceptValues() {
		myTab.accept();
	}
}
