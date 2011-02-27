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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.network.ICustomNetworkLink;
import org.geometerplus.fbreader.network.opds.OPDSLinkReader;

import org.geometerplus.android.util.UIUtil;

public class AddCustomCatalogActivity extends Activity {
	private ZLResource myResource;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		setContentView(R.layout.add_custom_catalog);

		myResource = ZLResource.resource("dialog").getResource("CustomCatalogDialog");

		setTitle(myResource.getResource("title").getValue());

		setTextFromResource(R.id.add_custom_catalog_title_label, "catalogTitle");
		setTextFromResource(R.id.add_custom_catalog_url_label, "catalogUrl");
		setTextFromResource(R.id.add_custom_catalog_summary_label, "catalogSummary");
		setTextFromResource(R.id.add_custom_catalog_title_example, "catalogTitleExample");
		setTextFromResource(R.id.add_custom_catalog_url_example, "catalogUrlExample");
		setTextFromResource(R.id.add_custom_catalog_summary_example, "catalogSummaryExample");

		setupButton(
			R.id.add_custom_catalog_ok_button, "ok", new View.OnClickListener() {
				public void onClick(View view) {
					startActivity(
						new Intent(AddCustomCatalogActivity.this, NetworkLibraryActivity.class)
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP)
					);
					finish();
				}
			}
		);
		setupButton(
			R.id.add_custom_catalog_cancel_button, "cancel", new View.OnClickListener() {
				public void onClick(View view) {
					finish();
				}
			}
		);

		final Uri uri = getIntent().getData();
		if (uri != null) {
			loadInfoByUri(uri);
		} else {
			findViewById(R.id.add_custom_catalog_title_group).setVisibility(View.GONE);
			findViewById(R.id.add_custom_catalog_summary_group).setVisibility(View.GONE);
		}
	}

	private void setText(int id, String text) {
		((TextView)findViewById(id)).setText(text);
	}

	private void setupButton(int id, String resourceKey, View.OnClickListener listener) {
		final Button button = (Button)findViewById(id);
		button.setText(
			ZLResource.resource("dialog").getResource("button").getResource(resourceKey).getValue()
		);
		button.setOnClickListener(listener);
	}

	private void setTextFromResource(int id, String resourceKey) {
		setText(id, myResource.getResource(resourceKey).getValue());
	}

	private void loadInfoByUri(Uri uri) {
		String httpUrl = uri.toString();
		if (uri.getScheme() == null) {
			httpUrl = "http://" + httpUrl;
			uri = Uri.parse(httpUrl);
		} else if ("opds".equals(uri.getScheme())) {
			httpUrl = "http" + uri.toString().substring(4);
		}

		setText(R.id.add_custom_catalog_url, httpUrl);
		String siteName = uri.getHost();
		if (siteName == null) {
			setTextFromResource(R.id.add_custom_catalog_error, "invalidUrl");
			return;
		}

		if (siteName.startsWith("www.")) {
			siteName = siteName.substring(4);
		}
		final ICustomNetworkLink link =
		OPDSLinkReader.createCustomLinkWithoutInfo(siteName, httpUrl);

		final Runnable loadInfoRunnable = new Runnable() {
			private String myError;

			public void run() {
				try {
					link.reloadInfo();
				} catch (ZLNetworkException e) {
					myError = e.getMessage();
				}
				runOnUiThread(new Runnable() {
					public void run() {
						setText(R.id.add_custom_catalog_title, link.getTitle());
						setText(R.id.add_custom_catalog_summary, link.getSummary());
						setText(R.id.add_custom_catalog_error, myError);
					}
				});
			}
		}; 
		UIUtil.wait("loadingCatalogInfo", loadInfoRunnable, this);
	}
}
