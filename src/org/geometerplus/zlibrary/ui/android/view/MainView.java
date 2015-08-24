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
		final Context context = getContext();
		if (!(context instanceof FBReaderMainActivity)) {
			return;
		}
		((FBReaderMainActivity)context).setScreenBrightnessSystem(percent);
		if (percent >= 50) {
			myColorLevel = null;
		} else {
			myColorLevel = 0x60 + (0xFF - 0x60) * Math.max(percent, 0) / 50;
		}
		updateColorLevel();
		postInvalidate();
	}

	public final int getScreenBrightness() {
		final Context context = getContext();
		if (!(context instanceof FBReaderMainActivity)) {
			return 50;
		}
		return ((FBReaderMainActivity)context).getScreenBrightnessSystem();
	}

	protected abstract void updateColorLevel();
}
