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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;

import org.geometerplus.android.fbreader.network.action.ActionCode;
import org.geometerplus.fbreader.network.urlInfo.BookBuyUrlInfo;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import com.yotadevices.yotaphone2.yotareader.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.network.SQLiteCookieDatabase;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.opds.OPDSBookItem;
import org.geometerplus.fbreader.network.tree.NetworkBookTree;

import org.geometerplus.android.fbreader.OrientationUtil;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.network.action.NetworkBookActions;
import org.geometerplus.android.fbreader.network.auth.ActivityNetworkContext;
import org.geometerplus.android.fbreader.util.AndroidImageSynchronizer;
import org.geometerplus.android.util.UIUtil;

import java.util.LinkedList;
import java.util.List;

public class YotaNetworkBookInfoActivity extends Activity implements NetworkLibrary.ChangeListener {
	private NetworkBookTree mTree;
	private NetworkBookItem mBook;

	private final ZLResource mResource = ZLResource.resource("bookInfo");
	private final BookCollectionShadow mBookCollection = new BookCollectionShadow();
	private final BookDownloaderServiceConnection mConnection = new BookDownloaderServiceConnection();

	private final AndroidImageSynchronizer mImageSynchronizer = new AndroidImageSynchronizer(this);

	private final ActivityNetworkContext mNetworkContext = new ActivityNetworkContext(this);
	private List<NetworkBookActions.NBAction> mBoookActions;

	private View.OnClickListener mActionButtonListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int actionCode = (Integer)v.getTag();
			for (NetworkBookActions.NBAction action : mBoookActions) {
				if (action.getActionCode() == actionCode) {
					action.run(mTree);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		mBookCollection.bindToService(this, null);

		SQLiteCookieDatabase.init(this);

		setContentView(R.layout.yota_network_book_info);
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		bar.setLogo(new ColorDrawable(Color.WHITE));
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setTitle(R.string.network_library);
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

				runOnUiThread(mViewInitializer);
			}
		}
	};

	private final Runnable mViewInitializer = new Runnable() {
		public void run() {
			if (mBook == null) {
				finish();
			} else {
				setupDescription();
				setupInfo();
				setupCover();
				initBookActions();
				initButtons();
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
		CharSequence description = mBook.getSummary();
		if (description == null) {
			description = mResource.getResource("noDescription").getValue();
		}
		final TextView descriptionView = (TextView)findViewById(R.id.yota_book_description);
		descriptionView.setText(description);
		descriptionView.setMovementMethod(new LinkMovementMethod());
	}

	private void setupInfo() {
		TextView title = (TextView)findViewById(R.id.yota_book_title);
		title.setText(mBook.Title);
		TextView author = (TextView)findViewById(R.id.yota_book_author);
		if (mBook.Authors.size() > 0) {
			final StringBuilder buffer = new StringBuilder();
			for (NetworkBookItem.AuthorData data : mBook.Authors) {
				if (buffer.length() > 0) {
					buffer.append(", ");
				}
				buffer.append(data.DisplayName);
			}
			author.setText(buffer.toString());
		} else {
			author.setVisibility(View.GONE);
		}

		if (mBook.Tags.size() > 0) {
			final StringBuilder tagsText = new StringBuilder();
			for (String tag : mBook.Tags) {
				if (tagsText.length() > 0) {
					tagsText.append(", ");
				}
				tagsText.append(tag);
			}
			TextView genreTitle = (TextView)findViewById(R.id.yota_book_genre_title);
			genreTitle.setVisibility(View.VISIBLE);
			TextView genre = (TextView)findViewById(R.id.yota_book_genre);
			genre.setText(tagsText.toString());
			genre.setVisibility(View.VISIBLE);
		}
	}

	private final void setupCover() {
		final ImageView coverView = (ImageView)findViewById(R.id.yota_book_cover);

		coverView.setVisibility(View.GONE);
		coverView.setImageDrawable(null);

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
		final View rootView = findViewById(R.id.root_view);
		rootView.invalidate();
		rootView.requestLayout();

		initBookActions();
		initButtons();
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

	private void initBookActions() {
		if (mTree != null) {
			mBoookActions = NetworkBookActions.getContextMenuActions(this, mTree, mBookCollection, mConnection);
		}
	}

	private void initButtons() {
		Button mActionButton = (Button)findViewById(R.id.yota_book_add_button);
		if (mBoookActions.size() == 0) {
			mActionButton.setVisibility(View.INVISIBLE);
		}
		for (NetworkBookActions.NBAction action : mBoookActions) {
			int actionCode = action.getActionCode();
			if (actionCode == ActionCode.BUY_DIRECTLY || actionCode == ActionCode.BUY_IN_BROWSER) {
				mActionButton.setTag(actionCode);
				final BookBuyUrlInfo reference = mTree.Book.buyInfo();
				final String priceString = reference.Price != null ? String.valueOf(reference.Price) : "";
				mActionButton.setText(getString(R.string.buy_book)+" "+priceString);
				break;
			}
			if (actionCode == ActionCode.DOWNLOAD_BOOK) {
				mActionButton.setTag(actionCode);
				mActionButton.setText(getString(R.string.download_book));
				break;
			}
			if (actionCode == ActionCode.READ_BOOK) {
				mActionButton.setTag(actionCode);
				mActionButton.setText(getString(R.string.read_book));
				break;
			}
		}
		mActionButton.setOnClickListener(mActionButtonListener);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
