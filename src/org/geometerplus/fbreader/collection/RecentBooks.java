/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.collection;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.core.config.ZLConfig;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

import org.geometerplus.fbreader.description.*;
import org.geometerplus.fbreader.formats.PluginCollection;

public final class RecentBooks {
	private static final int LIST_SIZE = 10;
	private static final String GROUP = "LastOpenedBooks";
	private static final String BOOK = "Book";

	private static RecentBooks ourInstance;

	public static RecentBooks Instance() {
		if (ourInstance == null) {
			ourInstance = new RecentBooks();
		}
		return ourInstance;
	}

	private final ArrayList<String> myFileNames = new ArrayList<String>();
	private final ArrayList<BookDescription> myBooks = new ArrayList<BookDescription>();
	private boolean myIsFullySynchronized;

	private RecentBooks() {
		final ZLStringOption option = new ZLStringOption(GROUP, "", "");
		for (int count = 0; ; ++count) {
			option.changeName(BOOK + count);
			String name = option.getValue();
			if (name.length() == 0) {
				break;
			}
			myFileNames.add(name);
		}
	}

	public void addBook(String fileName) {
		myFileNames.remove(fileName);
		myFileNames.add(0, fileName);
		myBooks.clear();
		save();
	}

	public void rebuild() {
		if (!myIsFullySynchronized) {
			myBooks.clear();
		}
	}	

	public void synchronize() {
		if (!myBooks.isEmpty()) {
			return;
		}
		myIsFullySynchronized = true;
		ZLConfig.Instance().executeAsATransaction(new Runnable() {
			public void run() {
				int count = 0;
				for (String fileName : myFileNames) {
					BookDescription description = BookDescription.getDescription(fileName);
					if (description != null) {
						myBooks.add(description);
					} else {
						myIsFullySynchronized = false;
					}
					if (++count >= LIST_SIZE) {
						break;
					}
				}
			}
		});
	}

	public ArrayList<BookDescription> books() {
		synchronize();
		return myBooks;
	}

	public void save() {
		ZLConfig.Instance().executeAsATransaction(new Runnable() {
			public void run() {
				final ZLStringOption option = new ZLStringOption(GROUP, "", "");
				int count = 0;
				for (String fileName : myFileNames) {
					option.changeName(BOOK + count);
					option.setValue(fileName);
					++count;
				}
				option.changeName(BOOK + count);
				option.setValue("");
			}
		});
	}
}
