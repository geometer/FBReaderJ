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

import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

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

	public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		System.err.println("CLICKED:" + position);
		switch (position) {
			case 0:
				break;
			case 1:
				startActivityForResult(new Intent(this, PredefinedImages.class), 1);
				break;
			case 2:
				FileChooserUtil.runFileChooser(this, 2, myResource.getValue(), "");
				break;
		}
	}
}
