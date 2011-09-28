/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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
	private final ZLStringListOption myBooksInBasketOption;

	protected BasketItem(INetworkLink link) {
		super(
			link,
			NetworkLibrary.resource().getResource("basket").getValue(),
			NetworkLibrary.resource().getResource("basketSummaryEmpty").getValue(),
			new UrlInfoCollection<UrlInfo>(),
			Accessibility.ALWAYS,
			FLAGS_DEFAULT & ~FLAGS_GROUP
		);
		myBooksInBasketOption = new ZLStringListOption(Link.getSiteName(), "Basket", null);
	}

	@Override
	public CharSequence getSummary() {
		final int size = bookIds().size();
		if (size == 0) {
			return super.getSummary();
		} else if (size == books().size()) {
			return NetworkLibrary.resource().getResource("basketSummary").getValue()
				.replace("%0", String.valueOf(size)).replace("%1", cost().toString());
		} else {
			return NetworkLibrary.resource().getResource("basketSummaryCountOnly").getValue()
				.replace("%0", String.valueOf(size));
		}
	}

	@Override
	public boolean canBeOpened() {
		return !bookIds().isEmpty();
	}

	@Override
	public String getStringId() {
		return "@Basket";
	}

	public final void add(NetworkBookItem book) {
		if (book.Id != null && !"".equals(book.Id)) {
			List<String> ids = bookIds();
			if (!ids.contains(book.Id)) {
				ids = new ArrayList<String>(ids);
				ids.add(book.Id);
				myBooksInBasketOption.setValue(ids);
				NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
			}
		}
	}

	public final void remove(NetworkBookItem book) {
		if (book.Id != null && !"".equals(book.Id)) {
			List<String> ids = bookIds();
			if (ids.contains(book.Id)) {
				ids = new ArrayList<String>(ids);
				ids.remove(book.Id);
				myBooksInBasketOption.setValue(ids);
				NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
			}
		}
	}

	public final void clear() {
		myBooksInBasketOption.setValue(null);
		NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
	}

	public final boolean contains(NetworkBookItem book) {
		return bookIds().contains(book.Id);
	}

	public List<String> bookIds() {
		return myBooksInBasketOption.getValue();
	}

	private List<NetworkBookItem> books() {
		// TODO: implement
		return Collections.emptyList();
	}

	private Money cost() {
		Money sum = Money.ZERO;
		for (NetworkBookItem b : books()) {
			final BookBuyUrlInfo info = b.buyInfo();
			if (info != null) {
				sum = sum.add(info.Price);
			}
		}
		return sum;
	}
}
