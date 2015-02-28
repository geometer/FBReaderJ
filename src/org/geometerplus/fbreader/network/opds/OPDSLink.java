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

package org.geometerplus.fbreader.network.opds;

import java.util.*;

import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.money.Money;

import org.geometerplus.fbreader.network.atom.ATOMLink;

class OPDSLink extends ATOMLink {
	public final LinkedList<Money> Prices = new LinkedList<Money>();
	public final LinkedList<String> Formats = new LinkedList<String>();

	protected OPDSLink(ZLStringMap attributes) {
		super(attributes);
	}

	private Money getPrice(String currency) {
		for (Money p : Prices) {
			if (currency.equals(p.Currency)) {
				return p;
			}
		}
		return null;
	}

	public Money selectBestPrice() {
		if (Prices.isEmpty()) {
			return null;
		} else if (Prices.size() == 1) {
			return Prices.get(0);
		}
		Money price;
		final Locale locale = Locale.getDefault();
		if (locale.getCountry().length() == 2) {
			final String bestCode = Currency.getInstance(locale).getCurrencyCode();
			if (bestCode != null) {
				price = getPrice(bestCode);
				if (price != null) {
					return price;
				}
			}
		}
		price = getPrice("USD");
		if (price != null) {
			return price;
		}
		price = getPrice("EUR");
		if (price != null) {
			return price;
		}
		return Prices.get(0);
	}
}
