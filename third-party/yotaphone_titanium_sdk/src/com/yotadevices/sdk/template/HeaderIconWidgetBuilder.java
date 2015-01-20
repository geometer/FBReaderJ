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
import android.view.View;
import android.widget.RemoteViews;

import com.yotadevices.sdk.R;

/**
 * Created by Alexei.Sazonov on 21.05.2014.
 */
public class HeaderIconWidgetBuilder extends WidgetBuilder {
    private CharSequence mTitle;
    private CharSequence mDate;
    private int mIconResource;
    private RemoteViews mContentView;

    public CharSequence getTitle() {
        return mTitle;
    }

    public HeaderIconWidgetBuilder setTitle(CharSequence mText) {
        this.mTitle = mText;
        return this;
    }

    public RemoteViews apply(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.template_widget_icon_header);
        remoteViews.setTextViewText(R.id.template_widget_title, mTitle);
        remoteViews.setImageViewResource(R.id.template_widget_icon, mIconResource);
        remoteViews.removeAllViews(R.id.template_widget_content);
        remoteViews.addView(R.id.template_widget_content, mContentView);
        if (mDate == null) {
            remoteViews.setViewVisibility(R.id.template_widget_title_date, View.GONE);
        } else {
            remoteViews.setTextViewText(R.id.template_widget_title_date, mDate);
        }

        return super.apply(context, remoteViews);
    }

    public int getIconResource() {
        return mIconResource;
    }

    public HeaderIconWidgetBuilder setIconResource(int mIconResource) {
        this.mIconResource = mIconResource;
        return this;
    }

    public RemoteViews getContentView() {
        return mContentView;
    }

    public HeaderIconWidgetBuilder setContentView(RemoteViews mContentView) {
        this.mContentView = mContentView;
        return this;
    }

    public CharSequence getDate() {
        return mDate;
    }

    public HeaderIconWidgetBuilder setDate(CharSequence mDate) {
        this.mDate = mDate;
        return this;
    }
}
