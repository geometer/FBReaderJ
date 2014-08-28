package com.yotadevices.sdk.template;

import android.content.Context;
import android.widget.RemoteViews;

import com.yotadevices.platinum.R;

public class TinyWidgetBuilder extends WidgetBuilder {
    private CharSequence mLeftText;

    private RemoteViews mContentView;
    private boolean mLoadingData = false;

    public CharSequence getLeftText() {
        return mLeftText;
    }

    public TinyWidgetBuilder setLeftText(CharSequence text) {
        mLeftText = text;
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
