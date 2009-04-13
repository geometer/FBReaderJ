/*
 * Copyright (C) 2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.preferences;

import android.content.Context;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.fbreader.FBReader;
import org.geometerplus.fbreader.collection.BookDescription;

class BookTitlePreference extends ZLStringPreference {
	private final BookDescription myDescription;

	BookTitlePreference(Context context, ZLResource rootResource, String resourceKey, BookDescription description) {
		super(context, rootResource, resourceKey);
		myDescription = description;
		setValue(description.getTitle());
	}

	public void accept() {
		//myDescription.setTitle(getEditText().getText().toString());
	}
}

public class BookInfoActivity extends ZLPreferenceActivity {
	private BookDescription myDescription;

	public BookInfoActivity() {
		super("BookInfo");
	}

	@Override
	protected void init() {
		final Category commonCategory = new Category(null);
		myDescription = ((FBReader)FBReader.Instance()).Model.Description;
		commonCategory.addPreference(new BookTitlePreference(this, commonCategory.getResource(), "title", myDescription));
	}

	@Override
	protected void onPause() {
		super.onPause();
		myDescription.save();
	}
}
