/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
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

import java.util.*;

final class ZLTextElementAreaVector extends ArrayList<ZLTextElementArea> {
	private static final long serialVersionUID = -7880472347947563506L;

	private final ArrayList<ZLTextRegion> myElementRegions = new ArrayList<ZLTextRegion>();
	private ZLTextRegion myCurrentElementRegion;

	@Override
	public void clear() {
		myElementRegions.clear();
		myCurrentElementRegion = null;
		super.clear();
	}

	@Override
	public boolean add(ZLTextElementArea area) {
		if (myCurrentElementRegion != null
			&& myCurrentElementRegion.getSoul().accepts(area)) {
			myCurrentElementRegion.extend();
		} else {
			ZLTextRegion.Soul soul = null;
			final ZLTextHyperlink hyperlink = area.Style.Hyperlink;
			if (hyperlink.Id != null) {
				soul = new ZLTextHyperlinkRegionSoul(area, hyperlink);
			} else if (area.Element instanceof ZLTextImageElement) {
				soul = new ZLTextImageRegionSoul(area, (ZLTextImageElement)area.Element);
			} else if (area.Element instanceof ZLTextWord && ((ZLTextWord)area.Element).isAWord()) {
				soul = new ZLTextWordRegionSoul(area, (ZLTextWord)area.Element);
			}
			if (soul != null) {
				myCurrentElementRegion = new ZLTextRegion(soul, this, size());
				myElementRegions.add(myCurrentElementRegion);
			} else {
				myCurrentElementRegion = null;
			}
		}
		return super.add(area);
	}

	ZLTextElementArea binarySearch(int x, int y) {
		int left = 0;
		int right = size();
		while (left < right) {
			final int middle = (left + right) / 2;
			final ZLTextElementArea candidate = get(middle);
			if (candidate.YStart > y) {
				right = middle;
			} else if (candidate.YEnd < y) {
				left = middle + 1;
			} else if (candidate.XStart > x) {
				right = middle;
			} else if (candidate.XEnd < x) {
				left = middle + 1;
			} else {
				return candidate;
			}
		}
		return null;
	}

	List<ZLTextRegion> elementRegions() {
		return Collections.unmodifiableList(myElementRegions);
	}

	ZLTextRegion getRegion(ZLTextRegion.Soul soul) {
		if (soul == null) {
			return null;
		}
		for (ZLTextRegion region : myElementRegions) {
			if (soul.equals(region.getSoul())) {
				return region;
			}
		}
		return null;
	}
}
