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

public class BuyBookReference extends BookReference {

	public final String Price;

	public BuyBookReference(String url, int format, int type, String price) {
		super(url, format, type);
		Price = price;
	}

	public static String price(String price, String currency) {
		if (price == null || currency == null) {
			return price;
		} else if (currency.equals("RUB")) {
			return price + " \u0440.";
		} else if (currency.equals("USD")) {
			return "$" + price;
		} else if (currency.equals("GBP")) {
			return "\u00A3" + price;
		} else if (currency.equals("EUR")) {
			return "\u20AC" + price;
		} else if (currency.equals("JPY")) {
			return "\u00A5" + price;
		}
		return currency + " " + price;
	}

}
