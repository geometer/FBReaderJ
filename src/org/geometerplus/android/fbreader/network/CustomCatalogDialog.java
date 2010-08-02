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

import android.app.Dialog;
import android.app.AlertDialog;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;
import android.content.DialogInterface;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.opds.OPDSLinkReader;


class CustomCatalogDialog extends NetworkDialog {

	private String myTitle;
	private String myUrl;
	private String mySummary;

	public CustomCatalogDialog() {
		super("CustomCatalogDialog");
	}

	@Override
	protected void clearData() {
		myTitle = myUrl = mySummary = null;
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
			final String err = myResource.getResource("titleIsEmpty").getValue();
			sendError(true, false, err);
			return;
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
		if (library.hasCustomLink(myTitle, (ICustomNetworkLink) myLink)) {
			final String err = myResource.getResource("alreadyExists").getValue();
			sendError(true, false, err);
			return;
		}

		if (myLink == null) {
			final OPDSLinkReader reader = new OPDSLinkReader();
			final HashMap<String, String> links = new HashMap<String, String>();
			links.put(INetworkLink.URL_MAIN, myUrl);
			final ICustomNetworkLink link = reader.createCustomLink(ICustomNetworkLink.INVALID_ID, 
					siteName, myTitle, mySummary, null, links);
			if (link != null) {
				NetworkLibrary.Instance().addCustomLink(link);
			} else {
				throw new RuntimeException("Unable to create link!!! Impossible!!!");
			}
			myLink = link;
		} else {
			final ICustomNetworkLink link = (ICustomNetworkLink) myLink;
			link.setSiteName(siteName);
			link.setTitle(myTitle);
			link.setSummary(mySummary);
			link.setLink(INetworkLink.URL_MAIN, myUrl);
			link.saveLink();
		}
		sendSuccess(true);
	}

	@Override
	protected void onNegative(DialogInterface dialog) {
		sendCancel(false);
	}

	@Override
	public void prepareDialog(Dialog dialog) {
		if (myLink != null) {
			if (myTitle == null) myTitle = myLink.getTitle();
			if (myUrl == null) myUrl = myLink.getLink(INetworkLink.URL_MAIN);
			if (mySummary == null) mySummary = myLink.getSummary();
		}
		((TextView) dialog.findViewById(R.id.network_catalog_title)).setText((myTitle != null) ? myTitle : "");
		((TextView) dialog.findViewById(R.id.network_catalog_url)).setText((myUrl != null) ? myUrl : "");
		((TextView) dialog.findViewById(R.id.network_catalog_summary)).setText((mySummary != null) ? mySummary : "");

		final int examplesVisibility = (myLink == null) ? View.VISIBLE : View.GONE;
		dialog.findViewById(R.id.network_catalog_title_example).setVisibility(examplesVisibility);
		dialog.findViewById(R.id.network_catalog_url_example).setVisibility(examplesVisibility);
		dialog.findViewById(R.id.network_catalog_summary_example).setVisibility(examplesVisibility);

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
