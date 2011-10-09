package org.geometerplus.android.fbreader.library;

import java.util.*;
import java.io.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.DisplayMetrics;
import android.net.Uri;

import com.devsmart.android.ui.HorizontalListView;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.core.network.*;
import org.geometerplus.zlibrary.core.xml.*;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.library.*;

import org.geometerplus.android.fbreader.FBReader;

public class ShelfActivity extends Activity implements Library.ChangeListener {
	private int myCoverWidth;
	private int myCoverHeight;

	private BooksDatabase myDatabase;
	private Library myLibrary;
	private ShelfAdapter myAdapter0;
	private ShelfAdapter myAdapter1;
	private ShelfAdapter myAdapter2;
	private ShelfAdapter myAdapter3;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		myDatabase = SQLiteBooksDatabase.Instance();
		if (myDatabase == null) {
			myDatabase = new SQLiteBooksDatabase(this, "LIBRARY");
		}
		if (myLibrary == null) {
			myLibrary = Library.Instance();
			myLibrary.addChangeListener(this);
			myLibrary.startBuild();
		}

		myAdapter0 = new ShelfAdapter(myLibrary.getRootTree(), Library.ROOT_RECENT);
		myAdapter1 = new ShelfAdapter(myLibrary.getRootTree(), Library.ROOT_RECENT);
		myAdapter2 = new ShelfAdapter(myLibrary.getRootTree(), Library.ROOT_RECENT);
		myAdapter3 = new ShelfAdapter(myLibrary.getRootTree(), Library.ROOT_RECENT);

		final LinearLayout mainView = new LinearLayout(this);
		mainView.setOrientation(LinearLayout.VERTICAL);
		
		final HorizontalListView view0 = new HorizontalListView(this);
		mainView.addView(view0, 0, new ViewGroup.LayoutParams(
			ViewGroup.LayoutParams.FILL_PARENT,
			0
		));
		view0.setAdapter(myAdapter0);
		view0.setSpacing(30);
		view0.setOnItemClickListener(myAdapter0);

		setContentView(mainView);

