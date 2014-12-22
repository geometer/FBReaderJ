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

import java.io.InputStream;
import java.io.IOException;
import java.util.*;

import org.geometerplus.zlibrary.core.network.*;

import org.geometerplus.fbreader.network.opds.*;

class BookElementsHolder {
	private static Map<Map<String,String>,List<BookElement>> ourCache =
		new HashMap<Map<String,String>,List<BookElement>>();

	static synchronized List<BookElement> getElements(Map<String,String> data) {
		List<BookElement> elements = ourCache.get(data);
		if (elements == null) {
			elements = new LinkedList<BookElement>();
			final int count = Integer.valueOf(data.get("size"));
			for (int i = 0; i < count; ++i) {
				elements.add(new BookElement());
			}
			startLoading(data.get("src"), elements);
			ourCache.put(data, elements);
		}
		return Collections.unmodifiableList(elements);
	}

	private static void startLoading(final String url, final List<BookElement> elements) {
		new Thread() {
			public void run() {
				final SimpleOPDSFeedHandler handler = new SimpleOPDSFeedHandler(url);
				new QuietNetworkContext().performQuietly(new ZLNetworkRequest.Get(url, true) {
					@Override
					public void handleStream(InputStream inputStream, int length) throws IOException, ZLNetworkException {
						new OPDSXMLReader(handler, false).read(inputStream);
					}
				});
				final List<OPDSBookItem> items = handler.books();
				if (items.size() > 0) {
					int index = 0;
					for (BookElement book : elements) {
						book.setData(items.get(index));
						index = (index + 1) % items.size();
					}
				} else {
					for (BookElement book : elements) {
						book.setFailed();
					}
				}
			}
		}.start();
	}
}
