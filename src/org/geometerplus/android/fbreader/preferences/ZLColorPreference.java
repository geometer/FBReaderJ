/*
 * Copyright (C) 2010-2012 Geometer Plus <contact@geometerplus.com>
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
import android.graphics.*;
import android.graphics.drawable.*;
import android.preference.DialogPreference;
import android.view.View;
import android.widget.SeekBar;

import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.options.ZLColorOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

class ZLColorPreference extends DialogPreference {
	private final ZLColorOption myOption;

	private SeekBar myRedSlider;
	private SeekBar myGreenSlider;
	private SeekBar myBlueSlider;
	private final GradientDrawable myPreviewDrawable = new GradientDrawable();

	ZLColorPreference(Context context, ZLResource resource, String resourceKey, ZLColorOption option) {
		super(context, null);
		myOption = option;
		final String title = resource.getResource(resourceKey).getValue();
		setTitle(title);
		setDialogTitle(title);
		setDialogLayoutResource(R.layout.color_dialog);

		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		setPositiveButtonText(buttonResource.getResource("ok").getValue());
		setNegativeButtonText(buttonResource.getResource("cancel").getValue());
	}

	private SeekBar createSlider(View view, int id, int value, String resourceKey) {
		final SeekBar slider = (SeekBar)view.findViewById(id);
		slider.setProgressDrawable(new SeekBarDrawable(
			slider.getProgressDrawable(),
			ZLResource.resource("color").getResource(resourceKey).getValue(),
			slider
		));
		slider.setProgress(value);
		return slider;
	}

	@Override
	protected void onBindDialogView(View view) {
		final ZLColor color = myOption.getValue();

		myRedSlider = createSlider(view, R.id.color_red, color.Red, "red");
		myGreenSlider = createSlider(view, R.id.color_green, color.Green, "green");
		myBlueSlider = createSlider(view, R.id.color_blue, color.Blue, "blue");

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

	static class SeekBarDrawable extends Drawable {
		private final SeekBar mySlider;
		private final Drawable myBase;
		private final String myText;
		private final Paint myPaint;
		private final Paint myOutlinePaint;
		private boolean myLabelOnRight;

		public SeekBarDrawable(Drawable base, String text, SeekBar slider) {
			mySlider = slider;
			myBase = base;
			myText = text;

			myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			myPaint.setTypeface(Typeface.DEFAULT_BOLD);
			myPaint.setColor(Color.BLACK);
			myPaint.setAlpha(255);

			myOutlinePaint = new Paint(myPaint);
			myOutlinePaint.setStyle(Paint.Style.STROKE);
			myOutlinePaint.setStrokeWidth(3);
			myOutlinePaint.setColor(0xFFAAAAAA);

			myLabelOnRight = mySlider.getProgress() < 128;
		}

		@Override
		protected void onBoundsChange(Rect bounds) {
			myBase.setBounds(bounds);
		}
		
		@Override
		protected boolean onStateChange(int[] state) {
			invalidateSelf();
			return false;
		}
		
		@Override
		public boolean isStateful() {
			return true;
		}
		
		@Override
		protected boolean onLevelChange(int level) {
			if (level < 4000) {
				myLabelOnRight = true;
			} else if (level > 6000) {
				myLabelOnRight = false;
			}
			return myBase.setLevel(level);
		}
		
		@Override
		public void draw(Canvas canvas) {
			myBase.draw(canvas);

			final Rect bounds = getBounds();
			final int textSize = bounds.height() * 2 / 3;
			myPaint.setTextSize(textSize);
			myOutlinePaint.setTextSize(textSize);
			final Rect textBounds = new Rect();
			myPaint.getTextBounds("a", 0, 1, textBounds);
			final String text = myText + ": " + mySlider.getProgress();
			final float textWidth = myOutlinePaint.measureText(text);
			final float x = myLabelOnRight ? bounds.width() - textWidth - 6 : 6;
			final float y = bounds.height() / 2 + textBounds.height();
			canvas.drawText(text, x, y, myOutlinePaint);
			canvas.drawText(text, x, y, myPaint);
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}

		@Override
		public void setAlpha(int alpha) {
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
		}
	}
}