		/*
		((HorizontalListView)findViewById(R.id.shelf0)).setAdapter(myAdapter0);
		((HorizontalListView)findViewById(R.id.shelf0)).setSpacing(30);
		((HorizontalListView)findViewById(R.id.shelf0)).setOnItemClickListener(myAdapter0);
		((HorizontalListView)findViewById(R.id.shelf1)).setAdapter(myAdapter1);
		((HorizontalListView)findViewById(R.id.shelf1)).setSpacing(30);
		((HorizontalListView)findViewById(R.id.shelf1)).setOnItemClickListener(myAdapter1);
		((HorizontalListView)findViewById(R.id.shelf2)).setAdapter(myAdapter2);
		((HorizontalListView)findViewById(R.id.shelf2)).setSpacing(30);
		((HorizontalListView)findViewById(R.id.shelf2)).setOnItemClickListener(myAdapter2);
		((HorizontalListView)findViewById(R.id.shelf3)).setAdapter(myAdapter3);
		((HorizontalListView)findViewById(R.id.shelf3)).setSpacing(30);
		((HorizontalListView)findViewById(R.id.shelf3)).setOnItemClickListener(myAdapter3);
//		((HorizontalListView)findViewById(R.id.shelf0)).setPadding(0, 0, 0, 0);
//		((HorizontalListView)findViewById(R.id.shelf0)).setPreferredHeight(getCoverHeight() + 0);
//		((HorizontalListView)findViewById(R.id.shelf1)).setPadding(0, 0, 0, 0);
//		((HorizontalListView)findViewById(R.id.shelf1)).setPreferredHeight(getCoverHeight() + 0);
//		((HorizontalListView)findViewById(R.id.shelf2)).setPadding(0, 0, 0, 0);
//		((HorizontalListView)findViewById(R.id.shelf2)).setPreferredHeight(getCoverHeight() + 0);
//		((HorizontalListView)findViewById(R.id.shelf3)).setPadding(0, 0, 0, 0);
//		((HorizontalListView)findViewById(R.id.shelf3)).setPreferredHeight(getCoverHeight() + 0);
		*/
	}

	public void onLibraryChanged(final Code code) {
		//System.err.println("onLibraryChanged " + code);
		if (myAdapter0 != null) {
			runOnUiThread(new Runnable() {
				public void run() {
					myAdapter0.updateList();
					myAdapter1.updateList();
					myAdapter2.updateList();
					myAdapter3.updateList();
				}
			});
		}
	}

	private int getCoverWidth() {
		if (myCoverWidth == 0) {
			initMetrics();
		}
		return myCoverWidth;
	}

	private int getCoverHeight() {
		if (myCoverHeight == 0) {
			initMetrics();
		}
		return myCoverHeight;
	}

	private void initMetrics() {
		final DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		/*
		int height = (int)metrics.ydpi;
		int widht = (int)metrics.xdpi * 3 / 4;
		final int maxHeight = metrics.heightPixels * 7 / 10;
		if (height > maxHeight) {
			widht = widht * maxHeight / height;
			height = maxHeight;
		}
		*/
		int height = 130;
		int widht = height * (int)metrics.xdpi * 3 / (int)metrics.ydpi / 4;
		myCoverWidth = widht;
		myCoverHeight = height;
	}

	private class ShelfAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
		private final LibraryTree myRootTree;
		private final String myTreeId;

		private final int myNumber;
		private final int myTotal;

		private final List<FBTree> myTrees;

		ShelfAdapter(LibraryTree rootTree, String treeId) {
			this(rootTree, treeId, 0, 1);
		}

		/**
		 * @param number 0-based number of the part
		 * @param total total number of parts
		 */
		ShelfAdapter(LibraryTree rootTree, String treeId, int number, int total) {
			myRootTree = rootTree;
			myTreeId = treeId;
			myNumber = number;
			myTotal = total;
			myTrees = new ArrayList<FBTree>(rootTree.getSubTree(treeId).subTrees());
		}

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			try {
				final BookTree tree = (BookTree)myTrees.get((int)id);
				final Book book = tree.Book;
				startActivity(
					new Intent(getApplicationContext(), FBReader.class)
						.setAction(Intent.ACTION_VIEW)
						.putExtra(FBReader.BOOK_PATH_KEY, book.File.getPath())
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
				);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		void updateList() {
			final List<FBTree> trees = myRootTree.getSubTree(myTreeId).subTrees();
			if (!myTrees.equals(trees)) {
				myTrees.clear();
				myTrees.addAll(trees);
				notifyDataSetChanged();
			}
		}

		private Bitmap getCoverBitmap(ZLImage cover) {
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

			if (data == null) {
				return null;
			}
			final Bitmap bitmap = data.getBitmap(2 * getCoverWidth(), 2 * getCoverHeight());
			if (bitmap == null) {
				return null;
			}
			final Bitmap coverBitmap = Bitmap.createBitmap(
				getCoverWidth(), getCoverHeight(), Bitmap.Config.ARGB_8888
			);
			final Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			final Rect dst;
			if (bitmap.getWidth() * getCoverHeight() > bitmap.getHeight() * getCoverWidth()) {
				final int h = bitmap.getHeight() * getCoverWidth() / bitmap.getWidth();
				final int dY = (getCoverHeight() - h) / 2;
				dst = new Rect(0, dY, getCoverWidth(), dY + h);
			} else {
				final int w = bitmap.getWidth() * getCoverHeight() / bitmap.getHeight();
				final int dX = (getCoverWidth() - w) / 2;
				dst = new Rect(dX, 0, dX + w, getCoverHeight());
			}
			new Canvas(coverBitmap).drawBitmap(bitmap, src, dst, new Paint());
			return coverBitmap;
		}

		private final Runnable myInvalidateViewsRunnable = new Runnable() {
			public void run() {
				notifyDataSetChanged();
			}
		};

		private int getOffset() {
			return myNumber * (myTrees.size() / myTotal);
		}

		public int getCount() {
			if (myNumber == myTotal - 1) {
				// Use such code because of integer division ( (a / b) * b != a ).
				return myTrees.size() - getOffset();
			} else {
				return myTrees.size() / myTotal;
			}
		}

		public FBTree getItem(int position) {
			return myTrees.get(position + getOffset());
		}

		public long getItemId(int position) {
			return position + getOffset();
		}

		private Bitmap myCoverPlaceHolder;

		public View getView(int position, View convertView, ViewGroup parent) {
			final LibraryTree tree = (LibraryTree)getItem(position);
			//final FrameLayout frame = new FrameLayout(ShelfActivity.this);
			Bitmap coverBitmap = getCoverBitmap(/*imageView,*/ tree.getCover());
			if (coverBitmap != null) {
				final ImageView coverView = new ImageView(ShelfActivity.this);
				coverView.setScaleType(ImageView.ScaleType.FIT_XY);
				coverView.setLayoutParams(new HorizontalListView.LayoutParams(
						getCoverWidth(),
						ViewGroup.LayoutParams.FILL_PARENT
				));
//				coverView.setMinimumWidth(getCoverWidth());
//				coverView.setMinimumHeight(getCoverHeight());
				coverView.setImageBitmap(coverBitmap);
				return coverView;
			} else {
				final TextView titleView = new TextView(ShelfActivity.this);
				if (myCoverPlaceHolder == null) {
					myCoverPlaceHolder = BitmapFactory.decodeResource(
						ShelfActivity.this.getResources(),
						R.drawable.book_cover
					);
				}
				if (myCoverPlaceHolder != null) {
					if (myCoverPlaceHolder.getWidth() != getCoverWidth() ||
						myCoverPlaceHolder.getHeight() != getCoverHeight()) {
						myCoverPlaceHolder = Bitmap.createScaledBitmap(
							myCoverPlaceHolder, getCoverWidth(), getCoverHeight(), false
						);
					}
					titleView.setBackgroundDrawable(new BitmapDrawable(myCoverPlaceHolder));
				}
				titleView.setTypeface(Typeface.DEFAULT_BOLD);
				titleView.setTextColor(Color.BLACK);
				titleView.setText(tree.getName());
				titleView.setPadding(
					getCoverWidth() * 12 / 100,
					getCoverHeight() * 4 / 100,
					getCoverWidth() * 10 / 100,
					getCoverHeight() * 15 / 100
				);
				titleView.setMinWidth(getCoverWidth());
				titleView.setMaxWidth(getCoverWidth());
				titleView.setMinHeight(getCoverHeight());
				titleView.setMaxHeight(getCoverHeight());
				titleView.setGravity(Gravity.CENTER);
				return titleView;
			}
		}
	}
}
