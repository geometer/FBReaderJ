/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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

import java.util.TreeMap;

import android.content.Context;
import android.content.Intent;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.language.ZLLanguageUtil;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;

import org.geometerplus.fbreader.library.Book;

import org.geometerplus.android.fbreader.SQLiteBooksDatabase;

class BookTitlePreference extends ZLStringPreference {
	private final Book myBook;

	BookTitlePreference(Context context, ZLResource rootResource, String resourceKey, Book book) {
		super(context, rootResource, resourceKey);
		myBook = book;
		setValue(book.getTitle());
	}

	public void onAccept() {
		myBook.setTitle(getValue());
	}
}

class LanguagePreference extends ZLStringListPreference {
	private final Book myBook;

	LanguagePreference(Context context, ZLResource rootResource, String resourceKey, Book book) {
		super(context, rootResource, resourceKey);
		myBook = book;
		final TreeMap<String,String> map = new TreeMap<String,String>();
		for (String code : ZLLanguageUtil.languageCodes()) {
			map.put(ZLLanguageUtil.languageName(code), code);
		}
		final int size = map.size();
		String[] codes = new String[size + 1];
		String[] names = new String[size + 1];
		int index = 0;
		for (TreeMap.Entry<String,String> entry : map.entrySet()) {
			codes[index] = entry.getValue();
			names[index] = entry.getKey();
			++index;
		}
		codes[size] = ZLLanguageUtil.OTHER_LANGUAGE_CODE;
		names[size] = ZLLanguageUtil.languageName(codes[size]);
		setLists(codes, names);
		String language = myBook.getLanguage();
		if (language == null) {
			language = ZLLanguageUtil.OTHER_LANGUAGE_CODE;
		}
		if (!setInitialValue(language)) {
			setInitialValue(ZLLanguageUtil.OTHER_LANGUAGE_CODE);
		}
	}

	public void onAccept() {
		final String value = getValue();
		myBook.setLanguage((value.length() != 0) ? value : null);
	}
}

public class BookInfoActivity extends ZLPreferenceActivity {
	public static final String CURRENT_BOOK_PATH_KEY = "CurrentBookPath";
	public static final String CURRENT_BOOK_ARCHIVE_ENTRY_KEY = "CurrentArchiveEntryPath";

	private Book myBook;

	public BookInfoActivity() {
		super("BookInfo");
	}

	@Override
	protected void init(Intent intent) {
		if (SQLiteBooksDatabase.Instance() == null) {
			new SQLiteBooksDatabase("LIBRARY");
		}

		final String path = intent.getStringExtra(CURRENT_BOOK_PATH_KEY);
		final String archiveEntry = intent.getStringExtra(CURRENT_BOOK_ARCHIVE_ENTRY_KEY);
		ZLFile file = ZLFile.createFile(null, path);
		if (archiveEntry != null) {
			file = ZLFile.createFile(file, archiveEntry);
		}
		myBook = Book.getByFile(file);

		final Category commonCategory = createCategory(null);
		if (myBook.File.getPhysicalFile() != null) {
			commonCategory.addPreference(new InfoPreference(
				this,
				commonCategory.Resource.getResource("fileName").getValue(),
				myBook.File.getPath())
			);
		}
		commonCategory.addPreference(new BookTitlePreference(this, commonCategory.Resource, "title", myBook));
		commonCategory.addPreference(new LanguagePreference(this, commonCategory.Resource, "language", myBook));
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (myBook.save()) {
			ZLTextHyphenator.Instance().load(myBook.getLanguage());
		}
	}
}
