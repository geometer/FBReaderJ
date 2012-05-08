/*
 * Copyright (C) 2007-2012 Geometer Plus <contact@geometerplus.com>
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
import org.geometerplus.zlibrary.text.model.ZLTextStyleEntry;

public class ZLTextExplicitlyDecoratedStyle extends ZLTextStyle implements ZLTextStyleEntry.Feature {
	private final ZLTextStyleEntry myEntry;

	public ZLTextExplicitlyDecoratedStyle(ZLTextStyle base, ZLTextStyleEntry entry) {
		super(base, base.Hyperlink);
		myEntry = entry;
	}

	public String getFontFamily() {
		// TODO: implement
		return Base.getFontFamily();
	}
	public int getFontSize() {
		// TODO: implement
		return Base.getFontSize();
	}

	public boolean isBold() {
		// TODO: implement
		return Base.isBold();
	}
	public boolean isItalic() {
		// TODO: implement
		return Base.isItalic();
	}
	public boolean isUnderline() {
		// TODO: implement
		return Base.isUnderline();
	}
	public boolean isStrikeThrough() {
		// TODO: implement
		return Base.isStrikeThrough();
	}

	public int getLeftIndent() {
		// TODO: implement
		return Base.getLeftIndent();
	}
	public int getRightIndent() {
		// TODO: implement
		return Base.getRightIndent();
	}
	public int getFirstLineIndentDelta() {
		// TODO: implement
		return Base.getFirstLineIndentDelta();
	}
	public int getLineSpacePercent() {
		// TODO: implement
		return Base.getLineSpacePercent();
	}
	public int getVerticalShift() {
		// TODO: implement
		return Base.getVerticalShift();
	}
	public int getSpaceBefore() {
		// TODO: implement
		return Base.getSpaceBefore();
	}
	public int getSpaceAfter() {
		// TODO: implement
		return Base.getSpaceAfter();
	}
	public byte getAlignment() {
		return
			myEntry.isFeatureSupported(ALIGNMENT_TYPE)
				? myEntry.getAlignmentType()
				: Base.getAlignment();
	}

	public boolean allowHyphenations() {
		// TODO: implement
		return Base.allowHyphenations();
	}
}
