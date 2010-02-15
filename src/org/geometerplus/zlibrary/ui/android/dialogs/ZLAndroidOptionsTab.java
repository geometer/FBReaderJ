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

package org.geometerplus.zlibrary.ui.android.dialogs;

import java.util.ArrayList;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.dialogs.*;

class ZLAndroidOptionsTab extends ZLDialogContent {
	static class OptionData {
		final String Name;
		final ZLOptionEntry Entry;

		OptionData(String name, ZLOptionEntry entry) {
			Name = name;
			Entry = entry;
		}
	}
	final ArrayList<OptionData> myEntries = new ArrayList<OptionData>();

	ZLAndroidOptionsTab(ZLResource resource) {
		super(resource);
		// TODO: implement
	}

	public void addOptionByName(String name, ZLOptionEntry option) {
		if (option != null) {
			myEntries.add(new OptionData(name, option));
		}
	}

	public void addOptionsByNames(String name0, ZLOptionEntry option0, String name1, ZLOptionEntry option1) {
		addOptionByName(name0, option0);
		addOptionByName(name1, option1);
	}
}
