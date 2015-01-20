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
import android.widget.RemoteViews;

/**
 * Created by Alexei.Sazonov on 21.05.2014.
 */
public class TextAndButtonWidgetBuilder extends WidgetBuilder {
    private CharSequence mText;
    private CharSequence mButtonText;
    private PendingIntent mOnButtonClick;

    public CharSequence getText() {
        return mText;
    }

    public TextAndButtonWidgetBuilder setText(CharSequence mText) {
        this.mText = mText;
        return this;
    }

    @Override
    public RemoteViews apply(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.template_widget_text_and_button);
        remoteViews.setTextViewText(R.id.template_widget_text, mText);
        remoteViews.setTextViewText(R.id.template_widget_button, mButtonText);
        remoteViews.setOnClickPendingIntent(R.id.template_widget_button, mOnButtonClick);

        return super.apply(context, remoteViews);
    }

    public CharSequence getButtonText() {
        return mButtonText;
    }

    public TextAndButtonWidgetBuilder setButtonText(CharSequence mButtonText) {
        this.mButtonText = mButtonText;
        return this;
    }

    public PendingIntent getOnButtonClick() {
        return mOnButtonClick;
    }

    public TextAndButtonWidgetBuilder setOnButtonClick(PendingIntent mOnButtonClick) {
        this.mOnButtonClick = mOnButtonClick;
        return this;
    }
}
