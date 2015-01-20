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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.yotadevices.sdk.R;

/**
 * Created by Alexei.Sazonov on 21.05.2014.
 */
abstract class WidgetBuilder {
    protected PendingIntent mMaxViewPendingIntent;

    abstract RemoteViews apply(Context context);

    protected RemoteViews apply(Context context, RemoteViews remoteViews) {
        remoteViews.setOnClickPendingIntent(R.id.widget_root, mMaxViewPendingIntent);
        return remoteViews;
    }

    public void setMaxViewActivity(PendingIntent intent) {
        mMaxViewPendingIntent = intent;
    }
}
