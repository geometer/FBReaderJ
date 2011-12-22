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

import java.util.*;
import java.io.IOException;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceInfo;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.network.NetworkLibrary;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.UIUtil;

public class ScanLocalNetworkActivity extends ListActivity {
	private final static String[] ourServiceTypes = { "_stanza._tcp.local." };

	private final ZLResource myResource = NetworkLibrary.Instance().resource().getResource("addCatalog");

	private MulticastLock myLock;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		setContentView(R.layout.scan_local_network);

		setTitle(myResource.getResource("localCatalogs").getValue());

		final Button cbutton = (Button)findViewById(R.id.scan_local_network_buttons).findViewById(R.id.cancel_button);
		cbutton.setText(ZLResource.resource("dialog").getResource("button").getResource("cancel").getValue());
		cbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});

		final Button rbutton = (Button)findViewById(R.id.scan_local_network_buttons).findViewById(R.id.ok_button);
		rbutton.setText(ZLResource.resource("dialog").getResource("button").getResource("reload").getValue());
		rbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				scan();
			}
		});

		WifiManager wifi = (android.net.wifi.WifiManager)getSystemService(android.content.Context.WIFI_SERVICE);
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
            
				try {
					final JmDNS mcDNS = JmDNS.create();
					for (String type : ourServiceTypes) {
						for (ServiceInfo si : mcDNS.list(type)) {
							services.add(new ServiceInfoItem(si));
						}
					}
					setError(
						services.isEmpty() ? myResource.getResource("noCatalogsFound").getValue() : null
					);
				} catch (IOException e) {
					setError(e.getMessage());
				}
            
				final ArrayAdapter<ServiceInfoItem> adapter = new ArrayAdapter<ServiceInfoItem>(
					ScanLocalNetworkActivity.this,
					R.layout.local_service_item,
					services
				);

				runOnUiThread(new Runnable() {
					public void run() {
						ScanLocalNetworkActivity.this.setListAdapter(adapter);
					}
				});
			}
		};
		UIUtil.wait("scanningLocalNetwork", scanRunnable, this);
	}

	private void setError(final String error) {
		runOnUiThread(new Runnable() {
			public void run() {
				final View listView = findViewById(android.R.id.list);
				final View errorView = findViewById(R.id.scan_local_network_error);
				if (error != null) {
					listView.setVisibility(View.GONE);
					errorView.setVisibility(View.VISIBLE);
					((TextView)errorView).setText(error);
				} else {
					listView.setVisibility(View.VISIBLE);
					errorView.setVisibility(View.GONE);
				}
			}
		});
	}

	private static class ServiceInfoItem {
		private final ServiceInfo myServiceInfo;

		public ServiceInfoItem(ServiceInfo si) {
			myServiceInfo = si;
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
