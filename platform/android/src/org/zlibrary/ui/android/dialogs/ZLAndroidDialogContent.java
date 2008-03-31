package org.zlibrary.ui.android.dialogs;

import java.util.ArrayList;

import android.content.Context;
import android.view.*;
import android.widget.*;

import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.dialogs.ZLOptionEntry;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.dialogs.*;

class ZLAndroidDialogContent extends ZLDialogContent {
	private final ListView myListView;
	private final View myMainView;

	ZLAndroidDialogContent(Context context, ZLResource resource, View header, View footer) {
		super(resource);
		myListView = new ListView(context);	
		if ((header != null) || (footer != null)) {
			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.VERTICAL);
			if (header != null) {
				layout.addView(header, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			}
			layout.addView(myListView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			if (footer != null) {
				layout.addView(footer, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			}
			myMainView = layout;
		} else {
			myMainView = myListView;
		}
		myListView.setAdapter(new ViewAdapter());
	}

	View getView() {
		return myMainView;
	}

	public void addOptionByName(String name, ZLOptionEntry option) {
		name = name.replaceAll("&", "");
		ZLAndroidOptionView view = null;
		switch (option.getKind()) {
			case ZLOptionKind.BOOLEAN:
				view = new ZLAndroidBooleanOptionView(
					this, name, (ZLBooleanOptionEntry)option
				);
				break;
			case ZLOptionKind.BOOLEAN3:
//				view = new Boolean3OptionView(name, tooltip, (ZLBoolean3OptionEntry*)option, *this, from, to);
				break;
			case ZLOptionKind.STRING:
				//view = new EditText(myListView.getContext());
				//((EditText)view).setText("HELLO", TextView.BufferType.EDITABLE);
//				view = new ZLStringOptionView(name, tooltip, (ZLStringOptionEntry) option, this);
				break;
			case ZLOptionKind.CHOICE:
//				view = new ZLChoiceOptionView(name, tooltip, (ZLChoiceOptionEntry) option, this);
				break;
			case ZLOptionKind.SPIN:
//				view = new ZLSpinOptionView(name, tooltip, (ZLSpinOptionEntry) option, this);
				break;
			case ZLOptionKind.COMBO:
				view = new ZLAndroidComboOptionView(
					this, name, (ZLComboOptionEntry)option
				);
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
		addView(view);
	}

	public void addOptionsByNames(String name0, ZLOptionEntry option0, String name1, ZLOptionEntry option1) {
		addOption(name0, option0);
		addOption(name1, option1);
	}

	private class ViewAdapter extends BaseAdapter {
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = ((ZLAndroidOptionView)getViews().get(position)).getAndroidView();
			}

			return convertView;
		}

		public boolean areAllItemsSelectable() {
			return true;
		}

		public boolean isSelectable(int position) {
			return true;
		}

		public int getCount() {
			return getViews().size();
		}

		public Object getItem(int position) {
			return "";
		}

		public long getItemId(int position) {
			return position;
		}
	}
}
