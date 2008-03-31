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
	private final ListView myTabListView;

	ZLAndroidOptionsDialog(Context context, ZLResource resource, Runnable applyAction) {
		super(resource, applyAction);
		myTabListView = new ListView(context);	
		myDialog = new AndroidDialog(context, myTabListView, resource.getResource("title").getValue());
	}

	protected String getSelectedTabKey() {
		final View selectedView = myTabListView.getSelectedView();
		android.util.Log.i("selectedView", "" + selectedView);
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
			new ZLAndroidDialogContent(myDialog.getContext(), getTabResource(key), null);
		myTabs.add(tab);
		return tab;
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
