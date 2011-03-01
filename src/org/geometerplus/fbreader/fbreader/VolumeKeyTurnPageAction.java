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

import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;

class VolumeKeyTurnPageAction extends FBAction {
	private final boolean myForward;

	VolumeKeyTurnPageAction(FBReaderApp fbreader, boolean forward) {
		super(fbreader);
		myForward = forward;
	}

	public boolean isEnabled() {
		return ScrollingPreferences.Instance().VolumeKeysOption.getValue();
	}

	public void run() {
		final ScrollingPreferences preferences = ScrollingPreferences.Instance();

		boolean forward = myForward;
		if (preferences.InvertVolumeKeysOption.getValue()) {
			forward = !forward;
		}

		if (forward) {
			ZLTextWordCursor cursor = Reader.getTextView().getEndCursor();
			if (cursor == null || cursor.isNull() ||
				(cursor.isEndOfParagraph() && cursor.getParagraphCursor().isLast())) {
				return;
			}
		} else {
			ZLTextWordCursor cursor = Reader.getTextView().getStartCursor();
			if (cursor == null || cursor.isNull() ||
				(cursor.isStartOfParagraph() && cursor.getParagraphCursor().isFirst())) {
				return;
			}
		}
		final FBView view = Reader.getTextView();
		if (view.getAnimationType() != FBView.Animation.none) {
			final boolean horizontal = preferences.HorizontalOption.getValue();
			if (forward) {
				view.startAutoScrolling(horizontal ? FBView.PAGE_RIGHT : FBView.PAGE_BOTTOM);
			} else {
				view.startAutoScrolling(horizontal ? FBView.PAGE_LEFT : FBView.PAGE_TOP);
			}
		} else {
			view.scrollPage(forward, FBView.ScrollingMode.NO_OVERLAPPING, 0);
			Reader.repaintView();
		}
	}
}
