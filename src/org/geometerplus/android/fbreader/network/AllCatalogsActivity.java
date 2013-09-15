/*
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.fbreader.network.*;
import org.geometerplus.android.fbreader.covers.CoverManager;

public class AllCatalogsActivity extends Activity {
	final NetworkLibrary myLibrary = NetworkLibrary.Instance();
	CheckListAdapter myAdapter;
	ArrayList<String> myIds = new ArrayList<String>();
	ArrayList<String> myInactiveIds = new ArrayList<String>();

	public final static String IDS_LIST = "org.geometerplus.android.fbreader.network.IDS_LIST";
	public final static String INACTIVE_IDS_LIST = "org.geometerplus.android.fbreader.network.INACTIVE_IDS_LIST";

	private boolean myIsChanged = false;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.network_library_filter);

		Intent intent = getIntent();
		myIds = intent.getStringArrayListExtra(IDS_LIST);
		myInactiveIds = intent.getStringArrayListExtra(INACTIVE_IDS_LIST);

	}

	@Override
	protected void onStart() {
		super.onStart();

		final ArrayList<CheckItem> idItems = new ArrayList<CheckItem>();

		if (myIds.size() > 0) {
			idItems.add(new SectionItem(getLabelByKey("active")));
			final TreeSet<CatalogItem> items = new TreeSet<CatalogItem>();
			for (String id : myIds) {
				items.add(new CatalogItem(id, true, myLibrary.getCatalogTreeByUrlAll(id)));
			}
			idItems.addAll(items);
		}

		if (myInactiveIds.size() > 0) {
			idItems.add(new SectionItem(getLabelByKey("inactive")));
			final TreeSet<CatalogItem> items = new TreeSet<CatalogItem>();
			for (String id : myInactiveIds) {
				items.add(new CatalogItem(id, false, myLibrary.getCatalogTreeByUrlAll(id)));
			}
			idItems.addAll(items);
		}

		ListView selectedList = (ListView) findViewById(R.id.selectedList);
		myAdapter = new CheckListAdapter(this, R.layout.checkbox_item, idItems, this);
		selectedList.setAdapter(myAdapter);
	}

	private String getLabelByKey(String keyName) {
		return NetworkLibrary.resource().getResource("allCatalogs").getResource(keyName).getValue();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (myIsChanged) {
			final ArrayList<String> ids = new ArrayList<String>();
			for (CheckItem item : myAdapter.getItems()) {
				if (item instanceof CatalogItem && ((CatalogItem)item).IsChecked) {
					ids.add(item.Id);
				}
			}
			myLibrary.setActiveIds(ids);
			myLibrary.synchronize();
		}
	}

	private static abstract class CheckItem {
		private final String Id;

		public CheckItem(String id) {
			Id = id;
		}
	}

	private static class SectionItem extends CheckItem {
		public SectionItem(String id) {
			super(id);
		}
	}

	private static class CatalogItem extends CheckItem implements Comparable<CatalogItem> {
		private final NetworkTree Tree;
		private boolean IsChecked;

		public CatalogItem(String id, boolean checked, NetworkTree tree) {
			super(id);
			IsChecked = checked;
			Tree = tree;
		}

		public String getTitle() {
			return Tree.getLink().getTitle();
		}

		public String getTitleLower() {
			return getTitle().toLowerCase(Locale.getDefault());
		}

		@Override
		public int compareTo(CatalogItem another) {
			return getTitleLower().compareTo(another.getTitleLower());
		}
	}

	private class CheckListAdapter extends ArrayAdapter<CheckItem> {
		Activity myActivity;
		private CoverManager myCoverManager;
		private ArrayList<CheckItem> items = new ArrayList<CheckItem>();

		public CheckListAdapter(Context context, int textViewResourceId, List<CheckItem> objects, Activity activity) {
			super(context, textViewResourceId, objects);
			myActivity = activity;
			items.addAll(objects);
		}

		public ArrayList<CheckItem> getItems() {
			return items;
		}

		@Override
		public View getView(int position, View convertView, final ViewGroup parent) {
			View v = convertView;
			final CheckItem item = this.getItem(position);

			if (item instanceof SectionItem) {
				v = LayoutInflater.from(getContext()).inflate(R.layout.checkbox_section, null);
				TextView tt = (TextView) v.findViewById(R.id.title);
				if (tt != null) {
					tt.setText(item.Id);
				}
			} else /* if (item instanceof CatalogItem) */ {
				final CatalogItem catalogItem = (CatalogItem)item;
				if (myCoverManager == null) {
					v.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					final int coverHeight = v.getMeasuredHeight();
					myCoverManager = new CoverManager(myActivity, coverHeight * 15 / 12, coverHeight);
					v.requestLayout();
				}

				v = LayoutInflater.from(getContext()).inflate(R.layout.checkbox_item, null);

				final INetworkLink link = catalogItem.Tree.getLink();
				((TextView)v.findViewById(R.id.title)).setText(link.getTitle());
				((TextView)v.findViewById(R.id.subtitle)).setText(link.getSummary());

				final ImageView coverView = (ImageView)v.findViewById(R.id.icon);
				if (!myCoverManager.trySetCoverImage(coverView, catalogItem.Tree)) {
					coverView.setImageResource(R.drawable.ic_list_library_books);
				}

				final CheckBox checkBox = (CheckBox)v.findViewById(R.id.check_item);
				checkBox.setChecked(catalogItem.IsChecked);
				checkBox.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						catalogItem.IsChecked = checkBox.isChecked();
						myIsChanged = true;
					}
				});
			}
			return v;
		}
	}
}
