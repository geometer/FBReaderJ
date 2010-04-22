/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.network;

import java.util.*;
import java.io.*;
import java.net.*;

import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.app.Service;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.net.Uri;
import android.content.Intent;
import android.content.Context;
import android.widget.RemoteViews;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;


public class CatalogDownloaderService extends Service {

	private volatile int myServiceCounter;

	private void doStart() {
		++myServiceCounter;
	}

	private void doStop() {
		if (--myServiceCounter == 0) {
			stopSelf();
		}
	}


	private static final int ourProcessingNotificationId = (int) System.currentTimeMillis();

	private void updateProgressNotification() {
		final RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.download_notification);
		String title = ZLResource.resource("networkView").getResource("downloadingCatalogs").getValue();
		contentView.setTextViewText(R.id.download_notification_title, title);
		contentView.setTextViewText(R.id.download_notification_progress_text, "");
		contentView.setProgressBar(R.id.download_notification_progress_bar, 100, 0, true);

		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);

		final Notification notification = new Notification();
		notification.icon = android.R.drawable.stat_notify_sync;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.contentView = contentView;
		notification.contentIntent = contentIntent;
		notification.number = myServiceCounter;

		final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(ourProcessingNotificationId, notification);
	}

	private void startProgressNotification() {
		updateProgressNotification();
	}

	private void endProgressNotification() {
		if (myServiceCounter == 0) {
			final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(ourProcessingNotificationId);
		} else {
			updateProgressNotification();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(ourProcessingNotificationId);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		doStart();

		final Uri uri = intent.getData();
		if (uri == null) {
			doStop();
			return;
		}
		intent.setData(null);

		if (NetworkLibraryActivity.Instance == null) {
			doStop();
			return;
		}

		final Handler finishHandler = new Handler() {
			public void handleMessage(Message message) {
				doStop();
				endProgressNotification();
			}
		};

		startProgressNotification();

		final Runnable runnable = NetworkLibraryActivity.Instance.getCatalogRunnable(uri);
		final Thread loader = new Thread(new Runnable() {
			public void run() {
				runnable.run();
				finishHandler.sendEmptyMessage(0);
			}
		});
		loader.setPriority(Thread.MIN_PRIORITY);
		loader.start();
	}
}
