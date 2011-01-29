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

import android.content.Intent;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.fbreader.FBAction;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

class CancelAction extends FBAction {
	private final FBReader myBaseActivity;

	enum Type {
		OPEN_BOOK,
		GOTO,
		CLOSE
	}

	static final class Description {
		final Type Type;
		final String Title;
		final String Summary;

		Description(Type type, String title, String summary) {
			Type = type;
			Title = title;
			Summary = summary;
		}
	}

	CancelAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(fbreader);
		myBaseActivity = baseActivity;
	}

	private void fillDescriptionList() {
		final ZLResource resource = ZLResource.resource("cancelMenu");
		myBaseActivity.CancelActionsList.clear();
		myBaseActivity.CancelActionsList.add(new Description(
			Type.OPEN_BOOK, resource.getResource("previousBook").getValue(), "this is a summary"
		));
		myBaseActivity.CancelActionsList.add(new Description(
			Type.GOTO, resource.getResource("goto").getValue(), "this is a summary"
		));
		myBaseActivity.CancelActionsList.add(new Description(
			Type.GOTO, resource.getResource("goto").getValue(), "this is a summary"
		));
		myBaseActivity.CancelActionsList.add(new Description(
			Type.GOTO, resource.getResource("goto").getValue(), "this is a summary"
		));
		myBaseActivity.CancelActionsList.add(new Description(
			Type.CLOSE, resource.getResource("close").getValue(), null
		));
	}

	public void run() {
		if (Reader.getCurrentView() != Reader.BookTextView) {
			Reader.showBookTextView();
		} else {
			fillDescriptionList();
			if (myBaseActivity.CancelActionsList.size() == 1) {
				Reader.closeWindow();
			} else {
				final Intent intent = new Intent();
				intent.setClass(myBaseActivity, CancelActivity.class);
				intent.putExtra(CancelActivity.LIST_SIZE, myBaseActivity.CancelActionsList.size());
				int index = 0;
				for (Description description : myBaseActivity.CancelActionsList) {
					intent.putExtra(CancelActivity.ITEM_TITLE + index, description.Title);
					intent.putExtra(CancelActivity.ITEM_SUMMARY + index, description.Summary);
					++index;
				}
				myBaseActivity.startActivityForResult(intent, FBReader.CANCEL_CODE);
			}
		}
	}
}
