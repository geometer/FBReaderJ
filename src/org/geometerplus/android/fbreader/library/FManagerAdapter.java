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

public class FManagerAdapter extends ArrayAdapter<FileOrder>{
	private List<FileOrder> myOrders;
	private Context myParent;
	
	public FManagerAdapter(Context context, List<FileOrder> orders, int textViewResourceId) {
		super(context, textViewResourceId);
		myParent = context;
		myOrders = orders;
	}

	private int myCoverWidth = -1;
	private int myCoverHeight = -1;
	private Runnable myInvalidateViewsRunnable = new Runnable() {
		public void run() {
			((ListActivity)myParent).getListView().invalidateViews();
		}
	};

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final View view = (convertView != null) ?  convertView :
			LayoutInflater.from(parent.getContext()).inflate(R.layout.library_tree_item, parent, false);

		FileOrder order = myOrders.get(position);
		if (order != null) {
			((TextView)view.findViewById(R.id.library_tree_item_name)).setText(order.getName());
			((TextView)view.findViewById(R.id.library_tree_item_childrenlist)).setText(order.getPath());

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
			
			if(order.getBook() == null)
				coverView.setImageResource(R.drawable.ic_list_library_folder);
			else{
				Log.v(FileManager.LOG, "isBook");
				Book book = order.getBook();
				FormatPlugin plugin = PluginCollection.Instance().getPlugin(book.File);
				Bitmap coverBitmap = null;
				ZLImage cover = plugin.readCover(book);
				if (cover != null) {
					Log.v(FileManager.LOG, "cover != null");
					
					ZLAndroidImageData data = null;
					final ZLAndroidImageManager mgr = (ZLAndroidImageManager)ZLAndroidImageManager.Instance();
					if (cover instanceof ZLLoadableImage) {
						Log.v(FileManager.LOG, "cover != null");
						
						final ZLLoadableImage img = (ZLLoadableImage)cover;
						if (img.isSynchronized()) {
							data = mgr.getImageData(img);
						} else {
							ZLAndroidImageLoader.Instance().startImageLoading(img, myInvalidateViewsRunnable);
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
				}else{
					coverView.setImageResource(R.drawable.ic_list_library_book);
				}
			}
        }
        return view;
	}
}


class FileOrder{
	private String myName;
	private String myPath;
	private int myIcon;
	private Book myBook = null; 
	
	public FileOrder(String name, String path, int icon){
		myName = name;
		myPath = path;
		myIcon = icon;
	}

	public FileOrder(ZLFile file){
		myPath = file.getPath();
		
		if (file.isDirectory() || file.isArchive()){
			myName = file.getName(false).substring(file.getName(false).lastIndexOf('/') + 1);
			myIcon = R.drawable.ic_list_library_folder;
		}
		else if(PluginCollection.Instance().getPlugin(file) != null){
			myBook = Book.getByFile(file);
			myName = myBook.getTitle();
			myIcon = -1;
		}
	}

	public String getName() {
		return myName;
	}

	public String getPath() {
		return myPath;
	}

	public int getIcon() {
		return myIcon;
	}

	public Book getBook(){
		return myBook;
	}
}
