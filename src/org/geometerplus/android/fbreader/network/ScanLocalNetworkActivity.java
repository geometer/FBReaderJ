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
import java.io.IOException;

import android.app.ListActivity;
import android.content.*;
import android.graphics.Color;
import android.net.*;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import javax.jmdns.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.network.NetworkLibrary;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.android.util.UIUtil;

public class ScanLocalNetworkActivity extends ListActivity {
	private final ZLResource myResource =
		NetworkLibrary.Instance().resource().getResource("addCatalog");

	private WifiManager.MulticastLock myLock;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		setContentView(R.layout.scan_local_network);

		setListAdapter(new ItemAdapter());

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

			/*
			final Button turnOnButton = (Button)buttonView.findViewById(R.id.ok_button);
			turnOnButton.setText(buttonResource.getResource("turnOn").getValue());
			turnOnButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					wifiManager.setWifiEnabled(true);
					finish();
				}
			});
			*/
			buttonView.findViewById(R.id.ok_button).setVisibility(View.GONE);
			cancelButton.setText(buttonResource.getResource("ok").getValue());

			myLock = null;
		} else {
			final Button rescanButton = (Button)buttonView.findViewById(R.id.ok_button);
			rescanButton.setText(buttonResource.getResource("rescan").getValue());
			rescanButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					runOnUiThread(new Runnable() {
						public void run() {
							clear();
							scan();
						}
					});
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
		if (myLock != null) {
			myLock.release();
		}
		super.onDestroy();
	}

	private List<InetAddress> getLocalIpAddresses() {
		final List<InetAddress> addresses = new LinkedList<InetAddress>();
		try {
			for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				if (iface.isPointToPoint()) {
					continue;
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

	private class ServiceCollector implements ServiceListener {
		private final static String STANZA_ZEROCONF_TYPE = "_stanza._tcp.local.";
		private final static String CALIBRE_ZEROCONF_TYPE = "_calibre._tcp.local.";
		private final static String OPDS_ZEROCONF_TYPE = "_opds._tcp.local.";

		private JmDNS myMCDNS;

		ServiceCollector(InetAddress address) {
			try {
				myMCDNS = JmDNS.create(address, "FBReader");
			} catch (IOException e) {
				return;
			}
			myMCDNS.addServiceListener(STANZA_ZEROCONF_TYPE, this);
			myMCDNS.addServiceListener(CALIBRE_ZEROCONF_TYPE, this);
			myMCDNS.addServiceListener(OPDS_ZEROCONF_TYPE, this);

			runOnUiThread(new Runnable() {
				public void run() {
					getListAdapter().addWaitItem();
				}
			});
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(new Runnable() {
						public void run() {
							final ItemAdapter adapter = getListAdapter();
							if (adapter.removeWaitItem() && adapter.getCount() == 0) {
								setErrorText(myResource.getResource("noCatalogsFound").getValue());
							}
						}
					});
					try {
						myMCDNS.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					timer.cancel();
				}
			}, 10000);
		}

		public void serviceAdded(ServiceEvent event) {
			ServiceInfo info = event.getInfo();
			if (info == null || !info.hasData()) {
				info = myMCDNS.getServiceInfo(event.getType(), event.getName(), true);
			}
			addInfo(info);
		}

		public void serviceRemoved(ServiceEvent event) {
			// TODO
		}

		public void serviceResolved(ServiceEvent event) {
			// TODO
		}

		private void addInfo(final ServiceInfo info) {
			if (info == null || !info.hasData()) {
				return;
			}

			final String path = info.getPropertyString("path");
			if (path == null) {
				return;
			}

			final String[] urls = info.getURLs();
			if (urls.length != 1) {
				return;
			}

			if (urls[0] == null || !urls[0].endsWith(path)) {
				return;
			}

			final String type = info.getType();
			if (STANZA_ZEROCONF_TYPE.equals(info.getType()) || "/stanza".equals(path)) {
				urls[0] = urls[0].substring(0, urls[0].length() - path.length()) + "/opds";
			}

			runOnUiThread(new Runnable() {
				public void run() {
					getListAdapter().addServiceItem(
						info.getName(),
						urls[0],
						R.drawable.ic_list_library_calibre
					);
				}
			});
		}
	}

	private void scan() {
		final List<InetAddress> addresses = getLocalIpAddresses();
		if (addresses.isEmpty()) {
			runOnUiThread(new Runnable() {
				public void run() {
					setErrorText(myResource.getResource("noLocalConnection").getValue());
				}
			});
		} else {
			for (final InetAddress a : addresses) {
				new Thread() {
					public void run() {
						new ServiceCollector(a);
					}
				}.start();
			}
		}
	}

	private void clear() {
		getListAdapter().clear();
		final View listView = findViewById(android.R.id.list);
		final TextView errorView = (TextView)findViewById(R.id.scan_local_network_error);
		listView.setVisibility(View.VISIBLE);
		errorView.setVisibility(View.GONE);
	}

	private void setErrorText(final String errorText) {
		final View listView = findViewById(android.R.id.list);
		final TextView errorView = (TextView)findViewById(R.id.scan_local_network_error);
		listView.setVisibility(View.GONE);
		errorView.setVisibility(View.VISIBLE);
		errorView.setText(errorText);
	}

	private class ItemAdapter extends BaseAdapter {
		final private WaitItem myWaitItem = new WaitItem(
			myResource.getResource("scanningLocalNetwork").getValue()
		);
		private volatile int myWaitItemCount;
		private final ArrayList<ServiceInfoItem> myItems = new ArrayList<ServiceInfoItem>();

		@Override
		public Item getItem(int position) {
			try {
				return myItems.get(position);
			} catch (Exception e) {
				return myWaitItem;
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public synchronized int getCount() {
			return myWaitItemCount > 0 ? myItems.size() + 1 : myItems.size();
		}

		synchronized void clear() {
			myItems.clear();
			myWaitItemCount = 0;
			notifyDataSetChanged();
		}

		synchronized boolean addWaitItem() {
			if (myWaitItemCount++ == 0) {
				notifyDataSetChanged();
				findViewById(R.id.scan_local_network_container).invalidate();
				return true;
			}
			return false;
		}

		synchronized boolean removeWaitItem() {
			if (myWaitItemCount == 0) {
				return false;
			}
			if (--myWaitItemCount == 0) {
				notifyDataSetChanged();
				findViewById(R.id.scan_local_network_container).invalidate();
				return true;
			}
			return false;
		}

		synchronized void addServiceItem(String name, String url, int iconId) {
			try {
				final ServiceInfoItem item = new ServiceInfoItem(name, Uri.parse(url), iconId);
				if (!myItems.contains(item)) {
					myItems.add(item);
					notifyDataSetChanged();
					findViewById(R.id.scan_local_network_container).invalidate();
				}
			} catch (ParseException e) {
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Item item = getItem(position);
			final View view;
			if (convertView == null) {
				view = LayoutInflater.from(ScanLocalNetworkActivity.this).inflate(R.layout.local_service_item, parent, false);
			} else {
				view = convertView;
			}

			final TextView textView = (TextView)view.findViewById(R.id.local_service_text);
			final ImageView iconView = (ImageView)view.findViewById(R.id.local_service_icon);
			final ProgressBar progress = (ProgressBar)view.findViewById(R.id.local_service_progress);
			if (convertView == null) {

				view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				final int h = view.getMeasuredHeight() * 6 / 10;
				iconView.getLayoutParams().width = h;
				iconView.getLayoutParams().height = h;
				iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				iconView.requestLayout();
				progress.getLayoutParams().width = h;
				progress.getLayoutParams().height = h;
				iconView.requestLayout();
			}

			textView.setText(item.Name);

			if (item instanceof ServiceInfoItem) {
				iconView.setVisibility(View.VISIBLE);
				progress.setVisibility(View.GONE);
				iconView.setImageResource(((ServiceInfoItem)item).IconId);
			} else /* item instanceof WaitItem */ {
				iconView.setVisibility(View.GONE);
				progress.setVisibility(View.VISIBLE);
			}

			return view;
		}
	}

	private static abstract class Item {
		public final String Name;

		public Item(String name) {
			Name = name;
		}
	}

	private static class ServiceInfoItem extends Item {
		public final Uri URI;
		public final int IconId;

		public ServiceInfoItem(String name, Uri uri, int iconId) {
			super(name);
			URI = uri;
			IconId = iconId;
		}

		@Override
		public int hashCode() {
			return Name.hashCode() + URI.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			return
				o instanceof ServiceInfoItem &&
				Name.equals(((ServiceInfoItem)o).Name) &&
				URI.equals(((ServiceInfoItem)o).URI);
		}
	}

	private static class WaitItem extends Item {
		public WaitItem(String name) {
			super(name);
		}
	}

	@Override
	protected void onListItemClick(ListView parent, View view, int position, long id) {
		final Item item = getListAdapter().getItem(position);
		if (item instanceof ServiceInfoItem) {
			try {
				startActivity(new Intent(
					Intent.ACTION_VIEW,
					((ServiceInfoItem)item).URI,
					getApplicationContext(),
					AddCustomCatalogActivity.class
				));
				finish();
			} catch (ActivityNotFoundException e) {
			}
		}
	}

	@Override
	public ItemAdapter getListAdapter() {
		return (ItemAdapter)super.getListAdapter();
	}
}
