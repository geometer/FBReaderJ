/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;

class AnimationSpeedPreference extends DialogPreference {
	private final ZLIntegerRangeOption myOption;
	private final ZLResource myResource;

	private SeekBar mySlider;

	AnimationSpeedPreference(Context context, ZLResource resource, String resourceKey, ZLIntegerRangeOption option) {
		super(context, null);
		myOption = option;
		myResource = resource.getResource(resourceKey);
		final String title = myResource.getValue();
		setTitle(title);
		setDialogTitle(title);
		setDialogLayoutResource(R.layout.animation_speed_dialog);

		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		setPositiveButtonText(buttonResource.getResource("ok").getValue());
		setNegativeButtonText(buttonResource.getResource("cancel").getValue());
	}

	@Override
	protected void onBindDialogView(View view) {
		mySlider = (SeekBar)view.findViewById(R.id.animation_speed_slider);
		mySlider.setMax(myOption.MaxValue - myOption.MinValue);
		mySlider.setProgress(myOption.getValue() - myOption.MinValue);
		mySlider.setProgressDrawable(new SeekBarDrawable());

		super.onBindDialogView(view);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			myOption.setValue(myOption.MinValue + mySlider.getProgress());
		}
	}

	private class SeekBarDrawable extends Drawable {
		private final Drawable myBase;
		private final Paint myPaint;
		private final Paint myOutlinePaint;

		public SeekBarDrawable() {
			myBase = mySlider.getProgressDrawable();

			myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			myPaint.setTypeface(Typeface.DEFAULT_BOLD);
			myPaint.setColor(Color.BLACK);
			myPaint.setAlpha(255);

			myOutlinePaint = new Paint(myPaint);
			myOutlinePaint.setStyle(Paint.Style.STROKE);
			myOutlinePaint.setStrokeWidth(3);
			myOutlinePaint.setColor(0xFFAAAAAA);
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
			final float y = bounds.height() / 2 + textBounds.height();

			final int progress = mySlider.getProgress();
			final int max = mySlider.getMax();
			if (progress >= max / 3) {
				final String text = myResource.getResource("slow").getValue();
				//final float textWidth = myOutlinePaint.measureText(text);
				final float x = 6;
				canvas.drawText(text, x, y, myOutlinePaint);
				canvas.drawText(text, x, y, myPaint);
			}
			if (progress <= 2 * max / 3) {
				final String text = myResource.getResource("fast").getValue();
				final float textWidth = myOutlinePaint.measureText(text);
				final float x = bounds.width() - textWidth - 6;
				canvas.drawText(text, x, y, myOutlinePaint);
				canvas.drawText(text, x, y, myPaint);
			}
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
