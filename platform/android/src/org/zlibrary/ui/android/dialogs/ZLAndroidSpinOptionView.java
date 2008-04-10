package org.zlibrary.ui.android.dialogs;

import android.content.Context;
import android.view.*;
import android.widget.*;

import org.zlibrary.core.dialogs.ZLSpinOptionEntry;

class ZLAndroidSpinOptionView extends ZLAndroidOptionView {
	private View mySpinView;
	private TextView myDataView;
	private Button myMinusButton;
	private Button myPlusButton;
	private int myValue;

	protected ZLAndroidSpinOptionView(ZLAndroidDialogContent tab, String name, ZLSpinOptionEntry option) {
		super(tab, name, option);
	}

	private void setValue(int value) {
		final ZLSpinOptionEntry spinOption = (ZLSpinOptionEntry)myOption;
		final int min = spinOption.minValue();
		final int max = spinOption.maxValue();
		if ((value >= min) && (value <= max)) {
			myValue = value;
			myDataView.setText("" + value);
			myMinusButton.setEnabled(value > min);
			myPlusButton.setEnabled(value < max);
		}
	}

	protected void createItem() {
		final Context context = myTab.getView().getContext();
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setHorizontalGravity(0x05);

		TextView label = new TextView(context);
		label.setText(myName);
		label.setPadding(0, 12, 0, 12);
		label.setTextSize(16);

		final ZLSpinOptionEntry spinOption = (ZLSpinOptionEntry)myOption;
		final int min = spinOption.minValue();
		final int max = spinOption.maxValue();
		final int value = spinOption.initialValue();

		Button minusButton = new Button(context) {
			public boolean onTouchEvent(MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					setValue(myValue - 1);
				}
				return true;
			}
		};
		minusButton.setText("-");
		minusButton.setTextSize(24);
		myMinusButton = minusButton;

		TextView data = new TextView(context);
		data.setPadding(0, 12, 0, 12);
		data.setTextSize(20);
		myDataView = data;

		Button plusButton = new Button(context) {
			public boolean onTouchEvent(MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					setValue(myValue + 1);
				}
				return true;
			}
		};
		plusButton.setText("+");
		plusButton.setTextSize(24);
		myPlusButton = plusButton;

		setValue(((ZLSpinOptionEntry)myOption).initialValue());

		layout.addView(label, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		layout.addView(minusButton, new LinearLayout.LayoutParams(40, LinearLayout.LayoutParams.WRAP_CONTENT));
		layout.addView(data, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		layout.addView(plusButton, new LinearLayout.LayoutParams(40, LinearLayout.LayoutParams.WRAP_CONTENT));

		mySpinView = layout;
	}

	void addAndroidViews() {
		myTab.addAndroidView(mySpinView, true);
	}

	protected void reset() {
		if (mySpinView != null) {
			setValue(((ZLSpinOptionEntry)myOption).initialValue());
		}
	}

	protected void _onAccept() {
		((ZLSpinOptionEntry)myOption).onAccept(myValue);
	}
}
