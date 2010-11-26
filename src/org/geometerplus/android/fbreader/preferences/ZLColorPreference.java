/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.*;
import android.preference.DialogPreference;
import android.view.View;
import android.widget.SeekBar;

import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.options.ZLColorOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

class ZLColorPreference extends DialogPreference implements ZLPreference {
	private final ZLColorOption myOption;

	private SeekBar myRedSlider;
	private SeekBar myGreenSlider;
	private SeekBar myBlueSlider;
	private final GradientDrawable myPreviewDrawable = new GradientDrawable();

	ZLColorPreference(Context context, ZLResource resource, String resourceKey, ZLColorOption option) {
		super(context, null);
		myOption = option;
		//setWidgetLayoutResource(R.layout.color_preference_widget);
		final String title = resource.getResource(resourceKey).getValue();
		setTitle(title);
		setDialogTitle(title);
		setDialogLayoutResource(R.layout.color_dialog);
	}

	@Override
	protected void onBindDialogView(View view) {
		final ZLColor color = myOption.getValue();

		myRedSlider = (SeekBar)view.findViewById(R.id.color_red);
		myRedSlider.setProgress(color.Red);

		myGreenSlider = (SeekBar)view.findViewById(R.id.color_green);
		myGreenSlider.setProgress(color.Green);

		myBlueSlider = (SeekBar)view.findViewById(R.id.color_blue);
		myBlueSlider.setProgress(color.Blue);

		final View colorBox = view.findViewById(R.id.color_box);
		colorBox.setBackgroundDrawable(myPreviewDrawable);
		myPreviewDrawable.setCornerRadius(7);
		myPreviewDrawable.setColor(ZLAndroidColorUtil.rgb(color));

		final SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				myPreviewDrawable.setColor(Color.rgb(
					myRedSlider.getProgress(),
					myGreenSlider.getProgress(),
					myBlueSlider.getProgress()
				));
				myPreviewDrawable.invalidateSelf();
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				myPreviewDrawable.setColor(Color.rgb(
					myRedSlider.getProgress(),
					myGreenSlider.getProgress(),
					myBlueSlider.getProgress()
				));
				myPreviewDrawable.invalidateSelf();
			}
		};
		myRedSlider.setOnSeekBarChangeListener(listener);
		myGreenSlider.setOnSeekBarChangeListener(listener);
		myBlueSlider.setOnSeekBarChangeListener(listener);

		super.onBindDialogView(view);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			myOption.setValue(new ZLColor(
				myRedSlider.getProgress(),
				myGreenSlider.getProgress(),
				myBlueSlider.getProgress()
			));
		}
	}

	/*
	@Override
	protected void onBindView(View view) {
		final ImageView colorView = (ImageView)view.findViewById(R.id.color_preference_color);
		//colorView.setImageResource(R.drawable.fbreader);
		final Drawable drawable = new ColorDrawable(0x00FF00);
		colorView.setImageDrawable(drawable);
		
		super.onBindView(view);
	}
	*/

	public void onAccept() {
	}
}
