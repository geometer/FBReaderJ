package org.geometerplus.android.fbreader;

import android.os.Bundle;
import android.view.Window;
import android.view.View;
import android.content.Context;
import android.app.ListActivity;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.fbreader.bookmodel.ContentsTree;

public class TOCActivity extends ListActivity {
	public final static Object DATA_KEY = new Object();

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		ContentsTree tree = (ContentsTree)((ZLAndroidApplication)getApplication()).getData(DATA_KEY);

		ZLTreeAdapter adapter = new TOCAdapter(getListView(), tree);
		getListView().setAdapter(adapter);
		getListView().setOnKeyListener(adapter);
		getListView().setOnItemClickListener(adapter);
		/*
		int selectedIndex = adapter.getSelectedIndex();
		if (selectedIndex >= 0) {
			view.setSelection(selectedIndex);
		}
		*/
	}
}
