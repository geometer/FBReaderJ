/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.dialogs.ZLDialogManager;

import org.geometerplus.zlibrary.ui.android.R;

public class BookDownloader extends Activity {
	private ProgressBar myProgressBar;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final Intent intent = getIntent();
		final Uri uri = intent.getData();
		if (uri == null) {
			finish();
			return;
		}

		final List<String> path = uri.getPathSegments();
		final String fileName = path.get(path.size() - 1);
		if (!fileName.endsWith(".fb2.zip") && !fileName.endsWith(".fb2") && !fileName.endsWith(".epub")) {
			startNextMatchingActivity(intent);
			finish();
			return;
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.downloader);
		myProgressBar = (ProgressBar)findViewById(android.R.id.progress);
		myProgressBar.setIndeterminate(true);
		final TextView text = (TextView)findViewById(R.id.downloadertext);
		text.setText(ZLDialogManager.getWaitMessageText("downloadingFile").replace("%s", fileName));
		intent.setData(null);
		startFileDownload(uri);
	}

	private void startFileDownload(final Uri uri) {
		final String uriString = uri.toString();
		final List<String> path = uri.getPathSegments();
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				try {
					finish();
				} catch (Exception e) {
				}
			}
		};
		new Thread(new Runnable() {
			public void run() {
				try {
					final URL url = new URL(uriString);
					final URLConnection connection = url.openConnection();
					final int length = connection.getContentLength();
					if (length > 0) {
						myProgressBar.setIndeterminate(false);
						myProgressBar.setMax(length);
						myProgressBar.setProgress(0);
					}
					final HttpURLConnection httpConnection = (HttpURLConnection)connection;
					final int response = httpConnection.getResponseCode();
					if (response == HttpURLConnection.HTTP_OK) {
						InputStream inStream = httpConnection.getInputStream();
						OutputStream outStream = new FileOutputStream("/sdcard/Books/xxx1.epub");
						final byte[] buffer = new byte[8192];
						int fullSize = 0;	
						while (true) {
							final int size = inStream.read(buffer);
							if (size <= 0) {
								break;
							}
							myProgressBar.incrementProgressBy(size);
							outStream.write(buffer, 0, size);
						}
						inStream.close();
						outStream.close();
					}
				} catch (MalformedURLException e) {
				} catch (IOException e) {
				}
				handler.sendEmptyMessage(0);
			}
		}).start();
	}
}
