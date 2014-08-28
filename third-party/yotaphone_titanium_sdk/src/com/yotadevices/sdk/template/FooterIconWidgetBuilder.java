package com.yotadevices.sdk.template;

import android.app.PendingIntent;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import com.yotadevices.platinum.R;

public class FooterIconWidgetBuilder extends WidgetBuilder {
    protected PendingIntent mMoreViewPendingIntent;

    private CharSequence mLeftText;
    private CharSequence mRightText;
    private CharSequence mMoreViewText;

    private int mIconResource;
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
        return this;
    }

    public RemoteViews apply(Context context) {
        if (!mLoadingData) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.template_widget_icon_footer);
            remoteViews.setTextViewText(R.id.left_text, mLeftText);
            remoteViews.setImageViewResource(R.id.icon, mIconResource);
            remoteViews.removeAllViews(R.id.content);
            remoteViews.addView(R.id.content, mContentView);
            if (mRightText == null) {
                remoteViews.setViewVisibility(R.id.right_text, View.GONE);
            } else {
                remoteViews.setTextViewText(R.id.right_text, mRightText);
                remoteViews.setViewVisibility(R.id.right_text, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.more_layout, View.GONE);
            }
            if (mMoreViewText != null) {
                remoteViews.setViewVisibility(R.id.right_text, View.GONE);
                remoteViews.setViewVisibility(R.id.more_layout, View.VISIBLE);
                remoteViews.setTextViewText(R.id.more_text, mMoreViewText);
                if (mMoreViewPendingIntent != null) {
                    remoteViews.setOnClickPendingIntent(R.id.more_layout, mMoreViewPendingIntent);
                }
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
