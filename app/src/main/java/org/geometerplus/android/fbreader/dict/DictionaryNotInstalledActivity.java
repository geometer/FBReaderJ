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

package org.geometerplus.android.fbreader.dict;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.android.util.UIMessageUtil;
import org.geometerplus.android.util.PackageUtil;

public class DictionaryNotInstalledActivity extends ListActivity {
	static final String DICTIONARY_NAME_KEY = "fbreader.dictionary.name";
	static final String PACKAGE_NAME_KEY = "fbreader.package.name";

	private ZLResource myResource;
	private String myDictionaryName;
	private String myPackageName;

	@Override
	protected void onCreate(Bundle saved) {
		super.onCreate(saved);
		myResource = ZLResource.resource("dialog").getResource("missingDictionary");
		myDictionaryName = getIntent().getStringExtra(DICTIONARY_NAME_KEY);
		myPackageName = getIntent().getStringExtra(PACKAGE_NAME_KEY);
		setTitle(myResource.getValue().replaceAll("%s", myDictionaryName));
		final Adapter adapter = new Adapter();
		setListAdapter(adapter);
		getListView().setOnItemClickListener(adapter);
	}

	private final class Adapter extends BaseAdapter implements AdapterView.OnItemClickListener {
		private final String[] myItems = new String[] {
			"install",
			"configure",
			"skip"
		};

		public int getCount() {
			return myItems.length;
		}

		public String getItem(int position) {
			return myItems[position];
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, final ViewGroup parent) {
			final View view = convertView != null
				? convertView
				: LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
			final TextView titleView = (TextView)view.findViewById(R.id.menu_item_title);
			titleView.setText(
				myResource.getResource(myItems[position]).getValue().replaceAll("%s", myDictionaryName)
			);
			return view;
		}

		public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			switch (position) {
				case 0: // install
					installDictionary();
					break;
				case 1: // configure
					startActivity(new Intent(
						Intent.ACTION_VIEW, Uri.parse("fbreader-action:preferences#dictionary")
					));
					break;
				case 2: // skip
					break;
			}
			finish();
		}
	}

	private void installDictionary() {
		if (!PackageUtil.installFromMarket(this, myPackageName)) {
			UIMessageUtil.showErrorMessage(this, "cannotRunAndroidMarket", myDictionaryName);
		}
	}
}
