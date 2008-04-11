/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.view.*;
import org.geometerplus.zlibrary.text.view.impl.*;

public abstract class FBView extends ZLTextViewImpl {
	private static ZLIntegerRangeOption ourLeftMarginOption;
	private static ZLIntegerRangeOption ourRightMarginOption;
	private static ZLIntegerRangeOption ourTopMarginOption;
	private static ZLIntegerRangeOption ourBottomMarginOption;
	
	private static ZLBooleanOption ourSelectionOption;

	private static FBIndicatorInfo ourIndicatorInfo;

	private String myCaption;
	
	private static ZLIntegerRangeOption createMarginOption(String name, int defaultValue) {
		return new ZLIntegerRangeOption(
			ZLOption.LOOK_AND_FEEL_CATEGORY, "Options", name, 0, 1000, defaultValue
		);
	}

	FBView(FBReader fbreader, ZLPaintContext context) {
		super(fbreader, context);
	}

	public static final ZLIntegerRangeOption getLeftMarginOption() {
		if (ourLeftMarginOption == null) {
			ourLeftMarginOption = createMarginOption("LeftMargin", 4);
		}
		return ourLeftMarginOption;
	}
	public int getLeftMargin() {
		return getLeftMarginOption().getValue();
	}

	public static final ZLIntegerRangeOption getRightMarginOption() {
		if (ourRightMarginOption == null) {
			ourRightMarginOption = createMarginOption("RightMargin", 4);
		}
		return ourRightMarginOption;
	}
	public int getRightMargin() {
		return getRightMarginOption().getValue();
	}

	public static final ZLIntegerRangeOption getTopMarginOption() {
		if (ourTopMarginOption == null) {
			ourTopMarginOption = createMarginOption("TopMargin", 0);
		}
		return ourTopMarginOption;
	}
	public int getTopMargin() {
		return getTopMarginOption().getValue();
	}

	public static final ZLIntegerRangeOption getBottomMarginOption() {
		if (ourBottomMarginOption == null) {
			ourBottomMarginOption = createMarginOption("BottomMargin", 4);
		}
		return ourBottomMarginOption;
	}
	public int getBottomMargin() {
		return getBottomMarginOption().getValue();
	}

	FBReader getFBReader() {
		return (FBReader)getApplication();
	}
	
	public String getCaption() {
		return myCaption;
	}

	void setCaption(String caption) {
		myCaption = caption;
	}

	public static ZLBooleanOption selectionOption() {
		if (ourSelectionOption == null) {
			ourSelectionOption = new ZLBooleanOption(ZLOption.LOOK_AND_FEEL_CATEGORY, "Options", "IsSelectionEnabled", true);
		}
		return ourSelectionOption;
	}

	public ZLTextIndicatorInfo getIndicatorInfo() {
		return getIndicatorInfoStatic();
	}
	
	public static FBIndicatorInfo getIndicatorInfoStatic() {
		if (ourIndicatorInfo == null) {
			ourIndicatorInfo = new FBIndicatorInfo();
		}
		return ourIndicatorInfo;
	}
}
