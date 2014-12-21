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

class BookHolder {
	private static Map<Map<String,String>,BookHolder> ourCache =
		new HashMap<Map<String,String>,BookHolder>();

	static BookHolder get(Map<String,String> data) {
		BookHolder holder = ourCache.get(data);
		if (holder == null) {
			holder = new BookHolder(data.get("src"), Integer.valueOf(data.get("size")));
			ourCache.put(data, holder);
		}
		return holder;
	}

	final List<BookElement> Elements = new LinkedList<BookElement>();

	private BookHolder(String url, int count) {
		startLoading(url);
		for (int i = 0; i < count; ++i) {
			Elements.add(new BookElement());
		}
	}

	private void startLoading(final String url) {
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
					for (BookElement book : Elements) {
						book.setData(items.get(index));
						index = (index + 1) % items.size();
					}
				} else {
					for (BookElement book : Elements) {
						book.setFailed();
					}
				}
			}
		}.start();
	}
}
