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
import android.text.format.DateFormat;
import android.view.View;
import android.widget.RemoteViews;

import com.yotadevices.sdk.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ModernWidgetFooterTemplate extends WidgetBuilder {
	boolean mShowTime = true;
	String mText;

	boolean mShowRightButton = false;
	int mButtonLeftIcon = 0;
	int mButtonRightIcon = 0;
	String mButtonText;
	PendingIntent mButtonAction;

	RemoteViews mContentView;
	long mTime;

	@Override
	public RemoteViews apply(Context context) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.modern_widget_footer_template);
		remoteViews.removeAllViews(R.id.content);
		remoteViews.addView(R.id.content, mContentView);
		String time = getFormattedTimeAgoString(context, mTime == 0 ? System.currentTimeMillis() : mTime);
		if (!mShowTime) {
			remoteViews.setTextViewCompoundDrawables(R.id.left_text, 0, 0, 0, 0);
			if (mText != null) {
				remoteViews.setTextViewText(R.id.left_text, mText);
			}
		}
		else {
			String text = mText != null ? String.format("%s, %s", time, mText) : time;
			remoteViews.setTextViewText(R.id.left_text, text);
			remoteViews.setTextViewCompoundDrawables(R.id.left_text, R.drawable.widget_clocks_icon, 0, 0, 0);
		}

		if (mShowRightButton) {
			remoteViews.setViewVisibility(R.id.right_text, View.VISIBLE);
			remoteViews.setTextViewCompoundDrawables(R.id.right_text, mButtonLeftIcon, 0, mButtonRightIcon, 0);
			if (mButtonText != null) {
				remoteViews.setTextViewText(R.id.right_text, mButtonText);
			}
			if (mButtonAction != null) {
				remoteViews.setOnClickPendingIntent(R.id.right_text, mButtonAction);
			}
		}
		else {
			remoteViews.setViewVisibility(R.id.right_text, View.GONE);
		}
		return super.apply(context, remoteViews);
	}

	public ModernWidgetFooterTemplate showTime(boolean value) {
		mShowTime = value;
		return this;
	}

	public void setTime(long time) {
		mTime = time;
	}

	public ModernWidgetFooterTemplate setText(String text) {
		mText = text;
		return this;
	}

	public ModernWidgetFooterTemplate showRightButton(int leftIcon, String text, PendingIntent action) {
		mButtonLeftIcon = leftIcon;
		mButtonRightIcon = 0;
		mButtonText = text;
		mButtonAction = action;
		mShowRightButton = true;
		return this;
	}

	public ModernWidgetFooterTemplate showRightButton(int leftIcon, int rightIcon, PendingIntent action) {
		mButtonLeftIcon = leftIcon;
		mButtonRightIcon = rightIcon;
		mButtonText = "";
		mButtonAction = action;
		mShowRightButton = true;
		return this;
	}

	public static String getFormattedTimeAgoString(Context context, long eventTime) {
		long difference = Math.abs((System.currentTimeMillis() - eventTime));
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(c.get(Calendar.YEAR), Calendar.JANUARY, 1, 0, 0, 0);
		long timeTillEndOfYear = System.currentTimeMillis() - c.getTimeInMillis();
		if (difference < 24f * 60f * 60f * 1000f) {
			SimpleDateFormat f;
			if (DateFormat.is24HourFormat(context)) {
				f = (SimpleDateFormat) DateFormat.getTimeFormat(context);
			} else {
				f = (SimpleDateFormat) java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT, Locale.US);
			}
			return f.format(new Date(eventTime));
		} else if (difference < 30f * 24f * 60f * 60f * 1000f) {
			return context.getResources().getString(R.string.d_ago, Math.round(difference / 1000f / 60f / 60f / 24f));
		} else if (difference < timeTillEndOfYear) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");
			return sdf.format(new Date(eventTime));
		} else {
			return DateFormat.getDateFormat(context).format(new Date(eventTime));
		}
	}

	public ModernWidgetFooterTemplate setContentView(RemoteViews content) {
		mContentView = content;
		return this;
	}

}
