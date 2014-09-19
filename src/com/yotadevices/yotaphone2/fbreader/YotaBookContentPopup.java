package com.yotadevices.yotaphone2.fbreader;

import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.ZLTreeAdapter;
import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.zlibrary.ui.android.R;

import java.util.List;

public class YotaBookContentPopup {
	private final android.widget.PopupWindow mPopup;
	private final View mRootView;
	private final View mPopupView;
	private ListView mContentsListView;
	private ListView mBookmarksListView;
	private final FBReader mFBReader;
	private TOCAdapter myAdapter;
	private ZLTree<?> mySelectedItem;

	RadioButton mContents;
	RadioButton mBookmarks;

	private ZLTextView mBookTextView;

	public YotaBookContentPopup(FBReader fbreader, View root) {
		mRootView = root;
		mFBReader = fbreader;

		mPopupView = View.inflate(fbreader, R.layout.yota_book_content_popup, null);

		mContentsListView = (ListView)mPopupView.findViewById(R.id.contents_list);
		mContentsListView.setVerticalScrollBarEnabled(false);
		mBookmarksListView = (ListView)mPopupView.findViewById(R.id.bookmarks_list);
		mBookmarksListView.setVerticalScrollBarEnabled(false);

		mContents = (RadioButton)mPopupView.findViewById(R.id.contents);
		mContents.setOnClickListener(mOnTabSwitch);

		mBookmarks = (RadioButton)mPopupView.findViewById(R.id.bookmarks);
		mBookmarks.setOnClickListener(mOnTabSwitch);

		mPopup = new android.widget.PopupWindow(fbreader);
		mPopup.setBackgroundDrawable(new ColorDrawable(0));
		mPopup.setContentView(mPopupView);
		mPopup.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
		mPopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
		mPopup.setFocusable(true);
		mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				mFBReader.hideBars();
			}
		});
	}

	public void show(FBReaderApp readerApp) {
		final TOCTree root = readerApp.Model.TOCTree;
		myAdapter = new TOCAdapter(root);
		final ZLTextWordCursor cursor = readerApp.BookTextView.getStartCursor();
		int index = cursor.getParagraphIndex();
		if (cursor.isEndOfParagraph()) {
			++index;
		}
		TOCTree treeToSelect = readerApp.getCurrentTOCElement();
		myAdapter.selectItem(treeToSelect);
		mySelectedItem = treeToSelect;
		mBookTextView = readerApp.getTextView();
		mPopup.showAsDropDown(mRootView, 0, 0);
	}

	public void hide() {
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
			chapter.setText(tree.getText());
			chapter.setPadding(70 * (tree.Level - 1), chapter.getPaddingTop(), 0, chapter.getPaddingBottom());
			if (tree.Level > 1) {
				chapter.setTypeface(Typeface.create("serif", Typeface.NORMAL));
			}
			else {
				chapter.setTypeface(Typeface.create("serif", Typeface.BOLD));
			}
			if (tree.Level >= 3) {
				chapter.setTextSize(TypedValue.COMPLEX_UNIT_PX, 54);
			}
			else {
				chapter.setTextSize(TypedValue.COMPLEX_UNIT_PX, 68);
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
}