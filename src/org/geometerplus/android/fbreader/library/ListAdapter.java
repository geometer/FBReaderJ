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

import android.graphics.Bitmap;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;

import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.library.*;

import org.geometerplus.android.fbreader.tree.ZLAndroidTree;

public class ListAdapter extends BaseAdapter implements View.OnCreateContextMenuListener {
	private final BaseActivity myActivity;
	private final List<FBTree> myItems;

	ListAdapter(BaseActivity activity, List<FBTree> items) {
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

	public void remove(final FBTree item) {
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				myItems.remove(item);
				notifyDataSetChanged();
			}
		});
	}

	public void add(final FBTree item) {
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				myItems.add(item);
				notifyDataSetChanged();
			}
		});
	}

	public void add(final int index, final FBTree item) {
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				myItems.add(index, item);
				notifyDataSetChanged();
			}
		});
	}

	public void addAll(final Collection<FBTree> items) {
		myActivity.runOnUiThread(new Runnable() {
			public void run() {
				myItems.addAll(items);
				notifyDataSetChanged();
			}
		});
	}

	@Override
	public int getCount() {
		return myItems.size();
	}

	@Override
	public FBTree getItem(int position) {
		return myItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public int getFirstSelectedItemIndex() {
		int index = 0;
		synchronized (myItems) {
			for (FBTree t : myItems) {
				if (myActivity.isTreeSelected(t)) {
					return index;
				}
				++index;
			}
		}
		return -1;
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

	private View createView(View convertView, ViewGroup parent, FBTree item) {
		final View view = (convertView != null) ?  convertView :
			LayoutInflater.from(parent.getContext()).inflate(R.layout.library_tree_item, parent, false);

        ((TextView)view.findViewById(R.id.library_tree_item_name)).setText(item.getName());
		((TextView)view.findViewById(R.id.library_tree_item_childrenlist)).setText(item.getSecondString());
		return view;
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		final FBTree tree = getItem(position);
		final View view = createView(convertView, parent, tree);
		if (myActivity.isTreeSelected(tree)) {
			view.setBackgroundColor(0xff555555);
		} else {
			view.setBackgroundColor(0);
		}

		final ImageView coverView = getCoverView(view);

		if (tree instanceof ZLAndroidTree) {
			coverView.setImageResource(((ZLAndroidTree)tree).getCoverResourceId());
		} else {
			final Bitmap coverBitmap = getCoverBitmap(tree.getCover());
			if (coverBitmap != null) {
				coverView.setImageBitmap(coverBitmap);
			} else if (tree instanceof AuthorTree) {
				coverView.setImageResource(R.drawable.ic_list_library_author);
			} else if (tree instanceof TagTree) {
				coverView.setImageResource(R.drawable.ic_list_library_tag);
			} else if (tree instanceof BookTree) {
				coverView.setImageResource(R.drawable.ic_list_library_book);
			} else if (tree instanceof FileItem) {
				coverView.setImageResource(((FileItem)tree).getIcon());
			} else {
				coverView.setImageResource(R.drawable.ic_list_library_books);
			}
		}

		return view;
	}

	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
		final FBTree tree = getItem(position);
		if (tree instanceof BookTree) {
			myActivity.createBookContextMenu(menu, ((BookTree)tree).Book);
		} else if (tree instanceof FileItem) {
			final Book book = ((FileItem)getItem(position)).getBook();
			if (book != null) {
				myActivity.createBookContextMenu(menu, book); 
			}
		}
	}
}
