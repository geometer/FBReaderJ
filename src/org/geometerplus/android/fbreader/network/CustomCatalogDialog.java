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

import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.content.DialogInterface;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.opds.OPDSLinkReader;


class CustomCatalogDialog extends NetworkDialog {

	private String myTitle;
	private String myUrl;
	private String mySummary;

	public CustomCatalogDialog() {
		super("CustomCatalogDialog");
	}

	private void clearData() {
		myTitle = myUrl = mySummary = null;
	}

	public Dialog createDialog(final Activity activity) {
		final View layout = activity.getLayoutInflater().inflate(R.layout.network_custom_catalog_dialog, null);

		setupLabel(layout, R.id.network_catalog_title_text, "catalogTitle", R.id.network_catalog_title);
		setupLabel(layout, R.id.network_catalog_url_text, "catalogUrl", R.id.network_catalog_url);
		setupLabel(layout, R.id.network_catalog_summary_text, "catalogSummary", R.id.network_catalog_summary);

		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				if (!NetworkView.Instance().isInitialized()) {
					return;
				}
				final NetworkLibrary library = NetworkLibrary.Instance();
				library.invalidate();
				library.invalidateVisibility();
				library.synchronize();
				NetworkView.Instance().fireModelChanged();
				if (message.what < 0) {
					if (message.what == -2) {
						final ZLResource dialogResource = ZLResource.resource("dialog");
						final ZLResource boxResource = dialogResource.getResource("networkError");
						final ZLResource buttonResource = dialogResource.getResource("button");
						new AlertDialog.Builder(activity)
							.setTitle(boxResource.getResource("title").getValue())
							.setMessage((String) message.obj)
							.setIcon(0)
							.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
							.create().show();
					} else {
						myErrorMessage = (String) message.obj;
						activity.showDialog(NetworkDialog.DIALOG_CUSTOM_CATALOG);
						return;
					}
				} else if (message.what > 0) {
					if (myOnSuccessRunnable != null) {
						myOnSuccessRunnable.run();
					}
				}
				clearData();
			}
		};

		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					AlertDialog alert = (AlertDialog) dialog;
					myTitle = ((TextView) alert.findViewById(R.id.network_catalog_title)).getText().toString().trim();
					myUrl = ((TextView) alert.findViewById(R.id.network_catalog_url)).getText().toString().trim();
					mySummary = ((TextView) alert.findViewById(R.id.network_catalog_summary)).getText().toString().trim();

					if (myTitle.length() == 0) {
						myTitle = null;
						final String err = myResource.getResource("titleIsEmpty").getValue();
						handler.sendMessage(handler.obtainMessage(-1, err));
						return;
					}
					if (myUrl.length() == 0) {
						myUrl = null;
						final String err = myResource.getResource("urlIsEmpty").getValue();
						handler.sendMessage(handler.obtainMessage(-1, err));
						return;
					}
					if (mySummary.length() == 0) {
						mySummary = null;
					}

					Uri uri = Uri.parse(myUrl);
					if (uri.getScheme() == null) {
						myUrl = "http://" + myUrl;
						uri = Uri.parse(myUrl);
					}

					String siteName = uri.getHost();
					if (siteName == null) {
						final String err = myResource.getResource("invalidUrl").getValue();
						handler.sendMessage(handler.obtainMessage(-1, err));
						return;
					}
					if (siteName.startsWith("www.")) {
						siteName = siteName.substring(4);
					}

					if (myLink == null) {
						final OPDSLinkReader reader = new OPDSLinkReader();
						final HashMap<String, String> links = new HashMap<String, String>();
						links.put(INetworkLink.URL_MAIN, myUrl);
						final ICustomNetworkLink link = reader.createCustomLink(ICustomNetworkLink.INVALID_ID, 
								siteName, myTitle, mySummary, null, links);
						myLink = link;
						if (link != null) {
							if (!NetworkLibrary.Instance().addCustomLink(link)) {
								final String err = myResource.getResource("alreadyExists").getValue();
								handler.sendMessage(handler.obtainMessage(-1, err));
								return;
							}
						} else {
							throw new RuntimeException("Unable to create link!!! Impossible!!!");
						}
					} else {
						final ICustomNetworkLink link = (ICustomNetworkLink) myLink;
						link.setSiteName(siteName);
						link.setTitle(myTitle);
						link.setSummary(mySummary);
						link.setLink(INetworkLink.URL_MAIN, myUrl);
						link.saveLink();
					}
					handler.sendEmptyMessage(1);
				} else {
					handler.sendEmptyMessage(0);
				}
			}
		};

		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		return new AlertDialog.Builder(activity)
			.setView(layout)
			.setTitle(myResource.getResource("title").getValue())
			.setPositiveButton(buttonResource.getResource("ok").getValue(), listener)
			.setNegativeButton(buttonResource.getResource("cancel").getValue(), listener)
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					listener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
				}
			})
			.create();
	}

	public void prepareDialog(Dialog dialog) {
		if (myLink != null) {
			if (myTitle == null) myTitle = myLink.getTitle();
			if (myUrl == null) myUrl = myLink.getLink(INetworkLink.URL_MAIN);
			if (mySummary == null) mySummary = myLink.getSummary();
		}
		((TextView) dialog.findViewById(R.id.network_catalog_title)).setText((myTitle != null) ? myTitle : "");
		((TextView) dialog.findViewById(R.id.network_catalog_url)).setText((myUrl != null) ? myUrl : "");
		((TextView) dialog.findViewById(R.id.network_catalog_summary)).setText((mySummary != null) ? mySummary : "");

		final TextView error = (TextView) dialog.findViewById(R.id.network_catalog_error);
		if (myErrorMessage == null) {
			error.setVisibility(View.GONE);
			error.setText("");
		} else {
			error.setVisibility(View.VISIBLE);
			error.setText(myErrorMessage);
		}

		View dlgView = dialog.findViewById(R.id.network_custom_catalog_dialog);
		dlgView.invalidate();
		dlgView.requestLayout();
	}
}
