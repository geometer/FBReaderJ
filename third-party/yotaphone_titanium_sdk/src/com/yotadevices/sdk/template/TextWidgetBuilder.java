package com.yotadevices.sdk.template;

import android.content.Context;
import android.widget.RemoteViews;

import com.yotadevices.platinum.R;

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
