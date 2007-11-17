package org.zlibrary.ui.android.library;

import android.os.Bundle;

public class ZLActivity extends android.app.Activity {
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		new ZLAndroidLibrary().run(this);
	}
}
