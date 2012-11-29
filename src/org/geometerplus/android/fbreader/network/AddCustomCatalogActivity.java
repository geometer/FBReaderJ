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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.opds.OPDSCustomNetworkLink;
import org.geometerplus.fbreader.network.urlInfo.*;

import org.geometerplus.android.util.UIUtil;

public class AddCustomCatalogActivity extends Activity {
	static final String TYPE = "type";

	private ZLResource myResource;
	private volatile ICustomNetworkLink myLink;
	private boolean myEditNotAdd;
	private INetworkLink.Type myType = INetworkLink.Type.Custom;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		AuthenticationActivity.initCredentialsCreator(this);

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
			R.id.ok_button, "ok", new View.OnClickListener() {
				public void onClick(View view) {
					final InputMethodManager imm =
						(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(findViewById(R.id.add_custom_catalog_url).getWindowToken(), 0);
					imm.hideSoftInputFromWindow(findViewById(R.id.add_custom_catalog_title).getWindowToken(), 0);
					imm.hideSoftInputFromWindow(findViewById(R.id.add_custom_catalog_summary).getWindowToken(), 0);
					onOkButton();
				}
			}
		);
		setupButton(
			R.id.cancel_button, "cancel", new View.OnClickListener() {
				public void onClick(View view) {
					finish();
				}
			}
		);

		Util.initLibrary(this);

		final Intent intent = getIntent();
		final String action = intent.getAction();
		myEditNotAdd = Util.EDIT_CATALOG_ACTION.equals(action);
		myLink = null;
		Uri uri = null;
		if (myEditNotAdd ||
			Intent.ACTION_VIEW.equals(action) ||
			Util.ADD_CATALOG_URL_ACTION.equals(action)) {
			uri = intent.getData();
			if (uri != null) {
				if ("opds".equals(uri.getScheme())) {
					uri = Uri.parse("http" + uri.toString().substring(4));
				}
				final INetworkLink link = NetworkLibrary.Instance().getLinkByUrl(uri.toString());
				if (link instanceof ICustomNetworkLink) {
					myLink = (ICustomNetworkLink)link;
				}
			}

			myType = INetworkLink.Type.byIndex(intent.getIntExtra(TYPE, myType.Index));
		}

		if (myLink != null) {
			setTextById(R.id.add_custom_catalog_url, myLink.getUrl(UrlInfo.Type.Catalog));
			setTextById(R.id.add_custom_catalog_title, myLink.getTitle());
			setTextById(R.id.add_custom_catalog_summary, myLink.getSummary());
			setExtraFieldsVisibility(true);
		} else if (uri != null) {
			if ("opds".equals(uri.getScheme())) {
				uri = Uri.parse("http" + uri.toString().substring(4));
			}
			loadInfoByUri(uri);
		} else {
			setExtraFieldsVisibility(false);
		}
	}

	private void onOkButton() {
		final String textUrl = getTextById(R.id.add_custom_catalog_url);
		if (isEmptyString(textUrl)) {
			setErrorByKey("urlIsEmpty");
			return;
		}

		final String title = getTextById(R.id.add_custom_catalog_title);
		final String summary = getTextById(R.id.add_custom_catalog_summary);
		Uri uri = null;
		try {
			uri = Uri.parse(textUrl);
			if (isEmptyString(uri.getScheme())) {
				uri = Uri.parse("http://" + textUrl);
			}
			if (isEmptyString(uri.getHost())) {
				setErrorByKey("invalidUrl");
				return;
			}
		} catch (Throwable t) {
			setErrorByKey("invalidUrl");
			return;
		}
		if (myLink == null) {
			loadInfoByUri(uri);
		} else if (isEmptyString(title)) {
			setErrorByKey("titleIsEmpty");
			setExtraFieldsVisibility(true);
		} else {
			myLink.setTitle(title);
			myLink.setSummary(summary);
			myLink.setUrl(UrlInfo.Type.Catalog, uri.toString(), MimeType.APP_ATOM_XML);

			final NetworkLibrary library = NetworkLibrary.Instance();
			library.addCustomLink(myLink);
			library.synchronize();

			final Intent intent = new Intent(
				NetworkLibraryActivity.OPEN_CATALOG_ACTION,
				myEditNotAdd ? null : uri,
				AddCustomCatalogActivity.this,
				NetworkLibraryPrimaryActivity.class
			).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
	}

	private boolean isEmptyString(String s) {
		return s == null || s.length() == 0;
	}

	private void setExtraFieldsVisibility(boolean show) {
		final int visibility = show ? View.VISIBLE : View.GONE;
		runOnUiThread(new Runnable() {
			public void run() {
				findViewById(R.id.add_custom_catalog_title_group).setVisibility(visibility);
				findViewById(R.id.add_custom_catalog_summary_group).setVisibility(visibility);
			}
		});
	}

	private void setTextById(int id, String text) {
		((TextView)findViewById(id)).setText(text);
	}

	private String getTextById(int id) {
		final String text = ((TextView)findViewById(id)).getText().toString();
		return text != null ? text.trim() : null;
	}

	private void setupButton(int id, String resourceKey, View.OnClickListener listener) {
		final Button button =
			(Button)findViewById(R.id.add_custom_catalog_buttons).findViewById(id);
		button.setText(
			ZLResource.resource("dialog").getResource("button").getResource(resourceKey).getValue()
		);
		button.setOnClickListener(listener);
	}

	private void setTextFromResource(int id, String resourceKey) {
		setTextById(id, myResource.getResource(resourceKey).getValue());
	}

	private void setErrorText(final String errorText) {
		runOnUiThread(new Runnable() {
			public void run() {
				final TextView errorView = (TextView)findViewById(R.id.add_custom_catalog_error);
				if (errorText != null) {
					errorView.setText(errorText);
					errorView.setVisibility(View.VISIBLE);
				} else {
					errorView.setVisibility(View.GONE);
				}
			}
		});
	}

	private void setErrorByKey(final String resourceKey) {
		setErrorText(myResource.getResource(resourceKey).getValue());
	}

	private void loadInfoByUri(Uri uri) {
		String textUrl = uri.toString();
		if (isEmptyString(uri.getScheme())) {
			textUrl = "http://" + textUrl;
			uri = Uri.parse(textUrl);
		}

		setTextById(R.id.add_custom_catalog_url, textUrl);
		final String siteName = uri.getHost();
		if (isEmptyString(siteName)) {
			setErrorByKey("invalidUrl");
			return;
		}
		final UrlInfoCollection<UrlInfoWithDate> infos = new UrlInfoCollection<UrlInfoWithDate>();
		infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.Catalog, textUrl, MimeType.APP_ATOM_XML));
		myLink = new OPDSCustomNetworkLink(
			ICustomNetworkLink.INVALID_ID, myType, siteName, null, null, null, infos
		);

		final Runnable loadInfoRunnable = new Runnable() {
			private String myError;

			public void run() {
				try {
					myError = null;
					myLink.reloadInfo(false, false);
				} catch (ZLNetworkException e) {
					myError = e.getMessage();
				}
				runOnUiThread(new Runnable() {
					public void run() {
						if (myError == null) {
							setTextById(R.id.add_custom_catalog_title, myLink.getTitle());
							setTextById(R.id.add_custom_catalog_summary, myLink.getSummary());
							setExtraFieldsVisibility(true);
						} else {
							myLink = null;
						}
					}
				});
				setErrorText(myError);
			}
		}; 
		UIUtil.wait("loadingCatalogInfo", loadInfoRunnable, this);
	}
}
