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

import android.content.Context;
import android.preference.DialogPreference;
import android.view.View;
import android.widget.NumberPicker;

public class VNTNumberPickerPreference extends DialogPreference {
	private final ZLIntegerRangeOption myOption;
	private NumberPicker myPicker;

	public VNTNumberPickerPreference(Context context, ZLResource resource, ZLIntegerRangeOption option) {
		super(context, null);
		myOption = option;
		setTitle(resource.getValue());
		updateSummary();
		setDialogLayoutResource(R.layout.picker_preference);
	}

	@Override
	protected void onBindDialogView(View view) {
		myPicker = (NumberPicker)view.findViewById(R.id.picker_preference_central);
		myPicker.setMinValue(myOption.MinValue);
		myPicker.setMaxValue(myOption.MaxValue);
		myPicker.setValue(myOption.getValue());
		myPicker.setWrapSelectorWheel(false);

		super.onBindDialogView(view);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			myOption.setValue(myPicker.getValue());
			updateSummary();
		}
	}

	private void updateSummary() {
		setSummary(String.valueOf(myOption.getValue()));
	}
}
