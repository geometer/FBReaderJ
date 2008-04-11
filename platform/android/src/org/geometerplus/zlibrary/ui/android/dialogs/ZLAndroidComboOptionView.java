package org.geometerplus.zlibrary.ui.android.dialogs;

import android.content.Context;
import android.view.*;
import android.widget.*;
import android.database.DataSetObserver;

import org.geometerplus.zlibrary.core.dialogs.ZLComboOptionEntry;

class ZLAndroidComboOptionView extends ZLAndroidOptionView {
	private Spinner mySpinner;

	protected ZLAndroidComboOptionView(ZLAndroidDialogContent tab, String name, ZLComboOptionEntry option) {
		super(tab, name, option);
	}

	protected void createItem() {
		mySpinner = new Spinner(myTab.getView().getContext());
		mySpinner.setAdapter(new ComboAdapter());
	}

	void addAndroidViews() {
		myTab.addAndroidView(mySpinner, true);
	}

	protected void reset() {
		((ZLComboOptionEntry)myOption).onReset();
		// TODO: implement
	}

	protected void _onAccept() {
		EditText editor = ((ComboAdapter)mySpinner.getAdapter()).myEditor;
		((ZLComboOptionEntry)myOption).onAccept(editor.getText().toString());
	}

	private class ComboAdapter implements SpinnerAdapter {
		EditText myEditor;

		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				TextView textView = new TextView(parent.getContext());
				textView.setPadding(0, 12, 0, 12);
				textView.setTextSize(20);
				textView.setText((String)getItem(position));
				convertView = textView;
			}
			return convertView;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			((ZLComboOptionEntry)myOption).onValueSelected(position);
			if (convertView == null) {
				myEditor = new EditText(parent.getContext());
				myEditor.setSingleLine(true);
				myEditor.setText((String)getItem(position), TextView.BufferType.EDITABLE);
				convertView = myEditor;
			}
			return convertView;
		}

		public View getMeasurementView(ViewGroup parent) {
			// TODO: implement
			return null;
		}

		public int getCount() {
			return ((ZLComboOptionEntry)myOption).getValues().size();
		}

		public Object getItem(int position) {
			return ((ZLComboOptionEntry)myOption).getValues().get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public int getNewSelectionForKey(int currentSelection, int keyCode, KeyEvent event) {
			return NO_SELECTION;
		}

		public boolean stableIds() {
			return true;
		}

		public void registerDataSetObserver(DataSetObserver observer) {
		}

		public void unregisterDataSetObserver(DataSetObserver observer) {
		}
	}
}
