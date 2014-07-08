package com.yotadevices.yotaphone2.fbreader;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import org.geometerplus.zlibrary.ui.android.R;

public class BSReadingActionBar {
    private final PopupWindow mPopup;
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final View mRootView;

    public BSReadingActionBar(Context ctx, View root) {
        mContext = ctx;
        mRootView = root;
        mLayoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = mLayoutInflater.inflate(R.layout.bs_action_bar_reading_mode, null);

        mPopup = new PopupWindow(ctx);
        mPopup.setBackgroundDrawable(new ColorDrawable(0));
        mPopup.setContentView(layout);
        mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mPopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
    }

    public void show() {
        mPopup.showAtLocation(mRootView, Gravity.NO_GRAVITY, 0, 0);
    }

    public void hide() {
        mPopup.dismiss();
    }
}
