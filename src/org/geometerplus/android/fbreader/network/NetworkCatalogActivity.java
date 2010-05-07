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

package org.geometerplus.android.fbreader.network;

import java.util.*;

import android.app.Activity;
import android.app.ListActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.IBinder;
import android.view.*;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.BaseAdapter;
import android.net.Uri;
import android.content.Intent;
import android.content.DialogInterface;
import android.graphics.Bitmap;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.image.ZLImage;

import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.dialogs.ZLAndroidDialogManager;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;


public class NetworkCatalogActivity extends NetworkBaseActivity {

	public static final String CATALOG_LEVEL_KEY = "org.geometerplus.android.fbreader.network.CatalogLevel";

	private NetworkCatalogTree myTree;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final NetworkView networkView = NetworkView.Instance();
		if (!networkView.isInitialized()) {
			finish();
			return;
		}

		final Intent intent = getIntent();
		final int level = intent.getIntExtra(CATALOG_LEVEL_KEY, -1);
		if (level == -1) {
			throw new RuntimeException("Catalog's Level was not specified!!!");
		}

		myTree = (NetworkCatalogTree) networkView.getOpenedTree(level);
		if (myTree == null) {
			finish();
			return;
		}

		networkView.setOpenedActivity(myTree, this);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		setListAdapter(new CatalogAdapter());
		getListView().invalidateViews();
	}

	@Override
	public void onDestroy() {
		if (myTree != null) {
			NetworkView.Instance().setOpenedActivity(myTree, null);
		}
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
	}


	private final class CatalogAdapter extends BaseAdapter {

		public final int getCount() {
			return myTree.subTrees().size();
		}

		public final NetworkTree getItem(int position) {
			if (position >= 0 && position < myTree.subTrees().size()) {
				return (NetworkTree) myTree.subTrees().get(position);
			}
			return null;
		}

		public final long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, final ViewGroup parent) {
			final NetworkTree tree = getItem(position);
			return setupNetworkTreeItemView(convertView, parent, tree);
		}
	}


	@Override
	public boolean onSearchRequested() {
		return false;
	}

	@Override
	public void onModelChanged() {
		getListView().invalidateViews();
	}
}
