/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class SearchResult {

	public final String Summary;
	public final LinkedHashMap<NetworkBookItem.AuthorData, LinkedList<NetworkBookItem>> BooksMap;

	public SearchResult(String summary) {
		Summary = summary;
		BooksMap = new LinkedHashMap<NetworkBookItem.AuthorData, LinkedList<NetworkBookItem>>();
	}

	public void addBook(NetworkBookItem book) {
		for (NetworkBookItem.AuthorData author: book.Authors) {
			LinkedList<NetworkBookItem> list = BooksMap.get(author);
			if (list == null) {
				list = new LinkedList<NetworkBookItem>();
				BooksMap.put(author, list);
			}
			list.add(book);
		}
	}

	public boolean empty() {
		return BooksMap.size() == 0;
	}
}
