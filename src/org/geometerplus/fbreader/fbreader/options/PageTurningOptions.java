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

package org.geometerplus.fbreader.fbreader.options;

import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.view.ZLView;

public class PageTurningOptions {
	public static enum FingerScrollingType {
		byTap, byFlick, byTapAndFlick
	}
	public final ZLEnumOption<FingerScrollingType> FingerScrolling =
		new ZLEnumOption<FingerScrollingType>("Scrolling", "Finger", FingerScrollingType.byTapAndFlick);

	public final ZLEnumOption<ZLView.Animation> Animation =
		new ZLEnumOption<ZLView.Animation>("Scrolling", "Animation", ZLView.Animation.slide);
	public final ZLIntegerRangeOption AnimationSpeed =
		new ZLIntegerRangeOption("Scrolling", "AnimationSpeed", 1, 10, 7);

	public final ZLBooleanOption Horizontal =
		new ZLBooleanOption("Scrolling", "Horizontal", true);
	public final ZLStringOption TapZoneMap =
		new ZLStringOption("Scrolling", "TapZoneMap", "");
}
