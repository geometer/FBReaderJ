/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.network.SQLiteCookieDatabase;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.opds.OPDSBookItem;
import org.geometerplus.fbreader.network.tree.NetworkBookTree;
import org.geometerplus.fbreader.network.urlInfo.RelatedUrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

import org.geometerplus.android.fbreader.OrientationUtil;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.network.action.NetworkBookActions;
import org.geometerplus.android.fbreader.network.action.OpenCatalogAction;
import org.geometerplus.android.fbreader.network.auth.ActivityNetworkContext;
import org.geometerplus.android.fbreader.util.AndroidImageSynchronizer;
import org.geometerplus.android.util.UIUtil;

public class YotaNetworkBookInfoActivity extends Activity implements NetworkLibrary.ChangeListener {
	private NetworkBookTree mTree;
	private NetworkBookItem mBook;

	private final ZLResource mResource = ZLResource.resource("bookInfo");
	private final BookCollectionShadow mBookCollection = new BookCollectionShadow();
	private final BookDownloaderServiceConnection mConnection = new BookDownloaderServiceConnection();

	private final AndroidImageSynchronizer mImageSynchronizer = new AndroidImageSynchronizer(this);

	private final ActivityNetworkContext mNetworkContext = new ActivityNetworkContext(this);

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		mBookCollection.bindToService(this, null);

		SQLiteCookieDatabase.init(this);

