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

package org.geometerplus.android.fbreader.widget;

import java.util.Set;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import org.geometerplus.zlibrary.ui.android.R;

public class SimpleWidgetConfigurationActivity extends Activity {
	private int myWidgetId;
	private SharedPreferences myPrefs;

	private Button myOkButton;
	private View[] myIconContainers;

	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.widget_simple_config);

		myWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
		final Bundle extras = getIntent().getExtras();
		if (extras != null) {
			myWidgetId = extras.getInt(
				AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID
			);
		}
		if (myWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
			return;
		}
		myPrefs = SimpleWidgetProvider.getSharedPreferences(this, myWidgetId);
		final SharedPreferences.Editor editor = myPrefs.edit();
		editor.clear();
		editor.apply();

		setResult(RESULT_CANCELED);

		final View buttons = findViewById(R.id.widget_simple_config_buttons);

		myOkButton = (Button)buttons.findViewById(R.id.ok_button);
		myOkButton.setText("Ok");
		myOkButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				setResult(
					RESULT_OK,
					new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, myWidgetId)
				);
				finish();
			}
		});
		myOkButton.setEnabled(false);

		final Button cancelButton = (Button)buttons.findViewById(R.id.cancel_button);
		cancelButton.setText("Cancel");
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				final SharedPreferences.Editor editor = myPrefs.edit();
				editor.clear();
				editor.apply();
				finish();
			}
		});

		myIconContainers = new View[] {
			iconContainer(R.id.widget_simple_config_fbreader, R.drawable.fbreader),
			iconContainer(R.id.widget_simple_config_classic, R.drawable.classic)
		};
		for (View container : myIconContainers) {
			setupIconButton(container);
		}
	}

	private void updateOkButton() {
		final Set<String> keys = myPrefs.getAll().keySet();
		myOkButton.setEnabled(keys.contains("icon"));
	}

	private View iconContainer(final int viewId, final int iconId) {
		final View container = findViewById(viewId);
		container.setTag(iconId);
		((ImageView)container.findViewById(R.id.icon_checkbox_icon)).setImageResource(iconId);
		return container;
	}

	private void setupIconButton(View container) {
		final AppWidgetManager manager = AppWidgetManager.getInstance(this);
		final RemoteViews views =
			new RemoteViews(getPackageName(), R.layout.widget_simple);
		final Integer iconId = (Integer)container.getTag();

		container.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				final SharedPreferences.Editor editor = myPrefs.edit();
				editor.putInt("icon", iconId);
				editor.apply();
				views.setImageViewResource(R.id.widget_simple, iconId);
				manager.updateAppWidget(myWidgetId, views);
				for (View c : myIconContainers) {
					((CheckBox)c.findViewById(R.id.icon_checkbox_checkbox)).setChecked(
						c.getTag() == iconId
					);
				}
				updateOkButton();
			}
		});
	}
}
