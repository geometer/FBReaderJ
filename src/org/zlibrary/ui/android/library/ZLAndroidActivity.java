package org.zlibrary.ui.android.library;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import org.zlibrary.core.application.ZLApplication;

public class ZLAndroidActivity extends Activity {
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		new ZLAndroidLibrary().run(this);
	}

	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		((ZLAndroidLibrary)ZLAndroidLibrary.getInstance()).getMainWindow().buildMenu(menu);
		return true;
	}
}
