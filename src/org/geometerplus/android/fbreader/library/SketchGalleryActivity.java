package org.geometerplus.android.fbreader.library;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geometerplus.android.fbreader.SQLiteBooksDatabase;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.library.Book;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class SketchGalleryActivity extends BaseGalleryActivity {
	public static String LOG = "FileManager";
	
//	public static String FILE_MANAGER_INSERT_MODE = "FileManagerInsertMode";
	
	private static final int DELETE_FILE_ITEM_ID = 10;
//	private static final int RENAME_FILE_ITEM_ID = 11; //FIXME delete later
	private static final int MOVE_FILE_ITEM_ID = 12;
	
	private String myPath;
//	private String myInsertPath;
//	public static String myInsertPathStatic;
//	public static SortType mySortType;
//	public static ViewType myViewType;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DatabaseInstance = SQLiteBooksDatabase.Instance();
		if (DatabaseInstance == null) {
			DatabaseInstance = new SQLiteBooksDatabase(this, "LIBRARY");
		}
		if (LibraryInstance == null) {
			LibraryInstance = new Library();
			startService(new Intent(getApplicationContext(), InitializationService.class));
		}

		GalleryAdapter galleryAdapter = new GalleryAdapter(); 
		myGallery.setAdapter(galleryAdapter);
		myGallery.setOnItemClickListener(galleryAdapter);
		myGallery.setOnItemSelectedListener(galleryAdapter);
		
		myPath = getIntent().getStringExtra(FileManager.FILE_MANAGER_PATH);
//		myInsertPath = getIntent().getStringExtra(FILE_MANAGER_INSERT_MODE);
		FileManager.mySortType = SortingDialog.getOprionSortType();
		FileManager.myViewType = ViewChangeDialog.getOprionViewType(); 

		if (myPath == null) {
			addItem(Paths.BooksDirectoryOption().getValue(), "fileTreeLibrary");
			addItem(Environment.getExternalStorageDirectory().getPath(), "fileTreeCard");
		} else {
			startUpdate();
		}
	}
	
	private void addItem(String path, String resourceKey) {
		final ZLResource resource = myResource.getResource(resourceKey);
		GalleryAdapter adapter = ((GalleryAdapter)myGallery.getAdapter());
		adapter.add(new FileItem(
			ZLFile.createFileByPath(path),
			resource.getValue(),
			resource.getResource("summary").getValue()
		));
		adapter.notifyDataSetChanged();
	}
	
	private void startUpdate() {
		new Thread(
			new SmartFilter(ZLFile.createFileByPath(myPath))
		).start();
	}
	
	public void runItem(FileItem item) {
		final ZLFile file = item.getFile();
		final Book book = item.getBook();
		if (book != null) {
			showBookInfo(book);
		} else if (file.isDirectory() || file.isArchive()) {
			Intent i = new Intent(this, SketchGalleryActivity.class)
				.putExtra(FileManager.SELECTED_BOOK_PATH_KEY, mySelectedBookPath)
				.putExtra(FileManager.FILE_MANAGER_PATH, file.getPath());
			startActivityForResult(i,CHILD_LIST_REQUEST);
		} else {
			UIUtil.showErrorMessage(SketchGalleryActivity.this, "permissionDenied");
		}
	}
	
	@Override
	protected void deleteBook(Book book, int mode) {
		super.deleteBook(book, mode);
		((GalleryAdapter)myGallery.getAdapter()).deleteFile(book.File);
		myGallery.invalidate();
	}
	

	public static void launchSketchGalleryActivity(Context context, String path){
		((Activity) context).startActivityForResult(new Intent(
				context, SketchGalleryActivity.class).putExtra(
				FileManager.FILE_MANAGER_PATH, path).addFlags(
				Intent.FLAG_ACTIVITY_CLEAR_TOP),
				FileManager.CHILD_LIST_REQUEST);
	}

	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Log.v(LOG, "onCreateOptionsMenu");
    	super.onCreateOptionsMenu(menu);
    	addMenuItem(menu, 0, "insert", R.drawable.ic_menu_sorting);
    	addMenuItem(menu, 1, "mkdir", R.drawable.ic_menu_mkdir);
    	addMenuItem(menu, 2, "sorting", R.drawable.ic_menu_sorting);
    	addMenuItem(menu, 3, "view", R.drawable.ic_menu_sorting);	
    	return true;
    }

    private MenuItem addMenuItem(Menu menu, int index, String resourceKey, int iconId) {
        final String label = myResource.getResource("menu").getResource(resourceKey).getValue();
        final MenuItem item = menu.add(0, index, Menu.NONE, label);
        item.setIcon(iconId);
        return item;
    }
    
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.v(LOG, "onPrepareOptionsMenu - start");
		super.onPrepareOptionsMenu(menu);
		
		if (FileManager.myInsertPathStatic == null){
			menu.findItem(0).setVisible(false).setEnabled(false);
			menu.findItem(1).setVisible(false).setEnabled(false);
        }else{
        	menu.findItem(0).setVisible(true).setEnabled(true);
			menu.findItem(1).setVisible(true).setEnabled(true);
        }
		
		Log.v(LOG, "onPrepareOptionsMenu - finish");
		return true;
	}
	
    private Runnable messFileMoved = new Runnable() {
		public void run() {
			Toast.makeText(SketchGalleryActivity.this,
					myResource.getResource("messFileMoved").getValue(), 
					Toast.LENGTH_SHORT).show();
		}
	};
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	    	case 0:
	    		try {
	    			FileUtil.moveFile(FileManager.myInsertPathStatic, myPath);
	    			FileManager.myInsertPathStatic = null;
	    			//refresh();	// TODO
	    			runOnUiThread(messFileMoved);
	    		} catch (IOException e) {
    				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    			}
	    		return true;
        	case 1:
        		new MkDirDialog(this, myPath, FileManager.myInsertPathStatic).show();
        		return true;
        	case 2:
        		new SortingDialog(this, myPath).show();
	            return true;
        	case 3:
        		new ViewChangeDialog(this, myPath).show();
        		return true;
        	default:
        		return super.onOptionsItemSelected(item);
        }
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

        public synchronized long getItemId(int position) {
            return position;
        }

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			runItem(myItems.get(position)); 
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

    		final int maxHeight = 180; // FIXME: hardcoded constant
    		final int maxWidth = maxHeight * 3 / 4;

            ImageView imageView = (ImageView)view.findViewById(R.id.sketch_item_image);
			imageView.getLayoutParams().height = maxHeight;
			imageView.getLayoutParams().width = maxWidth;
            
            TextView nameTextView = (TextView)view.findViewById(R.id.sketch_item_name);
    		String name = fileItem.getName();
