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

package org.geometerplus.android.fbreader.library;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.tree.FBTree;

import org.geometerplus.zlibrary.ui.android.R;

public class LibraryBaseActivity extends ListActivity {
	private final ZLResource myResource = ZLResource.resource("libraryView");

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setListAdapter(new LibraryAdapter());
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long rowId) {
	}

	private final class LibraryAdapter extends BaseAdapter {
		private final ArrayList<TopLevelTree> myItems = new ArrayList<TopLevelTree>();

		public LibraryAdapter() {
			myItems.add(new TopLevelTree(myResource.getResource("searchResults")));
			myItems.add(new TopLevelTree(myResource.getResource("recent")));
			myItems.add(new TopLevelTree(myResource.getResource("byAuthor")));
			myItems.add(new TopLevelTree(myResource.getResource("byTag")));
			myItems.add(new TopLevelTree(myResource.getResource("fileTree")));
		}

		@Override
		public final int getCount() {
			return myItems.size();
		}

		@Override
		public final FBTree getItem(int position) {
			return myItems.get(position);
		}

		@Override
		public final long getItemId(int position) {
			return position;
		}

		//private ZLImage myFBReaderIcon =
		//	ZLAndroidLibrary.Instance().createImage(R.drawable.fbreader);

		private int myCoverWidth = -1;
		private int myCoverHeight = -1;

		@Override
		public View getView(int position, View convertView, final ViewGroup parent) {
			final FBTree tree = getItem(position);
			final View view = (convertView != null) ?  convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.library_ng_tree_item, parent, false);

			((TextView)view.findViewById(R.id.library_ng_tree_item_name)).setText(tree.getName());
			((TextView)view.findViewById(R.id.library_ng_tree_item_childrenlist)).setText(tree.getSecondString());

			if (myCoverWidth == -1) {
				view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				myCoverHeight = view.getMeasuredHeight();
				myCoverWidth = myCoverHeight * 15 / 32;
				view.requestLayout();
			}

			final ImageView coverView = (ImageView)view.findViewById(R.id.library_ng_tree_item_icon);
			coverView.getLayoutParams().width = myCoverWidth;
			coverView.getLayoutParams().height = myCoverHeight;
			coverView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			coverView.requestLayout();
			coverView.setImageResource(R.drawable.fbreader);
			//setupCover(coverView, tree, myCoverWidth, myCoverWidth);

			return view;
		}
	}

	/*
	private void setupCover(final ImageView coverView, FBTree tree, int width, int height) {
		Bitmap coverBitmap = null;
		ZLImage cover = tree.getCover();
		if (cover == null) { 
			cover = myFBReaderIcon;
		}
		if (cover != null) {
			ZLAndroidImageData data = null;
			final ZLAndroidImageManager mgr = (ZLAndroidImageManager) ZLAndroidImageManager.Instance();
			data = mgr.getImageData(cover);
			if (data != null) {
				coverBitmap = data.getBitmap(2 * width, 2 * height);
			}
		}
		if (coverBitmap != null) {
			coverView.setImageBitmap(coverBitmap);
		} else {
			coverView.setImageDrawable(null);
		}
	}
	*/
}

class TopLevelTree extends FBTree {
	private final ZLResource myResource;

	public TopLevelTree(ZLResource resource) {
		myResource = resource;
	}

	@Override
	public String getName() {
		return myResource.getValue();
	}

	@Override
	public String getSummary() {
		return myResource.getResource("summary").getValue();
	}
}
