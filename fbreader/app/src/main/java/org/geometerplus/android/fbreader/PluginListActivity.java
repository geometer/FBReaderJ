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

package org.geometerplus.android.fbreader;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import android.view.*;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.XmlUtil;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.PackageUtil;
import org.geometerplus.android.util.ViewUtil;

public class PluginListActivity extends ListActivity {
	private final ZLResource myResource = ZLResource.resource("plugins");

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setTitle(myResource.getValue());
		final PluginListAdapter adapter = new PluginListAdapter();
		setListAdapter(adapter);
		getListView().setOnItemClickListener(adapter);
	}

	private static class Plugin {
		final String Id;
		final String PackageName;

		Plugin(String id, String packageName) {
			Id = id;
			PackageName = packageName;
		}
	}

	private class Reader extends DefaultHandler {
		final PackageManager myPackageManager = getPackageManager();
		final List<Plugin> myPlugins;

		Reader(List<Plugin> plugins) {
			myPlugins = plugins;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if ("plugin".equals(localName)) {
				try {
					if (Integer.valueOf(attributes.getValue("min-api")) > Build.VERSION.SDK_INT) {
						return;
					}
				} catch (Throwable t) {
					// ignore
				}
				final String id = attributes.getValue("id");
				final String packageName = attributes.getValue("package");
				try {
      				myPackageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
				} catch (PackageManager.NameNotFoundException e) {
					myPlugins.add(new Plugin(id, packageName));
				}
			}
		}
	}

	private class PluginListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
		private final List<Plugin> myPlugins = new LinkedList<Plugin>();

		PluginListAdapter() {
			XmlUtil.parseQuietly(
				ZLFile.createFileByPath("default/plugins.xml"),
				new Reader(myPlugins)
			);
		}

		public final int getCount() {
			return myPlugins.isEmpty() ? 1 : myPlugins.size();
		}

		public final Plugin getItem(int position) {
			return myPlugins.isEmpty() ? null : myPlugins.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, final ViewGroup parent) {
			final View view = convertView != null
				? convertView
				: LayoutInflater.from(parent.getContext()).inflate(R.layout.plugin_item, parent, false);
			final ImageView iconView = (ImageView)view.findViewById(R.id.plugin_item_icon);
			final TextView titleView = ViewUtil.findTextView(view, R.id.plugin_item_title);
			final TextView summaryView = ViewUtil.findTextView(view, R.id.plugin_item_summary);
			final Plugin plugin = getItem(position);
			if (plugin != null) {
				final ZLResource resource = myResource.getResource(plugin.Id);
				titleView.setText(resource.getValue());
				summaryView.setText(resource.getResource("summary").getValue());
				int iconId = R.drawable.fbreader;
				try {
					final Field f = R.drawable.class.getField("plugin_" + plugin.Id);
					iconId = f.getInt(R.drawable.class);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				iconView.setImageResource(iconId);
			} else {
				final ZLResource resource = myResource.getResource("noMorePlugins");
				titleView.setText(resource.getValue());
				summaryView.setVisibility(View.GONE);
				iconView.setVisibility(View.GONE);
			}
			return view;
		}

		public final void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
			final Plugin plugin = getItem(position);
			if (plugin != null) {
				runOnUiThread(new Runnable() {
					public void run() {
						finish();
						PackageUtil.installFromMarket(PluginListActivity.this, plugin.PackageName);
					}
				});
			}
		}
	}
}
