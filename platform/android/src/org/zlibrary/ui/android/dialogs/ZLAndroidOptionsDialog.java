package org.zlibrary.ui.android.dialogs;

import java.util.ArrayList;

import android.content.Context;
import android.view.*;
import android.widget.*;

import org.zlibrary.core.dialogs.*;
import org.zlibrary.core.resources.ZLResource;

class ZLAndroidOptionsDialog extends ZLOptionsDialog {
	private final AndroidDialog myDialog;
	private final String myCaption;
	private final TabListView myTabListView;
	private final ArrayList myCancelActions = new ArrayList();

	ZLAndroidOptionsDialog(Context context, ZLResource resource, Runnable exitAction, Runnable applyAction) {
		super(resource, exitAction, applyAction);
		myTabListView = new TabListView(context);	
		myCaption = resource.getResource("title").getValue();
		myDialog = new AndroidDialog(context, myTabListView, myCaption);
		myDialog.setExitAction(exitAction);
	}

	protected String getSelectedTabKey() {
		// TODO: implement
		int index = myTabListView.getSelectedItemPosition();
		if ((index >= 0) && (index <= myTabs.size())) {
			return ((ZLDialogContent)myTabs.get(index)).getKey();
		}
		return "";
	}
	
	protected void selectTab(String key) {
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
	
	public void run() {
		myTabListView.setAdapter(new TabListAdapter());
		super.run();
	}

	protected void runInternal() {
		myDialog.show();
	}

	public ZLDialogContent createTab(String key) {
		final Context context = myDialog.getContext();

		final int index = myTabs.size();

		Runnable applyAction = new ReturnFromTabAction(index, true);
		Runnable cancelAction = new ReturnFromTabAction(index, false);
		myCancelActions.add(cancelAction);

		final LinearLayout header = new LinearLayout(context);
		header.setOrientation(LinearLayout.HORIZONTAL);

		header.addView(
			new TabButton(
				context,
				ZLDialogManager.getButtonText(ZLDialogManager.APPLY_BUTTON).replaceAll("&", ""),
				applyAction
			),
			new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
		);
		header.addView(
			new TabButton(
				context,
				ZLDialogManager.getButtonText(ZLDialogManager.CANCEL_BUTTON).replaceAll("&", ""),
				cancelAction
			),
			new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
		);

		final ZLDialogContent tab =
			new ZLAndroidDialogContent(context, getTabResource(key), header, null);
		myTabs.add(tab);
		return tab;
	}

	private void gotoTab(int index) {
		ZLAndroidDialogContent tab =
			(ZLAndroidDialogContent)myTabListView.getAdapter().getItem(index);
		myDialog.setTitle(tab.getDisplayName());
		myDialog.setContentView(tab.getView());
		myDialog.setCancelAction((Runnable)myCancelActions.get(index));
	}

	private class ReturnFromTabAction implements Runnable {
		private final int myIndex;
		private final boolean myDoApply;

		ReturnFromTabAction(int index, boolean doApply) {
			myIndex = index;
			myDoApply = doApply;
		}

		public void run() {
			myDialog.setTitle(myCaption);
			myDialog.setContentView(myTabListView);
			myTabListView.requestFocus();
			myDialog.setCancelAction(null);
			ZLDialogContent tab = (ZLDialogContent)myTabs.get(myIndex);
			if (myDoApply) {
				acceptTab(tab);
			} else {
				resetTab(tab);
			}
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
					final int index = getSelectedItemPosition();
					if (index != -1) {
						gotoTab(index);
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
			myAction = action;
		}

		public boolean onTouchEvent(MotionEvent event) {
			myAction.run();
			return true;
		}
	}
}
