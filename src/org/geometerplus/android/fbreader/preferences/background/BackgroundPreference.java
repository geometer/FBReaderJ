/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.preferences.background;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.preference.Preference;
import android.view.View;
import android.widget.TextView;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

import org.geometerplus.fbreader.fbreader.options.ColorProfile;

public class BackgroundPreference extends Preference {
	private final ZLResource myResource;
	private final ColorProfile myProfile;

	public BackgroundPreference(Context context, ColorProfile profile, ZLResource resource) {
		super(context);
		setWidgetLayoutResource(R.layout.background_preference);

		myResource = resource;
		myProfile = profile;
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		final TextView titleView =
			(TextView)view.findViewById(R.id.background_preference_title);
		titleView.setText(myResource.getValue());

		final TextView summaryView =
			(TextView)view.findViewById(R.id.background_preference_summary);
		final View previewWidget = view.findViewById(R.id.background_preference_widget);
		final String value = myProfile.WallpaperOption.getValue();
		if (value.length() == 0) {
			summaryView.setText(myResource.getResource("solidColor").getValue());
			previewWidget.setBackgroundColor(
				ZLAndroidColorUtil.rgb(myProfile.BackgroundOption.getValue())
			);
		} else {
			if (value.startsWith("/")) {
				summaryView.setText(value.substring(value.lastIndexOf("/") + 1));
			} else {
				final String key =
					value.substring(value.lastIndexOf("/") + 1, value.lastIndexOf("."));
				summaryView.setText(myResource.getResource(key).getValue());
			}
			try {
				previewWidget.setBackgroundDrawable(
					new BitmapDrawable(
						getContext().getResources(),
						ZLFile.createFileByPath(value).getInputStream()
					)
				);
			} catch (Throwable t) {
				// ignore
			}
		}
	}

	@Override
	protected void onClick() {
		((Activity)getContext()).startActivity(new Intent(getContext(), Chooser.class));
	}
}
