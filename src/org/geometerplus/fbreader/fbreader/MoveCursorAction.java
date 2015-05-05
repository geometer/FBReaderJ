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

package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.text.view.ZLTextRegion;
import org.geometerplus.zlibrary.text.view.ZLTextWordRegionSoul;

class MoveCursorAction extends FBAction {
	private final FBView.Direction myDirection;

	MoveCursorAction(FBReaderApp fbreader, FBView.Direction direction) {
		super(fbreader);
		myDirection = direction;
	}

	@Override
	protected void run(Object ... params) {
		final FBView fbView = Reader.getTextView();
		ZLTextRegion region = fbView.getOutlinedRegion();
		final ZLTextRegion.Filter filter =
			(region != null && region.getSoul() instanceof ZLTextWordRegionSoul)
				|| Reader.MiscOptions.NavigateAllWords.getValue()
					? ZLTextRegion.AnyRegionFilter : ZLTextRegion.ImageOrHyperlinkFilter;
		region = fbView.nextRegion(myDirection, filter);
		if (region != null) {
			fbView.outlineRegion(region);
		} else {
			switch (myDirection) {
				case down:
					fbView.turnPage(true, FBView.ScrollingMode.SCROLL_LINES, 1);
					break;
				case up:
					fbView.turnPage(false, FBView.ScrollingMode.SCROLL_LINES, 1);
					break;
			}
		}

		Reader.getViewWidget().reset();
		Reader.getViewWidget().repaint();
	}
}
