package com.yotadevices.sdk.template;

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
