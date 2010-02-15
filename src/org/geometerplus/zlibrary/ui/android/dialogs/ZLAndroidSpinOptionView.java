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

import android.content.Context;
import android.view.*;
import android.widget.*;

import org.geometerplus.zlibrary.core.dialogs.ZLSpinOptionEntry;

class ZLAndroidSpinOptionView extends ZLAndroidOptionView {
	private TextView myLabel;
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

	void addAndroidViews() {
		final Context context = myTab.getContext();
		if (myName != null) {
			if (myLabel == null) {
				myLabel = new TextView(context);
				myLabel.setText(myName);
				myLabel.setPadding(0, 12, 0, 12);
				myLabel.setTextSize(18);
			}
			myTab.addAndroidView(myLabel, false);
		}

		if (mySpinView == null) {
			LinearLayout layout = new LinearLayout(context);
			layout.setOrientation(LinearLayout.HORIZONTAL);
			layout.setHorizontalGravity(0x05);
    
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
    
			layout.addView(minusButton, new LinearLayout.LayoutParams(40, LinearLayout.LayoutParams.WRAP_CONTENT));
			layout.addView(data, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			layout.addView(plusButton, new LinearLayout.LayoutParams(40, LinearLayout.LayoutParams.WRAP_CONTENT));
    
			mySpinView = layout;
		}
		myTab.addAndroidView(mySpinView, true);
	}

	protected void reset() {
		if (mySpinView != null) {
			setValue(((ZLSpinOptionEntry)myOption).initialValue());
		}
	}

	protected void _onAccept() {
		if (mySpinView != null) {
			((ZLSpinOptionEntry)myOption).onAccept(myValue);
			myLabel = null;
			mySpinView = null;
			myDataView = null;
			myMinusButton = null;
			myPlusButton = null;
		}
	}
}
