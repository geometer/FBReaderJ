package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.widget.Toast;

public class TranslateWordAction extends FBAction {
	TranslateWordAction(FBReaderApp fbreader) {
		super(fbreader);
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
			ZLAndroidApplication.Instance().myMainActivity.startActivity(intent);
		}
		catch(ActivityNotFoundException e){
			Toast.makeText(
					ZLAndroidApplication.Instance().myMainActivity,
					ZLResource.resource("errorMessage").getResource("dictNotInstalled").getValue(),
					Toast.LENGTH_LONG
				).show();
		}
	}
}
