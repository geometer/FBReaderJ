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

import android.content.Context;

import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

class FontStylePreference extends ZLStringListPreference {
	private final ZLBooleanOption myBoldOption;
	private final ZLBooleanOption myItalicOption;
	private final String[] myValues = { "regular", "bold", "italic", "boldItalic" };

	FontStylePreference(Context context, ZLResource resource, ZLBooleanOption boldOption, ZLBooleanOption italicOption) {
		super(context, resource);

		myBoldOption = boldOption;
		myItalicOption = italicOption;
		setList(myValues);

		final int intValue =
			(boldOption.getValue() ? 1 : 0) |
			(italicOption.getValue() ? 2 : 0);
		setInitialValue(myValues[intValue]);
	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		final int intValue = findIndexOfValue(getValue());
		myBoldOption.setValue((intValue & 0x1) == 0x1);
		myItalicOption.setValue((intValue & 0x2) == 0x2);
	}
}
