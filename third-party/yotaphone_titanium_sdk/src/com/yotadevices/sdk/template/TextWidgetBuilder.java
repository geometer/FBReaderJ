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

/**
 * Created by Alexei.Sazonov on 21.05.2014.
 */
public class TextWidgetBuilder extends WidgetBuilder {
    private CharSequence mText;

    public CharSequence getText() {
        return mText;
    }

    public TextWidgetBuilder setText(CharSequence mText) {
        this.mText = mText;
        return this;
    }

    public RemoteViews apply(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.template_widget_text);
        remoteViews.setTextViewText(R.id.template_widget_text, mText);

        return super.apply(context, remoteViews);
    }
}
