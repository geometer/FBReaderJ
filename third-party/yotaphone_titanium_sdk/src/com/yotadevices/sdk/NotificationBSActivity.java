package com.yotadevices.sdk;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.yotadevices.sdk.utils.EinkUtils;

public class NotificationBSActivity extends BSActivity {
    private RelativeLayout mMainLayout;
    private FrameLayout mNotifLayout;
    private ImageView mOK;
    private ImageView mCancel;
    private ImageView mAction;

    @Override
    protected void onBSCreate() {
        super.onBSCreate();
        setSystemBSUiVisibility(Constants.SystemBSFlags.SYSTEM_BS_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onBSResume() {
        super.onBSResume();
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
        mOK = (ImageView) findViewById(R.id.button_ok);
        mCancel = (ImageView) findViewById(R.id.button_cancel);
        mAction = (ImageView) findViewById(R.id.button_action);
        mOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        EinkUtils.setViewWaveform(mMainLayout, Drawer.Waveform.WAVEFORM_GC_PARTIAL);
    }

    protected void setOnOKClickListener(View.OnClickListener l) {
        mOK.setOnClickListener(l);
    }

    protected void setOnCancelClickListener(View.OnClickListener l) {
        mCancel.setOnClickListener(l);
    }

    protected void setOnActionClickListener(View.OnClickListener l) {
        mAction.setOnClickListener(l);
    }

    protected void setOKDrawable(Drawable drawable) {
        mOK.setImageDrawable(drawable);
    }

    protected void setCancelDrawable(Drawable drawable) {
        mCancel.setImageDrawable(drawable);
    }

    protected void setActionDrawable(Drawable drawable) {
        mAction.setImageDrawable(drawable);
    }

    protected void setOKVisibility(int visibility) {
        mOK.setVisibility(visibility);
    }

    protected void setCancelVisibility(int visibility) {
        mCancel.setVisibility(visibility);
    }

    protected void setActionVisibility(int visibility) {
        mAction.setVisibility(visibility);
    }
}
