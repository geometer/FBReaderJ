package com.yotadevices.sdk;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yotadevices.sdk.R;
import com.yotadevices.sdk.utils.EinkUtils;

public class NotificationBSActivity extends BSActivity {
    private static final float HEIGHT_LOCKED_DP = 529.66f;
    private static final float HEIGHT_UNLOCKED_DP = 597.33f;

    private RelativeLayout mMainLayout;
    private FrameLayout mNotifLayout;
    private TextView mOK;
    private View mDotLine;

    @Override
    public void onCreate() {
        super.onCreate();
        setSystemBSUiVisibility(Constants.SystemBSFlags.SYSTEM_BS_UI_FLAG_HIDE_NAVIGATION | Constants.SystemBSFlags.SYSTEM_BS_UI_FLAG_HIDE_STATUS_BAR);
    }

    @Override
    protected void onBSResume() {
        super.onBSResume();
        if (isBackScreenLocked()) {
            hideOKButton();
        } else {
            showOKButton();
        }
    }

    @Override
    public void setBSContentView(int layoutResID) {
        super.setBSContentView(R.layout.bs_notification);
        setViews();
        mNotifLayout.removeAllViews();
        mNotifLayout.addView(getBSDrawer().getBSLayoutInflater().inflate(layoutResID, null));
    }

    @Override
    public void setBSContentView(View view) {
        super.setBSContentView(getBSDrawer().getBSLayoutInflater().inflate(R.layout.bs_notification, null));
        setViews();
        mNotifLayout.removeAllViews();
        mNotifLayout.addView(view);
    }

    @Override
    public void setBSContentView(View view, ViewGroup.LayoutParams params) {
        super.setBSContentView(getBSDrawer().getBSLayoutInflater().inflate(R.layout.bs_notification, null), params);
        setViews();
        mNotifLayout.removeAllViews();
        mNotifLayout.addView(view);
    }

    private void setViews() {
        mMainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        mNotifLayout = (FrameLayout) findViewById(R.id.notification_layout);
        mOK = (TextView) findViewById(R.id.button_ok);
        mDotLine = findViewById(R.id.dot_line);
        mOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        EinkUtils.setViewWaveform(mMainLayout, Drawer.Waveform.WAVEFORM_GC_PARTIAL);
    }

    @Override
    protected void onBSLock() {
        super.onBSLock();
        hideOKButton();
    }

    @Override
    protected void onBSUnlock() {
        super.onBSUnlock();
        showOKButton();
    }

    protected void setOKButtonText(String string) {
        mOK.setText(string);
    }

    protected void setOnOKClickListener(View.OnClickListener l) {
        mOK.setOnClickListener(l);
    }

    private void hideOKButton() {
        if (isFinishing()) {
            return;
        }
        mOK.setVisibility(View.INVISIBLE);
        mDotLine.setVisibility(View.INVISIBLE);
        /*RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mMainLayout.getLayoutParams();
        lp.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HEIGHT_LOCKED_DP, getBSDrawer().getBSContext().getResources().getDisplayMetrics());
        mMainLayout.setLayoutParams(lp);*/
    }

    private void showOKButton() {
        if (isFinishing()) {
            return;
        }
        mOK.setVisibility(View.VISIBLE);
        mDotLine.setVisibility(View.VISIBLE);
        /*RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mMainLayout.getLayoutParams();
        lp.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, HEIGHT_UNLOCKED_DP, getBSDrawer().getBSContext().getResources().getDisplayMetrics());
        mMainLayout.setLayoutParams(lp);*/
    }
}
