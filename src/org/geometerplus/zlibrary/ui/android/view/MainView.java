/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.zlibrary.ui.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import org.geometerplus.android.fbreader.FBReaderMainActivity;

public abstract class MainView extends View {
	protected Integer myColorLevel;

	public MainView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MainView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MainView(Context context) {
		super(context);
	}

	public final void setScreenBrightness(int percent) {
		if (percent < 1) {
			percent = 1;
		} else if (percent > 100) {
			percent = 100;
		}

		final Context context = getContext();
		if (!(context instanceof FBReaderMainActivity)) {
			return;
		}
		final FBReaderMainActivity activity = (FBReaderMainActivity)context;
		activity.getZLibrary().ScreenBrightnessLevelOption.setValue(percent);
		if (percent >= 50) {
			myColorLevel = null;
		} else if (percent >= 25) {
			myColorLevel = null;
			percent = Math.max(2 * percent - 50, 1);
		} else {
			myColorLevel = 0x60 + (0xFF - 0x60) * Math.max(percent, 0) / 25;
			percent = 1;
		}
		activity.setScreenBrightnessSystem(percent);
		updateColorLevel();
		postInvalidate();
	}

	public final int getScreenBrightness() {
		if (myColorLevel != null) {
			return (myColorLevel - 0x60) * 25 / (0xFF - 0x60);
		}

		final Context context = getContext();
		if (!(context instanceof FBReaderMainActivity)) {
			return 50;
		}
		final float level = ((FBReaderMainActivity)context).getScreenBrightnessSystem();
		if (level >= .5f) {
			return (int)(100 * level);
		} else {
			return (int)(50 * level + 25);
		}
	}

	protected abstract void updateColorLevel();
}
