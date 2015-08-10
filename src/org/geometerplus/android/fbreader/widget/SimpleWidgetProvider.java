/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.*;
import android.widget.RemoteViews;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.android.fbreader.FBReader;

public class SimpleWidgetProvider extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final Intent intent = FBReader.defaultIntent(context);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		for (int id : appWidgetIds) {
			final SharedPreferences prefs = getSharedPreferences(context, id);
			final RemoteViews views =
				new RemoteViews(context.getPackageName(), R.layout.widget_simple);
			final int iconId = prefs.getInt("icon", -1);
			if (iconId != -1) {
				views.setImageViewResource(R.id.widget_simple, iconId);
			}
			views.setOnClickPendingIntent(R.id.widget_simple, pendingIntent);
			appWidgetManager.updateAppWidget(id, views);
		}
	}

	static SharedPreferences getSharedPreferences(Context context, int widgetId) {
		return context.getApplicationContext().getSharedPreferences(
			"SimpleWidget" + widgetId, Context.MODE_PRIVATE
		);
	}
}
