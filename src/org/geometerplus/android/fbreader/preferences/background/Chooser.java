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

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import yuku.ambilwarna.AmbilWarnaDialog;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

import org.geometerplus.fbreader.Paths;

import org.geometerplus.android.util.FileChooserUtil;

public class Chooser extends ListActivity implements AdapterView.OnItemClickListener {
	private final ZLResource myResource = ZLResource.resource("Preferences").getResource("colors").getResource("background");

	@Override
	protected void onStart() {
		super.onStart();
		setTitle(myResource.getValue());
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
			this, R.layout.background_chooser_item, R.id.background_chooser_item_title
		);
		final ZLResource chooserResource = myResource.getResource("chooser");
		adapter.add(chooserResource.getResource("solidColor").getValue());
		adapter.add(chooserResource.getResource("predefined").getValue());
		adapter.add(chooserResource.getResource("selectFile").getValue());
		setListAdapter(adapter);
		getListView().setOnItemClickListener(this);
	}

	private final AmbilWarnaDialog.OnAmbilWarnaListener myColorChooserListener =
		new AmbilWarnaDialog.OnAmbilWarnaListener() {
			@Override
			public void onOk(AmbilWarnaDialog dialog, int color) {
				final Intent result = new Intent()
					.putExtra(BackgroundPreference.VALUE_KEY, "")
					.putExtra(BackgroundPreference.COLOR_KEY, color);
				setResult(RESULT_OK, result);
				finish();
			}

			@Override
			public void onCancel(AmbilWarnaDialog dialog) {
			}
		};

	public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (position) {
			case 0:
			{
				final ZLResource buttonResource =
					ZLResource.resource("dialog").getResource("button");
				new AmbilWarnaDialog(
					this,
					getIntent().getIntExtra(BackgroundPreference.COLOR_KEY, 0),
					myColorChooserListener,
					buttonResource.getResource("ok").getValue(),
					buttonResource.getResource("cancel").getValue()
				).show();
				break;
			}
			case 1:
				startActivityForResult(new Intent(this, PredefinedImages.class), 1);
				break;
			case 2:
			{
				final String initialDir;
				final String currentValue =
					getIntent().getStringExtra(BackgroundPreference.VALUE_KEY);
				if (currentValue != null && currentValue.startsWith("/")) {
					initialDir = currentValue.substring(0, currentValue.lastIndexOf("/"));
				} else {
					final List<String> path = Paths.WallpaperPathOption.getValue();
					if (path.size() > 0) {
						initialDir = path.get(0);
					} else {
						initialDir = "";
					}
				}
				FileChooserUtil.runFileChooser(
					this, 2, myResource.getValue(), initialDir, ".+\\.(jpe?g|png)"
				);
				break;
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case 1:
					setResult(RESULT_OK, data);
					finish();
					break;
				case 2:
					final List<String> paths = FileChooserUtil.filePathsFromData(data);
					if (paths.size() == 1) {
						setResult(RESULT_OK, new Intent().putExtra(
							BackgroundPreference.VALUE_KEY, paths.get(0)
						));
						finish();
					}
					break;
			}
		}
	}
}
