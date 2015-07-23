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

package org.geometerplus.android.fbreader.preferences;

import java.util.*;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import com.mobeta.android.dslv.DragSortListView;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.android.fbreader.MenuData;
import org.geometerplus.android.util.ViewUtil;

public class MenuEditActivity extends ListActivity {

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
			intent.getStringArrayListExtra(MenuPreference.ENABLED_MENU_IDS_KEY);
		if (enabledIds.size() > 0) {
			final List<Menu_Item> cItems = new ArrayList<Menu_Item>();
			for (String id : enabledIds) {
				cItems.add(new Menu_Item(id, true));
			}
			myAllItems.addAll(cItems);
			mySelectedItems.addAll(cItems);
		}

		myAllItems.add(new SectionItem("disabled"));
		final List<String> disabledIds =
			intent.getStringArrayListExtra(MenuPreference.DISABLED_MENU_IDS_KEY);
		if (disabledIds.size() > 0) {
			final TreeSet<Menu_Item> cItems = new TreeSet<Menu_Item>();
			for (String id : disabledIds) {
				cItems.add(new Menu_Item(id, false));
			}
			myAllItems.addAll(cItems);
		}

		setListAdapter(new MenuListAdapter());
	}

	@Override
	protected void onDestroy() {

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
			Title = ZLResource.resource("networkLibrary").getResource("manageCatalogs").getResource(key).getValue(); //FIXME: do we need here separate resources?
		}
	}

	private static class Menu_Item implements Item, Comparable<Menu_Item> {
		private final String Id;
		private boolean IsChecked;
		private final boolean IsEnabled;

		public Menu_Item(String id, boolean checked) {
			Id = id;
			IsChecked = checked;
			IsEnabled = !"preferences".equals(id); //never uncheck!
		}

		public String getTitle() {
			return ZLResource.resource("menu").getResource(Id).getValue();
		}

		private String getTitleLower() {
			return getTitle().toLowerCase(Locale.getDefault());
		}

		@Override
		public int compareTo(Menu_Item another) {
			return getTitleLower().compareTo(another.getTitleLower());
		}
	}

	private class MenuListAdapter extends ArrayAdapter<Item> implements DragSortListView.DropListener, DragSortListView.RemoveListener {

		public MenuListAdapter() {
			super(MenuEditActivity.this, R.layout.catalog_manager_item, myAllItems);
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
			final ArrayList<String> eIds = new ArrayList<String>();
			final ArrayList<String> dIds = new ArrayList<String>();
			for (int i = 1; i < getCount(); ++i) {
				final Item item = getItem(i);
				if (item instanceof SectionItem) {
					continue;
				}
				final Menu_Item catalogItem = (Menu_Item)item;
				if (catalogItem.IsChecked) {
					eIds.add(catalogItem.Id);
				} else {
					dIds.add(catalogItem.Id);
				}
			}
			setResult(RESULT_OK, new Intent()
				.putStringArrayListExtra(MenuPreference.ENABLED_MENU_IDS_KEY, eIds)
				.putStringArrayListExtra(MenuPreference.DISABLED_MENU_IDS_KEY, dIds));
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
			} else /* if (item instanceof Menu_Item) */ {
				final Menu_Item menuItem = (Menu_Item)item;


				ViewUtil.setSubviewText(view, R.id.catalog_manager_item_title, menuItem.getTitle());

				final ImageView coverView = ViewUtil.findImageView(view, R.id.catalog_manager_item_icon);
				coverView.setPadding(5, 20, 5, 20);
				
				if (MenuData.iconId(menuItem.Id) != -1) {
					Bitmap b = BitmapFactory.decodeResource(getResources(), MenuData.iconId(menuItem.Id));
					coverView.setImageBitmap(Bitmap.createScaledBitmap(b, (int)(b.getWidth() * 0.8), (int)(b.getHeight() * 0.8), false));
					b.recycle();
				} else {
					coverView.setImageResource(0);
				}

				final CheckBox checkBox = (CheckBox)ViewUtil.findView(view, R.id.catalog_manager_item_checkbox);
				checkBox.setChecked(menuItem.IsChecked);
				checkBox.setEnabled(menuItem.IsEnabled);
				checkBox.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						menuItem.IsChecked = checkBox.isChecked();
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
			if (item instanceof Menu_Item) {
				remove(item);
				insert(item, to);
				if (((Menu_Item)item).IsEnabled) {
					((Menu_Item)item).IsChecked = to < indexOfDisabledSectionItem();
				}
				getListView().moveCheckState(from, to);
				setResultIds();
			}
		}

		// method from DragSortListView.RemoveListener
		public void remove(int which) {
			final Item item = getItem(which);
			if (item instanceof Menu_Item) {
				remove(item);
				getListView().removeCheckState(which);
			}
		}
	}
}
