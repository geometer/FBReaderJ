package com.yotadevices.yotaphone2.fbreader;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.yotadevices.yotaphone2.fbreader.view.GaugeView;

import org.geometerplus.zlibrary.ui.android.R;

public class BSReadingStatusBar {
    private final android.widget.PopupWindow mPopup;
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final View mRootView;
    private final View mPopupView;

    private final static int STATUS_BAR_HEIGHT = 117;

    public BSReadingStatusBar(Context ctx, View root) {
        mContext = ctx;
        mRootView = root;
        mLayoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPopupView = mLayoutInflater.inflate(R.layout.bs_status_bar_reading_mode, null);
        GaugeView gauge = (GaugeView)mPopupView.findViewById(R.id.gauge_view);
        gauge.init();

        mPopup = new android.widget.PopupWindow(ctx);
        mPopup.setBackgroundDrawable(new ColorDrawable(0));
        mPopup.setContentView(mPopupView);
        mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mPopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
    }

    public void show() {
        int yOffset = mRootView.getHeight() - STATUS_BAR_HEIGHT;
        mPopup.showAtLocation(mRootView, Gravity.NO_GRAVITY, 0, yOffset);
    }

    public void hide() {
        mPopup.dismiss();
    }
}
