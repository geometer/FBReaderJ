/*
 * Copyright (C) 2010-2012 Geometer Plus <contact@geometerplus.com>
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
import java.net.*;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

		final Button cancelButton = (Button)buttonView.findViewById(R.id.cancel_button);
		cancelButton.setText(buttonResource.getResource("cancel").getValue());
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});

		final WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		final int state = wifiManager.getWifiState();
		if (state != WifiManager.WIFI_STATE_ENABLED && state != WifiManager.WIFI_STATE_ENABLING) {
			setTitle(myResource.getResource("wifiIsTurnedOff").getValue());
			final View listView = findViewById(android.R.id.list);
			final TextView errorView = (TextView)findViewById(R.id.scan_local_network_error);
			listView.setVisibility(View.GONE);
			errorView.setVisibility(View.VISIBLE);
			errorView.setText(myResource.getResource("turnWiFiOn").getValue());

			final Button turnOnButton = (Button)buttonView.findViewById(R.id.ok_button);
			turnOnButton.setText(buttonResource.getResource("turnOn").getValue());
			turnOnButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					wifiManager.setWifiEnabled(true);
					finish();
				}
			});

			myLock = null;
		} else {
			final Button rescanButton = (Button)buttonView.findViewById(R.id.ok_button);
			rescanButton.setText(buttonResource.getResource("rescan").getValue());
			rescanButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					scan();
				}
			});

			myLock = wifiManager.createMulticastLock("FBReader_lock");
			myLock.setReferenceCounted(true);
			myLock.acquire();

			scan();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (myLock != null) {
			myLock.release();
		}
	}

	private List<InetAddress> getLocalIpAddresses() {
		final List<InetAddress> addresses = new LinkedList<InetAddress>();
		Method testPtoPMethod = null;
		try {
			testPtoPMethod = NetworkInterface.class.getMethod("isPointToPoint");
		} catch (NoSuchMethodException e) {
		}
		try {
			for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				try {
					if (testPtoPMethod != null && (Boolean)testPtoPMethod.invoke(iface)) {
						continue;
					}
				} catch (IllegalAccessException e) {
				} catch (InvocationTargetException e) {
				}
				for (InetAddress addr : Collections.list(iface.getInetAddresses())) {
					if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
						addresses.add(addr);
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return addresses;
	}

	private void scan() {
		final Runnable scanRunnable = new Runnable() {
			public void run() {
				final ArrayList<ServiceInfoItem> services = new ArrayList<ServiceInfoItem>();
				String errorText = null;

				try {
					final List<InetAddress> addresses = getLocalIpAddresses();
					if (addresses.isEmpty()) {
						errorText = myResource.getResource("noLocalConnection").getValue();
					} else {
						for (InetAddress address : addresses) {
							final JmDNS mcDNS = JmDNS.create(address, "FBReader");
							for (String type : ourServiceTypes) {
								for (ServiceInfo info : mcDNS.list(type)) {
									services.add(new ServiceInfoItem(info));
								}
							}
							mcDNS.close();
						}
						if (services.isEmpty()) {
							errorText = myResource.getResource("noCatalogsFound").getValue();
						}
					}
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
				final TextView errorView = (TextView)findViewById(R.id.scan_local_network_error);
				if (errorText != null) {
					listView.setVisibility(View.GONE);
					errorView.setVisibility(View.VISIBLE);
					errorView.setText(errorText);
					errorView.setTextColor(Color.RED);
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
