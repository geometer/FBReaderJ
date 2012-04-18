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

package org.geometerplus.android.fbreader;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.library.Bookmark;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;

import org.geometerplus.android.util.UIUtil;

public class SelectionBookmarkAction extends FBAndroidAction {
	SelectionBookmarkAction(FBReader baseApplication, FBReaderApp fbreader) {
		super(baseApplication, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		final FBView fbview = Reader.getTextView();
		final String text = fbview.getSelectedText();

		new Bookmark(
			Reader.Model.Book,
			fbview.getModel().getId(),
			fbview.getSelectionStartPosition(),
			text,
			true
		).save();
		fbview.clearSelection();

		UIUtil.showMessageText(
			BaseActivity,
			ZLResource.resource("selection").getResource("bookmarkCreated").getValue().replace("%s", text)
		);
	}
}
