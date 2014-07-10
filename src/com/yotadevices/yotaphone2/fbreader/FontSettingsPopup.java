package com.yotadevices.yotaphone2.fbreader;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yotadevices.yotaphone2.fbreader.view.GaugeView;

import org.geometerplus.zlibrary.ui.android.R;

public class FontSettingsPopup {
    private final android.widget.PopupWindow mPopup;
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final View mRootView;
    private final View mPopupView;

    private final static int ACTION_BAR_HEIGHT = 59;
    private final static float[] FONT_SIZES = {14, 16, 20, 21.3f, 24};

    public FontSettingsPopup(Context ctx, View root) {
        mContext = ctx;
        mRootView = root;
        mLayoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPopupView = mLayoutInflater.inflate(R.layout.font_popup_layout, null);

        LinearLayout lv = (LinearLayout)mPopupView.findViewById(R.id.font1);
        TextView tv = (TextView)lv.findViewById(R.id.font_example);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, FONT_SIZES[0]);

        lv = (LinearLayout)mPopupView.findViewById(R.id.font2);
        tv = (TextView)lv.findViewById(R.id.font_example);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, FONT_SIZES[1]);

        lv = (LinearLayout)mPopupView.findViewById(R.id.font3);
        tv = (TextView)lv.findViewById(R.id.font_example);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, FONT_SIZES[2]);

        lv = (LinearLayout)mPopupView.findViewById(R.id.font4);
        tv = (TextView)lv.findViewById(R.id.font_example);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, FONT_SIZES[3]);

        lv = (LinearLayout)mPopupView.findViewById(R.id.font5);
        tv = (TextView)lv.findViewById(R.id.font_example);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, FONT_SIZES[4]);

        mPopup = new android.widget.PopupWindow(ctx);
        mPopup.setBackgroundDrawable(new ColorDrawable(0));
        mPopup.setContentView(mPopupView);
        mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mPopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
    }

    public void show() {
        mPopup.showAtLocation(mRootView, Gravity.NO_GRAVITY, 0, ACTION_BAR_HEIGHT);
    }

    public void hide() {
        mPopup.dismiss();
    }

    public boolean isShowing() {
        return mPopup.isShowing();
    }
}