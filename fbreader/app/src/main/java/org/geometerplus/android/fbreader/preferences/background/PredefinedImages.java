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

package org.geometerplus.android.fbreader.preferences.background;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.fbreader.WallpapersUtil;

public class PredefinedImages extends ListActivity implements AdapterView.OnItemClickListener {
	private final ZLResource myResource = ZLResource.resource("Preferences").getResource("colors").getResource("background");

	@Override
	protected void onStart() {
		super.onStart();
		setTitle(myResource.getValue());
		final ArrayAdapter<ZLFile> adapter = new ArrayAdapter<ZLFile>(
			this, R.layout.background_predefined_item, R.id.background_predefined_item_title
		) {
			public View getView(int position, View convertView, final ViewGroup parent) {
				final View view = super.getView(position, convertView, parent);

				final TextView titleView =
					(TextView)view.findViewById(R.id.background_predefined_item_title);
				final String name = getItem(position).getShortName();
				final String key = name.substring(0, name.indexOf("."));
				titleView.setText(myResource.getResource(key).getValue());

				final View previewWidget =
					view.findViewById(R.id.background_predefined_item_preview);
				try {
					previewWidget.setBackgroundDrawable(
						new BitmapDrawable(getResources(), getItem(position).getInputStream())
					);
				} catch (Throwable t) {
				}

				return view;
			}
		};
		for (ZLFile file : WallpapersUtil.predefinedWallpaperFiles()) {
			adapter.add(file);
		}
		setListAdapter(adapter);
		getListView().setOnItemClickListener(this);
	}

	public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		setResult(RESULT_OK, new Intent().putExtra(
			BackgroundPreference.VALUE_KEY,
			((ZLFile)getListAdapter().getItem(position)).getPath()
		));
		finish();
	}
}
