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

import android.app.Dialog;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.content.DialogInterface;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.dialogs.ZLAndroidDialogManager;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.opds.OPDSLinkReader;


class CustomCatalogDialog extends NetworkDialog {

	private String myTitle;
	private String myUrl;
	private String mySummary;

	private boolean myLinkWithoutInfo;

	public CustomCatalogDialog() {
		super("CustomCatalogDialog");
	}

	@Override
	protected void clearData() {
		myTitle = myUrl = mySummary = null;
		myLinkWithoutInfo = false;
	}

	@Override
	protected View createLayout() {
		final View layout = myActivity.getLayoutInflater().inflate(R.layout.network_custom_catalog_dialog, null);

		((TextView) layout.findViewById(R.id.network_catalog_title_text)).setText(myResource.getResource("catalogTitle").getValue());
		((TextView) layout.findViewById(R.id.network_catalog_url_text)).setText(myResource.getResource("catalogUrl").getValue());
		((TextView) layout.findViewById(R.id.network_catalog_summary_text)).setText(myResource.getResource("catalogSummary").getValue());
		((TextView) layout.findViewById(R.id.network_catalog_title_example)).setText(myResource.getResource("catalogTitleExample").getValue());
		((TextView) layout.findViewById(R.id.network_catalog_url_example)).setText(myResource.getResource("catalogUrlExample").getValue());
		((TextView) layout.findViewById(R.id.network_catalog_summary_example)).setText(myResource.getResource("catalogSummaryExample").getValue());

		return layout;
	}

	@Override
	protected void onPositive(DialogInterface dialog) {
		AlertDialog alert = (AlertDialog) dialog;
		myTitle = ((TextView) alert.findViewById(R.id.network_catalog_title)).getText().toString().trim();
		myUrl = ((TextView) alert.findViewById(R.id.network_catalog_url)).getText().toString().trim();
		mySummary = ((TextView) alert.findViewById(R.id.network_catalog_summary)).getText().toString().trim();

		if (myTitle.length() == 0) {
			myTitle = null;
			if (myLink != null) {
				final String err = myResource.getResource("titleIsEmpty").getValue();
				sendError(true, false, err);
				return;
			}
		}
		if (myUrl.length() == 0) {
			myUrl = null;
			final String err = myResource.getResource("urlIsEmpty").getValue();
			sendError(true, false, err);
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
			sendError(true, false, err);
			return;
		}
		if (siteName.startsWith("www.")) {
			siteName = siteName.substring(4);
		}

		final NetworkLibrary library = NetworkLibrary.Instance();
		if (myLink != null && library.hasCustomLinkTitle(myTitle, (ICustomNetworkLink) myLink)) {
			final String err = myResource.getResource("titleAlreadyExists").getValue();
			sendError(true, false, err);
			return;
		}
		if (library.hasCustomLinkSite(siteName, (ICustomNetworkLink) myLink)) {
			final String err = myResource.getResource("siteAlreadyExists").getValue();
			sendError(true, false, err);
			return;
		}

		if (myLink != null) {
			final ICustomNetworkLink link = (ICustomNetworkLink) myLink;
			link.setSiteName(siteName);
			link.setTitle(myTitle);
			link.setSummary(mySummary);
			link.setLink(INetworkLink.URL_MAIN, myUrl);

			if (myLinkWithoutInfo) {
				NetworkLibrary.Instance().addCustomLink(link);
				myLinkWithoutInfo = false;
			} else {
				link.saveLink();
			}
			sendSuccess(true);
			return;
		}

		myLinkWithoutInfo = true;
		myLink = OPDSLinkReader.createCustomLinkWithoutInfo(siteName, myUrl);

		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				final String err = (String) msg.obj;
				if (err != null) {
					final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == DialogInterface.BUTTON_NEGATIVE) {
								sendSuccess(true);
							} else {
								if (which == DialogInterface.BUTTON_NEUTRAL) {
									myLinkWithoutInfo = false;
									myLink = null;
								}
								sendError(true, false, null);
							}
						}
					};
					final ZLResource dialogResource = ZLResource.resource("dialog");
					final ZLResource boxResource = dialogResource.getResource("networkError");
					final ZLResource buttonResource = dialogResource.getResource("button");
					new AlertDialog.Builder(myActivity)
						.setTitle(boxResource.getResource("title").getValue())
						.setMessage(err)
						.setIcon(0)
						.setPositiveButton(buttonResource.getResource("continue").getValue(), listener)
						.setNeutralButton(buttonResource.getResource("editUrl").getValue(), listener)
						.setNegativeButton(buttonResource.getResource("cancel").getValue(), listener)
						.setOnCancelListener(new DialogInterface.OnCancelListener() {
							public void onCancel(DialogInterface dialog) {
								listener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
							}
						})
						.create().show();
				} else {
					sendError(true, false, null);
				}
			}
		};

		final Runnable loadInfoRunnable = new Runnable() {
			public void run() {
				String error = null;
				try {
					((ICustomNetworkLink)myLink).reloadInfo();
				} catch (ZLNetworkException e) {
					error = e.getMessage();
				}
				handler.sendMessage(handler.obtainMessage(0, error));
			}
		}; 
		((ZLAndroidDialogManager)ZLAndroidDialogManager.Instance()).wait("loadingCatalogInfo", loadInfoRunnable, myActivity);
	}

	@Override
	protected void onNegative(DialogInterface dialog) {
		sendCancel(false);
	}

	@Override
	public void prepareDialogInternal(Dialog dialog) {
		if (myLink != null) {
			if (myTitle == null) myTitle = myLink.getTitle();
			if (myUrl == null) myUrl = myLink.getLink(INetworkLink.URL_MAIN);
			if (mySummary == null) mySummary = myLink.getSummary();
		}
		((TextView) dialog.findViewById(R.id.network_catalog_title)).setText(myTitle);
		((TextView) dialog.findViewById(R.id.network_catalog_url)).setText(myUrl);
		((TextView) dialog.findViewById(R.id.network_catalog_summary)).setText(mySummary);

		final int examplesVisibility = (myLink == null || myLinkWithoutInfo) ? View.VISIBLE : View.GONE;
		dialog.findViewById(R.id.network_catalog_title_example).setVisibility(examplesVisibility);
		dialog.findViewById(R.id.network_catalog_url_example).setVisibility(examplesVisibility);
		dialog.findViewById(R.id.network_catalog_summary_example).setVisibility(examplesVisibility);

		final int groupsVisibility = (myLink != null) ? View.VISIBLE : View.GONE;
		dialog.findViewById(R.id.network_catalog_title_group).setVisibility(groupsVisibility);
		dialog.findViewById(R.id.network_catalog_summary_group).setVisibility(groupsVisibility);

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
