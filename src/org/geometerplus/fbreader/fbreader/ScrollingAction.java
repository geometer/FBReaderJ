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

import org.geometerplus.zlibrary.text.view.ZLTextView;

class ScrollingAction extends FBAction {
	private final ScrollingOptions myOptions;
	private final boolean myForward;

	ScrollingAction(FBReader fbreader, ScrollingOptions options, boolean forward) {
		super(fbreader);
		myOptions = options;
		myForward = forward;
	}
		
	public boolean isEnabled() {
		// TODO: implement
		return true;
	}

	public void run() {
		// TODO: use delay option
		final int mode = myOptions.ModeOption.getValue();
		int value = 0;
		switch (mode) {
			case ZLTextView.ScrollingMode.KEEP_LINES:
				value = myOptions.LinesToKeepOption.getValue();
				break;
			case ZLTextView.ScrollingMode.SCROLL_LINES:
				value = myOptions.LinesToScrollOption.getValue();
				break;
			case ZLTextView.ScrollingMode.SCROLL_PERCENTAGE:
				value = myOptions.PercentToScrollOption.getValue();
				break;
		}
		Reader.getTextView().scrollPage(myForward, mode, value);
		Reader.refreshWindow();
	}		
}
