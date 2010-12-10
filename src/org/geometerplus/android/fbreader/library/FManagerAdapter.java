package org.geometerplus.android.fbreader.library;

import java.util.List;

import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.ui.android.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FManagerAdapter extends ArrayAdapter<FileOrder>{
	private List<FileOrder> myOrders;
	
	public FManagerAdapter(Context context, List<FileOrder> orders, int textViewResourceId) {
		super(context, textViewResourceId);
		myOrders = orders;
	}

	private int myCoverWidth = -1;
	private int myCoverHeight = -1;

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

        	coverView.setImageResource(order.getIcon());
        }
        return view;
	}
}

class FileOrder{
	private String myName;
	private String myPath;
	private int myIcon;
	
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
			myIcon = R.drawable.ic_list_library_book;
			Book book = Book.getByFile(file);
			myName = book.getTitle();
			FormatPlugin plugin = PluginCollection.Instance().getPlugin(file);
			ZLImage image = plugin.readCover(book);
		
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
}
