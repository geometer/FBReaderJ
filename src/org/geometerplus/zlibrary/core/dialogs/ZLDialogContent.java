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

import java.util.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.optionEntries.ZLSimpleBoolean3OptionEntry;
import org.geometerplus.zlibrary.core.optionEntries.ZLSimpleBooleanOptionEntry;
import org.geometerplus.zlibrary.core.optionEntries.ZLSimpleStringOptionEntry;
import org.geometerplus.zlibrary.core.options.*;

public abstract class ZLDialogContent {
	private final ZLResource myResource;
	protected final ArrayList<ZLOptionView> Views = new ArrayList<ZLOptionView>();

	private static ZLOptionEntry createEntryByOption(ZLSimpleOption option) {
		switch (option.getType()) {
		case ZLSimpleOption.Type.BOOLEAN:
			return new ZLSimpleBooleanOptionEntry((ZLBooleanOption) option);
		case ZLSimpleOption.Type.BOOLEAN3:
			return new ZLSimpleBoolean3OptionEntry((ZLBoolean3Option) option);
		case ZLSimpleOption.Type.STRING:
			return new ZLSimpleStringOptionEntry((ZLStringOption) option);
		default:
			return null;
		}
	}
	
	protected ZLDialogContent(ZLResource resource) {
		myResource = resource;
	}

	public final String getKey() {
		return myResource.Name;
	}
		
	public final String getDisplayName() {
		return myResource.getValue();
	}
	
	public final String getValue(String key) {
		return myResource.getResource(key).getValue();
	}
	
	public final ZLResource getResource(String key) {
		return myResource.getResource(key);
	}

	public abstract void addOptionByName(String name, ZLOptionEntry option);
	
	public final void addOption(String key, ZLOptionEntry option) {
		addOptionByName(myResource.getResource(key).getValue(), option);
	}
	
	public final void addOption(String key, ZLSimpleOption option) {
		addOption(key, createEntryByOption(option));
	}
	
	public abstract void addOptionsByNames(String name0, ZLOptionEntry option0, String name1, ZLOptionEntry option1);
	
	public final void addOptions(String key0, ZLOptionEntry option0, String key1, ZLOptionEntry option1) {
		final ZLResource resource0 = myResource.getResource(key0);
		final ZLResource resource1 = myResource.getResource(key1);
		addOptionsByNames(resource0.getValue(), option0, resource1.getValue(), option1);
	}
	
	public final void addOptions(String key0, ZLSimpleOption option0, String key1, ZLSimpleOption option1) {
		addOptions(key0, createEntryByOption(option0), key1, createEntryByOption(option1));
	}

	protected final void accept() {
		final int size = Views.size();
		for (int i = 0; i < size; i++) {
			Views.get(i).onAccept();
		}
	}

	final void reset() {
		final int size = Views.size();
		for (int i = 0; i < size; i++) {
			Views.get(i).reset();
		}
	}

	protected final void addView(ZLOptionView view) {
		if (view != null) {
			Views.add(view);
		}
	}
}
