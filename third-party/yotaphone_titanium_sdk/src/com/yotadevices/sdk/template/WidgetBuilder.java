package com.yotadevices.sdk.template;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.yotadevices.platinum.R;

/**
 * Created by Alexei.Sazonov on 21.05.2014.
 */
abstract class WidgetBuilder {
    protected PendingIntent mMaxViewPendingIntent;

    abstract RemoteViews apply(Context context);

    protected RemoteViews apply(Context context, RemoteViews remoteViews) {
        remoteViews.setOnClickPendingIntent(R.id.template_widget_root, mMaxViewPendingIntent);
        return remoteViews;
    }

    public void setMaxViewActivity(Context context, Intent intent) {
        mMaxViewPendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
