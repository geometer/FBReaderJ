/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.book;

import java.math.BigDecimal;

import org.geometerplus.zlibrary.core.util.MiscUtil;

public final class SeriesInfo {
	public static SeriesInfo createSeriesInfo(String title, String index) {
		if (title == null) {
			return null;
		}
		return new SeriesInfo(title, createIndex(index));
	}

	public static BigDecimal createIndex(String index) {
		try {
			return index != null ? new BigDecimal(index).stripTrailingZeros() : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public final Series Series;
	public final BigDecimal Index;

	SeriesInfo(String title, BigDecimal index) {
		Series = new Series(title);
		Index = index;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SeriesInfo)) {
			return false;
		}
		final SeriesInfo info = (SeriesInfo)other;
		return MiscUtil.equals(Series, info.Series) && MiscUtil.equals(Index, info.Index);
	}

	@Override
	public int hashCode() {
		return 23 * MiscUtil.hashCode(Series) + 31 * MiscUtil.hashCode(Index);
	}
}
