/*
 * Copyright (C) 2014-2015 Vanniktech - Niklas Baudy <http://vanniktech.de/Imprint>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vanniktech.vntnumberpickerpreference;

import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.DialogPreference;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

public class VNTNumberPickerPreference extends DialogPreference {
	private final ZLIntegerRangeOption myOption;
	private View myCentralView;

	public VNTNumberPickerPreference(Context context, ZLResource resource, ZLIntegerRangeOption option) {
		super(context, null);
		myOption = option;
		setTitle(resource.getValue());
		updateSummary();
		
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInteger(index, 0);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupNumberPicker() {
		final NumberPicker picker = (NumberPicker)myCentralView;
		picker.setMinValue(myOption.MinValue);
		picker.setMaxValue(myOption.MaxValue);
		picker.setValue(myOption.getValue());
		picker.setWrapSelectorWheel(false);
	}

	private void setupSimpleEditor() {
		final EditText text = (EditText)myCentralView;
		text.setText(String.valueOf(myOption.getValue()));
	}

	@Override
	protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		super.onPrepareDialogBuilder(builder);

		final View layout = ((Activity)getContext()).getLayoutInflater().inflate(
			R.layout.picker_preference, null
		);
		myCentralView = layout.findViewById(R.id.picker_preference_central);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setupNumberPicker();
		} else {
			setupSimpleEditor();
		}
		builder.setTitle(getTitle());
		builder.setView(layout);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private int getValueHoneycomb() {
		return ((NumberPicker)myCentralView).getValue();
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		int value = myOption.getValue();
		if (positiveResult) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				value = getValueHoneycomb();
			} else {
				try {
					final String text = ((EditText)myCentralView).getText().toString();
					value =
						Math.min(myOption.MaxValue, Math.max(myOption.MinValue, Integer.valueOf(text)));
				} catch (Throwable t) {
					// ignore
				}
			}
			myOption.setValue(value);
			updateSummary();
		}
	}

	private void updateSummary() {
		setSummary(String.valueOf(myOption.getValue()));
	}
}
