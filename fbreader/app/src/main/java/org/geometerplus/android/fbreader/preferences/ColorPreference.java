/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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
import android.preference.Preference;
import android.view.View;
import android.widget.TextView;

import yuku.ambilwarna.AmbilWarnaDialog;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLColor;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

public abstract class ColorPreference extends Preference {
	protected ColorPreference(Context context) {
		super(context);
		setWidgetLayoutResource(R.layout.color_preference);
	}

	public abstract String getTitle();
	protected abstract ZLColor getSavedColor();
	protected abstract void saveColor(ZLColor color);

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		((TextView)view.findViewById(R.id.color_preference_title)).setText(getTitle());
		view.findViewById(R.id.color_preference_widget).setBackgroundColor(
			ZLAndroidColorUtil.rgb(getSavedColor())
		);
	}

	@Override
	protected void onClick() {
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		new AmbilWarnaDialog(
			getContext(),
			ZLAndroidColorUtil.rgb(getSavedColor()),
			new AmbilWarnaDialog.OnAmbilWarnaListener() {
				@Override
				public void onOk(AmbilWarnaDialog dialog, int color) {
					if (!callChangeListener(color)) {
						return;
					}
					saveColor(new ZLColor(color));
					notifyChanged();
				}

				@Override
				public void onCancel(AmbilWarnaDialog dialog) {
				}
			},
			buttonResource.getResource("ok").getValue(),
			buttonResource.getResource("cancel").getValue()
		).show();
	}
}
