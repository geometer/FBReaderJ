package com.yotadevices.yotaphone2.fbreader;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.yotadevices.sdk.Drawer;
import com.yotadevices.sdk.utils.EinkUtils;

import org.geometerplus.fbreader.book.Author;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.ui.android.R;

import java.util.List;

public class BSReadingActionBar {
    private final PopupWindow mPopup;
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final View mRootView;

    private final ImageView mFontIcon;
    private final ImageView mBookmarkIcon;
    private final ImageView mBackButton;
	private final ImageView mContentsButton;
    private final View mTitle;
    private FontSettingsPopup mFontSettingsPopup;
	private YotaBookContentPopup mContentsPopup;
    private final FBReaderApp mReader;

    private TextView mBookAuthor;
    private TextView mBookTitle;
    private final FontSettingsPopup.OnFontChangeListener mFontListener;

    public BSReadingActionBar(Context ctx, View root, FBReaderApp readerApp, FontSettingsPopup.OnFontChangeListener listener) {
        mContext = ctx;
        mRootView = root;
        mReader = readerApp;
        mFontListener = listener;
        mLayoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = mLayoutInflater.inflate(R.layout.bs_action_bar_reading_mode, null);
        mFontIcon = (ImageView)layout.findViewById(R.id.font_action);
        mFontIcon.setOnClickListener(fontClickListener);
        mBookmarkIcon = (ImageView)layout.findViewById(R.id.bookmark_action);
        mBookmarkIcon.setOnClickListener(mBookmarkClickListener);
	    mContentsButton = (ImageView)layout.findViewById(R.id.search_action);
		mContentsButton.setOnClickListener(mContentsClickListener);

        mBookAuthor = (TextView)layout.findViewById(R.id.book_author);
        mBookTitle =  (TextView)layout.findViewById(R.id.book_title);

        mTitle = layout.findViewById(R.id.title);
        mBackButton = (ImageView)layout.findViewById(R.id.back_action);

        mTitle.setOnClickListener(mOpenCollection);
        mBackButton.setOnClickListener(mOpenCollection);

        Book currentBook = mReader.Collection.getRecentBook(0);
        if (currentBook != null) {
            StringBuilder authorString = new StringBuilder();
            List<Author> authors = currentBook.authors();
            if (authors.size() > 0) {
                for (int i = 0; i < authors.size() - 1; ++i) {
                    authorString.append(authors.get(i).DisplayName);
                    authorString.append(", ");
                }
            }
            authorString.append(authors.get(authors.size() - 1));
            mBookAuthor.setText(authorString.toString());
            mBookTitle.setText(currentBook.getTitle().toUpperCase());
        }
        mPopup = new PopupWindow(ctx);
        mPopup.setBackgroundDrawable(new ColorDrawable(0));
        mPopup.setContentView(layout);
        mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mPopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
    }

    private void updateData() {
        final ZLTextView textView = mReader.getTextView();
        if (textView.hasBookmarks()) {
            mBookmarkIcon.setImageResource(R.drawable.bookmark_action_enabled);
            mBookmarkIcon.setTag(true);
        } else {
            mBookmarkIcon.setImageResource(R.drawable.bookmark_action);
            mBookmarkIcon.setTag(false);
        }
    }

    public void addBookmark() {
        final Bookmark bookmark = mReader.createBookmark(20, true);
        mReader.Collection.saveBookmark(bookmark);
        mBookmarkIcon.setImageResource(R.drawable.bookmark_action_enabled);
        mBookmarkIcon.setTag(true);
        mFontListener.fontChanged();
    }

    public void deleteBookmar() {
        final ZLTextView textView = mReader.getTextView();
        if (textView.hasBookmarks()) {
            final Bookmark bookmark = textView.getCurrentBookmarkHighlighting().getBookmark();
            mReader.Collection.deleteBookmark(bookmark);
            mBookmarkIcon.setImageResource(R.drawable.bookmark_action);
            mBookmarkIcon.setTag(false);
            mFontListener.fontChanged();
        }
    }

    public void show() {
        updateData();
        mPopup.showAtLocation(mRootView, Gravity.NO_GRAVITY, 0, 0);
    }

    public void hide() {
        if (mFontSettingsPopup != null && mFontSettingsPopup.isShowing()) {
            mFontSettingsPopup.hide();
        }
        mPopup.dismiss();
    }

    public boolean isShowing() {
        return mPopup.isShowing();
    }

    private View.OnClickListener fontClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mFontSettingsPopup == null) {
                mFontSettingsPopup = new FontSettingsPopup(mContext, mRootView, mReader, mFontListener);
            }
            if (mFontSettingsPopup.isShowing()) {
                mFontSettingsPopup.hide();
            }
            else {
                mFontSettingsPopup.show();
            }
        }
    };

    private View.OnClickListener mBookmarkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean hasBookmar = (Boolean)v.getTag();
            if (hasBookmar) {
                deleteBookmar();
            }
            else {
                addBookmark();
            }
        }
    };

	private View.OnClickListener mContentsClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mContentsPopup == null) {
				mContentsPopup = new YotaBookContentPopup(mReader, mContext, true);
				mContentsPopup.setRootView(mRootView);
			}
			if (mContentsPopup.isShowing()) {
				mContentsPopup.hide();
			}
			else {
				mContentsPopup.show();
			}
		}
	};

    private View.OnClickListener mOpenCollection = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent();
            i.setClassName("com.yotadevices.yotaphone2.yotareader.collection",
                    "com.yotadevices.yotaphone2.yotareader.collection.CollectionBSActivity");
            mContext.startService(i);
        }
    };
}
