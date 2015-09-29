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

import android.content.Intent;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.fbreader.util.TextSnippet;

public class SelectionShareAction extends FBAndroidAction {
	SelectionShareAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		final FBView fbview = Reader.getTextView();
		final TextSnippet snippet = fbview.getSelectedSnippet();
		if (snippet == null) {
			return;
		}

		final String text = snippet.getText();
		final String title = Reader.getCurrentBook().getTitle();
		fbview.clearSelection();

		final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(android.content.Intent.EXTRA_SUBJECT,
			ZLResource.resource("selection").getResource("quoteFrom").getValue().replace("%s", title)
		);
		intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
		BaseActivity.startActivity(Intent.createChooser(intent, null));
	}
}
