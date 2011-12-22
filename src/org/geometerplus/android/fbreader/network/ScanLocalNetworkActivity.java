/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import javax.jmdns.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.network.NetworkLibrary;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.UIUtil;
 
public class ScanLocalNetworkActivity extends ListActivity {
	private final static String[] ourServiceTypes = { "_stanza._tcp.local." };

	private final ZLResource myResource =
		NetworkLibrary.Instance().resource().getResource("addCatalog");

	private WifiManager.MulticastLock myLock;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		setContentView(R.layout.scan_local_network);

		setTitle(myResource.getResource("localCatalogs").getValue());

		final View buttonView = findViewById(R.id.scan_local_network_buttons);
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");

		final Button rescanButton = (Button)buttonView.findViewById(R.id.ok_button);
		rescanButton.setText(buttonResource.getResource("rescan").getValue());
		rescanButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				scan();
			}
		});

		final Button cancelButton = (Button)buttonView.findViewById(R.id.cancel_button);
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});

		final WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		myLock = wifi.createMulticastLock("FBReader_lock");
		myLock.setReferenceCounted(true);
		myLock.acquire();

		scan();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myLock != null) {
			myLock.release();
		}
	}

	private void scan() {
		final Runnable scanRunnable = new Runnable() {
			public void run() {
				final ArrayList<ServiceInfoItem> services = new ArrayList<ServiceInfoItem>();
				String errorText;

				try {
					final JmDNS mcDNS = JmDNS.create();
					for (String type : ourServiceTypes) {
						for (ServiceInfo info : mcDNS.list(type)) {
							services.add(new ServiceInfoItem(info));
						}
					}
					errorText = services.isEmpty()
						? myResource.getResource("noCatalogsFound").getValue() : null;
				} catch (Exception e) {
					errorText = e.getMessage();
				}

				setup(services, errorText);
			}
		};
		UIUtil.wait("scanningLocalNetwork", scanRunnable, this);
	}

	private void setup(final ArrayList<ServiceInfoItem> services, final String errorText) {
		runOnUiThread(new Runnable() {
			public void run() {
				setListAdapter(new ArrayAdapter<ServiceInfoItem>(
					ScanLocalNetworkActivity.this,
					R.layout.local_service_item,
					services
				));
				final View listView = findViewById(android.R.id.list);
				final View errorView = findViewById(R.id.scan_local_network_error);
				if (errorText != null) {
					listView.setVisibility(View.GONE);
					errorView.setVisibility(View.VISIBLE);
					((TextView)errorView).setText(errorText);
				} else {
					listView.setVisibility(View.VISIBLE);
					errorView.setVisibility(View.GONE);
				}
			}
		});
	}

	private static class ServiceInfoItem {
		private final ServiceInfo myServiceInfo;

		public ServiceInfoItem(ServiceInfo info) {
			myServiceInfo = info;
		}

		public String toString() {
			return myServiceInfo.getName();
		}

		public String getUrl() {
			return myServiceInfo.getURLs()[0];
		}
	}

	@Override
	protected void onListItemClick(ListView parent, View view, int position, long id) {
		final ServiceInfoItem item = (ServiceInfoItem)getListAdapter().getItem(position);
		try {
			startActivity(new Intent(
				Intent.ACTION_VIEW,
				Uri.parse(item.getUrl()),
				getApplicationContext(),
				AddCustomCatalogActivity.class
			));
			finish();
		} catch (Exception e) {
			// TODO: show an error message
		}
	}
}
