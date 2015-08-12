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

package org.geometerplus.android.fbreader.widget.simple;

import java.util.Arrays;
import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.*;
import android.widget.RemoteViews;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.library.LibraryActivity;

public class Provider extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		for (int id : appWidgetIds) {
			setupViews(appWidgetManager, context, id);
		}
	}

	// ++++ these constants are used for storing widget state persistently ++++
	// ++++ never change them, just add new values ++++
	interface Key {
		String ICON = "icon";
		String ACTION = "action";
	}

	interface Icon {
		int FBREADER = 0;
		int CLASSIC = 1;
		int LIBRARY = 2;
		int LIBRARY_OLD = 3;
	}

	interface Action {
		String BOOK = "book";
		String LIBRARY = "library";

		List<String> ALL = Arrays.asList(BOOK, LIBRARY);
	}
	// ---- these constants are used for storing widget state persistently ----

	static int iconId(int icon) {
		switch (icon) {
			default:
			case Icon.FBREADER:
				return R.drawable.fbreader;
			case Icon.CLASSIC:
				return R.drawable.classic;
			case Icon.LIBRARY:
				return R.drawable.library_old;
			case Icon.LIBRARY_OLD:
				return R.drawable.library_old;
		}
	}

	static String defaultAction(int icon) {
		switch (icon) {
			default:
			case Icon.FBREADER:
			case Icon.CLASSIC:
				return Action.BOOK;
			case Icon.LIBRARY:
			case Icon.LIBRARY_OLD:
				return Action.LIBRARY;
		}
	}

	static void setupViews(AppWidgetManager appWidgetManager, Context context, int widgetId) {
		final SharedPreferences prefs = getSharedPreferences(context, widgetId);
		final RemoteViews views =
			new RemoteViews(context.getPackageName(), R.layout.widget_simple);

		views.setImageViewResource(
			R.id.widget_simple, iconId(prefs.getInt(Key.ICON, Icon.FBREADER))
		);

		final String action = prefs.getString(Key.ACTION, Action.BOOK);
		final Intent intent;
		if (Action.LIBRARY.equals(action)) {
			intent = new Intent(context, LibraryActivity.class);
			intent.setAction("android.fbreader.action.EXTERNAL_INTERNAL_LIBRARY");
		} else /* if Action.BOOK.equals(action) */ {
			intent = FBReader.defaultIntent(context);
		}
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		views.setOnClickPendingIntent(R.id.widget_simple, pendingIntent);

		appWidgetManager.updateAppWidget(widgetId, views);
	}

	static SharedPreferences getSharedPreferences(Context context, int widgetId) {
		return context.getApplicationContext().getSharedPreferences(
			"SimpleWidget" + widgetId, Context.MODE_PRIVATE
		);
	}
}
