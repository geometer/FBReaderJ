package com.yotadevices.yotaphone2.fbreader;

import android.graphics.drawable.ColorDrawable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.PopupWindow;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.ZLTreeAdapter;
import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.zlibrary.ui.android.R;

public class YotaBookContentPopup {
	private final android.widget.PopupWindow mPopup;
	private final View mRootView;
	private final View mPopupView;
	private ListView mContentsListView;
	private final FBReader mFBReader;
	private TOCAdapter myAdapter;
	private ZLTree<?> mySelectedItem;

	public YotaBookContentPopup(FBReader fbreader, View root) {
		mRootView = root;
		mFBReader = fbreader;

		mPopupView = View.inflate(fbreader, R.layout.yota_book_content_popup, null);
		mContentsListView = (ListView)mPopupView.findViewById(R.id.contents_list);
		mContentsListView.setVerticalScrollBarEnabled(false);
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
		mPopup.showAsDropDown(mRootView, 0, 0);
	}

	public void hide() {
		mPopup.dismiss();
	}

	public boolean isShowing() {
		return  mPopup.isShowing();
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
					LayoutInflater.from(parent.getContext()).inflate(R.layout.toc_tree_item, parent, false);
			final TOCTree tree = (TOCTree)getItem(position);
			view.setBackgroundColor(tree == mySelectedItem ? 0xff808080 : 0);
			setIcon(ViewUtil.findImageView(view, R.id.toc_tree_item_icon), tree);
			ViewUtil.findTextView(view, R.id.toc_tree_item_text).setText(tree.getText());
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