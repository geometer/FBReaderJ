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
import android.os.Bundle;
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
import org.geometerplus.fbreader.network.BookReference;


public class BookDownloaderService extends Service {

	public static final String BOOK_FORMAT_KEY = "org.geometerplus.android.fbreader.network.BookFormat";
	public static final String REFERENCE_TYPE_KEY = "org.geometerplus.android.fbreader.network.ReferenceType";
	public static final String CLEAN_URL_KEY = "org.geometerplus.android.fbreader.network.CleanURL";

	private volatile int myServiceCounter;

	private void doStart() {
		++myServiceCounter;
	}

	private void doStop() {
		if (--myServiceCounter == 0) {
			stopSelf();
		}
	}

	public static ZLResource getResource() {
		return ZLResource.resource("bookDownloader");
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

		final String url = uri.toString();
		final int bookFormat = intent.getIntExtra(BOOK_FORMAT_KEY, BookReference.Format.NONE);
		final int referenceType = intent.getIntExtra(REFERENCE_TYPE_KEY, BookReference.Type.UNKNOWN);
		String cleanURL = intent.getStringExtra(CLEAN_URL_KEY);
		if (cleanURL == null) {
			cleanURL = url;
		}

		String fileName = BookReference.makeBookFileName(cleanURL, bookFormat, referenceType);
		if (fileName == null) {
			doStop();
			return;
		}

		int index = fileName.lastIndexOf(File.separator);
		if (index != -1) {
			final String dir = fileName.substring(0, index);
			final File dirFile = new File(dir);
			if (!dirFile.exists() && !dirFile.mkdirs()) {
				// TODO: error message
				doStop();
				return;
			}
			if (!dirFile.exists() || !dirFile.isDirectory()) {
				// TODO: error message
				doStop();
				return;
			}
		}

		final File fileFile = new File(fileName);
		if (fileFile.exists()) {
			if (!fileFile.isFile()) {
				// TODO: error message
				doStop();
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
			doStop();
			startActivity(getFBReaderIntent(fileFile));
			return;
		}
		startFileDownload(url, fileFile);
	}

	private Intent getFBReaderIntent(final File file) {
		final Intent intent = new Intent(this, org.geometerplus.android.fbreader.FBReader.class);
		if (file != null) {
			intent.setAction(Intent.ACTION_VIEW).setData(Uri.fromFile(file));
		}
		return intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	}

	private Notification createDownloadFinishNotification(File file, boolean success) {
		final ZLResource resource = getResource();
		final String tickerText = success ?
			resource.getResource("tickerSuccess").getValue() :
			resource.getResource("tickerError").getValue();
		final String contentText = success ?
			resource.getResource("contentSuccess").getValue() :
			resource.getResource("contentError").getValue();
		final Notification notification = new Notification(
			android.R.drawable.stat_sys_download_done,
			tickerText,
			System.currentTimeMillis()
		);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		final Intent intent = getFBReaderIntent(success ? file : null);
		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
		notification.setLatestEventInfo(getApplicationContext(), file.getName(), contentText, contentIntent);
		return notification;
	}

	private Notification createDownloadProgressNotification(File file) {
		final RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.download_notification);
		String title = file.getName();
		if (title == null || title.length() == 0) {
			title = getResource().getResource("untitled").getValue();
		}
		contentView.setTextViewText(R.id.download_notification_title, title);
		contentView.setTextViewText(R.id.download_notification_progress_text, "");
		contentView.setProgressBar(R.id.download_notification_progress_bar, 100, 0, true);

		final Intent intent = getFBReaderIntent(null);
		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

		final Notification notification = new Notification();
		notification.icon = android.R.drawable.stat_sys_download;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.contentView = contentView;
		notification.contentIntent = contentIntent;

		return notification;
	}

	private void startFileDownload(final String uriString, final File file) {
		final int notificationId = (int) System.currentTimeMillis(); // notification unique identifier
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

		final Handler downloadFinishHandler = new Handler() {
			public void handleMessage(Message message) {
				final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.cancel(notificationId);
				notificationManager.notify(
					notificationId,
					createDownloadFinishNotification(file, message.what != 0)
				);
				doStop();
			}
		};

		new Thread(new Runnable() {
			public void run() {
				final int updateIntervalMillis = 1000; // FIXME: remove hardcoded time constant
				boolean downloadSuccess = false;
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
						OutputStream outStream = new FileOutputStream(file);
						try {
							InputStream inStream = httpConnection.getInputStream();
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
									/*if (downloadedPart * 100 / fileLength > 95) {
										throw new IOException();
									}*/
								}
								outStream.write(buffer, 0, size);
								/*try {
									Thread.currentThread().sleep(200);
								} catch (InterruptedException ex) {
								}*/
							}
							inStream.close();
						} finally {
							outStream.close();
						}
						downloadSuccess = true;
					}
				} catch (MalformedURLException e) {
					// TODO: error message; remove file, don't start FBReader
				} catch (IOException e) {
					// TODO: error message; remove file, don't start FBReader
				} finally {
					downloadFinishHandler.sendEmptyMessage(downloadSuccess ? 1 : 0);
					if (!downloadSuccess) {
						file.delete();
					}
				}
			}
		}).start();
	}
}
