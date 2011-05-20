/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.util;

import java.util.Queue;
import java.util.LinkedList;

import android.content.Context;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import org.geometerplus.zlibrary.core.resources.ZLResource;

public abstract class UIUtil {
	private static final Object ourMonitor = new Object();
	private static ProgressDialog ourProgress;
	private static class Pair {
		final Runnable Action;
		final String Message;

		Pair(Runnable action, String message) {
			Action = action;
			Message = message;
		}
	};
	private static final Queue<Pair> ourTaskQueue = new LinkedList<Pair>();
	private static final Handler ourProgressHandler = new Handler() {
		public void handleMessage(Message message) {
			try {
				synchronized (ourMonitor) {
					if (ourTaskQueue.isEmpty()) {
						ourProgress.dismiss();
						ourProgress = null;
					} else {
						ourProgress.setMessage(ourTaskQueue.peek().Message);
					}
					ourMonitor.notify();
				}
			} catch (Exception e) {
			}
		}
	};
	public static void wait(String key, Runnable action, Context context) {
		synchronized (ourMonitor) {
			final String message =
				ZLResource.resource("dialog").getResource("waitMessage").getResource(key).getValue();
			ourTaskQueue.offer(new Pair(action, message));
			if (ourProgress == null) {
				ourProgress = ProgressDialog.show(context, null, message, true, false);
			} else {
				return;
			}
		}
		final ProgressDialog currentProgress = ourProgress;
		new Thread(new Runnable() {
			public void run() {
				while ((ourProgress == currentProgress) && !ourTaskQueue.isEmpty()) {
					Pair p = ourTaskQueue.poll();
					p.Action.run();
					synchronized (ourMonitor) {
						ourProgressHandler.sendEmptyMessage(0);
						try {
							ourMonitor.wait();
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}).start();
	}

	public static void runWithMessage(Context context, String key, final Runnable action, final Runnable postAction) {
		final String message =
			ZLResource.resource("dialog").getResource("waitMessage").getResource(key).getValue();
		final ProgressDialog progress = ProgressDialog.show(context, null, message, true, false);

		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				progress.dismiss();
				postAction.run();
			}
		};

		final Thread runner = new Thread(new Runnable() {
			public void run() {
				action.run();
				handler.sendEmptyMessage(0);
			}
		});
		runner.setPriority(Thread.MIN_PRIORITY);
		runner.start();
	}

	public static void showMessageText(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	public static void showErrorMessage(Context context, String resourceKey) {
		showMessageText(
			context,
			ZLResource.resource("errorMessage").getResource(resourceKey).getValue()
		);
	}

	public static void showErrorMessage(Context context, String resourceKey, String parameter) {
		showMessageText(
			context,
			ZLResource.resource("errorMessage").getResource(resourceKey).getValue().replace("%s", parameter)
		);
	}
}
