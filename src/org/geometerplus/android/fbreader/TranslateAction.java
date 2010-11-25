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

package org.geometerplus.android.fbreader;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import org.geometerplus.fbreader.fbreader.FBAction;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.widget.Toast;

class TranslateAction extends FBAction {
	private final FBReader myBaseActivity;

	TranslateAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(fbreader);
		myBaseActivity = baseActivity;
	}

	/*
	private ZLAndroidWidget getWidget() {
		return ((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getWidget();
	}
	*/

	public boolean isVisible() {
		return true;
		//return Reader.Model != null;// && getWidget().myLongPressWord != "";
	}

	public void run() {
		Intent intent = new Intent(Intent.ACTION_SEARCH);
		intent.setComponent(new ComponentName(
			"com.socialnmobile.colordict",
			"com.socialnmobile.colordict.activity.Main"
		));
		//intent.putExtra(SearchManager.QUERY, getWidget().myLongPressWord);
		intent.putExtra(SearchManager.QUERY, "biology");
		try {
			myBaseActivity.startActivity(intent);
		}
		catch(ActivityNotFoundException e){
			Toast.makeText(
					myBaseActivity,
					ZLResource.resource("errorMessage").getResource("dictNotInstalled").getValue(),
					Toast.LENGTH_LONG
				).show();
		}
	}
}
