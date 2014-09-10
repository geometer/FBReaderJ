/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.fbreader.book.Note;
import org.geometerplus.fbreader.fbreader.FBReaderApp;

import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.util.UIUtil;

public class SelectionNoteAction extends FBAndroidAction {
	SelectionNoteAction(FBReader baseApplication, FBReaderApp fbreader) {
		super(baseApplication, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		final boolean existingNote;
		final Note note;

		if (params.length != 0) {
			existingNote = true;
			note = (Note)params[0];
			//UIUtil.showMessageText(BaseActivity,note.getText());
		} else {
			existingNote = false;
			note = Reader.addSelectionNote();
		}

		final Intent intent =
			new Intent(BaseActivity.getApplicationContext(), NoteActivity.class);
		FBReaderIntents.putNoteExtra(intent, note);
		intent.putExtra(NoteActivity.EXISTING_NOTE_KEY, existingNote);
		//UIUtil.showMessageText(BaseActivity,note.getText());
		OrientationUtil.startActivity(BaseActivity, intent);
	}
}
