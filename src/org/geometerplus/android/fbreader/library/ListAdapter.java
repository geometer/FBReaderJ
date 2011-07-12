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

package org.geometerplus.android.fbreader.library;

import java.util.*;

import android.app.ListActivity;
import android.graphics.Bitmap;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;

import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.tree.FBTree;

public abstract class ListAdapter<T> extends BaseAdapter implements View.OnCreateContextMenuListener {
	private final ListActivity myActivity;
	protected final List<T> myItems;

	ListAdapter(ListActivity activity, List<T> items) {
		myActivity = activity;
		myItems = Collections.synchronizedList(items);
	}

	public void clear() {
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				myItems.clear();
			}
		});
	}

	public void remove(final T item) {
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				myItems.remove(item);
				notifyDataSetChanged();
			}
		});
	}

	public void add(final T item) {
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				myItems.add(item);
				notifyDataSetChanged();
			}
		});
	}

	@Override
	public int getCount() {
		return myItems.size();
	}

	@Override
	public T getItem(int position) {
		return myItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	protected Bitmap getCoverBitmap(ZLImage cover) {
		if (cover == null) {
			return null;
		}

		ZLAndroidImageData data = null;
		final ZLAndroidImageManager mgr = (ZLAndroidImageManager)ZLAndroidImageManager.Instance();
		if (cover instanceof ZLLoadableImage) {
			final ZLLoadableImage img = (ZLLoadableImage)cover;
			if (img.isSynchronized()) {
				data = mgr.getImageData(img);
			} else {
				img.startSynchronization(myInvalidateViewsRunnable);
			}
		} else {
			data = mgr.getImageData(cover);
		}
		return data != null ? data.getBitmap(2 * myCoverWidth, 2 * myCoverHeight) : null;
	}

	private int myCoverWidth = -1;
	private int myCoverHeight = -1;
	private final Runnable myInvalidateViewsRunnable = new Runnable() {
		public void run() {
			myActivity.getListView().invalidateViews();
		}
	};

	protected ImageView getCoverView(View parent) {
		if (myCoverWidth == -1) {
			parent.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			myCoverHeight = parent.getMeasuredHeight();
			myCoverWidth = myCoverHeight * 15 / 32;
			parent.requestLayout();
		}

		final ImageView coverView = (ImageView)parent.findViewById(R.id.library_tree_item_icon);
		coverView.getLayoutParams().width = myCoverWidth;
		coverView.getLayoutParams().height = myCoverHeight;
		coverView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		coverView.requestLayout();
		return coverView;
	}

	protected View createView(View convertView, ViewGroup parent, FBTree item) {
		final View view = (convertView != null) ?  convertView :
			LayoutInflater.from(parent.getContext()).inflate(R.layout.library_tree_item, parent, false);

        ((TextView)view.findViewById(R.id.library_tree_item_name)).setText(item.getName());
		((TextView)view.findViewById(R.id.library_tree_item_childrenlist)).setText(item.getSecondString());
		return view;
	}
}
