package org.geometerplus.android.fbreader.library;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

public class GallerySketch extends Gallery {
	private GalleryAdapter myAdapter;
	private Context myContext;
	
	
	public GallerySketch(Context context) {
		super(context);
		myContext = context;
		setSpacing(20);
		
		GalleryAdapter galleryAdapter = new GalleryAdapter();
		setAdapter(galleryAdapter);
		setOnItemClickListener(galleryAdapter);
		setOnItemSelectedListener(galleryAdapter);
		
//		setScrollContainer(true);
//		setHorizontalScrollBarEnabled(true); // FIXME ???????????????? 
	}
	
	public GalleryAdapter getGalleryAdapter(){
		return myAdapter == null ? myAdapter = new GalleryAdapter() : myAdapter;
	}
	
	public class GalleryAdapter extends BaseAdapter implements OnItemClickListener, OnItemSelectedListener {
		private List<FileItem> myItems = new ArrayList<FileItem>();
		public int position = 0;
		
		public synchronized void clear() {
			myItems.clear();
		}

		public synchronized void add(FileItem item){
			myItems.add(item);
		}
		
		public synchronized void remove(FileItem fileItem) {
			myItems.remove(fileItem);
		}

        public int getCount() {
            return myItems.size();
        }

        public FileItem getItem(int position) {
            return myItems.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			this.position = position;
			if (myContext instanceof FileManager){
				((FileManager) myContext).runItem(myItems.get(position)); // TODO
			}
		}

		@Override
		public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
			view.setSelected(false);
			this.position = position;
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		
		}
		
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view = (convertView != null) ?  convertView :
    			LayoutInflater.from(parent.getContext()).inflate(R.layout.sketch_item, parent, false);
            
            final FileItem fileItem = getItem(position);

    		final int maxHeight = 200; // FIXME: hardcoded constant
    		final int maxWidth = maxHeight * 3 / 4;

            ImageView imageView = (ImageView)view.findViewById(R.id.sketch_item_image);
			imageView.getLayoutParams().height = maxHeight;
			imageView.getLayoutParams().width = maxWidth;
            
//            imageView.setImageResource(fileItem.getIcon());
//			imageView.setLayoutParams(new Gallery.LayoutParams(100, 200));
//			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            
            TextView nameTextView = (TextView)view.findViewById(R.id.sketch_item_name);
    		String name = fileItem.getName();
            name = name.length() > 10 ? name.substring(0, 10) : name; 
            nameTextView.setText(name);	// FIXME hardcoding style

    		TextView summaryTextView = (TextView)view.findViewById(R.id.sketch_item_summary); 
    		String summary = fileItem.getSummary() == null ? "" : fileItem.getSummary();
            summary = summary.length() > 10 ? summary.substring(0, 10) : summary; 
            summaryTextView.setText(summary);
            
    		final Bitmap coverBitmap = getBitmap(fileItem.getCover(), maxWidth, maxHeight);
			if (coverBitmap != null) {
				imageView.setImageBitmap(coverBitmap);
			} else {
				imageView.setImageResource(fileItem.getIcon());
			}
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            
            return view;
        }
        
    	private Bitmap getBitmap(ZLImage cover, int maxWidth, int maxHeight) {
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
}
