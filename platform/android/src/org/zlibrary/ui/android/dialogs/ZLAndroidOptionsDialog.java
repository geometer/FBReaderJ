package org.zlibrary.ui.android.dialogs;

import java.util.ArrayList;

import android.content.Context;
import android.view.*;
import android.widget.*;

import org.zlibrary.core.dialogs.ZLOptionsDialog;
import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.resources.ZLResource;

class ZLAndroidOptionsDialog extends ZLOptionsDialog {
	private final AndroidDialog myDialog;
	private final String myCaption;
	private final TabListView myTabListView;
	private final ReturnFromTabAction myReturnFromTabAction;

	ZLAndroidOptionsDialog(Context context, ZLResource resource, Runnable applyAction) {
		super(resource, applyAction);
		myTabListView = new TabListView(context);	
		myCaption = resource.getResource("title").getValue();
		myDialog = new AndroidDialog(context, myTabListView, myCaption);
		myReturnFromTabAction = new ReturnFromTabAction();
	}

	protected String getSelectedTabKey() {
		// TODO: implement
		final View selectedView = myTabListView.getSelectedView();
		if (selectedView != null) {
			int index = myTabListView.indexOfChild(selectedView);
			if ((index >= 0) && (index <= myTabs.size())) {
				return ((ZLAndroidDialogContent)myTabs.get(index)).getKey();
			}
		}
		return "";
	}
	
	protected void selectTab(String key) {
		final ArrayList tabs = myTabs;
		final int len = tabs.size();
		for (int i = 0; i < len; ++i) {
			ZLAndroidDialogContent tab = (ZLAndroidDialogContent)tabs.get(i);
			if (tab.getKey().equals(key)) {
				myTabListView.setSelection(i);
				return;
			}
		}
	}
	
	public void run() {
		myTabListView.setAdapter(new TabListAdapter());
		super.run();
	}

	protected void runInternal() {
		myDialog.show();
		// TODO: implement
		accept();
	}
	
	public ZLDialogContent createTab(String key) {
		ZLAndroidDialogContent tab =
			new ZLAndroidDialogContent(myDialog.getContext(), getTabResource(key), null, null);
		myTabs.add(tab);
		return tab;
	}

	private void gotoTab(int index) {
		ZLAndroidDialogContent tab =
			(ZLAndroidDialogContent)myTabListView.getAdapter().getItem(index);
		myDialog.setTitle(tab.getDisplayName());
		myDialog.setContentView(tab.getView());
		myDialog.setCancelAction(myReturnFromTabAction);
	}

	private class ReturnFromTabAction implements Runnable {
		public void run() {
			myDialog.setTitle(myCaption);
			myDialog.setContentView(myTabListView);
			myTabListView.requestFocus();
			myDialog.setCancelAction(null);
		}
	}

	private class TabListView extends ListView {
		TabListView(Context context) {
			super(context);
		}

		public boolean onKeyUp(int keyCode, KeyEvent event) {
			switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_CENTER:
				case KeyEvent.KEYCODE_NEWLINE:
					View selectedView = getSelectedView();
					if (selectedView != null) {
						gotoTab(indexOfChild(selectedView));
					}
					return false;
				default:	
					return super.onKeyUp(keyCode, event);
			}
		}

		public boolean onTouchEvent(MotionEvent event) {
			final int x = (int)event.getX();
			final int y = (int)event.getY();
			gotoTab(pointToPosition(x, y));
			return false;
		}
	}

	private class TabListAdapter extends BaseAdapter {
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView != null) {
				return convertView;
			}

			TextView textView = new TextView(parent.getContext());
			textView.setText(((ZLAndroidDialogContent)getItem(position)).getDisplayName());
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
}