//            name = name.length() > 10 ? name.substring(0, 10) : name; 
            nameTextView.setText(name);	// FIXME hardcoding style

    		TextView summaryTextView = (TextView)view.findViewById(R.id.sketch_item_summary); 
    		String summary = fileItem.getSummary() == null ? "" : fileItem.getSummary();
//            summary = summary.length() > 10 ? summary.substring(0, 10) : summary; 
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
	
	private final class SmartFilter implements Runnable {
		private final ZLFile myFile;

		public SmartFilter(ZLFile file) {
			myFile = file;
		}

		public void run() {
			if (!myFile.isReadable()) {
				runOnUiThread(new Runnable() {
					public void run() {
						UIUtil.showErrorMessage(SketchGalleryActivity.this, "permissionDenied");
					}
				});
				finish();
				return;
			}

			final ArrayList<ZLFile> children = new ArrayList<ZLFile>(myFile.children());
			Collections.sort(children, new FileComparator());
			for (final ZLFile file : children) {
				if (Thread.currentThread().isInterrupted()) {
					break;
				}
				if (file.isDirectory() || file.isArchive() ||
					PluginCollection.Instance().getPlugin(file) != null) {
					runOnUiThread(new Runnable() {
						public void run() {
							final GalleryAdapter adapter = (GalleryAdapter)myGallery.getAdapter();
							adapter.add(new FileItem(file));
							adapter.notifyDataSetChanged();				//hmm...
						}
					});
				}
			}
		}	
	}

	
}
