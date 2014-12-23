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

package org.geometerplus.zlibrary.text.view;

import java.util.*;

class BookElementsHolder {
	private static final Map<Map<String,String>,List<BookElement>> ourCache =
		new HashMap<Map<String,String>,List<BookElement>>();

	static synchronized List<BookElement> getElements(Map<String,String> data) {
		List<BookElement> elements = ourCache.get(data);
		if (elements == null) {
			try {
				final int count = Integer.valueOf(data.get("size"));
				elements = new ArrayList<BookElement>(count);
				for (int i = 0; i < count; ++i) {
					elements.add(new BookElement());
				}
				startLoading(data.get("src"), elements);
			} catch (Throwable t) {
				return Collections.emptyList();
			}
			ourCache.put(data, elements);
		}
		return Collections.unmodifiableList(elements);
	}

	private static void startLoading(final String url, final List<BookElement> elements) {
		new Thread() {
			public void run() {
			}
		}.start();
	}
}
