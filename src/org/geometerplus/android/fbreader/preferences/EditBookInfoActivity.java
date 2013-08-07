/*
 * Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com>
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

import java.util.*;

import android.content.Context;
import android.content.Intent;

import org.geometerplus.zlibrary.core.encodings.Encoding;
import org.geometerplus.zlibrary.core.language.Language;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;

import org.geometerplus.fbreader.book.Author;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.SeriesInfo;
import org.geometerplus.fbreader.book.Tag;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.formats.FormatPlugin;

import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;

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
		((EditBookInfoActivity)getContext()).saveBook();
	}
}

class BookAuthorsPreference extends ZLStringPreference {
	private final Book myBook;

	BookAuthorsPreference(Context context, ZLResource rootResource, String resourceKey, Book book) {
		super(context, rootResource, resourceKey);
		myBook = book;
		final StringBuilder buffer = new StringBuilder();
		final List<Author> authors = myBook.authors();
		for (Author a : authors) {
			if (buffer.length() > 0) {
				buffer.append("; ");
			}
			buffer.append(a.DisplayName);
		}
		super.setValue(buffer.toString());
	}
	@Override
	protected void setValue(String value) {
		super.setValue(value);
		myBook.replaceAuthorsWithList(value);
		
		((EditBookInfoActivity)getContext()).saveBook();
	}
}

class BookTagsPreference extends ZLStringPreference {
	private final Book myBook;

	BookTagsPreference(Context context, ZLResource rootResource, String resourceKey, Book book) {
		super(context, rootResource, resourceKey);
		myBook = book;
		final StringBuilder buffer = new StringBuilder();
		final List<Tag> tags = myBook.tags();
		for (Tag t : tags) {
			if (buffer.length() > 0) {
				buffer.append(", ");
			}
			buffer.append(t.Name);
		}
		super.setValue(buffer.toString());
	}
	@Override
	protected void setValue(String value) {
		super.setValue(value);
		myBook.replaceTagsWithList(value);
		EditBookInfoActivity vari = (EditBookInfoActivity)getContext();
		(vari).saveBook();
	}
}

class BookSeriesPreference extends ZLStringPreference {
	private final Book myBook;
	private final SeriesInfo series;

	BookSeriesPreference(Context context, ZLResource rootResource, String resourceKey, Book book) {
		super(context, rootResource, resourceKey);
		myBook = book;
		series = myBook.getSeriesInfo();
		String title = "";
		if(series != null){
			title = series.Series.getTitle();
		}
		super.setValue(title);
	}
	
	@Override
	protected void setValue(String value) {
		super.setValue(value);
		String seriesIndexString = "1";
		if(series != null && series.Index != null){
			seriesIndexString = series.Index.toString();
		}
		myBook.setSeriesInfo(value, seriesIndexString);
		((EditBookInfoActivity)getContext()).saveBook();
	}
}

class BookSeriesIndexPreference extends ZLStringPreference {
	private final Book myBook;
	private final SeriesInfo series;

	BookSeriesIndexPreference(Context context, ZLResource rootResource, String resourceKey, Book book) {
		super(context, rootResource, resourceKey);
		myBook = book;
		series = myBook.getSeriesInfo();
		String seriesIndexString = null;
		if (series != null && series.Index != null) {
			seriesIndexString = series.Index.toString();
		}
		super.setValue(seriesIndexString);
	}
	
	@Override
	protected void setValue(String value) {
		super.setValue(value);
		String title = "Unknown";
		if(series != null){
			title = series.Series.getTitle();
		}
		myBook.setSeriesInfo(title, value);
		((EditBookInfoActivity)getContext()).saveBook();
	}
}

class BookLanguagePreference extends LanguagePreference {
	private final Book myBook;

	private static List<Language> languages() {
		final TreeSet<Language> set = new TreeSet<Language>();
		for (String code : ZLTextHyphenator.Instance().languageCodes()) {
			set.add(new Language(code));
		}
		set.add(new Language(Language.OTHER_CODE));
		return new ArrayList<Language>(set);
	}

	BookLanguagePreference(Context context, ZLResource rootResource, String resourceKey, Book book) {
		super(context, rootResource, resourceKey, languages());
		myBook = book;
		final String language = myBook.getLanguage();
		if (language == null || !setInitialValue(language)) {
			setInitialValue(Language.OTHER_CODE);
		}
	}

	@Override
	protected void init() {
	}

	@Override
	protected void setLanguage(String code) {
		myBook.setLanguage(code.length() > 0 ? code : null);
		((EditBookInfoActivity)getContext()).saveBook();
	}
}

class EncodingPreference extends ZLStringListPreference {
	private final Book myBook;

	EncodingPreference(Context context, ZLResource rootResource, String resourceKey, Book book) {
		super(context, rootResource, resourceKey);
		myBook = book;

		final FormatPlugin plugin;
		try {
			plugin = book.getPlugin();
		} catch (BookReadingException e) {
			return;
		}

		final List<Encoding> encodings =
			new ArrayList<Encoding>(plugin.supportedEncodings().encodings());
		Collections.sort(encodings, new Comparator<Encoding>() {
			public int compare(Encoding e1, Encoding e2) {
				return e1.DisplayName.compareTo(e2.DisplayName);
			}
		});
		final String[] codes = new String[encodings.size()];
		final String[] names = new String[encodings.size()];
		int index = 0;
		for (Encoding e : encodings) {
			//addItem(e.Family, e.Name, e.DisplayName);
			codes[index] = e.Name;
			names[index] = e.DisplayName;
			++index;
		}
		setLists(codes, names);
		if (encodings.size() == 1) {
			setInitialValue(codes[0]);
			setEnabled(false);
		} else {
			final String bookEncoding = book.getEncoding();
			if (bookEncoding != null) {
				setInitialValue(bookEncoding.toLowerCase());
			}
		}
	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		if (result) {
			final String value = getValue();
			if (!value.equalsIgnoreCase(myBook.getEncoding())) {
				myBook.setEncoding(value);
				((EditBookInfoActivity)getContext()).saveBook();
			}
		}
	}
}

public class EditBookInfoActivity extends ZLPreferenceActivity {
	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private volatile boolean myInitialized;

	private Book myBook;

	public EditBookInfoActivity() {
		super("BookInfo");
	}

	void saveBook() {
		myCollection.bindToService(this, new Runnable() {
			public void run() {
				myCollection.saveBook(myBook, true);
			}
		});
	}

	@Override
	protected void init(Intent intent) {
	}

	@Override
	protected void onStart() {
		super.onStart();

		myBook = BookInfoActivity.bookByIntent(getIntent());

		if (myBook == null) {
			finish();
			return;
		}

		myCollection.bindToService(this, new Runnable() {
			public void run() {
				if (myInitialized) {
					return;
				}
				myInitialized = true;

				addPreference(new BookTitlePreference(EditBookInfoActivity.this, Resource, "title", myBook));
				addPreference(new BookAuthorsPreference(EditBookInfoActivity.this, Resource, "authors", myBook));
				addPreference(new BookSeriesPreference(EditBookInfoActivity.this, Resource, "series", myBook));
				addPreference(new BookSeriesIndexPreference(EditBookInfoActivity.this, Resource, "indexInSeries", myBook));
				addPreference(new BookTagsPreference(EditBookInfoActivity.this, Resource, "tags", myBook));
				addPreference(new BookLanguagePreference(EditBookInfoActivity.this, Resource, "language", myBook));
				addPreference(new EncodingPreference(EditBookInfoActivity.this, Resource, "encoding", myBook));
			}
		});
	}

	@Override
	protected void onStop() {
		myCollection.unbind();
		super.onStop();
	}
}