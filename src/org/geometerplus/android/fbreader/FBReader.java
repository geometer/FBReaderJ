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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidActivity;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.fbreader.ActionCode;

public final class FBReader extends ZLAndroidActivity {
	static FBReader Instance;

	private int myFullScreenFlag;

	private static class ControlButtonPanel implements ZLApplication.ButtonPanel {
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

		public void register() {
			ZLApplication.Instance().registerButtonPanel(this);
		}

		public void registerControlPanel(RelativeLayout root, boolean fillWidth) {
			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
				fillWidth ? ViewGroup.LayoutParams.FILL_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT
			);
			p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			p.addRule(RelativeLayout.CENTER_HORIZONTAL);
			root.addView(ControlPanel, p);
			ControlPanel.requestLayout();
		}

		public void destroyControlPanel(RelativeLayout root) {
			if (ControlPanel != null) {
				ControlPanel.hide(false);
				root.removeView(ControlPanel);
				ControlPanel = null;
			}
		}

		public boolean getVisibility() {
			if (ControlPanel != null) {
				return ControlPanel.getVisibility() == View.VISIBLE;
			}
			return false;
		}

		public void setVisibility(boolean visibility) {
			if (ControlPanel != null) {
				ControlPanel.setVisibility(visibility ? View.VISIBLE : View.GONE);
			}
		}

		public void restoreVisibility() {
			setVisibility(Visible);
		}

