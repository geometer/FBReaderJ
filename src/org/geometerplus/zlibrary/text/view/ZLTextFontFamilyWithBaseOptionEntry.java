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

package org.geometerplus.zlibrary.text.view;

import java.util.ArrayList;

import org.geometerplus.zlibrary.core.optionEntries.ZLFontFamilyOptionEntry;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;

public class ZLTextFontFamilyWithBaseOptionEntry extends ZLFontFamilyOptionEntry {
	private static final ArrayList ourAllFamilies = new ArrayList();

	private final ZLResource myResource;
		
	public ZLTextFontFamilyWithBaseOptionEntry(ZLStringOption option, ZLPaintContext context, ZLResource resource) {
		super(option, context);
		myResource = resource;
	}

	public ArrayList getValues() {
		if (ourAllFamilies.size() == 0) {
			final ArrayList families = super.getValues();
			ourAllFamilies.add(myResource.getResource("unchanged").getValue());
			ourAllFamilies.addAll(families);
		}
		return ourAllFamilies;
	}

	public String initialValue() {
		final String value = super.initialValue();
		return ((value == null) || (value.length() == 0)) ? (String)(getValues().get(0)) : value;
	}

	public void onAccept(String value) {
		super.onAccept((value.equals(getValues().get(0))) ? "" : value);
	}
}
