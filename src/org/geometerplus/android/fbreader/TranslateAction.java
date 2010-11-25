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

	private ZLAndroidWidget getWidget() {
		return ((ZLAndroidLibrary)ZLAndroidLibrary.Instance()).getWidget();
	}

	public boolean isVisible() {
		return Reader.Model != null && getWidget().myLongPressWord != "";
	}

	public void run() {
		Intent intent = new Intent(Intent.ACTION_SEARCH);
		intent.setComponent(
			new ComponentName("com.socialnmobile.colordict",
				"com.socialnmobile.colordict.activity.Main"));
		intent.putExtra(SearchManager.QUERY, getWidget().myLongPressWord);
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
