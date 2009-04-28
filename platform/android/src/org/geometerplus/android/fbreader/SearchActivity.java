/*
 * Copyright (C) 2009 Geometer Plus <contact@geometerplus.com>
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

import android.os.*;
import android.app.*;
import android.content.Intent;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.dialogs.ZLDialogManager;

abstract class SearchActivity extends Activity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	   		final String pattern = intent.getStringExtra(SearchManager.QUERY);
			final Handler successHandler = new Handler() {
				public void handleMessage(Message message) {
					onSuccess();
				}
			};
			final Handler failureHandler = new Handler() {
				private AlertDialog myAlertDialog;
				public void handleMessage(Message message) {
					switch (message.what) {
						case 0:
							onFailure();
 							myAlertDialog =
								new AlertDialog.Builder(getParentActivity()).setMessage(
									ZLResource.resource("errorMessage").getResource(getFailureMessageResourceKey()).getValue()
								).create();
							myAlertDialog.show();
							break;
						case 1:
							myAlertDialog.dismiss();
							break;
					}
				}
			};
			ZLDialogManager.Instance().wait(getWaitMessageResourceKey(), new Runnable() {
				public void run() {
					if (runSearch(pattern)) {
						successHandler.sendEmptyMessage(0);
					} else {
						failureHandler.sendEmptyMessage(0);
						new Thread(new Runnable() {
							public synchronized void run() {
								try {
									wait(3000);
								} catch (InterruptedException e) {
								}
								failureHandler.sendEmptyMessage(1);
							}
						}).start();
					}
				}
			});
		}
		finish();
	}

	abstract boolean runSearch(String pattern);
	abstract void onSuccess();
	abstract void onFailure();
	abstract String getWaitMessageResourceKey();
	abstract String getFailureMessageResourceKey();
	abstract Activity getParentActivity();
}
