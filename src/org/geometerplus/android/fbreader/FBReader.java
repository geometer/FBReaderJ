/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidActivity;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.fbreader.ActionCode;

public final class FBReader extends ZLAndroidActivity {
	static FBReader Instance;

	private int myFullScreenFlag;

	private static class TextSearchButtonPanel implements ZLApplication.ButtonPanel {
		boolean Visible;
		ControlPanel ControlPanel;

		public void hide() {
			Visible = false;
			if (ControlPanel != null) {
				ControlPanel.hide(false);
			}
		}

		public void updateStates() {
			if (ControlPanel != null) {
				ControlPanel.updateStates();
			}
		}
	}
	private static TextSearchButtonPanel myPanel;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		/*
		android.telephony.TelephonyManager tele =
			(android.telephony.TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		System.err.println(tele.getNetworkOperator());
		*/
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
			finish();
			startActivity(new Intent(this, this.getClass()));
		}

		if (myPanel.ControlPanel == null) {
			myPanel.ControlPanel = new ControlPanel(this);

			myPanel.ControlPanel.addButton(ActionCode.FIND_PREVIOUS, false, R.drawable.text_search_previous);
			myPanel.ControlPanel.addButton(ActionCode.CLEAR_FIND_RESULTS, true, R.drawable.text_search_close);
			myPanel.ControlPanel.addButton(ActionCode.FIND_NEXT, false, R.drawable.text_search_next);
        
			RelativeLayout root = (RelativeLayout)findViewById(R.id.root_view);
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
            p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            p.addRule(RelativeLayout.CENTER_HORIZONTAL);
            root.addView(myPanel.ControlPanel, p);
		}
	}

	private PowerManager.WakeLock myWakeLock;

	@Override
	public void onResume() {
		super.onResume();
		if (myPanel.ControlPanel != null) {
			myPanel.ControlPanel.setVisibility(myPanel.Visible ? View.VISIBLE : View.GONE);
		}
		if (ZLAndroidApplication.Instance().DontTurnScreenOffOption.getValue()) {
			myWakeLock =
				((PowerManager)getSystemService(POWER_SERVICE)).
					newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "FBReader");
			myWakeLock.acquire();
		} else {
			myWakeLock = null;
		}
	}

	@Override
	public void onPause() {
		if (myWakeLock != null) {
			myWakeLock.release();
		}
		if (myPanel.ControlPanel != null) {
			myPanel.Visible = myPanel.ControlPanel.getVisibility() == View.VISIBLE;
		}
		super.onPause();
	}

	@Override
	public void onStop() {
		if (myPanel.ControlPanel != null) {
			myPanel.ControlPanel.hide(false);
			myPanel.ControlPanel = null;
		}
		super.onStop();
	}

	void showTextSearchControls(boolean show) {
		if (myPanel.ControlPanel != null) {
			if (show) {
				myPanel.ControlPanel.show(true);
			} else {
				myPanel.ControlPanel.hide(false);
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
		if (myPanel.ControlPanel != null) {
			final boolean visible = myPanel.ControlPanel.getVisibility() == View.VISIBLE;
			myPanel.ControlPanel.hide(false);
			SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
			manager.setOnCancelListener(new SearchManager.OnCancelListener() {
				public void onCancel() {
					if ((myPanel.ControlPanel != null) && visible) {
						myPanel.ControlPanel.show(false);
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
