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

/**
 * Created by Alexei.Sazonov on 21.05.2014.
 */
public class CustomWidgetBuilder extends WidgetBuilder {
    private CharSequence mText;
    private int mLayout;

    public CharSequence getText() {
        return mText;
    }

    public CustomWidgetBuilder(int layout) {
        mLayout = layout;
    }

    public CustomWidgetBuilder setText(CharSequence mText) {
        this.mText = mText;
        return this;
    }

    public RemoteViews apply(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), mLayout);

        return super.apply(context, remoteViews);
    }
}
