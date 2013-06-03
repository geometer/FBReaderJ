package org.geometerplus.android.fbreader.network;

import org.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class NetworkLibraryFilterActivity extends Activity {
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.network_library_filter);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		int id = R.id.network_book_info_title_test;
		((TextView)findViewById(id)).setText("Test");
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
}
