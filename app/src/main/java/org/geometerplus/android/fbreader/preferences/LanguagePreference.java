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

import java.util.*;

import android.content.Context;

import org.geometerplus.zlibrary.core.language.Language;
import org.geometerplus.zlibrary.core.resources.ZLResource;

abstract class LanguagePreference extends ZLStringListPreference {
	LanguagePreference(
		Context context, ZLResource resource, List<Language> languages
	) {
		super(context, resource);

		final int size = languages.size();
		String[] codes = new String[size];
		String[] names = new String[size];
		int index = 0;
		for (Language l : languages) {
			codes[index] = l.Code;
			names[index] = l.Name;
			++index;
		}
		setLists(codes, names);
		init();
	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		if (result) {
			setLanguage(getValue());
		}
	}

	protected abstract void init();
	protected abstract void setLanguage(String code);
}
