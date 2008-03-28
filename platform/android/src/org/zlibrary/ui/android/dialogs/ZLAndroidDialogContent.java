package org.zlibrary.ui.android.dialogs;

import java.util.ArrayList;

import android.content.Context;
import android.view.*;
import android.widget.*;
/*
import android.app.Dialog;
import android.os.*;
*/

import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.dialogs.ZLOptionEntry;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.dialogs.*;

class ZLAndroidDialogContent extends ZLDialogContent {
	private final ListView myListView;
	private final ArrayList myOptions = new ArrayList();

	View getView() {
		return myListView;
	}

	ZLAndroidDialogContent(Context context, ZLResource resource) {
		super(resource);
		myListView = new ListView(context);	
		myListView.setAdapter(new ViewAdapter());
	}

	public void addOption(String name, String tooltip, ZLOptionEntry option) {
		name = name.replaceAll("&", "");
		//ZLOptionView view = null;
		View view = null;
		switch (option.getKind()) {
			case ZLOptionKind.BOOLEAN:
				view = new CheckBox(myListView.getContext());
				((TextView)view).setText(name);
//				view = new ZLBooleanOptionView(name, tooltip, (ZLBooleanOptionEntry) option, this);
				break;
			case ZLOptionKind.BOOLEAN3:
//				view = new Boolean3OptionView(name, tooltip, (ZLBoolean3OptionEntry*)option, *this, from, to);
				break;
			case ZLOptionKind.STRING:
				view = new EditText(myListView.getContext());
				((EditText)view).setText("HELLO", TextView.BufferType.EDITABLE);
//				view = new ZLStringOptionView(name, tooltip, (ZLStringOptionEntry) option, this);
				break;
			case ZLOptionKind.CHOICE:
//				view = new ZLChoiceOptionView(name, tooltip, (ZLChoiceOptionEntry) option, this);
				break;
			case ZLOptionKind.SPIN:
//				view = new ZLSpinOptionView(name, tooltip, (ZLSpinOptionEntry) option, this);
				break;
			case ZLOptionKind.COMBO:
//				view = new ZLComboOptionView(name, tooltip, (ZLComboOptionEntry) option, this);
				break;
			case ZLOptionKind.COLOR:
//				view = new ColorOptionView(name, tooltip, (ZLColorOptionEntry*)option, *this, from, to);
				break;
			case ZLOptionKind.KEY:
//				view = new KeyOptionView(name, tooltip, (ZLKeyOptionEntry*)option, *this, from, to);
				break;
			case ZLOptionKind.ORDER:
				// TODO: implement
				break;
			case ZLOptionKind.MULTILINE:
				// TODO: implement
				break;
		}
		if (view != null) {
			myOptions.add(view);
		}
	}

	public void addOptions(String name0, String tooltip0, ZLOptionEntry option0, String name1, String tooltip1, ZLOptionEntry option1) {
		addOption(name0, tooltip0, option0);
		addOption(name1, tooltip1, option1);
	}

	private class ViewAdapter extends BaseAdapter {
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = (View)myOptions.get(position);
			}

			return convertView;
		}

		public boolean areAllItemsSelectable() {
			return false;
		}

		public boolean isSelectable(int position) {
			return true;
		}

		public int getCount() {
			return myOptions.size();
		}

		public Object getItem(int position) {
			return "";
		}

		public long getItemId(int position) {
			return position;
		}
	}
}
