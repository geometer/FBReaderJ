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

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.config.ZLConfig;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

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

	private final RootTree myBooks = new RootTree();
	private final ArrayList<ZLFile> myFiles = new ArrayList<ZLFile>(LIST_SIZE);
	private boolean myIsFullySynchronized;

	private RecentBooks() {
		final ZLStringOption option = new ZLStringOption(GROUP, "", "");
		for (int count = 0; ; ++count) {
			option.changeName(BOOK + count);
			String name = option.getValue();
			if (name.length() == 0) {
				break;
			}
			myFiles.add(ZLFile.createFileByPath(name));
		}
	}

	public void addBook(ZLFile file) {
		myFiles.remove(file);
		myFiles.add(0, file);
		myBooks.clear();
		save();
	}

	public void rebuild() {
		if (!myIsFullySynchronized) {
			myBooks.clear();
		}
	}	

	public void synchronize() {
		if (myBooks.hasChildren()) {
			return;
		}
		myIsFullySynchronized = true;

		int count = 0;
		for (ZLFile file : myFiles) {
			if (!file.exists()) {
				continue;
			}
			BookDescription description = BookDescription.getDescription(file);
			if (description != null) {
				myBooks.createBookSubTree(description);
			} else {
				myIsFullySynchronized = false;
			}
			if (++count >= LIST_SIZE) {
				break;
			}
		}
	}

	public CollectionTree books() {
		synchronize();
		return myBooks;
	}

	public void save() {
		ZLConfig.Instance().executeAsATransaction(new Runnable() {
			public void run() {
				final ZLStringOption option = new ZLStringOption(GROUP, "", "");
				int count = 0;
				for (ZLFile file : myFiles) {
					option.changeName(BOOK + count);
					option.setValue(file.getPath());
					++count;
				}
				option.changeName(BOOK + count);
				option.setValue("");
			}
		});
	}
}
