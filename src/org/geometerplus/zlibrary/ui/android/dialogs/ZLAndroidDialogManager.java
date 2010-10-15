/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.ui.android.dialogs;

import java.util.*;

import android.app.*;
import android.content.*;
import android.os.*;

import org.geometerplus.zlibrary.core.dialogs.*;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.application.ZLAndroidApplicationWindow;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;


public class ZLAndroidDialogManager extends ZLDialogManager {
	private Activity myActivity;
	
	public ZLAndroidDialogManager() {
	}

	public void setActivity(Activity activity) {
		myActivity = activity;
	}
	
	static void runDialog(Activity activity, ZLAndroidDialogInterface dialog) {
		((ZLAndroidApplication)activity.getApplication()).putData(
			DialogActivity.DIALOG_KEY, dialog
		);
		Intent intent = new Intent();
		intent.setClass(activity, DialogActivity.class);
		activity.startActivity(intent);
	}

	public void runActivity(Class<?> activityClass, Map<String,String> data) {
		Intent intent = new Intent(myActivity.getApplicationContext(), activityClass);
		for (Map.Entry<String,String> entry : data.entrySet()) {
			intent.putExtra(entry.getKey(), entry.getValue());
		}
		myActivity.startActivity(intent);
	}

	public void runActivity(Class<?> activityClass) {
		runActivity(activityClass, Collections.<String,String>emptyMap());
	}

	public ZLOptionsDialog createOptionsDialog(String key, Runnable exitAction, Runnable applyAction, boolean showApplyButton) {
		return new ZLAndroidOptionsDialog(myActivity, getResource().getResource(key), exitAction, applyAction);
	}

	private ProgressDialog myProgress;
	private static class Pair {
		final Runnable Action;
		final String Message;

		Pair(Runnable action, String message) {
			Action = action;
			Message = message;
		}
	};
	private final Queue<Pair> myTaskQueue = new LinkedList<Pair>();
	final Handler myProgressHandler = new Handler() {
		public void handleMessage(Message message) {
			try {
				synchronized (ZLAndroidDialogManager.this) {
					if (myTaskQueue.isEmpty()) {
						myProgress.dismiss();
						myProgress = null;
					} else {
						myProgress.setMessage(myTaskQueue.peek().Message);
					}
					ZLAndroidDialogManager.this.notify();
				}
			} catch (Exception e) {
			}
		}
	};
	public void wait(String key, Runnable action) {
		wait(key, action, myActivity);
	}
	public void wait(String key, Runnable action, Activity activity) {
		synchronized (this) {
			final String message = getWaitMessageText(key);
			myTaskQueue.offer(new Pair(action, message));
			if (myProgress == null) {
				myProgress = ProgressDialog.show(activity, null, message, true, false);
			} else {
				return;
			}
		}
		final ProgressDialog currentProgress = myProgress;
		new Thread(new Runnable() {
			public void run() {
				while ((myProgress == currentProgress) && !myTaskQueue.isEmpty()) {
					Pair p = myTaskQueue.poll();
					p.Action.run();
					synchronized (ZLAndroidDialogManager.this) {
						myProgressHandler.sendEmptyMessage(0);
						try {
							ZLAndroidDialogManager.this.wait();
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}).start();
	}

	public void startSearch() {
		myActivity.onSearchRequested();
	}
}
