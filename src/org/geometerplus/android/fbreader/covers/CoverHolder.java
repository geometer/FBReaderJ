/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader.covers;

import java.util.concurrent.Future;

import android.widget.ImageView;
import android.graphics.Bitmap;

import org.geometerplus.zlibrary.core.image.ZLImageProxy;

import org.geometerplus.fbreader.tree.FBTree;

class CoverHolder {
	private final CoverManager myManager;
	final ImageView CoverView;
	volatile FBTree.Key Key;

	private CoverSyncRunnable coverSyncRunnable;
	Future<?> coverBitmapTask;
	private Runnable coverBitmapRunnable;

	CoverHolder(CoverManager manager, ImageView coverView, FBTree.Key key) {
		myManager = manager;
		manager.setupCoverView(coverView);
		CoverView = coverView;
		Key = key;

		myManager.Cache.HoldersCounter++;
	}

	synchronized void setKey(FBTree.Key key) {
		if (!Key.equals(key)) {
			if (coverBitmapTask != null) {
				coverBitmapTask.cancel(true);
				coverBitmapTask = null;
			}
			coverBitmapRunnable = null;
		}
		Key = key;
	}

	class CoverSyncRunnable implements Runnable {
		private final ZLImageProxy myImage;
		private final FBTree.Key myKey;

		CoverSyncRunnable(ZLImageProxy image) {
			myImage = image;
			synchronized (CoverHolder.this) {
				myKey = Key;
				coverSyncRunnable = this;
			}
		}

		public void run() {
			synchronized (CoverHolder.this) {
				try {
					if (coverSyncRunnable != this) {
						return;
					}
					if (!Key.equals(myKey)) {
						return;
					}
					if (!myImage.isSynchronized()) {
						return;
					}
					myManager.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							synchronized (CoverHolder.this) {
								if (Key.equals(myKey)) {
									myManager.setCoverForView(CoverHolder.this, myImage);
								}
							}
						}
					});
				} finally {
					if (coverSyncRunnable == this) {
						coverSyncRunnable = null;
					}
				}
			}
		}
	}

	class CoverBitmapRunnable implements Runnable {
		private final ZLImageProxy myImage;
		private final FBTree.Key myKey;

		CoverBitmapRunnable(ZLImageProxy image) {
			myImage = image;
			synchronized (CoverHolder.this) {
				myKey = Key;
				coverBitmapRunnable = this;
			}
		}

		public void run() {
			synchronized (CoverHolder.this) {
				if (coverBitmapRunnable != this) {
					return;
				}
			}
			try {
				if (!myImage.isSynchronized()) {
					return;
				}
				final Bitmap coverBitmap = myManager.getBitmap(myImage);
				if (coverBitmap == null) {
					// If bitmap is null, then there's no image
					// and CoverView already has a stock image
					myManager.Cache.putBitmap(myKey, null);
					return;
				}
				if (Thread.currentThread().isInterrupted()) {
					// We have been cancelled
					return;
				}
				/*
				synchronized (CoverHolder.this) {
					// I'm not sure why, but cover bitmaps disappear all the time
					// So if by the time bitmap is generated holder has switched
					// to another key/tree, just scrap it, will retry later
					if (!Key.equals(myKey)) {
						return;
					}
				}
				*/
				myManager.Cache.putBitmap(myKey, coverBitmap);
				myManager.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						synchronized (CoverHolder.this) {
							if (Key.equals(myKey)) {
								CoverView.setImageBitmap(coverBitmap);
							}
						}
					}
				});
			} finally {
				synchronized (CoverHolder.this) {
					if (coverBitmapRunnable == this) {
						coverBitmapRunnable = null;
						coverBitmapTask = null;
					}
				}
			}
		}
	}
}
