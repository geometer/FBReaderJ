package com.yotadevices.sdk.template;

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
