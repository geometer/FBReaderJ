/*
 * Copyright (C) 2009-2012 Geometer Plus <contact@geometerplus.com>
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
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.language.ZLLanguageUtil;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;

import org.geometerplus.fbreader.formats.util.NativeUtil;
import org.geometerplus.fbreader.library.Book;

import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.android.fbreader.library.SQLiteBooksDatabase;
import org.geometerplus.android.fbreader.FBReader;

class BookTitlePreference extends ZLStringPreference {
	private final Book myBook;

	BookTitlePreference(Context context, ZLResource rootResource, String resourceKey, Book book) {
		super(context, rootResource, resourceKey);
		myBook = book;
		super.setValue(book.getTitle());
	}

	@Override
	protected void setValue(String value) {
		super.setValue(value);
		myBook.setTitle(value);
	}
}

class LanguagePreference extends ZLStringListPreference {
	private final Book myBook;

	LanguagePreference(Context context, ZLResource rootResource, String resourceKey, Book book) {
		super(context, rootResource, resourceKey);
		myBook = book;
		final TreeSet<String> set = new TreeSet<String>(new ZLLanguageUtil.CodeComparator());
		set.addAll(ZLTextHyphenator.Instance().languageCodes());
		set.add(ZLLanguageUtil.OTHER_LANGUAGE_CODE);

		final int size = set.size();
		String[] codes = new String[size];
		String[] names = new String[size];
		int index = 0;
		for (String code : set) {
			codes[index] = code;
			names[index] = ZLLanguageUtil.languageName(code);
			++index;
		}
		setLists(codes, names);
		String language = myBook.getLanguage();
		if (language == null) {
			language = ZLLanguageUtil.OTHER_LANGUAGE_CODE;
		}
		if (!setInitialValue(language)) {
			setInitialValue(ZLLanguageUtil.OTHER_LANGUAGE_CODE);
		}
	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		if (result) {
			final String value = getValue();
			myBook.setLanguage(value.length() > 0 ? value : null);
		}
	}
}

class EncodingPreference extends ZLStringListPreference {
	private final Activity myActivity;
	private final Book myBook;

	EncodingPreference(Activity activity, ZLResource rootResource, String resourceKey, Book book) {
		super(activity, rootResource, resourceKey);
		myActivity = activity;
		myBook = book;
		String encoding = myBook.getEncoding();
		if (encoding == null) {
			encoding = "auto";
		}
		final TreeMap<String, String> namesMap = new TreeMap<String, String>();
		if (!"auto".equals(encoding)) {
			NativeUtil.collectEncodingNames(namesMap);
			if (!namesMap.containsKey(encoding)) {
				for (String key: namesMap.keySet()) {
					if (key.equalsIgnoreCase(encoding)) {
						encoding = key;
						break;
					}
				}
			}
		}
		if (!namesMap.containsKey(encoding)) {
			namesMap.put(encoding, encoding);
		}
		String[] names = new String[namesMap.size()];
		String[] values = new String[namesMap.size()];
		int index = 0;
		for (String key : namesMap.keySet()) {
			values[index] = key;
			names[index++] = namesMap.get(key);
		}
		setLists(values, names);
		setInitialValue(encoding);
	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		if (result) {
			final String value = getValue();
			if (!"auto".equals(value) && !value.equalsIgnoreCase(myBook.getEncoding())) {
				myBook.setEncoding(value);
				myActivity.setResult(FBReader.RESULT_RELOAD_BOOK);
			}
		}
	}

	public void onAccept() {
	}
}

public class EditBookInfoActivity extends ZLPreferenceActivity {
	private Book myBook;

	public EditBookInfoActivity() {
		super("BookInfo");
	}

	@Override
	protected void init(Intent intent) {
		if (SQLiteBooksDatabase.Instance() == null) {
			new SQLiteBooksDatabase(this, "LIBRARY");
		}

		final String path = intent.getStringExtra(BookInfoActivity.CURRENT_BOOK_PATH_KEY);
		final ZLFile file = ZLFile.createFileByPath(path);
		myBook = Book.getByFile(file);
		setResult(FBReader.RESULT_REPAINT);

		if (myBook == null) {
			finish();
			return;
		}

		myBook.loadLanguageAndEncoding();

		addPreference(new BookTitlePreference(this, Resource, "title", myBook));
		addPreference(new LanguagePreference(this, Resource, "language", myBook));
		addPreference(new EncodingPreference(this, Resource, "encoding", myBook));
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (myBook != null) {
			myBook.save();
		}
	}
}