		setContentView(R.layout.yota_network_book_info);
	}

	@Override
	protected void onStart() {
		super.onStart();

		OrientationUtil.setOrientation(this, getIntent());

		mConnection.bindToService(this, new Runnable() {
			public void run() {
				if (!myInitializerStarted) {
					UIUtil.wait("loadingNetworkBookInfo", myInitializer, YotaNetworkBookInfoActivity.this);
				}
			}
		});

		NetworkLibrary.Instance().addChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mNetworkContext.onResume();
		NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mNetworkContext.onActivityResult(requestCode, resultCode, data);
	}

	private volatile boolean myInitializerStarted;

	private final Runnable myInitializer = new Runnable() {
		public void run() {
			synchronized (this) {
				if (myInitializerStarted) {
					return;
				}
				myInitializerStarted = true;
			}
			final NetworkLibrary library = NetworkLibrary.Instance();
			if (!library.isInitialized()) {
				if (SQLiteNetworkDatabase.Instance() == null) {
					new SQLiteNetworkDatabase(getApplication());
				}
				library.initialize(mNetworkContext);
			}

			if (mBook == null) {
				final Uri url = getIntent().getData();
				if (url != null && "litres-book".equals(url.getScheme())) {
					mBook = OPDSBookItem.create(
							mNetworkContext,
							library.getLinkBySiteName("litres.ru"),
							url.toString().replace("litres-book://", "http://")
					);
					if (mBook != null) {
						mTree = library.getFakeBookTree(mBook);
					}
				} else {
					final NetworkTree tree = library.getTreeByKey(
							(NetworkTree.Key)getIntent().getSerializableExtra(
									NetworkLibraryActivity.TREE_KEY_KEY
							)
					);
					if (tree instanceof NetworkBookTree) {
						mTree = (NetworkBookTree)tree;
						mBook = mTree.Book;
					}
				}

				runOnUiThread(myViewInitializer);
			}
		}
	};

	private final Runnable myViewInitializer = new Runnable() {
		public void run() {
			if (mBook == null) {
				finish();
			} else {

				setupDescription();
				setupInfo();
				setupCover();

				invalidateOptionsMenu();
			}
		}
	};

	private void setTextById(int id, CharSequence text) {
		((TextView)findViewById(id)).setText(text);
	}

	private void setTextFromResource(int id, String resourceKey) {
		setTextById(id, mResource.getResource(resourceKey).getValue());
	}

	@Override
	public void onDestroy() {
		mImageSynchronizer.clear();
		mBookCollection.unbind();

		super.onDestroy();
	}

	private final void setupDescription() {
		TextView title = (TextView)findViewById(R.id.yota_book_title);
		title.setText(mBook.Title);

		CharSequence description = mBook.getSummary();
		if (description == null) {
			description = mResource.getResource("noDescription").getValue();
		}
		final TextView descriptionView = (TextView)findViewById(R.id.yota_book_description);
		descriptionView.setText(description);
		descriptionView.setMovementMethod(new LinkMovementMethod());
	}

	private void setPairLabelTextFromResource(int id, String resourceKey) {
		((TextView)findViewById(id).findViewById(R.id.book_info_key))
				.setText(mResource.getResource(resourceKey).getValue());
	}

	private void setPairLabelTextFromResource(int id, String resourceKey, int param) {
		((TextView)findViewById(id).findViewById(R.id.book_info_key))
				.setText(mResource.getResource(resourceKey).getValue(param));
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
		setPairLabelTextFromResource(R.id.network_book_catalog, "catalog");

		setPairValueText(R.id.network_book_title, mBook.Title);

		if (mBook.Authors.size() > 0) {
			findViewById(R.id.network_book_authors).setVisibility(View.VISIBLE);
			final StringBuilder authorsText = new StringBuilder();
			for (NetworkBookItem.AuthorData author : mBook.Authors) {
				if (authorsText.length() > 0) {
					authorsText.append(", ");
				}
				authorsText.append(author.DisplayName);
			}
			setPairLabelTextFromResource(R.id.network_book_authors, "authors", mBook.Authors.size());
			setPairValueText(R.id.network_book_authors, authorsText);
		} else {
			findViewById(R.id.network_book_authors).setVisibility(View.GONE);
		}

		if (mBook.SeriesTitle != null) {
			findViewById(R.id.network_book_series_title).setVisibility(View.VISIBLE);
			setPairValueText(R.id.network_book_series_title, mBook.SeriesTitle);
			final float indexInSeries = mBook.IndexInSeries;
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

		if (mBook.Tags.size() > 0) {
			findViewById(R.id.network_book_tags).setVisibility(View.VISIBLE);
			final StringBuilder tagsText = new StringBuilder();
			for (String tag : mBook.Tags) {
				if (tagsText.length() > 0) {
					tagsText.append(", ");
				}
				tagsText.append(tag);
			}
			setPairLabelTextFromResource(R.id.network_book_tags, "tags", mBook.Tags.size());
			setPairValueText(R.id.network_book_tags, tagsText);
		} else {
			findViewById(R.id.network_book_tags).setVisibility(View.GONE);
		}

		setPairValueText(R.id.network_book_catalog, mBook.Link.getTitle());
	}

	private final void setupCover() {
		final View rootView = findViewById(R.id.network_book_root);
		final ImageView coverView = (ImageView)findViewById(R.id.network_book_cover);

		final DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		final int maxHeight = metrics.heightPixels * 2 / 3;
		final int maxWidth = maxHeight * 2 / 3;
		Bitmap coverBitmap = null;
		final ZLImage cover = NetworkTree.createCover(mBook, false);
		if (cover != null) {
			ZLAndroidImageData data = null;
			final ZLAndroidImageManager mgr = (ZLAndroidImageManager)ZLAndroidImageManager.Instance();
			if (cover instanceof ZLImageProxy) {
				final ZLImageProxy img = (ZLImageProxy)cover;
				img.startSynchronization(mImageSynchronizer, new Runnable() {
					public void run() {
						if (img instanceof NetworkImage) {
							((NetworkImage)img).synchronizeFast();
						}
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

	private void addMenuItem(Menu menu, int index, String label, boolean showAsAction) {
		final MenuItem item = menu.add(0, index, Menu.NONE, label);
		item.setShowAsAction(
				showAsAction ? MenuItem.SHOW_AS_ACTION_IF_ROOM : MenuItem.SHOW_AS_ACTION_NEVER
		);
	}

	private void updateView() {
		final View rootView = findViewById(R.id.network_book_root);
		rootView.invalidate();
		rootView.requestLayout();
		invalidateOptionsMenu();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		OrientationUtil.setOrientation(this, intent);
	}

	@Override
	protected void onStop() {
		mConnection.unbind(this);

		NetworkLibrary.Instance().removeChangeListener(this);

		super.onStop();
	}

	public void onLibraryChanged(NetworkLibrary.ChangeListener.Code code, Object[] params) {
		if (code == NetworkLibrary.ChangeListener.Code.InitializationFailed) {
			// TODO: implement
			return;
		}

		if (mBook == null || mTree == null) {
			return;
		}

		runOnUiThread(new Runnable() {
			public void run() {
				updateView();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mTree != null) {
			for (final NetworkBookActions.NBAction a : NetworkBookActions.getContextMenuActions(this, mTree, mBookCollection, mConnection)) {
				addMenuItem(menu, a.Code, a.getContextLabel(null), a.ShowAsAction);
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		for (final NetworkBookActions.NBAction a : NetworkBookActions.getContextMenuActions(this, mTree, mBookCollection, mConnection)) {
			if (a.Code == item.getItemId()) {
				a.run(mTree);
				YotaNetworkBookInfoActivity.this.updateView();
				return true;
			}
		}
		return false;
	}
}
