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

package org.geometerplus.zlibrary.text.view;

import java.util.List;

import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.text.model.ZLTextMetrics;

public abstract class ZLTextStyle {
	public final ZLTextStyle Parent;
	public final ZLTextHyperlink Hyperlink;

	protected ZLTextStyle(ZLTextStyle parent, ZLTextHyperlink hyperlink) {
		Parent = parent != null ? parent : this;
		Hyperlink = hyperlink;
	}

	public abstract List<FontEntry> getFontEntries();
	public abstract int getFontSize(ZLTextMetrics metrics);

	public abstract boolean isBold();
	public abstract boolean isItalic();
	public abstract boolean isUnderline();
	public abstract boolean isStrikeThrough();

	public final int getLeftIndent(ZLTextMetrics metrics) {
		return getLeftMargin(metrics) + getLeftPadding(metrics);
	}
	public final int getRightIndent(ZLTextMetrics metrics) {
		return getRightMargin(metrics) + getRightPadding(metrics);
	}
	public abstract int getLeftMargin(ZLTextMetrics metrics);
	public abstract int getRightMargin(ZLTextMetrics metrics);
	public abstract int getLeftPadding(ZLTextMetrics metrics);
	public abstract int getRightPadding(ZLTextMetrics metrics);

	public abstract int getFirstLineIndent(ZLTextMetrics metrics);
	public abstract int getLineSpacePercent();
	public abstract int getVerticalAlign(ZLTextMetrics metrics);
	public abstract boolean isVerticallyAligned();
	public abstract int getSpaceBefore(ZLTextMetrics metrics);
	public abstract int getSpaceAfter(ZLTextMetrics metrics);
	public abstract byte getAlignment();

	public abstract boolean allowHyphenations();
}
