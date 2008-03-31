package org.zlibrary.ui.android.library;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;

import org.zlibrary.core.application.ZLApplication;

import org.zlibrary.ui.android.util.ZLAndroidKeyUtil;

public class ZLAndroidActivity extends Activity {
	public static long StartTime;

	public void onCreate(Bundle icicle) {
		StartTime = System.currentTimeMillis();
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
