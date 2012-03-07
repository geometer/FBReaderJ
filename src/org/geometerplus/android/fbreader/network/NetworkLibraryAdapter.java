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
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;

import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.tree.*;
import org.geometerplus.fbreader.tree.FBTree.Key;

import org.geometerplus.android.fbreader.tree.TreeAdapter;

import org.geometerplus.android.fbreader.network.action.NetworkBookActions;

class NetworkLibraryAdapter extends TreeAdapter {
	NetworkLibraryAdapter(NetworkLibraryActivity activity) {
		super(activity);
	}

	private int myCoverWidth = -1;
	private int myCoverHeight = -1;

	private volatile int numViewHolders = 0;
	private final static class ViewHolder
	{
		private Key key;
		private NetworkTree tree;
		private TextView nameView;
		private TextView summaryView;
		private ImageView coverView;
		private ImageView statusView;
		private Runnable coverSyncRunnable;
		private Future<?> coverBitmapTask;
		private Runnable coverBitmapRunnable;
	}

	@SuppressWarnings("serial")
	private final Map<Key, Bitmap> myCachedBitmaps =
			Collections.synchronizedMap(new LinkedHashMap<Key, Bitmap>(10, 0.75f, true) {
				@Override
				protected boolean removeEldestEntry(Entry<Key, Bitmap> eldest) {
					return size() > numViewHolders;
				}
			});

	// Copied from ZLAndroidImageLoader
	private static class MinPriorityThreadFactory implements ThreadFactory {
		private final ThreadFactory myDefaultThreadFactory = Executors.defaultThreadFactory();

		public Thread newThread(Runnable r) {
			final Thread th = myDefaultThreadFactory.newThread(r);
			th.setPriority(Thread.MIN_PRIORITY);
			return th;
		}
	}
	private static final int IMAGE_RESIZE_THREADS_NUMBER = 1; // TODO: how many threads ???
	private final ExecutorService myPool = Executors.newFixedThreadPool(IMAGE_RESIZE_THREADS_NUMBER, new MinPriorityThreadFactory());

	private final class CoverBitmapRunnable implements Runnable {
		private final ViewHolder myHolder;
		private final ZLLoadableImage myImage;
		private final int myWidth;
		private final int myHeight;
		private final Key myKey;
		private final NetworkTree myTree;

		CoverBitmapRunnable(ViewHolder holder, ZLLoadableImage image, int width, int height) {
			myHolder = holder;
			myImage = image;
			myWidth = width;
			myHeight = height;
			synchronized(holder) {
				myKey = holder.key;
				myTree = holder.tree;
				holder.coverBitmapRunnable = this;
			}
		}

