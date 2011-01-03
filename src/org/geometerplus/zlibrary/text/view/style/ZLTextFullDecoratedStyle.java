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

package org.geometerplus.zlibrary.text.view.style;

import org.geometerplus.zlibrary.text.view.ZLTextStyle;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;
import org.geometerplus.zlibrary.text.model.ZLTextAlignmentType;

public class ZLTextFullDecoratedStyle extends ZLTextPartialDecoratedStyle {
	private final ZLTextFullStyleDecoration myFullDecoration;
	
	ZLTextFullDecoratedStyle(ZLTextStyle base, ZLTextFullStyleDecoration decoration, ZLTextHyperlink hyperlink) {
		super(base, decoration, hyperlink);
		myFullDecoration = decoration;	
	}

	@Override
	public int getLeftIndent() {
		return Base.getLeftIndent() + myFullDecoration.LeftIndentOption.getValue();
	}

	@Override
	public int getRightIndent() {
		return Base.getRightIndent() + myFullDecoration.RightIndentOption.getValue();
	}

	@Override
	public int getFirstLineIndentDelta() {
		return (getAlignment() == ZLTextAlignmentType.ALIGN_CENTER) ? 0 : Base.getFirstLineIndentDelta() + myFullDecoration.FirstLineIndentDeltaOption.getValue();
	}
	
	@Override
	public int getLineSpacePercent() {
		int value = myFullDecoration.LineSpacePercentOption.getValue();
		return (value != -1) ? value : Base.getLineSpacePercent();
	}

	@Override
	public int getSpaceBefore() {
		return myFullDecoration.SpaceBeforeOption.getValue();
	}

	@Override
	public int getSpaceAfter() {
		return myFullDecoration.SpaceAfterOption.getValue();
	}		

	@Override
	public byte getAlignment() {
		byte value = (byte)myFullDecoration.AlignmentOption.getValue();
		return (value == ZLTextAlignmentType.ALIGN_UNDEFINED) ? Base.getAlignment() : value;
	}
}
