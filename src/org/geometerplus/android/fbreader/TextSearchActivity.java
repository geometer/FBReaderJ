/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
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

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

import org.geometerplus.android.util.UIUtil;

public class TextSearchActivity extends Activity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		final SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(null);

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
					UIUtil.showErrorMessage(getParentActivity(), getFailureMessageResourceKey());
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
			UIUtil.wait(getWaitMessageResourceKey(), runnable, getParentActivity());
		}
		finish();
	}

	private void onSuccess() {
		FBReader.Instance.showTextSearchControls(true);
	}

	private String getFailureMessageResourceKey() {
		return "textNotFound";
	}

	private String getWaitMessageResourceKey() {
		return "search";
	}

	private boolean runSearch(final String pattern) {
		final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
		fbReader.TextSearchPatternOption.setValue(pattern);
		return fbReader.getTextView().search(pattern, true, false, false, false) != 0;
	}

	private Activity getParentActivity() {
		return FBReader.Instance;
	}
}
