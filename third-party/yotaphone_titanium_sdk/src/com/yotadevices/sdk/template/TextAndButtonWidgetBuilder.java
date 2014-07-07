package com.yotadevices.sdk.template;

import com.yotadevices.platinum.R;

import android.app.PendingIntent;
import android.content.Context;
import android.widget.RemoteViews;

/**
 * Created by Alexei.Sazonov on 21.05.2014.
 */
public class TextAndButtonWidgetBuilder extends WidgetBuilder {
    private CharSequence mText;
    private CharSequence mButtonText;
    private PendingIntent mOnButtonClick;

    public CharSequence getText() {
        return mText;
    }

    public TextAndButtonWidgetBuilder setText(CharSequence mText) {
        this.mText = mText;
        return this;
    }

    @Override
    public RemoteViews apply(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.template_widget_text_and_button);
        remoteViews.setTextViewText(R.id.template_widget_text, mText);
        remoteViews.setTextViewText(R.id.template_widget_button, mButtonText);
        remoteViews.setOnClickPendingIntent(R.id.template_widget_button, mOnButtonClick);

        return super.apply(context, remoteViews);
    }

    public CharSequence getButtonText() {
        return mButtonText;
    }

    public TextAndButtonWidgetBuilder setButtonText(CharSequence mButtonText) {
        this.mButtonText = mButtonText;
        return this;
    }

    public PendingIntent getOnButtonClick() {
        return mOnButtonClick;
    }

    public TextAndButtonWidgetBuilder setOnButtonClick(PendingIntent mOnButtonClick) {
        this.mOnButtonClick = mOnButtonClick;
        return this;
    }
}
