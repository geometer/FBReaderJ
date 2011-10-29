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

package org.geometerplus.zlibrary.core.money;

import java.io.Serializable;
import java.math.BigDecimal;

import org.geometerplus.zlibrary.core.util.ZLMiscUtil;

public class Money implements Comparable<Money>, Serializable {
	public static final Money ZERO = new Money();

	public final BigDecimal Amount;
	public final String Currency;

	private Money() {
		Amount = BigDecimal.ZERO;
		Currency = null;
	}

	public Money(BigDecimal amount, String currency) {
		Amount = amount;
		Currency = currency;
	}

	public Money(String amount, String currency) {
		BigDecimal a = null;
		try {
			a = new BigDecimal(amount);
		} catch (NumberFormatException e) {
			a = new BigDecimal(amount.replace(",", "."));
		}
		Amount = a;
		Currency = currency;
	}

	public Money(String text) {
		text = text.trim();
		if (text.startsWith("$")) {
			Amount = new BigDecimal(text.substring(1).trim());
			Currency = "USD";
		} else if (text.endsWith("$")) {
			Amount = new BigDecimal(text.substring(0, text.length() - 1).trim());
			Currency = "USD";
		} else {
			throw new MoneyException("Unknown money format: '" + text + "'");
		}
	}

	public Money add(Money m) {
		if (Amount.equals(ZERO.Amount)) {
			return m;
		}
		if (m.Amount.equals(ZERO.Amount)) {
			return this;
		}

		if (!Currency.equals(m.Currency)) {
			throw new MoneyException("Different currencies");
		}
		return new Money(Amount.add(m.Amount), Currency);
	}

	public Money subtract(Money m) {
		if (Amount.equals(ZERO.Amount)) {
			return new Money(m.Amount.negate(), m.Currency);
		}
		if (m.Amount.equals(ZERO.Amount)) {
			return this;
		}

		if (!Currency.equals(m.Currency)) {
			throw new MoneyException("Different currencies");
		}
		return new Money(Amount.subtract(m.Amount), Currency);
	}

	public int compareTo(Money m) {
		if (Amount.equals(ZERO.Amount)) {
			return m.Amount.equals(ZERO.Amount) ? 0 : -1;
		}
		if (m.Amount.equals(ZERO.Amount)) {
			return 1;
		}

		if (!Currency.equals(m.Currency)) {
			throw new MoneyException("Different currencies");
		}
		return Amount.compareTo(m.Amount);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Money)) {
			return false;
		}
		final Money m = (Money)o;
		if (Amount.equals(ZERO.Amount)) {
			return m.Amount.equals(ZERO.Amount);
		}
		return Amount.equals(m.Amount) && ZLMiscUtil.equals(Currency, m.Currency);
	}

	@Override
	public int hashCode() {
		return Amount.hashCode() + ZLMiscUtil.hashCode(Currency);
	}

	@Override
	public String toString() {
		if (Currency == null) {
			return Amount.toString();
		} else if ("RUB".equals(Currency)) {
			return Amount + " \u0440.";
		} else if ("USD".equals(Currency)) {
			return "$" + Amount;
		} else if ("GBP".equals(Currency)) {
			return "\u00A3" + Amount;
		} else if ("EUR".equals(Currency)) {
			return "\u20AC" + Amount;
		} else if ("JPY".equals(Currency)) {
			return "\u00A5" + Amount;
		}
		return Currency + " " + Amount;
	}
}
