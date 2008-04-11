package org.geometerplus.zlibrary.ui.android.library;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;

import org.geometerplus.zlibrary.core.application.ZLApplication;

import org.geometerplus.zlibrary.ui.android.util.ZLAndroidKeyUtil;

public class ZLAndroidActivity extends Activity {
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setFlags(WindowManager.LayoutParams.NO_STATUS_BAR_FLAG, WindowManager.LayoutParams.NO_STATUS_BAR_FLAG);
		setContentView(R.layout.main);
		new ZLAndroidLibrary().run(this);
	}

	private static ZLAndroidLibrary getLibrary() {
		return (ZLAndroidLibrary)ZLAndroidLibrary.getInstance();
	}

	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		getLibrary().getMainWindow().buildMenu(menu);
		return true;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		View view = findViewById(R.id.zlandroidactivity);
		return (view != null) ? view.onKeyDown(keyCode, event) : true;
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		View view = findViewById(R.id.zlandroidactivity);
		return (view != null) ? view.onKeyUp(keyCode, event) : true;
	}
}
