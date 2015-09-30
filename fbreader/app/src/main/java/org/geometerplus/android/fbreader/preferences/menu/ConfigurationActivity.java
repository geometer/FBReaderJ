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

package org.geometerplus.android.fbreader.preferences.menu;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import com.mobeta.android.dslv.DragSortListView;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.android.fbreader.MenuData;
import org.geometerplus.android.util.ViewUtil;

public class ConfigurationActivity extends ListActivity {
	static final String ENABLED_MENU_IDS_KEY = "enabledMenuIds";
	static final String DISABLED_MENU_IDS_KEY = "disabledMenuIds";

	private final List<Item> myAllItems = new ArrayList<Item>();

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.menu_configure_view);
	}

	@Override
	protected void onStart() {
		super.onStart();

		myAllItems.clear();

		final Intent intent = getIntent();

		myAllItems.add(new SectionItem("enabled"));
		final List<String> enabledIds =
			intent.getStringArrayListExtra(ENABLED_MENU_IDS_KEY);
		if (enabledIds.size() > 0) {
			for (String id : enabledIds) {
				myAllItems.add(new MenuNodeItem(id, true));
			}
		}

		myAllItems.add(new SectionItem("disabled"));
		final List<String> disabledIds =
			intent.getStringArrayListExtra(DISABLED_MENU_IDS_KEY);
		if (disabledIds.size() > 0) {
			for (String id : disabledIds) {
				myAllItems.add(new MenuNodeItem(id, false));
			}
		}

		setListAdapter(new MenuListAdapter());
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
			Title = ZLResource.resource("Preferences").getResource("menu").getResource(key).getValue();
		}
	}

	private static class MenuNodeItem implements Item {
		private final String Id;
		private boolean IsChecked;
		private final boolean IsEnabled;

		public MenuNodeItem(String id, boolean checked) {
			Id = id;
			IsChecked = checked;
			IsEnabled = !MenuData.isCodeAlwaysEnabled(id);
		}

		public String getTitle() {
			return ZLResource.resource("menu").getResource(Id).getValue();
		}
	}

	private class MenuListAdapter extends ArrayAdapter<Item> implements DragSortListView.DropListener, DragSortListView.RemoveListener {
		public MenuListAdapter() {
			super(ConfigurationActivity.this, R.layout.menu_configure_item, myAllItems);
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
				final MenuNodeItem menuItem = (MenuNodeItem)item;
				if (menuItem.IsChecked) {
					eIds.add(menuItem.Id);
				} else {
					dIds.add(menuItem.Id);
				}
			}
			setResult(RESULT_OK, new Intent()
				.putStringArrayListExtra(ENABLED_MENU_IDS_KEY, eIds)
				.putStringArrayListExtra(DISABLED_MENU_IDS_KEY, dIds));
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
						? R.layout.menu_configure_section_head : R.layout.menu_configure_item,
					null
				);
				view.setTag(item.getClass());
			}

			if (item instanceof SectionItem) {
				ViewUtil.setSubviewText(
					view, R.id.menu_configure_section_head_title, ((SectionItem)item).Title
				);
			} else /* if (item instanceof MenuNodeItem) */ {
				final MenuNodeItem menuItem = (MenuNodeItem)item;

				final TextView titleView =
					ViewUtil.findTextView(view, R.id.menu_configure_item_title);
				titleView.setText(menuItem.getTitle());

				final ImageView iconView =
					ViewUtil.findImageView(view, R.id.menu_configure_item_icon);
				iconView.setImageResource(MenuData.configIconId(menuItem.Id));

				final CheckBox checkBox =
					(CheckBox)ViewUtil.findView(view, R.id.menu_configure_item_checkbox);
				checkBox.setChecked(menuItem.IsChecked);
				checkBox.setEnabled(menuItem.IsEnabled);

				if (menuItem.IsEnabled) {
					final View.OnClickListener updateCheckbox = new View.OnClickListener() {
						public void onClick(View v) {
							if (v != checkBox) {
								checkBox.performClick();
							}
							menuItem.IsChecked = checkBox.isChecked();
							setResultIds();
						}
					};
					checkBox.setOnClickListener(updateCheckbox);
					iconView.setOnClickListener(updateCheckbox);
					titleView.setOnClickListener(updateCheckbox);
				}
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
			if (item instanceof MenuNodeItem) {
				remove(item);
				insert(item, to);
				if (((MenuNodeItem)item).IsEnabled) {
					((MenuNodeItem)item).IsChecked = to < indexOfDisabledSectionItem();
				}
				getListView().moveCheckState(from, to);
				setResultIds();
			}
		}

		// method from DragSortListView.RemoveListener
		public void remove(int which) {
			final Item item = getItem(which);
			if (item instanceof MenuNodeItem) {
				remove(item);
				getListView().removeCheckState(which);
			}
		}
	}
}
