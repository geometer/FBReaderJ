/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import org.geometerplus.zlibrary.core.application.ZLApplication;
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
	private static volatile Handler ourProgressHandler;

	private static boolean init() {
		if (ourProgressHandler != null) {
			return true;
		}
		try {
			ourProgressHandler = new Handler() {
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
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}

	public static void wait(String key, Runnable action, Context context) {
		if (!init()) {
			action.run();
			return;
		}

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

	public static ZLApplication.SynchronousExecutor createExecutor(final Activity activity, final String key) {
		return new ZLApplication.SynchronousExecutor() {
			private final ZLResource myResource =
				ZLResource.resource("dialog").getResource("waitMessage");
			private final String myMessage = myResource.getResource(key).getValue();
			private volatile ProgressDialog myProgress;

			public void execute(final Runnable action, final Runnable uiPostAction) {
				activity.runOnUiThread(new Runnable() {
					public void run() {
						myProgress = ProgressDialog.show(activity, null, myMessage, true, false);
						final Thread runner = new Thread() {
							public void run() {
								action.run();
								activity.runOnUiThread(new Runnable() {
									public void run() {
										try {
											myProgress.dismiss();
											myProgress = null;
										} catch (Exception e) {
											e.printStackTrace();
										}
										if (uiPostAction != null) {
											uiPostAction.run();
										}
									}
								});
							}
						};
						runner.setPriority(Thread.MAX_PRIORITY);
						runner.start();
					}
				});
			}

			private void setMessage(final ProgressDialog progress, final String message) {
				if (progress == null) {
					return;
				}
				activity.runOnUiThread(new Runnable() {
					public void run() {
						progress.setMessage(message);
					}
				});
			}

			public void executeAux(String key, Runnable runnable) {
				setMessage(myProgress, myResource.getResource(key).getValue());
				runnable.run();
				setMessage(myProgress, myMessage);
			}
		};
	}

	public static void showMessageText(final Activity activity, final String text) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
			}
		});
	}

	public static void showErrorMessage(Activity activity, String resourceKey) {
		showMessageText(
			activity,
			ZLResource.resource("errorMessage").getResource(resourceKey).getValue()
		);
	}

	public static void showErrorMessage(Activity activity, String resourceKey, String parameter) {
		showMessageText(
			activity,
			ZLResource.resource("errorMessage").getResource(resourceKey).getValue().replace("%s", parameter)
		);
	}
}
