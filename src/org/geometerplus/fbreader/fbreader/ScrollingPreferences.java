/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.options.ZLEnumOption;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.view.ZLView;

public class ScrollingPreferences {
	private static ScrollingPreferences ourInstance;

	public static ScrollingPreferences Instance() {
		return (ourInstance != null) ? ourInstance : new ScrollingPreferences();
	}

	public static enum FingerScrolling {
		byTap, byFlick, byTapAndFlick
	}
	public final ZLEnumOption<FingerScrolling> FingerScrollingOption =
		new ZLEnumOption<FingerScrolling>("Scrolling", "Finger", FingerScrolling.byTapAndFlick);

	public final ZLEnumOption<ZLView.Animation> AnimationOption =
		new ZLEnumOption<ZLView.Animation>("Scrolling", "Animation", ZLView.Animation.slide);
	public final ZLIntegerRangeOption AnimationSpeedOption =
		new ZLIntegerRangeOption("Scrolling", "AnimationSpeed", 1, 10, 4);

	public final ZLBooleanOption HorizontalOption =
		new ZLBooleanOption("Scrolling", "Horizontal", true);
	public static enum TapZonesScheme {
		left_to_right, right_to_left, up, down, custom
	}
	public final ZLEnumOption<TapZonesScheme> TapZonesSchemeOption =
		new ZLEnumOption<TapZonesScheme>("Scrolling", "TapZonesScheme", TapZonesScheme.right_to_left);

	private ScrollingPreferences() {
		ourInstance = this;
	}
}
