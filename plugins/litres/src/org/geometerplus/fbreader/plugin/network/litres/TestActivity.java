package org.geometerplus.fbreader.plugin.network.litres;

import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;

public class TestActivity extends Activity {
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		try {
			System.err.println(getApplication().getPackageManager().getPackageInfo(getApplication().getPackageName(), 0).versionCode);
		} catch (PackageManager.NameNotFoundException e) {
		}
		finish();
	}
}
