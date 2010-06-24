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

import android.os.*;
import android.app.*;
import android.content.Intent;
import android.widget.Toast;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.dialogs.ZLAndroidDialogManager;

abstract class SearchActivity extends Activity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		final Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	   		final String pattern = intent.getStringExtra(SearchManager.QUERY);
			final Handler successHandler = new Handler() {
				public void handleMessage(Message message) {
					onSuccess();
				}
			};
			final Handler failureHandler = new Handler() {
				public void handleMessage(Message message) {
					Toast.makeText(
						getParentActivity(),
						ZLResource.resource("errorMessage").getResource(
							getFailureMessageResourceKey()
						).getValue(),
						Toast.LENGTH_SHORT
					).show();
				}
			};
			final Runnable runnable = new Runnable() {
				public void run() {
					if (runSearch(pattern)) {
						successHandler.sendEmptyMessage(0);
					} else {
						failureHandler.sendEmptyMessage(0);
					}
				}
			};
			((ZLAndroidDialogManager)ZLAndroidDialogManager.Instance()).wait(getWaitMessageResourceKey(), runnable, getParentActivity());
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
