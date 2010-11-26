/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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
import android.preference.DialogPreference;
import android.view.View;
import android.widget.ImageView;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;

class ZLColorPreference extends DialogPreference implements ZLPreference {
	ZLColorPreference(Context context, ZLResource resource, String resourceKey) {
		super(context, null);
		setWidgetLayoutResource(R.layout.color_preference_widget);
		setTitle(resource.getResource(resourceKey).getValue());
	}

	@Override
	protected void onBindView(View view) {
		final ImageView colorView = (ImageView)view.findViewById(R.id.color_preference_color);
		colorView.setImageResource(R.drawable.fbreader);
		super.onBindView(view);
	}

	public void onAccept() {
	}
}
