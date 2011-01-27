package org.geometerplus.android.fbreader.library;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.android.fbreader.tree.ZLAndroidTree;
import org.geometerplus.fbreader.library.AuthorTree;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.BookTree;
import org.geometerplus.fbreader.library.LibraryTree;
import org.geometerplus.fbreader.library.TagTree;
import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

abstract class FMBaseAdapter extends BaseAdapter implements View.OnCreateContextMenuListener {
	protected List<FileItem> myItems = new ArrayList<FileItem>();

	public synchronized void clear() {
		myItems.clear();
	}

	public synchronized void add(FileItem item){
		myItems.add(item);
	}
	
	public synchronized void remove(FileItem fileItem) {
		myItems.remove(fileItem);
	}

	public synchronized void deleteFile(ZLFile file) {
		for (FileItem item : myItems) {
			if (file.equals(item.getFile())) {
				myItems.remove(item);
				break;
			}
		}
	}

	public synchronized int getCount() {
		return myItems.size();
	}

	public synchronized FileItem getItem(int position) {
		return myItems.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);
	
	@Override
	public abstract void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo);
}

abstract class LibraryBaseAdapter extends BaseAdapter implements View.OnCreateContextMenuListener {
	protected final List<FBTree> myItems;

	public LibraryBaseAdapter(List<FBTree> items) {
		myItems = items;
	}

	public final int getCount() {
		return myItems.size();
	}

	public final FBTree getItem(int position) {
		return myItems.get(position);
	}

	public final long getItemId(int position) {
		return position;
	}

	public abstract int getFirstSelectedItemIndex(); 

	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);
	
	@Override
	public abstract void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo);
}

class GalleryAdapterUtil {
	public static View getView(View convertView, ViewGroup parent,
			String summary, ZLImage cover, int idIcon,
			int maxHeight, int maxWidth, int paddingTop){
        
		final View view = (convertView != null) ?  convertView :
			LayoutInflater.from(parent.getContext()).inflate(R.layout.sketch_item, parent, false);
        
		ImageView imageView = (ImageView)view.findViewById(R.id.sketch_item_image);
		imageView.setPadding(0, paddingTop, 0, 0);
		imageView.getLayoutParams().height = maxHeight;
		imageView.getLayoutParams().width = maxWidth;

		final Bitmap coverBitmap = getBitmap(cover , maxWidth, maxHeight);
		if (coverBitmap != null) {
			imageView.setImageBitmap(coverBitmap);
		} else {
			imageView.setImageResource(idIcon);
		}
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		TextView summaryTextView = (TextView)view.findViewById(R.id.sketch_item_summary); 
		summaryTextView.setText(summary);
        return view;
	}
	
	private static Bitmap getBitmap(ZLImage cover, int maxWidth, int maxHeight) {
		if (cover instanceof ZLLoadableImage) {
			final ZLLoadableImage loadableImage = (ZLLoadableImage)cover;
			if (!loadableImage.isSynchronized()) {
				loadableImage.synchronize();
			}
		}
		final ZLAndroidImageData data =
			((ZLAndroidImageManager)ZLAndroidImageManager.Instance()).getImageData(cover);
		if (data == null) {
			return null;
		}

		final Bitmap coverBitmap = data.getBitmap(2 * maxWidth, 2 * maxHeight);
		return coverBitmap;
	}	
}
