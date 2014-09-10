/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.Window;

import org.geometerplus.fbreader.book.Note;

import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;

public class NoteActivity extends PreferenceActivity {
	public static final String EXISTING_NOTE_KEY = "existing.note";

	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	//private boolean myExistingNote;
	private Note myNote;

	@Override
	protected void onCreate(Bundle icicle) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
		setPreferenceScreen(screen);

		myCollection.bindToService(this, new Runnable() {
			public void run() {
				//myExistingNote = getIntent().getBooleanExtra(EXISTING_NOTE_KEY, false);
				myNote = FBReaderIntents.getNoteExtra(getIntent());
				if (myNote == null) {
					finish();
					return;
				}
				screen.addPreference(new TextPreference());
				screen.addPreference(new DeletePreference());
			}
		});
	}

	@Override
	protected void onDestroy() {
		myCollection.unbind();

		super.onDestroy();
	}

	private class TextPreference extends EditTextPreference {
		TextPreference() {
			super(NoteActivity.this);
			super.setDefaultValue(myNote.getText());
			super.setTitle("Note text");//toBeTranslated
			super.setSummary(myNote.getText());
		}

		/* if(!myExistingNote){onClick();} // How do I simulate a click? */

		@Override
		protected void onDialogClosed(boolean result) {
			if (result) {
				myNote.setText(getEditText().getText().toString());
				myCollection.saveNote(myNote);
				setSummary(myNote.getText());
				finish();
			}
			super.onDialogClosed(result);
		}
	}

	private class DeletePreference extends CheckBoxPreference {
		DeletePreference() {
			super(NoteActivity.this);
			setChecked(true);
			//setTitle("Uncheck to delete this note:");
		}

		@Override
		protected void onClick() {
			super.onClick();
			if (!isChecked()) {
				myCollection.deleteNote(myNote);
			}
		}
	}
}
