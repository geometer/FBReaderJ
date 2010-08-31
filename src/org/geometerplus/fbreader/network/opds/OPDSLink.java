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

package org.geometerplus.fbreader.network.opds;

import java.util.Currency;
import java.util.LinkedList;
import java.util.Locale;

import org.geometerplus.fbreader.network.atom.ATOMLink;


class OPDSPrice {

	public final String Price;
	public final String Currency;

	// @param price     price value; must be not null
	// @param currency  currency code value; must be not null;
	//                  http://www.iso.org/iso/en/prods-services/popstds/currencycodeslist.html
	public OPDSPrice(String price, String currency) {
		Price = price;
		Currency = currency;
	}
}

class OPDSLink extends ATOMLink {

	public final LinkedList<OPDSPrice> Prices = new LinkedList<OPDSPrice>();
	public final LinkedList<String> Formats = new LinkedList<String>();

	private OPDSPrice getPrice(String currency) {
		for (OPDSPrice p: Prices) {
			if (currency.equals(p.Currency)) {
				return p;
			}
		}
		return null;
	}

	public OPDSPrice selectBestPrice() {
		if (Prices.isEmpty()) {
			return null;
		} else if (Prices.size() == 1) {
			return Prices.get(0);
		}
		OPDSPrice price;
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
