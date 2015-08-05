/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.util.*;

import org.geometerplus.zlibrary.core.money.Money;
import org.geometerplus.zlibrary.core.options.ZLStringListOption;

import org.geometerplus.fbreader.network.urlInfo.*;

public abstract class BasketItem extends NetworkCatalogItem {
	private final NetworkLibrary myLibrary;
	private long myGeneration = 0;

	private final ZLStringListOption myBooksInBasketOption;
	private final Map<String,NetworkBookItem> myBooks =
		Collections.synchronizedMap(new HashMap<String,NetworkBookItem>());

	protected BasketItem(NetworkLibrary library, INetworkLink link) {
		super(
			link,
			NetworkLibrary.resource().getResource("basket").getValue(),
			NetworkLibrary.resource().getResource("basketSummaryEmpty").getValue(),
			new UrlInfoCollection<UrlInfo>(),
			Accessibility.ALWAYS,
			FLAGS_DEFAULT & ~FLAGS_GROUP
		);
		myLibrary = library;
		myBooksInBasketOption = new ZLStringListOption(Link.getStringId(), "Basket", Collections.<String>emptyList(), ",");
	}

	public void addItem(NetworkBookItem book) {
		myBooks.put(book.Id, book);
	}

	@Override
	public CharSequence getSummary() {
		final int size = bookIds().size();
		if (size == 0) {
			return super.getSummary();
		} else {
			final Money basketCost = cost();
			if (basketCost != null) {
				return NetworkLibrary.resource().getResource("basketSummary").getValue(size)
					.replace("%0", String.valueOf(size)).replace("%1", basketCost.toString());
			} else {
				return NetworkLibrary.resource().getResource("basketSummaryCountOnly").getValue(size)
					.replace("%0", String.valueOf(size));
			}
		}
	}

	@Override
	public boolean canBeOpened() {
		return !bookIds().isEmpty();
	}

	@Override
	public String getStringId() {
		return "@Basket:" + Link.getStringId();
	}

	public long getGeneration() {
		return myGeneration;
	}

	public final void add(NetworkBookItem book) {
		List<String> ids = bookIds();
		if (!ids.contains(book.Id)) {
			ids = new ArrayList<String>(ids);
			ids.add(book.Id);
			myBooksInBasketOption.setValue(ids);
			addItem(book);
			++myGeneration;
			myLibrary.fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
		}
	}

	public final void remove(NetworkBookItem book) {
		List<String> ids = bookIds();
		if (ids.contains(book.Id)) {
			ids = new ArrayList<String>(ids);
			ids.remove(book.Id);
			myBooksInBasketOption.setValue(ids);
			myBooks.remove(book);
			++myGeneration;
			myLibrary.fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
		}
	}

	public final void clear() {
		myBooksInBasketOption.setValue(null);
		myBooks.clear();
		++myGeneration;
		myLibrary.fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
	}

	public final boolean contains(NetworkBookItem book) {
		return bookIds().contains(book.Id);
	}

	public List<String> bookIds() {
		return myBooksInBasketOption.getValue();
	}

	public NetworkBookItem getBook(String id) {
		return myBooks.get(id);
	}

	protected boolean isFullyLoaded() {
		synchronized (myBooks) {
			for (String id : bookIds()) {
				final NetworkBookItem b = myBooks.get(id);
				if (b == null) {
					return false;
				}
			}
		}
		return true;
	}

	private Money cost() {
		Money sum = Money.ZERO;
		synchronized (myBooks) {
			for (String id : bookIds()) {
				final NetworkBookItem b = myBooks.get(id);
				if (b == null) {
					return null;
				}
				final BookBuyUrlInfo info = b.buyInfo();
				if (info == null) {
					return null;
				}
				if (b.getStatus(null) == NetworkBookItem.Status.CanBePurchased) {
					if (info.Price == null) {
						return null;
					}
					sum = sum.add(info.Price);
				}
			}
		}
		return sum;
	}
}
