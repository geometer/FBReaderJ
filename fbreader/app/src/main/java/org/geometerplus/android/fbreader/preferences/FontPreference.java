/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader.preferences;

import java.util.ArrayList;

import android.content.Context;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;

class FontPreference extends ZLStringListPreference implements ReloadablePreference {
	private final ZLStringOption myOption;
	private final boolean myIncludeDummyValue;

	private static String UNCHANGED = "inherit";

	FontPreference(Context context, ZLResource resource, ZLStringOption option, boolean includeDummyValue) {
		super(context, resource);

		myOption = option;
		myIncludeDummyValue = includeDummyValue;

		reload();
	}

	public void reload() {
		final ArrayList<String> fonts = new ArrayList<String>();
		AndroidFontUtil.fillFamiliesList(fonts);
		if (myIncludeDummyValue) {
			fonts.add(0, UNCHANGED);
		}
		setList((String[])fonts.toArray(new String[fonts.size()]));

		final String optionValue = myOption.getValue();
		final String initialValue = optionValue.length() > 0 ?
			AndroidFontUtil.realFontFamilyName(optionValue) : UNCHANGED;
		for (String fontName : fonts) {
			if (initialValue.equals(fontName)) {
				setInitialValue(fontName);
				return;
			}
		}
		for (String fontName : fonts) {
			if (initialValue.equals(AndroidFontUtil.realFontFamilyName(fontName))) {
				setInitialValue(fontName);
				return;
			}
		}
	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		final String value = getValue();
		myOption.setValue(UNCHANGED.equals(value) ? "" : value);
	}
}
