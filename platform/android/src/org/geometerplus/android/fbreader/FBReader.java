/*
 * Copyright (C) 2009 Geometer Plus <contact@geometerplus.com>
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

import android.app.SearchManager;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.content.pm.ActivityInfo;
import android.widget.RelativeLayout;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidActivity;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.ui.android.R;

public class FBReader extends ZLAndroidActivity {
	static FBReader Instance;

	private int myFullScreenFlag;

	private static class TextSearchButtonPanel implements ZLApplication.ButtonPanel {
		boolean Visible;
		TextSearchControls TextSearchControls;

		public void hide() {
			Visible = false;
			if (TextSearchControls != null) {
				TextSearchControls.hide(false);
			}
		}

		public void updateStates() {
			if (TextSearchControls != null) {
				TextSearchControls.updateStates();
			}
		}
	}
	private static TextSearchButtonPanel myPanel;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Instance = this;
		final ZLAndroidApplication application = ZLAndroidApplication.Instance();
		myFullScreenFlag = 
			application.ShowStatusBarOption.getValue() ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN, myFullScreenFlag
		);
		if (myPanel == null) {
			myPanel = new TextSearchButtonPanel();
			ZLApplication.Instance().registerButtonPanel(myPanel);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		final ZLAndroidApplication application = ZLAndroidApplication.Instance();

		final int fullScreenFlag = 
			application.ShowStatusBarOption.getValue() ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
		if (fullScreenFlag != myFullScreenFlag) {
			startActivity(new Intent(this, this.getClass()));
			finish();
		}
		setRequestedOrientation(
			application.AutoOrientationOption.getValue() ?
				ActivityInfo.SCREEN_ORIENTATION_SENSOR :
				ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
		);

		if (myPanel.TextSearchControls == null) {
			myPanel.TextSearchControls = new TextSearchControls(this);
        
			RelativeLayout root = (RelativeLayout)findViewById(R.id.root_view);
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
            p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            p.addRule(RelativeLayout.CENTER_HORIZONTAL);
            root.addView(myPanel.TextSearchControls, p);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (myPanel.TextSearchControls != null) {
			myPanel.TextSearchControls.setVisibility(myPanel.Visible ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public void onPause() {
		if (myPanel.TextSearchControls != null) {
			myPanel.Visible = myPanel.TextSearchControls.getVisibility() == View.VISIBLE;
		}
		super.onPause();
	}

	@Override
	public void onStop() {
		if (myPanel.TextSearchControls != null) {
			myPanel.TextSearchControls.hide(false);
			myPanel.TextSearchControls = null;
		}
		super.onStop();
	}

	void showTextSearchControls(boolean show) {
		if (myPanel.TextSearchControls != null) {
			if (show) {
				myPanel.TextSearchControls.show(true);
			} else {
				myPanel.TextSearchControls.hide(false);
			}
		}
	}

	protected ZLApplication createApplication(String fileName) {
		new SQLiteBooksDatabase();
		String[] args = (fileName != null) ? new String[] { fileName } : new String[0];
		return new org.geometerplus.fbreader.fbreader.FBReader(args);
	}

	@Override
	public boolean onSearchRequested() {
		if (myPanel.TextSearchControls != null) {
			final boolean visible = myPanel.TextSearchControls.getVisibility() == View.VISIBLE;
			myPanel.TextSearchControls.hide(false);
			SearchManager manager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
			manager.setOnCancelListener(new SearchManager.OnCancelListener() {
				public void onCancel() {
					if ((myPanel.TextSearchControls != null) && visible) {
						myPanel.TextSearchControls.show(false);
					}
				}
			});
		}
		final org.geometerplus.fbreader.fbreader.FBReader fbreader =
			(org.geometerplus.fbreader.fbreader.FBReader)ZLApplication.Instance();
		startSearch(fbreader.TextSearchPatternOption.getValue(), true, null, false);
		return true;
	}
}
