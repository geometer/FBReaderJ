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

package org.geometerplus.android.fbreader;

import java.util.ArrayList;

import android.app.*;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.network.SQLiteCookieDatabase;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.network.NetworkImage;
import org.geometerplus.fbreader.network.sync.SyncData;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

import org.geometerplus.android.fbreader.network.BookDownloaderService;
import org.geometerplus.android.fbreader.sync.MissingBookActivity;

class AppNotifier implements FBReaderApp.Notifier {
	private final Activity myActivity;
	private final ArrayList<String> myLatestHashes = new ArrayList<String>();
	private volatile long myLatestNotificationStamp;

	AppNotifier(Activity activity) {
		myActivity = activity;
	}

	@Override
	public void showMissingBookNotification(final SyncData.ServerBookInfo info) {
		synchronized (this) {
			myLatestHashes.retainAll(info.Hashes);
			if (!myLatestHashes.isEmpty() &&
				myLatestNotificationStamp > System.currentTimeMillis() - 5 * 60 * 1000) {
				return;
			}
			myLatestHashes.addAll(info.Hashes);
			myLatestNotificationStamp = System.currentTimeMillis();
		}
		new Thread() {
			public void run() {
				showMissingBookNotificationInternal(info);
			}
		}.start();
	}

	private void showMissingBookNotificationInternal(SyncData.ServerBookInfo info) {
		final String errorTitle = MissingBookActivity.errorTitle();

		final NotificationManager notificationManager =
			(NotificationManager)myActivity.getSystemService(Activity.NOTIFICATION_SERVICE);
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(myActivity)
			.setSmallIcon(R.drawable.fbreader)
			.setTicker(errorTitle)
			.setContentTitle(errorTitle)
			.setContentText(info.Title)
			.setAutoCancel(false);

		if (info.ThumbnailUrl != null) {
			SQLiteCookieDatabase.init(myActivity);
			final NetworkImage thumbnail = new NetworkImage(info.ThumbnailUrl, Paths.systemInfo(myActivity));
			thumbnail.synchronize();
			try {
				builder.setLargeIcon(
					BitmapFactory.decodeStream(thumbnail.getRealImage().inputStream())
				);
			} catch (Throwable t) {
				// ignore
			}
		}

		Uri uri = null;
		try {
			uri = Uri.parse(info.DownloadUrl);
		} catch (Exception e) {
		}
		if (uri != null) {
			final boolean useBigNotification =
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;

			final Intent downloadIntent = useBigNotification
				? new Intent(myActivity, BookDownloaderService.class)
				: new Intent(myActivity, MissingBookActivity.class);
			downloadIntent
				.setData(uri)
				.putExtra(BookDownloaderService.Key.FROM_SYNC, true)
				.putExtra(BookDownloaderService.Key.BOOK_MIME, info.Mimetype)
				.putExtra(BookDownloaderService.Key.BOOK_KIND, UrlInfo.Type.Book)
				.putExtra(BookDownloaderService.Key.BOOK_TITLE, info.Title);
			if (useBigNotification) {
				final String errorMessage = MissingBookActivity.errorMessage(info.Title);
				builder.setStyle(new NotificationCompat.BigTextStyle().bigText(errorMessage));
				final ZLResource buttonResource =
					ZLResource.resource("dialog").getResource("button");
				final PendingIntent pi =
					PendingIntent.getService(myActivity, 0, downloadIntent, 0);
				builder.addAction(
					android.R.drawable.stat_sys_download_done,
					buttonResource.getResource("download").getValue(),
					pi
				);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					builder.setFullScreenIntent(pi, true);
				}
			} else {
				builder.setContentIntent(PendingIntent.getActivity(myActivity, 0, downloadIntent, 0));
			}
		} else {
			builder.setContentIntent(PendingIntent.getActivity(myActivity, 0, new Intent(), 0));
		}
		notificationManager.notify(NotificationUtil.MISSING_BOOK_ID, builder.build());
	}
}
