package org.geometerplus.android.fbreader.library;

import java.io.IOException;

import org.geometerplus.android.fbreader.library.ViewChangeDialog.ViewType;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.Paths;
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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class SketchGalleryActivity extends BaseGalleryActivity implements HasAdapter {
	public static String LOG = "FileManager";
	
//	public static String FILE_MANAGER_INSERT_MODE = "FileManagerInsertMode";
	
	private static final int DELETE_FILE_ITEM_ID = 10;
//	private static final int RENAME_FILE_ITEM_ID = 11; //FIXME delete later
	private static final int MOVE_FILE_ITEM_ID = 12;
	
	private String myPath;
	
	@Override 
	public FMBaseAdapter getAdapter() {
		return (FMBaseAdapter)myGallery.getAdapter();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		GalleryAdapter galleryAdapter = new GalleryAdapter(); 
		myGallery.setAdapter(galleryAdapter);
		myGallery.setOnItemClickListener(galleryAdapter);
		myGallery.setOnItemSelectedListener(galleryAdapter);
		myGallery.setOnCreateContextMenuListener(galleryAdapter);
		
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
	
	@Override
	protected void onResume() {
		super.onResume();

		if (FileManager.myViewType == ViewType.SIMPLE){
			FileManager.launchFileManagerActivity(this, myPath);
			finish();
			return;
		}

		if (FileManager.myInsertPathStatic != null) {
			setTitle(myResource.getResource("moveTitle").getValue());
		} else if (myPath == null) {
			setTitle(myResource.getResource("fileTree").getValue());
		} else {
			setTitle(myPath);
		}
	}
	
	private void addItem(String path, String resourceKey) {
		final ZLResource resource = myResource.getResource(resourceKey);
		FMBaseAdapter adapter = getAdapter();
		adapter.add(new FileItem(
			ZLFile.createFileByPath(path),
			resource.getValue(),
			resource.getResource("summary").getValue()
		));
		adapter.notifyDataSetChanged(); // TODO see ...
	}
	
	private void startUpdate() {
		new Thread(
			new SmartFilter(this, ZLFile.createFileByPath(myPath))
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
		getAdapter().deleteFile(book.File);
		myGallery.invalidate();
	}
	

	public static void launchSketchGalleryActivity(Context context, String path){
		Intent i = new Intent(context, SketchGalleryActivity.class)
			.putExtra(FileManager.FILE_MANAGER_PATH, path)
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		((Activity) context).startActivity(i);
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
	    			FileUtil.refreshActivity(this, myPath);
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
    
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final FileItem fileItem = getAdapter().getItem(position);
		final Book book = fileItem.getBook(); 
		if (book != null) {
			onContextItemSelected(item.getItemId(), book);
		}
		switch (item.getItemId()) {
			case MOVE_FILE_ITEM_ID:
				Log.v(LOG, "MOVE_FILE_ITEM_ID"); 
				FileManager.myInsertPathStatic = fileItem.getFile().getPhysicalFile().getPath();
				FileUtil.refreshActivity(this, myPath);			
				return true;
//			case RENAME_FILE_ITEM_ID:
//				new RenameDialog(this, fileItem.getFile()).show();
//				return true;
			case DELETE_FILE_ITEM_ID:
				FileUtil.deleteFileItem(this, fileItem);
				return true;
		}
		return super.onContextItemSelected(item);
	}
    
	public class GalleryAdapter extends FMBaseAdapter implements OnItemClickListener, OnItemSelectedListener {

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			runItem(myItems.get(position)); 
		}

		@Override
		public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
			view.setSelected(false);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		
		}

		private int maxHeight = 0;
		private int maxWidth = 0;
		private int paddingTop = 0;
		private int orientation = -1;
		
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view = (convertView != null) ?  convertView :
    			LayoutInflater.from(parent.getContext()).inflate(R.layout.sketch_item, parent, false);
            
            final FileItem fileItem = getItem(position);
    		String summary = fileItem.getSummary();
    		TextView summaryTextView = (TextView)view.findViewById(R.id.sketch_item_summary); 
    		summary = summary != null ? summary : fileItem.getName();  
    		summary = summary.length() > 16 ? summary.substring(0, 15) : summary; 
    		summaryTextView.setText(summary);

            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            if (orientation != display.getOrientation()){
            	orientation = display.getOrientation();
            	switch (display.getOrientation()) {
					case 0:
						maxWidth = display.getWidth() / 2;
						maxHeight = maxWidth * 4 / 3;
					break;

					case 1:
						maxWidth = (int) (display.getWidth() / 3);
						maxHeight = maxWidth * 4 / 3;
						paddingTop = (display.getHeight() - maxHeight) / 4;
					break;
            	}
            }
            	
    		Log.v(FileManager.LOG, "maxHeight = " + maxHeight + " maxWidth = " + maxWidth + " orientation = " + display.getOrientation());
    		ImageView imageView = (ImageView)view.findViewById(R.id.sketch_item_image);
    		imageView.setPadding(0, paddingTop, 0, 0);
    		imageView.getLayoutParams().height = maxHeight;
			imageView.getLayoutParams().width = maxWidth;
			
    		final Bitmap coverBitmap = getBitmap(fileItem.getCover(), maxWidth, maxHeight);
			if (coverBitmap != null) {
				imageView.setImageBitmap(coverBitmap);
			} else {
				imageView.setImageResource(fileItem.getIcon());
			}
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            return view;
        }
        
    	protected ImageView getCoverView(View parent) {
    		parent.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    		int myCoverHeight = parent.getMeasuredHeight();
    		int myCoverWidth = myCoverHeight * 3 / 4;
    		parent.requestLayout();
    		
    		final ImageView coverView = (ImageView)parent.findViewById(R.id.sketch_item_image);
    		coverView.getLayoutParams().width = myCoverWidth;
    		coverView.getLayoutParams().height = myCoverHeight;
    		coverView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    		coverView.requestLayout();
    		return coverView;
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
    	
    	@Override
		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			if (myPath == null)
				return;
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final FileItem item = getItem(position);

			menu.setHeaderTitle(item.getName());
			if (item.getFile().isDirectory()){
				if (ZLFile.createFileByPath(myPath).isArchive())
					return;
				//menu.add(0, RENAME_FILE_ITEM_ID, 0, myResource.getResource("rename").getValue());
				menu.add(0, DELETE_FILE_ITEM_ID, 0, myResource.getResource("delete").getValue());
			}else{
				final Book book = item.getBook();
				if (book != null) {
					createBookContextMenu(menu, book); 
				}
				if (ZLFile.createFileByPath(myPath).isArchive())
					return;
				//menu.add(0, RENAME_FILE_ITEM_ID, 0, myResource.getResource("rename").getValue());
				menu.add(0, MOVE_FILE_ITEM_ID, 0, myResource.getResource("move").getValue());
				if (book == null) {
					menu.add(0, DELETE_FILE_ITEM_ID, 0, myResource.getResource("delete").getValue());
				}
			}
		}
    	
	}
	
}
