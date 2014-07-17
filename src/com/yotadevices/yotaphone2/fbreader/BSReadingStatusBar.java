package com.yotadevices.yotaphone2.fbreader;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.yotadevices.yotaphone2.fbreader.view.GaugeView;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.ui.android.R;

public class BSReadingStatusBar {
    private final android.widget.PopupWindow mPopup;
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final View mRootView;
    private final View mPopupView;
    private final FBReaderApp mReader;

    private final static int STATUS_BAR_HEIGHT = 117;
    //private final static int SYSTEM_NAVIGATION_BAR = 80;
    private final static int SYSTEM_NAVIGATION_BAR = 0;
    private TextView mBackToPage;
    private TextView mTotalPages;
    private TextView mPagesLeft;

    public BSReadingStatusBar(Context ctx, View root, FBReaderApp readerApp) {
        mContext = ctx;
        mRootView = root;
        mReader = readerApp;
        mLayoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPopupView = mLayoutInflater.inflate(R.layout.bs_status_bar_reading_mode, null);
        GaugeView gauge = (GaugeView)mPopupView.findViewById(R.id.gauge_view);
        gauge.init();

        mBackToPage = (TextView)mPopupView.findViewById(R.id.back_to_page);
        mTotalPages = (TextView)mPopupView.findViewById(R.id.page_of_pages);
        mPagesLeft  = (TextView)mPopupView.findViewById(R.id.pages_left);

        mPopup = new android.widget.PopupWindow(ctx);
        mPopup.setBackgroundDrawable(new ColorDrawable(0));
        mPopup.setContentView(mPopupView);
        mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mPopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
    }

    private void updateData() {
        final ZLTextView textView = mReader.getTextView();
        final ZLTextView.PagePosition pagePosition = textView.pagePosition();

        mBackToPage.setVisibility(View.GONE);

        mTotalPages.setText(pagePosition.Current+" "+
                mContext.getResources().getString(R.string.of) +
                " "+pagePosition.Total);

        mPagesLeft.setText(pagePosition.Total - pagePosition.Current+" "+
                mContext.getResources().getString(R.string.pages_left));
    }

    public void show() {
        updateData();
        int yOffset = mRootView.getHeight() - (STATUS_BAR_HEIGHT + SYSTEM_NAVIGATION_BAR);
        mPopup.showAtLocation(mRootView, Gravity.NO_GRAVITY, 0, yOffset);
    }

    public void hide() {
        mPopup.dismiss();
    }

    public boolean isShowing() {
        return mPopup.isShowing();
    }

}
