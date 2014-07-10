package com.yotadevices.yotaphone2.fbreader;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.ui.android.R;

public class BSReadingActionBar {
    private final PopupWindow mPopup;
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final View mRootView;

    private final ImageView mFontIcon;
    private FontSettingsPopup mFontSettingsPopup;
    private final FBReaderApp mReader;

    private TextView mAuthor;
    private TextView mTitle;

    public BSReadingActionBar(Context ctx, View root, FBReaderApp readerApp) {
        mContext = ctx;
        mRootView = root;
        mReader = readerApp;
        mLayoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = mLayoutInflater.inflate(R.layout.bs_action_bar_reading_mode, null);
        mFontIcon = (ImageView)layout.findViewById(R.id.font_action);
        mFontIcon.setOnClickListener(fontClickListener);
        mAuthor = (TextView)layout.findViewById(R.id.book_author);
        mTitle =  (TextView)layout.findViewById(R.id.book_title);
        
        Book currentBook = mReader.Collection.getRecentBook(0);

        mPopup = new PopupWindow(ctx);
        mPopup.setBackgroundDrawable(new ColorDrawable(0));
        mPopup.setContentView(layout);
        mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mPopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
    }

    private void updateData() {

    }

    public void show() {
        mPopup.showAtLocation(mRootView, Gravity.NO_GRAVITY, 0, 0);
    }

    public void hide() {
        if (mFontSettingsPopup != null && mFontSettingsPopup.isShowing()) {
            mFontSettingsPopup.hide();
        }
        mPopup.dismiss();
    }

    private View.OnClickListener fontClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mFontSettingsPopup == null) {
                mFontSettingsPopup = new FontSettingsPopup(mContext, mRootView);
            }
            if (mFontSettingsPopup.isShowing()) {
                mFontSettingsPopup.hide();
            }
            else {
                mFontSettingsPopup.show();
            }
        }
    };
}
