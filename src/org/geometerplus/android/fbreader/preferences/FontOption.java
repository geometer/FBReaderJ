/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

class FontOption extends ZLStringListPreference {
	private final ZLStringOption myOption;

	FontOption(Context context, ZLResource resource, String resourceKey, ZLStringOption option) {
		super(context, resource, resourceKey);

		myOption = option;
		// TODO: full list of fonts
		setList(new String[] { "Droid Mono", "Droid Sans", "Droid Serif" });

		// TODO: get real font name
		setInitialValue(option.getValue());
	}

	public void onAccept() {
		myOption.setValue(getValue());
	}
}
