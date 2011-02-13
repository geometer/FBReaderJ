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

package org.geometerplus.android.fbreader;

import java.util.List;

import org.geometerplus.fbreader.fbreader.FBAction;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import android.content.Intent;

class CancelAction extends FBAction {
	private final FBReader myBaseActivity;
	private final boolean myForceMenu;

	CancelAction(FBReader baseActivity, FBReaderApp fbreader, boolean forceMenu) {
		super(fbreader);
		myBaseActivity = baseActivity;
		myForceMenu = forceMenu;
	}

	public void run() {
		if (Reader.getCurrentView() != Reader.BookTextView) {
			Reader.showBookTextView();
		} else {
			if (!myForceMenu && Reader.HyperlinkHistory.hasBackHistory()) {
				backActivity();
			} else {
				final List<FBReaderApp.CancelActionDescription> descriptionList =
					Reader.getCancelActionsList();
				if (descriptionList.size() == 1) {
					Reader.closeWindow();
				} else {
					final Intent intent = new Intent();
					intent.setClass(myBaseActivity, CancelActivity.class);
					intent.putExtra(CancelActivity.LIST_SIZE, descriptionList.size());
					int index = 0;
					for (FBReaderApp.CancelActionDescription description : descriptionList) {
						intent.putExtra(CancelActivity.ITEM_TITLE + index, description.Title);
						intent.putExtra(CancelActivity.ITEM_SUMMARY + index, description.Summary);
						intent.putExtra(CancelActivity.ITEM_ENABLED + index, description.Enabled);
						++index;
					}
					myBaseActivity.startActivityForResult(intent, FBReader.CANCEL_CODE);
				}
			}
		}
	}

	private void backActivity() {
		ZLTextPosition position = Reader.getTextView().getStartCursor();
		position = Reader.HyperlinkHistory.back(position);
		Reader.BookTextView.gotoPosition(position);
		Reader.showBookTextView();
	}
}
