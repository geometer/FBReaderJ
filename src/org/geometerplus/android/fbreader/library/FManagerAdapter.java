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

import java.util.List;

import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageLoader;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

import android.R.bool;
import android.app.ListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FManagerAdapter extends ArrayAdapter<FileItem> {
	private List<FileItem> myItems;
	private Context myParent;
	
	public FManagerAdapter(Context context, List<FileItem> items, int textViewResourceId) {
		super(context, textViewResourceId);
		myParent = context;
		myItems = items;
	}

	private int myCoverWidth = -1;
	private int myCoverHeight = -1;
	private Runnable myInvalidateViewsRunnable = new Runnable() {
		public void run() {
			System.err.println("run myInvalidateViewsRunnable");
			((ListActivity)myParent).getListView().invalidateViews();
		}
	};

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view = (convertView != null) ?  convertView :
			LayoutInflater.from(parent.getContext()).inflate(R.layout.library_tree_item, parent, false);
        
        FileItem item = myItems.get(position);
        if (item != null) {
        	((TextView)view.findViewById(R.id.library_tree_item_name)).setText(item.getName());
			((TextView)view.findViewById(R.id.library_tree_item_childrenlist)).setText(item.getPath());

			if (myCoverWidth == -1) {
				view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				myCoverHeight = view.getMeasuredHeight();
				myCoverWidth = myCoverHeight * 15 / 32;
				view.requestLayout();
			}

			final ImageView coverView = (ImageView)view.findViewById(R.id.library_tree_item_icon);
			coverView.getLayoutParams().width = myCoverWidth;
			coverView.getLayoutParams().height = myCoverHeight;
			coverView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			coverView.requestLayout();
			
			final ZLImage cover = item.getCover();
			Bitmap coverBitmap = null;
			if (cover != null) {
				System.err.println("cover != null");
				
				ZLAndroidImageData data = null;
				final ZLAndroidImageManager mgr =
					(ZLAndroidImageManager)ZLAndroidImageManager.Instance();

				if (cover instanceof ZLLoadableImage) {
					System.err.println("cover instanceof ZLLoadableImage");
					final ZLLoadableImage loadableImage = (ZLLoadableImage)cover;
					if (loadableImage.isSynchronized()) {
						data = mgr.getImageData(loadableImage);
					} else {
						System.err.println("startImageLoading");
						ZLAndroidImageLoader.Instance().startImageLoading(loadableImage, myInvalidateViewsRunnable);
					}
				} else {
					data = mgr.getImageData(cover);
				}
				if (data != null) {
					Log.v(FileManager.LOG, "data != null");
					coverBitmap = data.getBitmap(2 * myCoverWidth, 2 * myCoverHeight);
				}
			}

			if (coverBitmap != null) {
				coverView.setImageBitmap(coverBitmap);
			} else {
				coverView.setImageResource(item.getIcon());
			}
        }
        return view;
	}
}


class FileItem {
	private final ZLFile myFile;
	private final String myName;

	private Book myBook = null; 
	private boolean myBookIsInitialized = false;
	private ZLImage myCover = null;
	private boolean myCoverIsInitialized = false;
	
	public FileItem(ZLFile file, String name) {
		myFile = file;
		myName = name;
	}
	
	public FileItem(ZLFile file) {
		this(file, null);
	}
	
	public String getName() {
		if (myName != null) {
			return myName;
		}

		if (myFile.isDirectory()) {
			final String fileName = myFile.getName(false);
			return fileName.substring(fileName.lastIndexOf('/') + 1);
		}

		final Book book = getBook();
		if (book != null) {
			return book.getTitle();
		}

		if (!myFile.isArchive()) {
			System.err.println(
				"File " + myFile.getPath() +
				" that is not a directory, not a book and not a archive " +
				"has been found in getIcon()"
			);
		}

		final String fileName = myFile.getName(false);
		return fileName.substring(fileName.lastIndexOf('/') + 1);
	}

	public String getPath() {
		return myFile.getPath();
	}

	public int getIcon() {
		if (myFile.isDirectory()) {
			return R.drawable.ic_list_library_folder;
		} else if (PluginCollection.Instance().getPlugin(myFile) != null) {
			return R.drawable.ic_list_library_book;
		} else if (myFile.isArchive()) {
			return R.drawable.ic_list_library_folder;
		} else {
			System.err.println(
				"File " + myFile.getPath() +
				" that is not a directory, not a book and not a archive " +
				"has been found in getIcon()"
			);
			return R.drawable.ic_list_library_book;
		}
	}

	public ZLImage getCover() {
		if (!myCoverIsInitialized) {
			myCoverIsInitialized = true;
			final Book book = getBook();
			final FormatPlugin plugin = PluginCollection.Instance().getPlugin(myFile);
			if (book != null && plugin != null) {
				myCover = plugin.readCover(book);
			}
		}
		return myCover;
	}

	private Book getBook() {
		if (!myBookIsInitialized) {
			myBookIsInitialized = true;
			myBook = Book.getByFile(myFile);
		}
		return myBook;
	}
}
