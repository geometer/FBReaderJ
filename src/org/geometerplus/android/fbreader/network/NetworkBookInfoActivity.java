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

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;
import android.content.Intent;
import android.graphics.Bitmap;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.network.SQLiteCookieDatabase;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.NetworkBookTree;
import org.geometerplus.fbreader.network.urlInfo.*;

public class NetworkBookInfoActivity extends Activity implements NetworkView.EventListener {
	private NetworkBookItem myBook;
	private View myMainView;

	private final ZLResource myResource = ZLResource.resource("networkBookView");
	private BookDownloaderServiceConnection myConnection;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		SQLiteCookieDatabase.init(this);

		myMainView = getLayoutInflater().inflate(R.layout.network_book, null, false);
		setContentView(myMainView);
		myMainView.setOnCreateContextMenuListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!NetworkView.Instance().isInitialized()) {
			if (NetworkInitializer.Instance == null) {
				new NetworkInitializer(null);
				NetworkInitializer.Instance.start();
			} else {
				NetworkInitializer.Instance.setActivity(null);
			}
		}

		if (myBook == null) {
			final NetworkTree tree = Util.getTreeFromIntent(getIntent());
			if (!(tree instanceof NetworkBookTree)) {
				finish();
				return;
			}
			myBook = ((NetworkBookTree)tree).Book;
        
			myConnection = new BookDownloaderServiceConnection();
			bindService(
				new Intent(getApplicationContext(), BookDownloaderService.class),
				myConnection,
				BIND_AUTO_CREATE
			);
        
			setTitle(myBook.Title);
        
			setupDescription();
			setupExtraLinks();
			setupInfo();
			setupCover();
			setupButtons();
		}
	}

	View getMainView() {
		return myMainView;
	}

	private void setTextById(int id, CharSequence text) {
		((TextView)findViewById(id)).setText(text);
	}

	private void setTextFromResource(int id, String resourceKey) {
		setTextById(id, myResource.getResource(resourceKey).getValue());
	}

	@Override
	public void onDestroy() {
		if (!NetworkView.Instance().isInitialized() && NetworkInitializer.Instance != null) {
			NetworkInitializer.Instance.setActivity(null);
		}
		if (myConnection != null) {
			unbindService(myConnection);
			myConnection = null;
		}
		super.onDestroy();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		NetworkView.Instance().getTopupActions().buildContextMenu(this, menu, myBook.Link);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		TopupActions.runAction(this, myBook.Link, item.getItemId());
		return true;
	}

	private final void setupDescription() {
		setTextFromResource(R.id.network_book_description_title, "description");

		String description = myBook.getSummary();
		if (description == null) {
			description = myResource.getResource("noDescription").getValue();
		}
		setTextById(R.id.network_book_description, description);
	}

	private final void setupExtraLinks() {
		final List<UrlInfo> extraLinks = myBook.getAllInfos(UrlInfo.Type.Related);
		if (extraLinks.isEmpty()) {
			findViewById(R.id.network_book_extra_links_title).setVisibility(View.GONE);
			findViewById(R.id.network_book_extra_links).setVisibility(View.GONE);
		} else {
			setTextFromResource(R.id.network_book_extra_links_title, "extraLinks");
			final LinearLayout extraLinkSection =
				(LinearLayout)findViewById(R.id.network_book_extra_links);
			final LayoutInflater inflater = getLayoutInflater();
			View linkView = null;
			for (UrlInfo info : extraLinks) {
				if (!(info instanceof RelatedUrlInfo)) {
					continue;
				}
				final RelatedUrlInfo relatedInfo = (RelatedUrlInfo)info;
				linkView = inflater.inflate(R.layout.extra_link_item, extraLinkSection, false);
				linkView.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						final NetworkCatalogItem catalogItem =
							myBook.createRelatedCatalogItem(relatedInfo);
						if (catalogItem != null) {
							NetworkCatalogActions.doExpandCatalog(
								NetworkBookInfoActivity.this,
								NetworkLibrary.Instance().getFakeCatalogTree(catalogItem)
							);
						} else if (MimeType.TEXT_HTML.equals(relatedInfo.Mime)) {
							Util.openInBrowser(NetworkBookInfoActivity.this, relatedInfo.Url);
						}
					}
				});
				((TextView)linkView.findViewById(R.id.extra_link_title)).setText(relatedInfo.Title);
				extraLinkSection.addView(linkView);
			}
			linkView.findViewById(R.id.extra_link_divider).setVisibility(View.GONE);
		}
	}

	private void setPairLabelTextFromResource(int id, String resourceKey) {
		final LinearLayout layout = (LinearLayout)findViewById(id);
		((TextView)layout.findViewById(R.id.book_info_key))
			.setText(myResource.getResource(resourceKey).getValue());
	}

	private void setPairValueText(int id, CharSequence text) {
		final LinearLayout layout = (LinearLayout)findViewById(id);
		((TextView)layout.findViewById(R.id.book_info_value)).setText(text);
	}

	private void setupInfo() {
		setTextFromResource(R.id.network_book_info_title, "bookInfo");

		setPairLabelTextFromResource(R.id.network_book_title, "title");
		setPairLabelTextFromResource(R.id.network_book_authors, "authors");
		setPairLabelTextFromResource(R.id.network_book_series_title, "series");
		setPairLabelTextFromResource(R.id.network_book_series_index, "indexInSeries");
		setPairLabelTextFromResource(R.id.network_book_tags, "tags");
		setPairLabelTextFromResource(R.id.network_book_catalog, "catalog");

		setPairValueText(R.id.network_book_title, myBook.Title);

		if (myBook.Authors.size() > 0) {
			findViewById(R.id.network_book_authors).setVisibility(View.VISIBLE);
			final StringBuilder authorsText = new StringBuilder();
			for (NetworkBookItem.AuthorData author : myBook.Authors) {
				if (authorsText.length() > 0) {
					authorsText.append(", ");
				}
				authorsText.append(author.DisplayName);
			}
			setPairValueText(R.id.network_book_authors, authorsText);
		} else {
			findViewById(R.id.network_book_authors).setVisibility(View.GONE);
		}

		if (myBook.SeriesTitle != null) {
			findViewById(R.id.network_book_series_title).setVisibility(View.VISIBLE);
			setPairValueText(R.id.network_book_series_title, myBook.SeriesTitle);
			final float indexInSeries = myBook.IndexInSeries;
			if (indexInSeries > 0) {
				final String seriesIndexString;
				if (Math.abs(indexInSeries - Math.round(indexInSeries)) < 0.01) {
					seriesIndexString = String.valueOf(Math.round(indexInSeries));
				} else {
					seriesIndexString = String.format("%.1f", indexInSeries);
				}
				setPairValueText(R.id.network_book_series_index, seriesIndexString);
				findViewById(R.id.network_book_series_index).setVisibility(View.VISIBLE);
			} else {
				findViewById(R.id.network_book_series_index).setVisibility(View.GONE);
			}
		} else {
			findViewById(R.id.network_book_series_title).setVisibility(View.GONE);
			findViewById(R.id.network_book_series_index).setVisibility(View.GONE);
		}

		if (myBook.Tags.size() > 0) {
			findViewById(R.id.network_book_tags).setVisibility(View.VISIBLE);
			final StringBuilder tagsText = new StringBuilder();
			for (String tag : myBook.Tags) {
				if (tagsText.length() > 0) {
					tagsText.append(", ");
				}
				tagsText.append(tag);
			}
			setPairValueText(R.id.network_book_tags, tagsText);
		} else {
			findViewById(R.id.network_book_tags).setVisibility(View.GONE);
		}

		setPairValueText(R.id.network_book_catalog, myBook.Link.getTitle());
	}

	private final void setupCover() {
		final View rootView = findViewById(R.id.network_book_root);
		final ImageView coverView = (ImageView)findViewById(R.id.network_book_cover);

		final DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		final int maxHeight = metrics.heightPixels * 2 / 3;
		final int maxWidth = maxHeight * 2 / 3;
		Bitmap coverBitmap = null;
		final ZLImage cover = NetworkTree.createCover(myBook);
		if (cover != null) {
			ZLAndroidImageData data = null;
			final ZLAndroidImageManager mgr = (ZLAndroidImageManager)ZLAndroidImageManager.Instance();
			if (cover instanceof ZLLoadableImage) {
				final ZLLoadableImage img = (ZLLoadableImage)cover;
				img.startSynchronization(new Runnable() {
					public void run() {
						img.synchronizeFast();
						final ZLAndroidImageData data = mgr.getImageData(img);
						if (data != null) {
							final Bitmap coverBitmap = data.getBitmap(maxWidth, maxHeight);
							if (coverBitmap != null) {
								coverView.setImageBitmap(coverBitmap);
								coverView.setVisibility(View.VISIBLE);
								rootView.invalidate();
								rootView.requestLayout();
							}
						}
					}
				});
			} else {
				data = mgr.getImageData(cover);
			}
			if (data != null) {
				coverBitmap = data.getBitmap(maxWidth, maxHeight);
			}
		}
		if (coverBitmap != null) {
			coverView.setImageBitmap(coverBitmap);
			coverView.setVisibility(View.VISIBLE);
		} else {
			coverView.setVisibility(View.GONE);
		}
	}

	private final void setupButtons() {
		final ZLResource resource = ZLResource.resource("networkView");
		final int buttons[] = new int[] {
				R.id.network_book_button0,
				R.id.network_book_button1,
				R.id.network_book_button2,
				R.id.network_book_button3,
		};
		final Set<NetworkBookActions.Action> actions = NetworkBookActions.getContextMenuActions(myBook, myConnection);

		final boolean skipSecondButton =
			actions.size() < buttons.length &&
			actions.size() % 2 == 1;
		int buttonNumber = 0;
		for (final NetworkBookActions.Action a : actions) {
			if (skipSecondButton && buttonNumber == 1) {
				++buttonNumber;
			}
			if (buttonNumber >= buttons.length) {
				break;
			}

			final String text;
			if (a.Arg == null) {
				text = resource.getResource(a.Key).getValue();
			} else {
				text = resource.getResource(a.Key).getValue().replace("%s", a.Arg);
			}

			final int buttonId = buttons[buttonNumber++];
			TextView button = (TextView)findViewById(buttonId);
			button.setText(text);
			button.setVisibility(View.VISIBLE);
			button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					NetworkBookActions.runActionStatic(NetworkBookInfoActivity.this, myBook, a.Id);
					NetworkBookInfoActivity.this.updateView();
				}
			});
			button.setEnabled(a.Id != NetworkTreeActions.TREE_NO_ACTION);
		}
		findViewById(R.id.network_book_left_spacer).setVisibility(skipSecondButton ? View.VISIBLE : View.GONE);
		findViewById(R.id.network_book_right_spacer).setVisibility(skipSecondButton ? View.VISIBLE : View.GONE);
		if (skipSecondButton) {
			final int buttonId = buttons[1];
			View button = findViewById(buttonId);
			button.setVisibility(View.GONE);
			button.setOnClickListener(null);
		}
		while (buttonNumber < buttons.length) {
			final int buttonId = buttons[buttonNumber++];
			View button = findViewById(buttonId);
			button.setVisibility(View.GONE);
			button.setOnClickListener(null);
		}
	}

	private void updateView() {
		setupButtons();
		final View rootView = findViewById(R.id.network_book_root);
		rootView.invalidate();
		rootView.requestLayout();
	}

	@Override
	protected void onStart() {
		super.onStart();
		NetworkView.Instance().addEventListener(this);
	}

	@Override
	protected void onStop() {
		NetworkView.Instance().removeEventListener(this);
		super.onStop();
	}

	public void onModelChanged() {
		runOnUiThread(new Runnable() {
			public void run() {
				updateView();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case NetworkBaseActivity.CUSTOM_AUTHENTICATION_CODE:
				Util.processCustomAuthentication(this, myBook.Link, resultCode, data);
				break;
			case NetworkBaseActivity.SIGNUP_CODE:
				Util.processSignup(myBook.Link, resultCode, data);
				break;
		}
	}
}
