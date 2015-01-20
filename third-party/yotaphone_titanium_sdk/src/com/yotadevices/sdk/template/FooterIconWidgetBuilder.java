package com.yotadevices.sdk.template;

/**
 * Copyright 2012 Yota Devices LLC, Russia
 * 
 * This source code is Yota Devices Confidential Proprietary
 * This software is protected by copyright.  All rights and titles are reserved.
 * You shall not use, copy, distribute, modify, decompile, disassemble or
 * reverse engineer the software. Otherwise this violation would be treated by 
 * law and would be subject to legal prosecution.  Legal use of the software 
 * provides receipt of a license from the right holder only.
 * 
 * */

import com.yotadevices.sdk.R;

import android.app.PendingIntent;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

public class FooterIconWidgetBuilder extends WidgetBuilder {
    protected PendingIntent mMoreViewPendingIntent;
    protected PendingIntent mLeftPendingIntent;

    private CharSequence mLeftText;
    private CharSequence mRightText;
    private CharSequence mMoreViewText;

    private int mIconResource = 0;
    private int mMoreIconResource = 0;
    private RemoteViews mContentView;
    private boolean mLoadingData = false;

    public CharSequence getLeftText() {
        return mLeftText;
    }

    public CharSequence getRightText() {
        return  mRightText;
    }

    public FooterIconWidgetBuilder setLeftText(CharSequence text) {
        mLeftText = text;
        return this;
    }

    public FooterIconWidgetBuilder setRightText(CharSequence text) {
        mRightText = text;
        return this;
    }

    public FooterIconWidgetBuilder setLoadingDataState(boolean val) {
        mLoadingData = val;
        return this;
    }

    public boolean getLoadingDataState() {
        return mLoadingData;
    }

    public FooterIconWidgetBuilder setMoreViewAction(PendingIntent intent, CharSequence text) {
        mMoreViewPendingIntent = intent;
        mMoreViewText = text;
        mMoreIconResource = 0;
        return this;
    }

    public FooterIconWidgetBuilder setMoreViewAction(PendingIntent intent, int imageResource) {
        mMoreViewPendingIntent = intent;
        mMoreIconResource = imageResource;
        mMoreViewText = null;
        return this;
    }

    public FooterIconWidgetBuilder setLeftAction(PendingIntent intent) {
        mLeftPendingIntent = intent;
        return this;
    }

    @Override
    public RemoteViews apply(Context context) {
        if (!mLoadingData) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.template_widget_icon_footer);
            remoteViews.setTextViewText(R.id.left_text, mLeftText);
            remoteViews.setImageViewResource(R.id.icon, mIconResource);
            if (mLeftPendingIntent != null) {
                remoteViews.setOnClickPendingIntent(R.id.left_text, mLeftPendingIntent);
                remoteViews.setOnClickPendingIntent(R.id.icon, mLeftPendingIntent);
            }
            remoteViews.removeAllViews(R.id.content);
            remoteViews.addView(R.id.content, mContentView);
            if (mRightText == null) {
                remoteViews.setViewVisibility(R.id.right_text, View.GONE);
            } else {
                remoteViews.setTextViewText(R.id.right_text, mRightText);
                remoteViews.setViewVisibility(R.id.right_text, View.VISIBLE);
            }
            if (mMoreViewText != null) {
                remoteViews.setViewVisibility(R.id.more_image, View.GONE);
                remoteViews.setViewVisibility(R.id.more_layout, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.more_text, View.VISIBLE);
                remoteViews.setTextViewText(R.id.more_text, mMoreViewText);
                if (mMoreViewPendingIntent != null) {
                    remoteViews.setOnClickPendingIntent(R.id.more_layout, mMoreViewPendingIntent);
                }
            }
            if (mMoreIconResource != 0) {
                remoteViews.setViewVisibility(R.id.more_text, View.GONE);
                remoteViews.setViewVisibility(R.id.more_image, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.more_layout, View.VISIBLE);
                remoteViews.setImageViewResource(R.id.more_image, mMoreIconResource);
                if (mMoreViewPendingIntent != null) {
                    remoteViews.setOnClickPendingIntent(R.id.more_layout, mMoreViewPendingIntent);
                }
            }
            if (mMoreIconResource == 0 && mMoreViewText == null) {
                remoteViews.setViewVisibility(R.id.more_text, View.GONE);
                remoteViews.setViewVisibility(R.id.more_image, View.GONE);
                remoteViews.setViewVisibility(R.id.more_layout, View.GONE);
            }
            return super.apply(context, remoteViews);
        }
        else  {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.template_widget_loading_data);
            return remoteViews;
        }
    }

    public int getIconResource() {
        return mIconResource;
    }

    public FooterIconWidgetBuilder setIconResource(int icon) {
        mIconResource = icon;
        return this;
    }

    public RemoteViews getContentView() {
        return mContentView;
    }

    public FooterIconWidgetBuilder setContentView(RemoteViews content) {
        mContentView = content;
        return this;
    }
}
