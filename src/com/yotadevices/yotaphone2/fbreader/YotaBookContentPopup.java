package com.yotadevices.yotaphone2.fbreader;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import com.yotadevices.sdk.Drawer;
import com.yotadevices.sdk.utils.EinkUtils;
import com.yotadevices.yotaphone2.fbreader.util.TimeUtils;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.ZLTreeAdapter;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.BookmarkQuery;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.ui.android.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class YotaBookContentPopup extends ZLApplication.PopupPanel {
	private final static int ACTION_BAR_HEIGHT = 72;
	public final static String ID = "YotaBookContentPopup";

	private final android.widget.PopupWindow mPopup;
	private View mRootView;
	private final View mPopupView;
	private ListView mContentsListView;
	private ListView mBookmarksListView;
	private final Context mFBReader;
	private final FBReaderApp mFBReaderApp;
	private TOCAdapter myAdapter;

	private volatile Book mBook;
	private final BookCollectionShadow mCollection = new BookCollectionShadow();
	private final Comparator<Bookmark> mComparator = new Bookmark.ByTimeComparator();
	private BookmarksAdapter mBookmarksAdapter;

	RadioButton mContents;
	RadioButton mBookmarks;

	private ZLTextView mBookTextView;

	private Handler mHandler;
	private final boolean mOnBackScreen;

	public YotaBookContentPopup(FBReaderApp app, Context context, boolean onBackScreen) {
		super(app);
		mFBReader = context;
		mFBReaderApp = app;
		mHandler = new Handler(Looper.getMainLooper());
		mOnBackScreen = onBackScreen;

		mPopupView = View.inflate(context, onBackScreen ? R.layout.yota_bs_book_content_popup :
				R.layout.yota_book_content_popup, null);

		mContentsListView = (ListView)mPopupView.findViewById(R.id.contents_list);
		mContentsListView.setVerticalScrollBarEnabled(false);
		mBookmarksListView = (ListView)mPopupView.findViewById(R.id.bookmarks_list);
		mBookmarksListView.setVerticalScrollBarEnabled(false);
		if (mOnBackScreen) {
			mBookmarksListView.setDivider(new ColorDrawable(Color.BLACK));
			mBookmarksListView.setDividerHeight(3);
		}
		mContents = (RadioButton)mPopupView.findViewById(R.id.contents);
		mContents.setOnClickListener(mOnTabSwitch);

		mBookmarks = (RadioButton)mPopupView.findViewById(R.id.bookmarks);
		mBookmarks.setOnClickListener(mOnTabSwitch);

		mPopup = new android.widget.PopupWindow(context);
		mPopup.setBackgroundDrawable(new ColorDrawable(0));
		mPopup.setContentView(mPopupView);
		mPopup.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
		mPopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);

		mPopup.setFocusable(true);
		mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				hideBars();
			}
		});
	}

	private void hideBars() {
		if (mFBReader instanceof FBReader) {
			((FBReader)mFBReader).hideBars();
		} else {
			FBReaderApp.Instance().runAction(ActionCode.TOGGLE_BARS);
		}
	}

	private void runOnUiThread(Runnable action) {
		mHandler.post(action);
	}

	public void show() {
		final TOCTree root = mFBReaderApp.Model.TOCTree;
		mBook = mFBReaderApp.Model.Book;
		myAdapter = new TOCAdapter(root);
		TOCTree treeToSelect = mFBReaderApp.getCurrentTOCElement();
		myAdapter.selectItem(treeToSelect);
		mBookTextView = mFBReaderApp.getTextView();
		if (mOnBackScreen) {
			mPopup.showAtLocation(mRootView, Gravity.NO_GRAVITY, 0, ACTION_BAR_HEIGHT);
		}
		else {
			mPopup.showAsDropDown(mRootView, 0, 0);
		}
		mCollection.bindToService(mFBReader, new Runnable() {
			public void run() {
				if (mBook != null) {
					mBookmarksAdapter = new BookmarksAdapter(mBookmarksListView);
				}
				new Thread(new Initializer()).start();
			}
		});
	}

	public void hide() {
		mCollection.unbind();
		mPopup.dismiss();
	}

	public boolean isShowing() {
		return  mPopup.isShowing();
	}

	private void showContents() {
		mBookmarksListView.setVisibility(View.GONE);
		mContentsListView.setVisibility(View.VISIBLE);
	}

	private void showBookmarks() {
		mContentsListView.setVisibility(View.GONE);
		mBookmarksListView.setVisibility(View.VISIBLE);
	}

	private int getPageNumber(TOCTree tree) {
		TOCTree.Reference ref = tree.getReference();
		if (ref != null) {
			int textLength = mBookTextView.getModel().getTextLength(ref.ParagraphIndex);
			return mBookTextView.computeTextPageNumber(textLength);
		}
		return -1;
	}

	private int getPageNumber(Bookmark mark) {
		if (mark != null) {
			int textLength = mBookTextView.getModel().getTextLength(mark.getParagraphIndex());
			return mBookTextView.computeTextPageNumber(textLength);
		}
		return -1;
	}

	private View.OnClickListener mOnTabSwitch = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.contents:
					mContents.setChecked(true);
					mBookmarks.setChecked(false);
					showContents();
					break;
				case R.id.bookmarks:
					mContents.setChecked(false);
					mBookmarks.setChecked(true);
					showBookmarks();
					break;
			}
		}
	};

	private void gotoBookmark(Bookmark bookmark) {
		bookmark.markAsAccessed();
		mCollection.saveBookmark(bookmark);
		final Book book = mCollection.getBookById(bookmark.getBookId());
		if (!mOnBackScreen) {
			if (book != null) {
				FBReader.openBookActivity(mFBReader, book, bookmark);
			} else {
				//UIUtil.showErrorMessage(mFBReader, "cannotOpenBook");
			}
		}
		else {
			((FBReaderApp)FBReaderApp.Instance()).openBook(book, bookmark, null, null);
		}
		hide();
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	protected void update() {

	}

	@Override
	protected void hide_() {
		hide();
	}

	@Override
	protected void show_() {
		show();
	}


	private final class TOCAdapter extends ZLTreeAdapter {
		TOCAdapter(TOCTree root) {
			super(mContentsListView, root);
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
					LayoutInflater.from(parent.getContext()).inflate(R.layout.yota_toc_tree_item, parent, false);
			final TOCTree tree = (TOCTree)getItem(position);
			//view.setBackgroundColor(tree == mySelectedItem ? 0xff808080 : 0);
			TextView chapter = ViewUtil.findTextView(view, R.id.toc_tree_item_text);
			TextView pageNumber = ViewUtil.findTextView(view, R.id.page_number);
			if (mOnBackScreen) {
				pageNumber.setTextColor(Color.BLACK);
			}
			chapter.setText(tree.getText());
			chapter.setPadding(70 * (tree.Level - 1), chapter.getPaddingTop(), 0, chapter.getPaddingBottom());
			if (tree.Level > 1) {
				chapter.setTypeface(Typeface.create("serif", Typeface.NORMAL));
			}
			else {
				chapter.setTypeface(Typeface.create("serif", Typeface.BOLD));
			}
			if (tree.Level >= 3) {
				chapter.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
			}
			else {
				chapter.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
			}
			int page = getPageNumber(tree);
			String pageText = page >= 0 ? ""+page : "";
			pageNumber.setText(pageText);
			return view;
		}

		void openBookText(TOCTree tree) {
			final TOCTree.Reference reference = tree.getReference();
			if (reference != null) {
				hide();
				final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
				fbreader.addInvisibleBookmark();
				fbreader.BookTextView.gotoPosition(reference.ParagraphIndex, 0, 0);
				fbreader.showBookTextView();
				fbreader.storePosition();
			}
		}

		@Override
		protected boolean runTreeItem(ZLTree<?> tree) {
			if (super.runTreeItem(tree)) {
				return true;
			}
			openBookText((TOCTree)tree);
			return true;
		}
	}

	private final class BookmarksAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
		private final List<Bookmark> mBookmarks =
				Collections.synchronizedList(new LinkedList<Bookmark>());

		BookmarksAdapter(ListView listView) {
			listView.setAdapter(this);
			listView.setOnItemClickListener(this);
		}

		public void addAll(final List<Bookmark> bookmarks) {
			for (Bookmark b : bookmarks) {
				final int position = Collections.binarySearch(mBookmarks, b, mComparator);
				if (position < 0) {
					mBookmarks.add(- position - 1, b);
				}
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					notifyDataSetChanged();
				}
			});
		}

		public void add(final Bookmark b) {
			final int position = Collections.binarySearch(mBookmarks, b, mComparator);
			if (position < 0) {
				mBookmarks.add(- position - 1, b);
			}
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					notifyDataSetChanged();
				}
			});
		}

		@Override
		public int getCount() {
			return mBookmarks.size();
		}

		@Override
		public Bookmark getItem(int position) {
			return mBookmarks.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
					LayoutInflater.from(parent.getContext()).inflate(R.layout.yota_bookmarks_item, parent, false);

			final TextView textView = ViewUtil.findTextView(view, R.id.bookmark);
			final TextView date = ViewUtil.findTextView(view, R.id.date);
			final TextView page = ViewUtil.findTextView(view, R.id.page);
			if (mOnBackScreen) {
				date.setTextColor(Color.BLACK);
				page.setTextColor(Color.BLACK);
			}
			final Bookmark bookmark = getItem(position);
			final Date created = bookmark.getDate(Bookmark.DateType.Creation);
			final int pageNumber = getPageNumber(bookmark);
			if (pageNumber > 0) {
				page.setText(mFBReader.getString(R.string.page)+" "+pageNumber);
			}

			final String bookmarkText = bookmark.getText();
			final int length = bookmarkText.length();
			SpannableStringBuilder style = new SpannableStringBuilder(bookmarkText);
			if (!mOnBackScreen) {
				style.setSpan(new BackgroundColorSpan(mFBReader.getResources().getColor(R.color.yota_higlighted_bookmark)), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			else {
				textView.setTextColor(Color.WHITE);
				style.setSpan(new BackgroundColorSpan(Color.BLACK), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			textView.setText(style);
			date.setText(TimeUtils.getFormattedTimeAgoString(mFBReader, created.getTime()));

			return view;
		}

		public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final Bookmark bookmark = getItem(position);
			if (bookmark != null) {
				gotoBookmark(bookmark);
			}
		}
	}

	private class Initializer implements Runnable {
		public void run() {
			if (mBook != null) {
				for (BookmarkQuery query = new BookmarkQuery(mBook, 20); ; query = query.next()) {
					final List<Bookmark> thisBookBookmarks = mCollection.bookmarks(query);
					if (thisBookBookmarks.isEmpty()) {
						break;
					}
					mBookmarksAdapter.addAll(thisBookBookmarks);
				}
			}
		}
	}

	public void setRootView(View root) {
		mRootView = root;
	}

}