		public void saveVisibility() {
			Visible = getVisibility();
		}
	}
	private static ControlButtonPanel myTextSearchPanel;
	private static ControlButtonPanel myNavigatePanel;

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
		if (myTextSearchPanel == null) {
			myTextSearchPanel = new ControlButtonPanel();
			myTextSearchPanel.register();
		}
		if (myNavigatePanel == null) {
			myNavigatePanel = new ControlButtonPanel();
			myNavigatePanel.register();
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

		final RelativeLayout root = (RelativeLayout)FBReader.this.findViewById(R.id.root_view);
		if (myTextSearchPanel.ControlPanel == null) {
			myTextSearchPanel.ControlPanel = new ControlPanel(this);

			myTextSearchPanel.ControlPanel.addButton(ActionCode.FIND_PREVIOUS, false, R.drawable.text_search_previous);
			myTextSearchPanel.ControlPanel.addButton(ActionCode.CLEAR_FIND_RESULTS, true, R.drawable.text_search_close);
			myTextSearchPanel.ControlPanel.addButton(ActionCode.FIND_NEXT, false, R.drawable.text_search_next);

			myTextSearchPanel.registerControlPanel(root, false);
		}
		if (myNavigatePanel.ControlPanel == null) {
			myNavigatePanel.ControlPanel = new NavigationControlPanel(this);
			createNavigation();
			myNavigatePanel.registerControlPanel(root, true);
		}

		findViewById(R.id.main_view).setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				if (!myNavigatePanel.getVisibility()) {
					navigate();
				}
				return true;
			}
		});
	}

	private PowerManager.WakeLock myWakeLock;

	@Override
	public void onResume() {
		super.onResume();
		myTextSearchPanel.restoreVisibility();
		myNavigatePanel.restoreVisibility();
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
		myTextSearchPanel.saveVisibility();
		myNavigatePanel.saveVisibility();
		super.onPause();
	}

	@Override
	public void onStop() {
		final RelativeLayout root = (RelativeLayout)FBReader.this.findViewById(R.id.root_view);
		myTextSearchPanel.destroyControlPanel(root);
		myNavigatePanel.destroyControlPanel(root);
		super.onStop();
	}

	void showTextSearchControls(boolean show) {
		if (myTextSearchPanel.ControlPanel != null) {
			if (show) {
				ZLApplication.Instance().hideAllPanels();
				myTextSearchPanel.ControlPanel.show(true);
			} else {
				myTextSearchPanel.ControlPanel.hide(false);
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
		final boolean textSearchVisible = myTextSearchPanel.getVisibility();
		final boolean navigateVisible = myNavigatePanel.getVisibility();
		ZLApplication.Instance().hideAllPanels();
		final SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(new SearchManager.OnCancelListener() {
			public void onCancel() {
				myTextSearchPanel.setVisibility(textSearchVisible);
				myNavigatePanel.setVisibility(navigateVisible);
				manager.setOnCancelListener(null);
			}
		});
		final org.geometerplus.fbreader.fbreader.FBReader fbreader =
			(org.geometerplus.fbreader.fbreader.FBReader)ZLApplication.Instance();
		startSearch(fbreader.TextSearchPatternOption.getValue(), true, null, false);
		return true;
	}


	private static ZLTextPosition myPosition;

	public void navigate() {
		if (myNavigatePanel.ControlPanel != null) {
			final ZLTextView textView = (ZLTextView) ZLApplication.Instance().getCurrentView();
			if (myTextSearchPanel.ControlPanel.getVisibility() == View.VISIBLE) {
				textView.clearFindResults();
			}
			myPosition = new ZLTextFixedPosition(textView.getStartCursor());
			ZLApplication.Instance().hideAllPanels();
			setupNavigation();
			myNavigatePanel.ControlPanel.show(true);
		}
	}


	private boolean myNavigateDragging;
	private class NavigationControlPanel extends ControlPanel {
		public NavigationControlPanel(Context context) {
			super(context);
		}

		@Override
		public void preparePanel() {
			setupNavigation();
		}

		@Override
		public void updateStates() {
			super.updateStates();
			if (!myNavigateDragging) {
				setupNavigation();
			}
		}
	}

	public boolean canNavigate() {
		final org.geometerplus.fbreader.fbreader.FBReader fbreader =
			(org.geometerplus.fbreader.fbreader.FBReader)ZLApplication.Instance();
		final ZLView view = fbreader.getCurrentView();
		return view instanceof ZLTextView
				&& ((ZLTextView) view).getModel() != null
				&& ((ZLTextView) view).getModel().getParagraphsNumber() != 0
				&& fbreader.Model != null
				&& fbreader.Model.Book != null;
	}

	private void createNavigation() {
		final ControlPanel panel = myNavigatePanel.ControlPanel;

		final View layout = getLayoutInflater().inflate(R.layout.navigate, panel, false);
		final SeekBar slider = (SeekBar) layout.findViewById(R.id.book_position_slider);
		final TextView text = (TextView) layout.findViewById(R.id.book_position_text);

		slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			private void gotoPage(int page) {
				final ZLView view = ZLApplication.Instance().getCurrentView();
				if (view instanceof ZLTextView) {
					ZLTextView textView = (ZLTextView) view;
					if (page == 1) {
						textView.gotoHome();
					} else {
						textView.gotoPage(page);
					}
					ZLApplication.Instance().repaintView();
				}
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				myNavigateDragging = false;
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				myNavigateDragging = true;
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					final int page = progress + 1;
					final int pagesNumber = seekBar.getMax() + 1; 
					text.setText(makeProgressText(page, pagesNumber));
					gotoPage(page);
				}
			}
		});

		final Button btnOk = (Button) layout.findViewById(android.R.id.button1);
		final Button btnCancel = (Button) layout.findViewById(android.R.id.button3);
		View.OnClickListener listener = new View.OnClickListener() {
			public void onClick(View v) {
				if (v == btnCancel && myPosition != null) {
					((ZLTextView) ZLApplication.Instance().getCurrentView()).gotoPosition(myPosition);
				}
				myPosition = null;
				panel.hide(true);
			}
		};
		btnOk.setOnClickListener(listener);
		btnCancel.setOnClickListener(listener);
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		btnOk.setText(buttonResource.getResource("ok").getValue());
		btnCancel.setText(buttonResource.getResource("cancel").getValue());

		panel.setExtension(layout);
	}

	private void setupNavigation() {
		final ControlPanel panel = myNavigatePanel.ControlPanel;

		final SeekBar slider = (SeekBar) panel.findViewById(R.id.book_position_slider);
		final TextView text = (TextView) panel.findViewById(R.id.book_position_text);

		final ZLTextView textView = (ZLTextView) ZLApplication.Instance().getCurrentView();
		final int page = textView.computeCurrentPage();
		final int pagesNumber = textView.computePageNumber();

		slider.setMax(pagesNumber - 1);
		slider.setProgress(page - 1);
		text.setText(makeProgressText(page, pagesNumber));
	}

	private static String makeProgressText(int page, int pagesNumber) {
		return "" + page + " / " + pagesNumber;
	}
}
