package org.zlibrary.ui.android.dialogs;

import android.content.Context;
import android.view.*;
import android.widget.*;

import org.zlibrary.core.util.ZLColor;
import org.zlibrary.core.dialogs.ZLColorOptionEntry;

class ZLAndroidColorOptionView extends ZLAndroidOptionView {
	private View myContainer;
	private View myColorArea;

	protected ZLAndroidColorOptionView(ZLAndroidDialogContent tab, String name, ZLColorOptionEntry option) {
		super(tab, name, option);
	}

	protected void createItem() {
		final Context context = myTab.getView().getContext();
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout vLayout = new LinearLayout(context);
		vLayout.setOrientation(LinearLayout.VERTICAL);

		final ZLColorOptionEntry colorOption = (ZLColorOptionEntry)myOption;
		final ZLColor color = colorOption.initialColor();

		TextView redLabel = new TextView(context);
		redLabel.setText("Red:");
		vLayout.addView(redLabel, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

		TextView greenLabel = new TextView(context);
		greenLabel.setText("Green:");
		vLayout.addView(greenLabel, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

		TextView blueLabel = new TextView(context);
		blueLabel.setText("Blue:");
		vLayout.addView(blueLabel, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

		myColorArea = new View(context);
		myColorArea.setBackgroundColor(0xFF000000 + (color.Red << 16) + (color.Green << 8) + color.Blue);
		/*
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
		minusButton.setEnabled(value > min);
		myMinusButton = minusButton;

		TextView data = new TextView(context);
		data.setPadding(0, 12, 0, 12);
		data.setTextSize(20);
		data.setText("" + value);
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
		plusButton.setEnabled(value < max);
		myPlusButton = plusButton;

		setValue(((ZLSpinOptionEntry)myOption).initialValue());

		layout.addView(label, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		layout.addView(minusButton, new LinearLayout.LayoutParams(40, LinearLayout.LayoutParams.WRAP_CONTENT));
		layout.addView(data, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		layout.addView(plusButton, new LinearLayout.LayoutParams(40, LinearLayout.LayoutParams.WRAP_CONTENT));
		*/

		layout.addView(vLayout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		layout.addView(myColorArea, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		myContainer = layout;
		addAndroidView(layout, true);
	}

	protected void reset() {
		final ZLColorOptionEntry colorOption = (ZLColorOptionEntry)myOption;
		final ZLColor color = colorOption.initialColor();
		myColorArea.setBackgroundColor(0xFF000000 + (color.Red << 16) + (color.Green << 8) + color.Blue);
		//myColorArea.postInvalidate();
	}

	protected void _onAccept() {
		//((ZLSpinOptionEntry)myOption).onAccept(myValue);
	}
}
