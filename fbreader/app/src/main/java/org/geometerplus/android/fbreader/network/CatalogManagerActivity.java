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

package org.geometerplus.android.fbreader.network;

import java.util.*;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import com.mobeta.android.dslv.DragSortListView;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.fbreader.network.*;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.covers.CoverManager;

import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.android.fbreader.util.AndroidImageSynchronizer;

public class CatalogManagerActivity extends ListActivity {
	private final AndroidImageSynchronizer myImageSynchronizer = new AndroidImageSynchronizer(this);

	private final List<Item> myAllItems = new ArrayList<Item>();
	private final List<Item> mySelectedItems = new ArrayList<Item>();

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.catalog_manager_view);
	}

	@Override
	protected void onStart() {
		super.onStart();

		myAllItems.clear();

		final Intent intent = getIntent();

		myAllItems.add(new SectionItem("enabled"));
		final List<String> enabledIds =
			intent.getStringArrayListExtra(NetworkLibraryActivity.ENABLED_CATALOG_IDS_KEY);
		if (enabledIds.size() > 0) {
			final List<CatalogItem> cItems = new ArrayList<CatalogItem>();
			for (String id : enabledIds) {
				final NetworkTree tree = Util.networkLibrary(this).getCatalogTreeByUrlAll(id);
				if (tree != null && tree.getLink() != null) {
					cItems.add(new CatalogItem(id, true, tree));
				}
			}
			myAllItems.addAll(cItems);
			mySelectedItems.addAll(cItems);
		}

		myAllItems.add(new SectionItem("disabled"));
		final List<String> disabledIds =
			intent.getStringArrayListExtra(NetworkLibraryActivity.DISABLED_CATALOG_IDS_KEY);
		if (disabledIds.size() > 0) {
			final TreeSet<CatalogItem> cItems = new TreeSet<CatalogItem>();
			for (String id : disabledIds) {
				final NetworkTree tree = Util.networkLibrary(this).getCatalogTreeByUrlAll(id);
				if (tree != null && tree.getLink() != null) {
					cItems.add(new CatalogItem(id, false, tree));
				}
			}
			myAllItems.addAll(cItems);
		}

		setListAdapter(new CatalogsListAdapter());
	}

	@Override
	protected void onDestroy() {
		myImageSynchronizer.clear();

		super.onDestroy();
	}

	@Override
	public DragSortListView getListView() {
		return (DragSortListView)super.getListView();
	}

	private static interface Item {
	}

	private static class SectionItem implements Item {
		private final String Title;

		public SectionItem(String key) {
			Title = NetworkLibrary.resource().getResource("manageCatalogs").getResource(key).getValue();
		}
	}

	private static class CatalogItem implements Item, Comparable<CatalogItem> {
		private final String Id;
		private final NetworkTree Tree;
		private boolean IsChecked;

		public CatalogItem(String id, boolean checked, NetworkTree tree) {
			Id = id;
			IsChecked = checked;
			Tree = tree;
		}

		public String getTitle() {
			return Tree.getLink().getTitle();
		}

		private String getTitleLower() {
			return getTitle().toLowerCase(Locale.getDefault());
		}

		@Override
		public int compareTo(CatalogItem another) {
			return getTitleLower().compareTo(another.getTitleLower());
		}
	}

	private class CatalogsListAdapter extends ArrayAdapter<Item> implements DragSortListView.DropListener, DragSortListView.RemoveListener {
		private CoverManager myCoverManager;

		public CatalogsListAdapter() {
			super(CatalogManagerActivity.this, R.layout.catalog_manager_item, myAllItems);
		}

		private int indexOfDisabledSectionItem() {
			for (int i = 1; i < getCount(); i++) {
				if (getItem(i) instanceof SectionItem) {
					return i;
				}
			}
			// should be impossible
			return 0;
		}

		private void setResultIds() {
			final ArrayList<String> ids = new ArrayList<String>();
			for (int i = 1; i < getCount(); ++i) {
				final Item item = getItem(i);
				if (item instanceof SectionItem) {
					continue;
				}
				final CatalogItem catalogItem = (CatalogItem)item;
				if (catalogItem.IsChecked) {
					ids.add(catalogItem.Id);
				}
			}
			setResult(RESULT_OK, new Intent().putStringArrayListExtra(NetworkLibraryActivity.ENABLED_CATALOG_IDS_KEY, ids));
		}

		@Override
		public View getView(int position, View convertView, final ViewGroup parent) {
			final Item item = getItem(position);

			final View view;
			if (convertView != null && item.getClass().equals(convertView.getTag())) {
				view = convertView;
			} else {
				view = getLayoutInflater().inflate(
					item instanceof SectionItem
						? R.layout.catalog_manager_section_head : R.layout.catalog_manager_item,
					null
				);
				view.setTag(item.getClass());
			}

			if (item instanceof SectionItem) {
				ViewUtil.setSubviewText(
					view, R.id.catalog_manager_section_head_title, ((SectionItem)item).Title
				);
			} else /* if (item instanceof CatalogItem) */ {
				final CatalogItem catalogItem = (CatalogItem)item;

				if (myCoverManager == null) {
					view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					final int coverHeight = view.getMeasuredHeight();
					myCoverManager = new CoverManager(CatalogManagerActivity.this, myImageSynchronizer, coverHeight * 15 / 22, coverHeight);
					view.requestLayout();
				}

				final INetworkLink link = catalogItem.Tree.getLink();
				ViewUtil.setSubviewText(view, R.id.catalog_manager_item_title, link.getTitle());
				ViewUtil.setSubviewText(view, R.id.catalog_manager_item_subtitle, link.getSummary());

				final ImageView coverView = ViewUtil.findImageView(view, R.id.catalog_manager_item_icon);
				if (!myCoverManager.trySetCoverImage(coverView, catalogItem.Tree)) {
					coverView.setImageResource(R.drawable.ic_list_library_books);
				}

				final CheckBox checkBox = (CheckBox)ViewUtil.findView(view, R.id.catalog_manager_item_checkbox);
				checkBox.setChecked(catalogItem.IsChecked);
				checkBox.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						catalogItem.IsChecked = checkBox.isChecked();
						setResultIds();
					}
				});
			}
			return view;
		}

		// method from DragSortListView.DropListener
		public void drop(int from, int to) {
			to = Math.max(to, 1);
			if (from == to) {
				return;
			}
			final Item item = getItem(from);
			if (item instanceof CatalogItem) {
				remove(item);
				insert(item, to);
				((CatalogItem)item).IsChecked = to < indexOfDisabledSectionItem();
				getListView().moveCheckState(from, to);
				setResultIds();
			}
		}

		// method from DragSortListView.RemoveListener
		public void remove(int which) {
			final Item item = getItem(which);
			if (item instanceof CatalogItem) {
				remove(item);
				getListView().removeCheckState(which);
			}
		}
	}
}
