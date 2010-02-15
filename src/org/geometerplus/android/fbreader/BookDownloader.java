/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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

import java.util.List;
import java.io.*;
import java.net.*;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.net.Uri;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import org.geometerplus.zlibrary.core.dialogs.ZLDialogManager;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.Constants;

public class BookDownloader extends Activity {
	private ProgressBar myProgressBar;
	private int myFileLength = -1;
	private int myDownloadedPart = 0;
	private String myFileName = "";

	private static String getFileName(List<String> path) {
		final String fileName = path.get(path.size() - 1).toLowerCase();
		final int index = fileName.indexOf('?');
		return (index >= 0) ? fileName.substring(0, index) : fileName;
	}

	public static boolean acceptsUri(Uri uri) {
		final List<String> path = uri.getPathSegments();
		if ((path == null) || path.isEmpty()) {
			return false;
		}

		final String fileName = getFileName(path);
		return
			fileName.endsWith(".fb2.zip") ||
			fileName.endsWith(".fb2") ||
			fileName.endsWith(".epub") ||
			fileName.endsWith(".mobi") ||
			fileName.endsWith(".prc");
	}

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setRequestedOrientation(
			ZLAndroidApplication.Instance().AutoOrientationOption.getValue() ?
				ActivityInfo.SCREEN_ORIENTATION_SENSOR :
				ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
		);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.downloader);
		myProgressBar = (ProgressBar)findViewById(android.R.id.progress);

		final Intent intent = getIntent();
		final Uri uri = intent.getData();
		if (uri != null) {
			intent.setData(null);

			if (!acceptsUri(uri)) {
				startNextMatchingActivity(intent);
				finish();
				return;
			}

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
				finish();
				return;
			}

			myFileName = getFileName(path);
			final File fileFile = new File(dirFile, myFileName);
			if (fileFile.exists()) {
				if (!fileFile.isFile()) {
					// TODO: error message
					finish();
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
				runFBReader(fileFile);
				return;
			}
			startFileDownload(uri.toString(), fileFile);
		}

		if (myFileLength <= 0) {
			myProgressBar.setIndeterminate(true);
		} else {
			myProgressBar.setIndeterminate(false);
			myProgressBar.setMax(myFileLength);
			myProgressBar.setProgress(myDownloadedPart);
		}

		final TextView textView = (TextView)findViewById(R.id.downloadertext);
		textView.setText(ZLDialogManager.getWaitMessageText("downloadingFile").replace("%s", myFileName));
	}

	private void runFBReader(final File file) {
		finish();
		final Activity oldActivity = org.geometerplus.android.fbreader.FBReader.Instance;
		if (oldActivity != null) {
			oldActivity.finish();
		}
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromFile(file), this, FBReader.class));
	}

	private void startFileDownload(final String uriString, final File file) {
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				try {
					runFBReader(file);
				} catch (Exception e) {
				}
			}
		};
		new Thread(new Runnable() {
			public void run() {
				try {
					final URL url = new URL(uriString);
					final URLConnection connection = url.openConnection();
					myFileLength = connection.getContentLength();
					if (myFileLength > 0) {
						myProgressBar.setIndeterminate(false);
						myProgressBar.setMax(myFileLength);
						myProgressBar.setProgress(0);
						myDownloadedPart = 0;
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
							myDownloadedPart += size;
							myProgressBar.setProgress(myDownloadedPart);
							outStream.write(buffer, 0, size);
						}
						inStream.close();
						outStream.close();
					}
				} catch (MalformedURLException e) {
					// TODO: error message; remove file, don't start FBReader
				} catch (IOException e) {
					// TODO: error message; remove file, don't start FBReader
				}
				handler.sendEmptyMessage(0);
				myFileLength = -1;
			}
		}).start();
	}
}
