package com.yotadevices.yotaphone2.fbreader.util;

import android.content.Context;
import android.text.format.DateFormat;

import com.yotadevices.yotaphone2.yotareader.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {
	public static String getFormattedTimeAgoString(Context context, long eventTime) {
		long difference = Math.abs((System.currentTimeMillis() - eventTime));
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(c.get(Calendar.YEAR),Calendar.JANUARY,1,0,0,0);
		long timeTillEndOfYear=System.currentTimeMillis()-c.getTimeInMillis();
		if (difference<24f*60f*60f*1000f) {
			SimpleDateFormat f;
			if (DateFormat.is24HourFormat(context)) {
				f = (SimpleDateFormat) DateFormat.getTimeFormat(context);
			} else {
				f = (SimpleDateFormat) java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT, Locale.US);
			}
			return f.format(new Date(eventTime));
		} else if (difference<30f*24f*60f*60f*1000f) {
			return context.getResources().getString(R.string.d_ago, Math.round(difference / 1000f / 60f / 60f / 24f));
		} else if (difference<timeTillEndOfYear) {
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");
			return sdf.format(new Date(eventTime));
		} else {
			return DateFormat.getDateFormat(context).format(new Date(eventTime));
		}
	}
}
