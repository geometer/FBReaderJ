package org.geometerplus.android.fbreader.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.android.fbreader.FBReader;

public class SimpleWidgetProvider extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final Intent intent = FBReader.defaultIntent(context);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_simple);
		views.setOnClickPendingIntent(R.id.widget_simple, pendingIntent);
		for (int id : appWidgetIds) {
			appWidgetManager.updateAppWidget(id, views);
		}
	}
}
