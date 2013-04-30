/*
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

import java.util.Comparator;

import org.geometerplus.zlibrary.core.util.ZLColor;

public abstract class ZLTextHighlighting {
	public abstract boolean isEmpty();

	public abstract ZLTextPosition getStartPosition();
	public abstract ZLTextPosition getEndPosition();
	public abstract ZLTextElementArea getStartArea(ZLTextPage page);
	public abstract ZLTextElementArea getEndArea(ZLTextPage page);

	public abstract ZLColor getBackgroundColor();

	public static final Comparator<ZLTextHighlighting> ByStartComparator =
		new Comparator<ZLTextHighlighting>() {
			public int compare(ZLTextHighlighting h0, ZLTextHighlighting h1) {
				final int cmp = h0.getStartPosition().compareTo(h1.getStartPosition());
				return cmp != 0 ? cmp : h0.getEndPosition().compareTo(h1.getEndPosition());
			}
		};

	public static final Comparator<ZLTextHighlighting> ByEndComparator =
		new Comparator<ZLTextHighlighting>() {
			public int compare(ZLTextHighlighting h0, ZLTextHighlighting h1) {
				final int cmp = h0.getEndPosition().compareTo(h1.getEndPosition());
				return cmp != 0 ? cmp : h0.getStartPosition().compareTo(h1.getStartPosition());
			}
		};
}
