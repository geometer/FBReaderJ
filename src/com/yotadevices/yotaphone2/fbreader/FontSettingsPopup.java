package com.yotadevices.yotaphone2.fbreader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Pair;
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

import com.yotadevices.sdk.Drawer;
import com.yotadevices.sdk.utils.EinkUtils;
import com.yotadevices.yotaphone2.fbreader.view.GaugeView;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.options.ColorProfile;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.ui.android.R;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class FontSettingsPopup {
    private final android.widget.PopupWindow mPopup;
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private final View mRootView;
    private final View mPopupView;
    private final FBReaderApp mReader;
    private final OnFontChangeListener mListener;

    private final static int ACTION_BAR_HEIGHT = 59;
    private final static float[] FONT_SIZES = {14, 16, 20, 21.3f, 24};

    private ArrayList<Pair<TextView, ImageView>> mFontViews = new ArrayList<Pair<TextView, ImageView>>(5);

    private Pair<TextView, ImageView> mSerifFont;
    private Pair<TextView, ImageView> mSansFont;

    private Pair<TextView, ImageView> mWhiteTheme;
    private Pair<TextView, ImageView> mBlackTheme;

    public interface OnFontChangeListener {
        public void fontChanged();
    }

    public FontSettingsPopup(Context ctx, View root, FBReaderApp readerApp, OnFontChangeListener listener) {
        mContext = ctx;
        mRootView = root;
        mReader = readerApp;
        mListener = listener;
        mLayoutInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPopupView = mLayoutInflater.inflate(R.layout.font_popup_layout, null);

        LinearLayout lv = (LinearLayout)mPopupView.findViewById(R.id.font1);
        lv.setTag(0);
        lv.setOnClickListener(mOnFontSizeListener);
        TextView tv = (TextView)lv.findViewById(R.id.font_example);
        ImageView iv = (ImageView)lv.findViewById(R.id.line);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, FONT_SIZES[0]);
        mFontViews.add(new Pair<TextView, ImageView>(tv, iv));

        lv = (LinearLayout)mPopupView.findViewById(R.id.font2);
        lv.setTag(1);
        lv.setOnClickListener(mOnFontSizeListener);
        tv = (TextView)lv.findViewById(R.id.font_example);
        iv = (ImageView)lv.findViewById(R.id.line);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, FONT_SIZES[1]);
        mFontViews.add(new Pair<TextView, ImageView>(tv, iv));

        lv = (LinearLayout)mPopupView.findViewById(R.id.font3);
        lv.setTag(2);
        lv.setOnClickListener(mOnFontSizeListener);
        tv = (TextView)lv.findViewById(R.id.font_example);
        iv = (ImageView)lv.findViewById(R.id.line);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, FONT_SIZES[2]);
        mFontViews.add(new Pair<TextView, ImageView>(tv, iv));

        lv = (LinearLayout)mPopupView.findViewById(R.id.font4);
        lv.setTag(3);
        lv.setOnClickListener(mOnFontSizeListener);
        tv = (TextView)lv.findViewById(R.id.font_example);
        iv = (ImageView)lv.findViewById(R.id.line);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, FONT_SIZES[3]);
        mFontViews.add(new Pair<TextView, ImageView>(tv, iv));

        lv = (LinearLayout)mPopupView.findViewById(R.id.font5);
        lv.setTag(4);
        lv.setOnClickListener(mOnFontSizeListener);
        tv = (TextView)lv.findViewById(R.id.font_example);
        iv = (ImageView)lv.findViewById(R.id.line);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, FONT_SIZES[4]);
        mFontViews.add(new Pair<TextView, ImageView>(tv, iv));

        tv = (TextView)mPopupView.findViewById(R.id.font_serif);
        tv.setOnClickListener(mOnFontStyleListener);
        iv = (ImageView)mPopupView.findViewById(R.id.serif_line);
        mSerifFont = new Pair<TextView, ImageView>(tv, iv);

        tv = (TextView)mPopupView.findViewById(R.id.font_sans);
        tv.setOnClickListener(mOnFontStyleListener);
        iv = (ImageView)mPopupView.findViewById(R.id.sans_line);
        mSansFont = new Pair<TextView, ImageView>(tv, iv);

        tv = (TextView)mPopupView.findViewById(R.id.theme_white);
        tv.setOnClickListener(mOnThemeListener);
        iv = (ImageView)mPopupView.findViewById(R.id.theme_white_line);
        mWhiteTheme = new Pair<TextView, ImageView>(tv, iv);

        tv = (TextView)mPopupView.findViewById(R.id.theme_black);
        tv.setOnClickListener(mOnThemeListener);
        iv = (ImageView)mPopupView.findViewById(R.id.theme_black_line);
        mBlackTheme = new Pair<TextView, ImageView>(tv, iv);

        mPopup = new android.widget.PopupWindow(ctx);
        mPopup.setBackgroundDrawable(new ColorDrawable(0));
        mPopup.setContentView(mPopupView);
        mPopup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mPopup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
    }

    public void show() {
        setupFontSizes();
        setupFontStyle();
        setupTheme();
        mPopup.showAtLocation(mRootView, Gravity.NO_GRAVITY, 0, ACTION_BAR_HEIGHT);
        EinkUtils.performSingleUpdate(mPopupView, Drawer.Waveform.WAVEFORM_GC_FULL);
    }

    private void setupFontStyle() {
        mSerifFont.second.setVisibility(View.INVISIBLE);
        mSansFont.second.setVisibility(View.INVISIBLE);

        final ZLStringOption option =
                mReader.ViewOptions.getTextStyleCollection().getBaseStyle().FontFamilyOption;
        if (option.getValue().contains("Serif")) {
            mSerifFont.second.setVisibility(View.VISIBLE);
        } else {
            mSansFont.second.setVisibility(View.VISIBLE);
        }

    }

    private void setupTheme() {
        mBlackTheme.second.setVisibility(
                mReader.ViewOptions.ColorProfileName.getValue().equals(ColorProfile.NIGHT) ?
                        View.VISIBLE : View.INVISIBLE);
        mWhiteTheme.second.setVisibility(
                mReader.ViewOptions.ColorProfileName.getValue().equals(ColorProfile.DAY) ?
                        View.VISIBLE : View.INVISIBLE);

    }

    private void setupFontSizes() {
        final ZLIntegerRangeOption option =
                mReader.ViewOptions.getTextStyleCollection().getBaseStyle().FontSizeOption;
        float fontSize = convertPixelsToDp(option.getValue(), mContext) / 1.2f;
        int matched = -1;
        for (int i = 0; i < FONT_SIZES.length - 1; ++i) {
            float curValue = FONT_SIZES[i];
            float nextValue = FONT_SIZES[i+1];
            float diff = (nextValue - curValue) / 2;
            if (fontSize <= curValue + diff) {
                matched = i;
                break;
            }
            else if (fontSize <= nextValue) {
                matched = i+1;
                break;
            }
        }
        if (matched == -1) {
            matched = FONT_SIZES.length - 1;
        }
        for (Pair<TextView, ImageView> item : mFontViews) {
            item.second.setVisibility(View.INVISIBLE);
        }
        mFontViews.get(matched).second.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mPopup.dismiss();
    }

    public boolean isShowing() {
        return mPopup.isShowing();
    }

    private View.OnClickListener mOnFontSizeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Integer fontSize = (Integer)v.getTag();
            float px = convertDpToPixel((FONT_SIZES[fontSize] * 1.2f), mContext);
            final ZLIntegerRangeOption option =
                    mReader.ViewOptions.getTextStyleCollection().getBaseStyle().FontSizeOption;
            option.setValue((int)px);
            mReader.clearTextCaches();
            mReader.getViewWidget().repaint();
            //vibrate();
            mListener.fontChanged();
            Pair<TextView, ImageView> fontView = mFontViews.get(fontSize);
            fontView.second.setVisibility(View.VISIBLE);
        }
    };

    private View.OnClickListener mOnFontStyleListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final ZLStringOption option =
                    mReader.ViewOptions.getTextStyleCollection().getBaseStyle().FontFamilyOption;
            if (v.getId() == R.id.font_serif) {
                option.setValue("Droid Serif");
                mReader.clearTextCaches();
                mReader.getViewWidget().repaint();
                //vibrate();
                mListener.fontChanged();

            }
            else if (v.getId() == R.id.font_sans) {
                option.setValue("Droid Sans");
                mReader.clearTextCaches();
                mReader.getViewWidget().repaint();
                //vibrate();
                mListener.fontChanged();
            }
        }
    };

    private View.OnClickListener mOnThemeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.theme_black) {
                mReader.ViewOptions.ColorProfileName.setValue(ColorProfile.NIGHT);
                mReader.getViewWidget().reset();
                mReader.getViewWidget().repaint();
                mListener.fontChanged();
            }
            else if (v.getId() == R.id.theme_white) {
                mReader.ViewOptions.ColorProfileName.setValue(ColorProfile.DAY);
                mReader.getViewWidget().reset();
                mReader.getViewWidget().repaint();
                mListener.fontChanged();
            }
        }
    };

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    private void vibrate() {
        // do not forget to add permission in
        //((Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
    }

}