		public void run() {
			synchronized(myHolder) {
				if (myHolder.coverBitmapRunnable != this)
					return;
			}
			try {
				if (!myImage.isSynchronized())
					return;
				final ZLAndroidImageManager mgr = (ZLAndroidImageManager)ZLAndroidImageManager.Instance();
				final ZLAndroidImageData data = mgr.getImageData(myImage);
				if (data == null)
					return;
				final Bitmap coverBitmap = data.getBitmap(2 * myWidth, 2 * myHeight);
				if (coverBitmap == null)
					// If bitmap is null, then there's no image
					// and coverView already has a stock image
					return;
				if (Thread.currentThread().isInterrupted())
					// We have been cancelled
					return;
				synchronized(myHolder) {
					// I'm not sure why, but cover bitmaps disappear all the time
					// So if by the time bitmap is generated holder has switched
					// to another key/tree, just scrap it, will retry later
					if (myHolder.key != myKey || myHolder.tree != myTree)
						return;
				}
				myCachedBitmaps.put(myKey, coverBitmap);
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						synchronized(myHolder) {
							if (myHolder.key == myKey && myHolder.tree == myTree) {
								myHolder.coverView.setImageBitmap(coverBitmap);
							}
						}
					}
				});
			} finally {
				synchronized(myHolder) {
					if (myHolder.coverBitmapRunnable == this) {
						myHolder.coverBitmapRunnable = null;
						myHolder.coverBitmapTask = null;
					}
				}
			}
		}
	}

	private void startUpdateCover(ViewHolder holder, ZLLoadableImage image, int width, int height)
	{
		synchronized(holder) {
			Bitmap coverBitmap = myCachedBitmaps.get(holder.key);
			if (coverBitmap != null) {
				holder.coverView.setImageBitmap(coverBitmap);
				return;
			}
			if (holder.coverBitmapTask == null) {
				holder.coverBitmapTask = myPool.submit(new CoverBitmapRunnable(holder, image, width, height));
			}
		}
	}

	private final class CoverSyncRunnable implements Runnable {
		private final ViewHolder myHolder;
		private final ZLLoadableImage myImage;
		private final int myWidth;
		private final int myHeight;
		private final Key myKey;
		private final NetworkTree myTree;

		CoverSyncRunnable(ViewHolder holder, ZLLoadableImage image, int width, int height) {
			myHolder = holder;
			myImage = image;
			myWidth = width;
			myHeight = height;
			synchronized(holder) {
				myKey = holder.key;
				myTree = holder.tree;
				holder.coverSyncRunnable = this;
			}
		}

		public void run() {
			synchronized (myHolder) {
				try {
					if (myHolder.coverSyncRunnable != this)
						return;
					if (myHolder.key != myKey || myHolder.tree != myTree)
						return;
					if (!myImage.isSynchronized())
						return;
					if (myCachedBitmaps.containsKey(myHolder.key))
						return;
					final ZLAndroidImageManager mgr = (ZLAndroidImageManager)ZLAndroidImageManager.Instance();
					final ZLAndroidImageData data = mgr.getImageData(myImage);
					if (data == null)
						return;
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							synchronized(myHolder) {
								if (myHolder.key != myKey || myHolder.tree != myTree)
									return;
								startUpdateCover(myHolder, myImage, myWidth, myHeight);
							}
						}
					});
				} finally {
					if (myHolder.coverSyncRunnable == this)
						myHolder.coverSyncRunnable = null;
				}
			}
		}
	}

	public View getView(int position, View view, final ViewGroup parent) {
		final NetworkTree tree = (NetworkTree)getItem(position);
		if (tree == null) {
			throw new IllegalArgumentException("tree == null");
		}
		final ViewHolder holder;
		if (view == null)
		{
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.network_tree_item, parent, false);
			if (myCoverWidth == -1) {
				view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				myCoverHeight = view.getMeasuredHeight();
				myCoverWidth = myCoverHeight * 15 / 32;
				view.requestLayout();
			}
			holder = new ViewHolder();
			holder.nameView = (TextView)view.findViewById(R.id.network_tree_item_name);
			holder.summaryView = (TextView)view.findViewById(R.id.network_tree_item_childrenlist);
			holder.coverView = (ImageView)view.findViewById(R.id.network_tree_item_icon);
			holder.coverView.getLayoutParams().width = myCoverWidth;
			holder.coverView.getLayoutParams().height = myCoverHeight;
			holder.coverView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			holder.coverView.requestLayout();
			holder.statusView = (ImageView)view.findViewById(R.id.network_tree_item_status);
			view.setTag(holder);
			numViewHolders++;
		} else {
			holder = (ViewHolder)view.getTag();
		}

		synchronized(holder) {
			if (holder.tree != tree) {
				if (holder.coverBitmapTask != null) {
					holder.coverBitmapTask.cancel(true);
					holder.coverBitmapTask = null;
				}
				holder.coverBitmapRunnable = null;
			}
			holder.key = tree.getUniqueKey();
			holder.tree = tree;
		}

		holder.nameView.setText(tree.getName());
		holder.summaryView.setText(tree.getSummary());

		setupCover(holder, tree, myCoverWidth, myCoverWidth);

		final int status = (tree instanceof NetworkBookTree)
			? NetworkBookActions.getBookStatus(
				((NetworkBookTree)tree).Book,
				((NetworkLibraryActivity)getActivity()).Connection
			  )
			: 0;
		if (status != 0) {
			holder.statusView.setVisibility(View.VISIBLE);
			holder.statusView.setImageResource(status);
		} else {
			holder.statusView.setVisibility(View.GONE);
		}
		holder.statusView.requestLayout();

		return view;
	}

	private void setupCover(final ViewHolder holder, NetworkTree tree, int width, int height) {
		Bitmap coverBitmap = myCachedBitmaps.get(holder.key);
		final ZLImage cover = tree.getCover();
		if (coverBitmap == null && cover != null) {
			ZLLoadableImage img = null;
			ZLAndroidImageData data = null;
			final ZLAndroidImageManager mgr = (ZLAndroidImageManager)ZLAndroidImageManager.Instance();
			if (cover instanceof ZLLoadableImage) {
				img = (ZLLoadableImage)cover;
				if (img.isSynchronized()) {
					data = mgr.getImageData(img);
					if (data != null)
						startUpdateCover(holder, img, width, height);
				} else {
					img.startSynchronization(new CoverSyncRunnable(holder, img, width, height));
				}
			} else {
				data = mgr.getImageData(cover);
				if (data != null)
					coverBitmap = data.getBitmap(2 * width, 2 * height);
			}
		}
		if (coverBitmap != null) {
			holder.coverView.setImageBitmap(coverBitmap);
		} else if (tree instanceof NetworkBookTree) {
			holder.coverView.setImageResource(R.drawable.ic_list_library_book);
		} else if (tree instanceof SearchCatalogTree) {
			holder.coverView.setImageResource(R.drawable.ic_list_library_search);
		} else if (tree instanceof BasketCatalogTree) {
			holder.coverView.setImageResource(R.drawable.ic_list_library_basket);
		} else if (tree instanceof AddCustomCatalogItemTree) {
			holder.coverView.setImageResource(R.drawable.ic_list_plus);
		} else {
			holder.coverView.setImageResource(R.drawable.ic_list_library_books);
		}
	}
}
