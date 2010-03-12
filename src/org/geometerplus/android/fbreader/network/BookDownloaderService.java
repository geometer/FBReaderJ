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

import java.util.List;
import java.util.LinkedList;
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

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.fbreader.Constants;


public class BookDownloaderService extends Service {

	private LinkedList<Integer> myStartIds = new LinkedList<Integer>();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		myStartIds.offer(startId);

		final Uri uri = intent.getData();
		if (uri == null || !BookDownloader.acceptsUri(uri)) {
			stopSelf(myStartIds.poll().intValue());
			return;
		}
		intent.setData(null);

		String host = uri.getHost();
		if (host.equals("www.feedbooks.com")) {
			host = "feedbooks.com";
		}
		String dir = Constants.BOOKS_DIRECTORY + "/" + host;
		final List<String> path = uri.getPathSegments();
		for (int i = 0; i < path.size() - 1; ++i) {
			dir += '/' + path.get(i);
		}
		final File dirFile = new File(dir);
		dirFile.mkdirs();
		if (!dirFile.isDirectory()) {
			// TODO: error message
			stopSelf(myStartIds.poll().intValue());
			return;
		}

		String fileName = BookDownloader.getFileName(path);
		final File fileFile = new File(dirFile, fileName);
		if (fileFile.exists()) {
			if (!fileFile.isFile()) {
				// TODO: error message
				stopSelf(myStartIds.poll().intValue());
				return;
			}
			// TODO: question box: redownload?
			/*
			ZLDialogManager.Instance().showQuestionBox(
				"redownloadBox", "Redownload?",
				"no", null,
				"yes", null,
				null, null
			);
			*/
			stopSelf(myStartIds.poll().intValue());
			startActivity(getFBReaderIntent(fileFile));
			return;
		}
		startFileDownload(uri.toString(), fileFile);

		//textView.setText(ZLDialogManager.getWaitMessageText("downloadingFile").replace("%s", myFileName));
	}

	private Intent getFBReaderIntent(final File file) {
		return new Intent(Intent.ACTION_VIEW, Uri.fromFile(file), this, org.geometerplus.android.fbreader.FBReader.class)
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	}

	private Notification createDownloadSuccessfulNotification(File file) {
		final Notification notification = new Notification(
			android.R.drawable.stat_sys_download_done,
			"Book has been downloaded", // TODO: i18n
			System.currentTimeMillis()
		);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		final PendingIntent contentIntent = PendingIntent.getActivity(
			this,
			0,
			getFBReaderIntent(file),
			0
		);
		notification.setLatestEventInfo(
			getApplicationContext(),
			file.getName(),
			"Download successful", // TODO: i18n
			contentIntent
		);
		return notification;
	}

	private Notification createDownloadProgressNotification(File file) {
		final RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.download_notification);
		String title = file.getName();
		if (title == null || title.length() == 0) {
			title = "<Untitled>"; // TODO: i18n
		}
		contentView.setTextViewText(R.id.download_notification_title, title);
		contentView.setTextViewText(R.id.download_notification_progress_text, "");
		contentView.setProgressBar(R.id.download_notification_progress_bar, 100, 0, true);

		final Intent intent = new Intent(this, org.geometerplus.android.fbreader.FBReader.class)
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
			.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

		final Notification notification = new Notification();
		notification.icon = android.R.drawable.stat_sys_download;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.contentView = contentView;
		notification.contentIntent = contentIntent;

		return notification;
	}

	private void startFileDownload(final String uriString, final File file) {
		final int notificationId = file.getPath().hashCode();
		final Notification progressNotification = createDownloadProgressNotification(file);

		final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(notificationId, progressNotification);

		final Handler progressHandler = new Handler() {
			public void handleMessage(Message message) {
				final int progress = message.what;
				final RemoteViews contentView = (RemoteViews)progressNotification.contentView;

				if (progress < 0) {
					contentView.setTextViewText(R.id.download_notification_progress_text, "");
					contentView.setProgressBar(R.id.download_notification_progress_bar, 100, 0, true);
				} else {
					contentView.setTextViewText(R.id.download_notification_progress_text, "" + progress + "%");
					contentView.setProgressBar(R.id.download_notification_progress_bar, 100, progress, false);
				}
				final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(notificationId, progressNotification);
			}
		};

		final Handler downloadSuccessfulHandler = new Handler() {
			public void handleMessage(Message message) {
				final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.cancel(notificationId);
				notificationManager.notify(
					notificationId,
					createDownloadSuccessfulNotification(file)
				);
				stopSelf(myStartIds.poll().intValue());
			}
		};

		new Thread(new Runnable() {
			public void run() {
				final int updateIntervalMillis = 1000; // FIXME: remove hardcoded time constant
				try {
					final URL url = new URL(uriString);
					final URLConnection connection = url.openConnection();
					final int fileLength = connection.getContentLength();
					int downloadedPart = 0;
					long progressTime = System.currentTimeMillis() + updateIntervalMillis;
					if (fileLength <= 0) {
						progressHandler.sendEmptyMessage(-1);
					}
					final HttpURLConnection httpConnection = (HttpURLConnection)connection;
					final int response = httpConnection.getResponseCode();
					if (response == HttpURLConnection.HTTP_OK) {
						InputStream inStream = httpConnection.getInputStream();
						OutputStream outStream = new FileOutputStream(file);
						final byte[] buffer = new byte[8192];
						int fullSize = 0;	
						while (true) {
							final int size = inStream.read(buffer);
							if (size <= 0) {
								break;
							}
							downloadedPart += size;
							if (fileLength > 0) {
								final long currentTime = System.currentTimeMillis();
								if (currentTime > progressTime) {
									progressTime = currentTime + updateIntervalMillis;
									progressHandler.sendEmptyMessage(downloadedPart * 100 / fileLength);
								}
							}
							outStream.write(buffer, 0, size);
							/*try {
								Thread.currentThread().sleep(200);
							} catch (InterruptedException ex) {
							}*/
						}
						inStream.close();
						outStream.close();
					}
				} catch (MalformedURLException e) {
					// TODO: error message; remove file, don't start FBReader
				} catch (IOException e) {
					// TODO: error message; remove file, don't start FBReader
				}
				downloadSuccessfulHandler.sendEmptyMessage(0);
			}
		}).start();
	}
}
