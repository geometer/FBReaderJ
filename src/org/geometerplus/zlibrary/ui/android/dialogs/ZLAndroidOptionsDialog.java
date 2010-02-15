/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.ui.android.dialogs;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.dialogs.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;

class ZLAndroidOptionsDialog extends ZLOptionsDialog implements ZLAndroidDialogInterface {
	private final String myCaption;
	private TabListView myTabListView;
	private final Activity myMainActivity;
	private final Runnable myApplyAction;
	private final Runnable myExitAction;

	ZLAndroidOptionsDialog(Activity activity, ZLResource resource, Runnable exitAction, Runnable applyAction) {
		super(resource, exitAction, applyAction);
		myCaption = resource.getResource("title").getValue();
		myMainActivity = activity;
		myApplyAction = applyAction;
		myExitAction = exitAction;
	}

	public void setActivity(DialogActivity activity) {
		myTabListView = new TabListView(activity);	
		myTabListView.setAdapter(new TabListAdapter());
		activity.setContentView(myTabListView);
		activity.setTitle(myCaption);
	}

	public void endActivity() {
		if (myApplyAction != null) {
			myApplyAction.run();
		}
		if (myExitAction != null) {
			myExitAction.run();
		}
	}

	protected String getSelectedTabKey() {
		/*
		if (myTabListView != null) {
			int index = myTabListView.getSelectedItemPosition();
			if ((index >= 0) && (index <= myTabs.size())) {
				return ((ZLDialogContent)myTabs.get(index)).getKey();
			}
		}
		*/
		return "";
	}
	
	protected void selectTab(String key) {
		/*
		if (myTabListView != null) {
			final ArrayList tabs = myTabs;
			final int len = tabs.size();
			for (int i = 0; i < len; ++i) {
				ZLDialogContent tab = (ZLDialogContent)tabs.get(i);
				if (tab.getKey().equals(key)) {
					myTabListView.setSelection(i);
					return;
				}
			}
		}
		*/
	}
	
	protected void runInternal() {
		ZLAndroidDialogManager.runDialog(myMainActivity, this);
	}

	public ZLDialogContent createTab(String key) {
		final Context context = myMainActivity;

		final int index = myTabs.size();

		final ZLDialogContent tab =
			new ZLAndroidDialogContent(context, getTabResource(key));
		myTabs.add(tab);
		return tab;
	}

	private void gotoTab(int index) {
		myTabListView.setSelection(index);
		final ZLAndroidDialogContent tab =
			(ZLAndroidDialogContent)myTabListView.getAdapter().getItem(index);
		final Activity activity = (Activity)myTabListView.getContext();
		ZLAndroidDialogManager.runDialog(activity, tab);
	}

	private class TabListView extends ListView implements AdapterView.OnItemClickListener {
		TabListView(Context context) {
			super(context);
			setOnItemClickListener(this);
		}

		public boolean onKeyUp(int keyCode, KeyEvent event) {
			switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_CENTER:
				case KeyEvent.KEYCODE_ENTER:
					final int index = getSelectedItemPosition();
					if (index != -1) {
						gotoTab(index);
					}
					return false;
				default:	
					return super.onKeyUp(keyCode, event);
			}
		}

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			gotoTab(position);
		}
	}

	private class TabListAdapter extends BaseAdapter {
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = (convertView != null) ? (TextView)convertView : new TextView(parent.getContext());
			textView.setText(((ZLDialogContent)getItem(position)).getDisplayName());
			textView.setPadding(0, 12, 0, 12);
			textView.setTextSize(20);
			return textView;
		}

		public boolean areAllItemsSelectable() {
			return false;
		}

		public boolean isSelectable(int position) {
			return true;
		}

		public int getCount() {
			return myTabs.size();
		}

		public Object getItem(int position) {
			return myTabs.get(position);
		}

		public long getItemId(int position) {
			return position;
		}
	}

	private static class TabButton extends Button {
		private Runnable myAction;

		TabButton(Context context, String text, Runnable action) {
			super(context);
			setText(text);
			setFocusable(false);
			myAction = action;
		}

		public boolean onTouchEvent(MotionEvent event) {
			myAction.run();
			return true;
		}
	}
}
