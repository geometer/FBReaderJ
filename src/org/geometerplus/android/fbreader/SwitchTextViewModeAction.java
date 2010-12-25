/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader;

import org.geometerplus.zlibrary.text.view.ZLTextViewMode;

import org.geometerplus.fbreader.fbreader.FBAction;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

class SwitchTextViewModeAction extends FBAction {
	private final FBReader myBaseActivity;
	private final int myMode;

	SwitchTextViewModeAction(FBReader baseActivity, FBReaderApp fbreader, int mode) {
		super(fbreader);
		myBaseActivity = baseActivity;
		myMode = mode;
	}
		
	@Override
	public boolean isEnabled() {
		return Reader.TextViewModeOption.getValue() != myMode;
	}

	@Override
	public void run() {
		if (myMode == ZLTextViewMode.MODE_VISIT_ALL_WORDS) {
			DictionaryUtil.installDictionaryIfNotInstalled(myBaseActivity);
		}

		Reader.TextViewModeOption.setValue(myMode);
		Reader.BookTextView.resetRegionPointer();
		Reader.FootnoteView.resetRegionPointer();
		Reader.repaintView();
	}
}
