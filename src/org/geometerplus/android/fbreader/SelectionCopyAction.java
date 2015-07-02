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

package org.geometerplus.android.fbreader;

import android.app.Application;
import android.content.ClipboardManager;
import android.content.ClipData;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

import org.geometerplus.android.util.UIMessageUtil;

public class SelectionCopyAction extends FBAndroidAction {
	SelectionCopyAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		final String text = Reader.getTextView().getSelectedSnippet().getText();
		Reader.getTextView().clearSelection();

		final ClipboardManager clipboard =
			(ClipboardManager)BaseActivity.getApplication().getSystemService(Application.CLIPBOARD_SERVICE);
		clipboard.setPrimaryClip(ClipData.newPlainText("FBReader", text));
		UIMessageUtil.showMessageText(
			BaseActivity,
			ZLResource.resource("selection").getResource("textInBuffer").getValue().replace("%s", clipboard.getText())
		);
	}
}
