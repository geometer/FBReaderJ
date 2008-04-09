package org.zlibrary.ui.android.dialogs;

import java.util.ArrayList;

import android.content.Context;
import android.view.*;
import android.widget.*;

import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.dialogs.ZLOptionEntry;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.dialogs.*;
import org.zlibrary.core.util.ZLArrayUtils;

class ZLAndroidDialogContent extends ZLDialogContent {
	private final ListView myListView;
	private final View myMainView;

	private final ArrayList myAndroidViews = new ArrayList();
	private boolean[] mySelectableMarks = new boolean[10];

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
		if (name != null) {
			name = name.replaceAll("&", "");
		}
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
				view = new ZLAndroidStringOptionView(
					this, name, (ZLStringOptionEntry)option
				);
				break;
			case ZLOptionKind.CHOICE:
				view = new ZLAndroidChoiceOptionView(
					this, name, (ZLChoiceOptionEntry)option
				);
				break;
			case ZLOptionKind.SPIN:
				view = new ZLAndroidSpinOptionView(
					this, name, (ZLSpinOptionEntry)option
				);
				break;
			case ZLOptionKind.COMBO:
				view = new ZLAndroidComboOptionView(
					this, name, (ZLComboOptionEntry)option
				);
				break;
			case ZLOptionKind.COLOR:
				view = new ZLAndroidColorOptionView(
					this, name, (ZLColorOptionEntry)option
				);
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
			view.setVisible(option.isVisible());
		}
		addView(view);
	}

	public void addOptionsByNames(String name0, ZLOptionEntry option0, String name1, ZLOptionEntry option1) {
		if (option0 != null) {
			addOptionByName(name0, option0);
		}
		if (option1 != null) {
			addOptionByName(name1, option1);
		}
	}

	void addAndroidView(View view, boolean isSelectable) {
		boolean[] marks = mySelectableMarks;
		final int len = marks.length;
		final int index = myAndroidViews.size();
		if (index == len) {
			marks = ZLArrayUtils.createCopy(marks, len, 2 * len);
			mySelectableMarks = marks;
		}
		myAndroidViews.add(view);
		marks[index] = isSelectable;
	}

	private class ViewAdapter extends BaseAdapter {
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = (View)myAndroidViews.get(position);
			}

			return convertView;
		}

		public boolean areAllItemsSelectable() {
			return true;
		}

		public boolean isSelectable(int position) {
			return mySelectableMarks[position];
		}

		public int getCount() {
			return myAndroidViews.size();
		}

		public Object getItem(int position) {
			return "";
		}

		public long getItemId(int position) {
			return position;
		}
	}
}
