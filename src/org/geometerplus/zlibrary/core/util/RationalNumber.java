/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.zlibrary.core.util;

public class RationalNumber {
	public static RationalNumber create(long numerator, long denominator) {
		if (denominator == 0) {
			return null;
		}
		return new RationalNumber(numerator, denominator);
	}

	public final long Numerator;
	public final long Denominator;

	private RationalNumber(long numerator, long denominator) {
		final long gcd = GCD(numerator, denominator);
		if (gcd > 1) {
			numerator /= gcd;
			denominator /= gcd;
		}
		if (denominator < 0) {
			numerator = -numerator;
			denominator = -denominator;
		}
		Numerator = numerator;
		Denominator = denominator;
	}

	public float toFloat() {
		return 1.0f * Numerator / Denominator;
	}

	private long GCD(long a, long b) {
		if (a < 0) {
			a = -a;
		}
		if (b < 0) {
			b = -b;
		}
		while (a != 0 && b != 0) {
			if (a > b) {
				a = a % b;
			} else {
				b = b % a;
			}
		}
		return a + b;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof RationalNumber)) {
			return false;
		}
		final RationalNumber otherNumber = (RationalNumber)other;
		return otherNumber.Numerator == Numerator && otherNumber.Denominator == Denominator;
	}

	@Override
	public int hashCode() {
		return (int)(37 * Numerator + Denominator);
	}
}
