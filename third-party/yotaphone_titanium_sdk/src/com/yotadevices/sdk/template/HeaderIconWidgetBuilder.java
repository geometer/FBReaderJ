package com.yotadevices.sdk.template;

import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import com.yotadevices.platinum.R;

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
