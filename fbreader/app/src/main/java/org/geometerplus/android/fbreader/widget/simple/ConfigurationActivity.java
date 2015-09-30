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

package org.geometerplus.android.fbreader.widget.simple;

import java.util.*;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

public class ConfigurationActivity extends Activity {
	private int myWidgetId;
	private SharedPreferences myPrefs;

	private Button myOkButton;
	private View[] myIconContainers;
	private Spinner myActionsCombo;

	private final View.OnClickListener myIconCheckboxListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (findCheckbox(view).isChecked()) {
				return;
			}
			final int icon = (Integer)view.getTag();
			myPrefs.edit().putInt(Provider.Key.ICON, icon).apply();
			for (View c : myIconContainers) {
				findCheckbox(c).setChecked(c == view);
			}
			myActionsCombo.setSelection(Provider.Action.ALL.indexOf(Provider.defaultAction(icon)));
			updateOkButton();
		}

		private CheckBox findCheckbox(View v) {
			return (CheckBox)v.findViewById(R.id.icon_checkbox_checkbox);
		}
	};

	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.widget_simple_config);

		final ZLResource widgetResource = ZLResource.resource("widget").getResource("simple");

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
		myPrefs = Provider.getSharedPreferences(this, myWidgetId);
		myPrefs.edit().clear().apply();

		setResult(RESULT_CANCELED);

		final View buttons = findViewById(R.id.widget_simple_config_buttons);
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");

		myOkButton = (Button)buttons.findViewById(R.id.ok_button);
		myOkButton.setText(buttonResource.getResource("ok").getValue());
		myOkButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Provider.setupViews(
					AppWidgetManager.getInstance(ConfigurationActivity.this),
					ConfigurationActivity.this,
					myWidgetId
				);
				setResult(
					RESULT_OK,
					new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, myWidgetId)
				);
				finish();
			}
		});
		myOkButton.setEnabled(false);

		final Button cancelButton = (Button)buttons.findViewById(R.id.cancel_button);
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				myPrefs.edit().clear().apply();
				finish();
			}
		});

		final TextView iconLabel = (TextView)findViewById(R.id.widget_simple_config_icon_label);
		iconLabel.setText(widgetResource.getResource("selectIcon").getValue());
		myIconContainers = new View[] {
			iconContainer(R.id.widget_simple_config_fbreader, Provider.Icon.FBREADER),
			iconContainer(R.id.widget_simple_config_classic, Provider.Icon.CLASSIC),
			iconContainer(R.id.widget_simple_config_library, Provider.Icon.LIBRARY),
			iconContainer(R.id.widget_simple_config_library_old, Provider.Icon.LIBRARY_OLD)
		};

		final List<String> actionCodes = Provider.Action.ALL;
		final List<String> actionNames = new ArrayList<String>(actionCodes.size());
		for (String code : actionCodes) {
			actionNames.add(widgetResource.getResource(code).getValue());
		}
		final TextView actionLabel = (TextView)findViewById(R.id.widget_simple_config_action_label);
		actionLabel.setText(widgetResource.getResource("selectAction").getValue());
		myActionsCombo = (Spinner)findViewById(R.id.widget_simple_config_actions);
		myActionsCombo.setAdapter(new ArrayAdapter<String>(
			this, android.R.layout.select_dialog_item, actionNames
		));
		myActionsCombo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				myPrefs.edit().putString(
					Provider.Key.ACTION, actionCodes.get(position)
				).apply();
			}
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	private void updateOkButton() {
		final Set<String> keys = myPrefs.getAll().keySet();
		myOkButton.setEnabled(
			keys.contains(Provider.Key.ICON) &&
			keys.contains(Provider.Key.ACTION)
		);
	}

	private View iconContainer(final int viewId, final int icon) {
		final View container = findViewById(viewId);
		container.setTag(icon);
		((ImageView)container.findViewById(R.id.icon_checkbox_icon))
			.setImageResource(Provider.iconId(icon));
		container.setOnClickListener(myIconCheckboxListener);
		return container;
	}
}
