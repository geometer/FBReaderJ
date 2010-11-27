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

class ZLColorPreference extends DialogPreference implements ZLPreference {
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

	@Override
	protected void onBindDialogView(View view) {
		final ZLColor color = myOption.getValue();

		myRedSlider = (SeekBar)view.findViewById(R.id.color_red);
		myRedSlider.setProgress(color.Red);
		myRedSlider.setProgressDrawable(new SeekBarDrawable(
			myRedSlider.getProgressDrawable(),
			ZLResource.resource("color").getResource("red").getValue(),
			color.Red < 128
		));

		myGreenSlider = (SeekBar)view.findViewById(R.id.color_green);
		myGreenSlider.setProgress(color.Green);
		myGreenSlider.setProgressDrawable(new SeekBarDrawable(
			myGreenSlider.getProgressDrawable(),
			ZLResource.resource("color").getResource("green").getValue(),
			color.Green < 128
		));

		myBlueSlider = (SeekBar)view.findViewById(R.id.color_blue);
		myBlueSlider.setProgress(color.Blue);
		myBlueSlider.setProgressDrawable(new SeekBarDrawable(
			myBlueSlider.getProgressDrawable(),
			ZLResource.resource("color").getResource("blue").getValue(),
			color.Blue < 128
		));

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

	static class SeekBarDrawable extends Drawable {
		private final Drawable myBase;
		private final String myText;
		private final Paint myPaint;
		private final Paint myOutlinePaint;
		private final float myTextWidth;
		private boolean myLabelOnRight;

		public SeekBarDrawable(Drawable base, String text, boolean labelOnRight) {
			myBase = base;
			myText = text;

			myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			myPaint.setTypeface(Typeface.DEFAULT_BOLD);
			myPaint.setTextSize(20);
			myPaint.setColor(Color.BLACK);
			myPaint.setAlpha(255);

			myOutlinePaint = new Paint(myPaint);
			myOutlinePaint.setStyle(Paint.Style.STROKE);
			myOutlinePaint.setStrokeWidth(3);
			myOutlinePaint.setColor(0xFFAAAAAA);

			myTextWidth = myOutlinePaint.measureText(myText);
			myLabelOnRight = labelOnRight;
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
			final float x = myLabelOnRight ? bounds.width() - myTextWidth - 6 : 6;
			final float y = (bounds.height() + myPaint.getTextSize()) / 2;
			if (myLabelOnRight) {
				canvas.drawText(myText, x, y, myOutlinePaint);
			}
			canvas.drawText(myText, x, y, myPaint);
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
