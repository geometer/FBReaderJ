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

import android.content.Context;
import android.widget.RemoteViews;

import com.yotadevices.sdk.R;

public class TinyWidgetBuilder extends WidgetBuilder {
    private CharSequence mLeftText;
    
    private int mLeftTextDrawablesLeft;
    private int mLeftTextDrawablesTop;
    private int mLeftTextDrawablesRight;
    private int mLeftTextDrawablesBottom;

    private RemoteViews mContentView;
    private boolean mLoadingData = false;

    public CharSequence getLeftText() {
        return mLeftText;
    }

    public TinyWidgetBuilder setLeftText(CharSequence text) {
        mLeftText = text;
        return this;
    }
    
    public TinyWidgetBuilder setLeftTextCompoundDrawables(int left, int top, int right, int bottom) {
        mLeftTextDrawablesLeft = left;
        mLeftTextDrawablesTop = top;
        mLeftTextDrawablesRight = right;
        mLeftTextDrawablesBottom = bottom;
        return this;
    }

    public TinyWidgetBuilder setLoadingDataState(boolean val) {
        mLoadingData = val;
        return this;
    }

    public boolean getLoadingDataState() {
        return mLoadingData;
    }


    public RemoteViews apply(Context context) {
        if (!mLoadingData) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.template_widget_tiny);
            remoteViews.setTextViewText(R.id.left_text, mLeftText);
            remoteViews.setTextViewCompoundDrawables(R.id.left_text, mLeftTextDrawablesLeft, mLeftTextDrawablesTop,
            		mLeftTextDrawablesRight, mLeftTextDrawablesBottom);
            remoteViews.removeAllViews(R.id.content);
            remoteViews.addView(R.id.content, mContentView);
            return super.apply(context, remoteViews);
        }
        else  {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.template_widget_loading_data);
            return remoteViews;
        }
    }

    public RemoteViews getContentView() {
        return mContentView;
    }

    public TinyWidgetBuilder setContentView(RemoteViews content) {
        mContentView = content;
        return this;
    }

}
