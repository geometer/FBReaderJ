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

package org.geometerplus.android.fbreader.dict;

import android.content.Intent;

import org.geometerplus.android.fbreader.FBReaderMainActivity;

final class ColorDict extends DictionaryUtil.PackageInfo {
	private interface ColorDict3 {
		String ACTION = "colordict.intent.action.SEARCH";
		String QUERY = "EXTRA_QUERY";
		String HEIGHT = "EXTRA_HEIGHT";
		String WIDTH = "EXTRA_WIDTH";
		String GRAVITY = "EXTRA_GRAVITY";
		String MARGIN_LEFT = "EXTRA_MARGIN_LEFT";
		String MARGIN_TOP = "EXTRA_MARGIN_TOP";
		String MARGIN_BOTTOM = "EXTRA_MARGIN_BOTTOM";
		String MARGIN_RIGHT = "EXTRA_MARGIN_RIGHT";
		String FULLSCREEN = "EXTRA_FULLSCREEN";
	}

	ColorDict(String id, String title) {
		super(id, title);
	}

	@Override
	void open(String text, Runnable outliner, FBReaderMainActivity fbreader, DictionaryUtil.PopupFrameMetric frameMetrics) {
		final Intent intent = getActionIntent(text);
		intent.putExtra(ColorDict3.HEIGHT, frameMetrics.Height);
		intent.putExtra(ColorDict3.GRAVITY, frameMetrics.Gravity);
		intent.putExtra(ColorDict3.FULLSCREEN, !fbreader.getZLibrary().ShowStatusBarOption.getValue());
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		InternalUtil.startDictionaryActivity(fbreader, intent, this);
	}
}
