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

package org.geometerplus.zlibrary.text.view.style;

import java.util.List;

import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.text.model.ZLTextMetrics;
import org.geometerplus.zlibrary.text.view.ZLTextStyle;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;

public abstract class ZLTextDecoratedStyle extends ZLTextStyle {
	// fields to be cached
	protected final ZLTextBaseStyle BaseStyle;

	private List<FontEntry> myFontEntries;
	private boolean myIsItalic;
	private boolean myIsBold;
	private boolean myIsUnderline;
	private boolean myIsStrikeThrough;
	private int myLineSpacePercent;

	private boolean myIsNotCached = true;

	private int myFontSize;
	private int mySpaceBefore;
	private int mySpaceAfter;
	private int myVerticalAlign;
	private Boolean myIsVerticallyAligned;
	private int myLeftMargin;
	private int myRightMargin;
	private int myLeftPadding;
	private int myRightPadding;
	private int myFirstLineIndent;
	private ZLTextMetrics myMetrics;

	protected ZLTextDecoratedStyle(ZLTextStyle base, ZLTextHyperlink hyperlink) {
		super(base, (hyperlink != null) ? hyperlink : base.Hyperlink);
		BaseStyle = base instanceof ZLTextBaseStyle
			? (ZLTextBaseStyle)base
			: ((ZLTextDecoratedStyle)base).BaseStyle;
	}

	private void initCache() {
		myFontEntries = getFontEntriesInternal();
		myIsItalic = isItalicInternal();
		myIsBold = isBoldInternal();
		myIsUnderline = isUnderlineInternal();
		myIsStrikeThrough = isStrikeThroughInternal();
		myLineSpacePercent = getLineSpacePercentInternal();

		myIsNotCached = false;
	}

	private void initMetricsCache(ZLTextMetrics metrics) {
		myMetrics = metrics;
		myFontSize = getFontSizeInternal(metrics);
		mySpaceBefore = getSpaceBeforeInternal(metrics, myFontSize);
		mySpaceAfter = getSpaceAfterInternal(metrics, myFontSize);
		myVerticalAlign = getVerticalAlignInternal(metrics, myFontSize);
		myLeftMargin = getLeftMarginInternal(metrics, myFontSize);
		myRightMargin = getRightMarginInternal(metrics, myFontSize);
		myLeftPadding = getLeftPaddingInternal(metrics, myFontSize);
		myRightPadding = getRightPaddingInternal(metrics, myFontSize);
		myFirstLineIndent = getFirstLineIndentInternal(metrics, myFontSize);
	}

	@Override
	public final List<FontEntry> getFontEntries() {
		if (myIsNotCached) {
			initCache();
		}
		return myFontEntries;
	}
	protected abstract List<FontEntry> getFontEntriesInternal();

	@Override
	public final int getFontSize(ZLTextMetrics metrics) {
		if (!metrics.equals(myMetrics)) {
			initMetricsCache(metrics);
		}
		return myFontSize;
	}
	protected abstract int getFontSizeInternal(ZLTextMetrics metrics);

	@Override
	public final int getSpaceBefore(ZLTextMetrics metrics) {
		if (!metrics.equals(myMetrics)) {
			initMetricsCache(metrics);
		}
		return mySpaceBefore;
	}
	protected abstract int getSpaceBeforeInternal(ZLTextMetrics metrics, int fontSize);

	@Override
	public final int getSpaceAfter(ZLTextMetrics metrics) {
		if (!metrics.equals(myMetrics)) {
			initMetricsCache(metrics);
		}
		return mySpaceAfter;
	}
	protected abstract int getSpaceAfterInternal(ZLTextMetrics metrics, int fontSize);

	@Override
	public final boolean isItalic() {
		if (myIsNotCached) {
			initCache();
		}
		return myIsItalic;
	}
	protected abstract boolean isItalicInternal();

	@Override
	public final boolean isBold() {
		if (myIsNotCached) {
			initCache();
		}
		return myIsBold;
	}
	protected abstract boolean isBoldInternal();

	@Override
	public final boolean isUnderline() {
		if (myIsNotCached) {
			initCache();
		}
		return myIsUnderline;
	}
	protected abstract boolean isUnderlineInternal();

	@Override
	public final boolean isStrikeThrough() {
		if (myIsNotCached) {
			initCache();
		}
		return myIsStrikeThrough;
	}
	protected abstract boolean isStrikeThroughInternal();

	@Override
	public final int getVerticalAlign(ZLTextMetrics metrics) {
		if (!metrics.equals(myMetrics)) {
			initMetricsCache(metrics);
		}
		return myVerticalAlign;
	}
	protected abstract int getVerticalAlignInternal(ZLTextMetrics metrics, int fontSize);

	@Override
	public boolean isVerticallyAligned() {
		if (myIsVerticallyAligned == null) {
			myIsVerticallyAligned = Parent.isVerticallyAligned() || isVerticallyAlignedInternal();
		}
		return myIsVerticallyAligned;
	}
	protected abstract boolean isVerticallyAlignedInternal();

	@Override
	public final int getLeftMargin(ZLTextMetrics metrics) {
		if (!metrics.equals(myMetrics)) {
			initMetricsCache(metrics);
		}
		return myLeftMargin;
	}
	protected abstract int getLeftMarginInternal(ZLTextMetrics metrics, int fontSize);

	@Override
	public final int getRightMargin(ZLTextMetrics metrics) {
		if (!metrics.equals(myMetrics)) {
			initMetricsCache(metrics);
		}
		return myRightMargin;
	}
	protected abstract int getRightMarginInternal(ZLTextMetrics metrics, int fontSize);

	@Override
	public final int getLeftPadding(ZLTextMetrics metrics) {
		if (!metrics.equals(myMetrics)) {
			initMetricsCache(metrics);
		}
		return myLeftPadding;
	}
	protected abstract int getLeftPaddingInternal(ZLTextMetrics metrics, int fontSize);

	@Override
	public final int getRightPadding(ZLTextMetrics metrics) {
		if (!metrics.equals(myMetrics)) {
			initMetricsCache(metrics);
		}
		return myRightPadding;
	}
	protected abstract int getRightPaddingInternal(ZLTextMetrics metrics, int fontSize);

	@Override
	public final int getFirstLineIndent(ZLTextMetrics metrics) {
		if (!metrics.equals(myMetrics)) {
			initMetricsCache(metrics);
		}
		return myFirstLineIndent;
	}
	protected abstract int getFirstLineIndentInternal(ZLTextMetrics metrics, int fontSize);

	@Override
	public final int getLineSpacePercent() {
		if (myIsNotCached) {
			initCache();
		}
		return myLineSpacePercent;
	}
	protected abstract int getLineSpacePercentInternal();
}
