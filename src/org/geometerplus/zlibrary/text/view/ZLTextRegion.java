/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.view.ZLPaintContext;

public final class ZLTextRegion {
	public static abstract class Soul implements Comparable<Soul> {
		final int ParagraphIndex;
		final int StartElementIndex;
		final int EndElementIndex;

		protected Soul(int paragraphIndex, int startElementIndex, int endElementIndex) {
			ParagraphIndex = paragraphIndex;
			StartElementIndex = startElementIndex;
			EndElementIndex = endElementIndex;
		}

		final boolean accepts(ZLTextElementArea area) {
			return compareTo(area) == 0;
		}
	
		@Override
		public final boolean equals(Object other) {
			if (other == this) {
				return true;
			}
			if (!(other instanceof Soul)) {
				return false;
			}
			final Soul soul = (Soul)other;
			return
				ParagraphIndex == soul.ParagraphIndex &&
				StartElementIndex == soul.StartElementIndex &&
				EndElementIndex == soul.EndElementIndex;
		}

		public final int compareTo(Soul soul) {
			if (ParagraphIndex != soul.ParagraphIndex) {
				return ParagraphIndex < soul.ParagraphIndex ? -1 : 1;
			}
			if (EndElementIndex < soul.StartElementIndex) {
				return -1;
			}
			if (StartElementIndex > soul.EndElementIndex) {
				return 1;
			}
			return 0;
		}

		public final int compareTo(ZLTextElementArea area) {
			if (ParagraphIndex != area.ParagraphIndex) {
				return ParagraphIndex < area.ParagraphIndex ? -1 : 1;
			}
			if (EndElementIndex < area.ElementIndex) {
				return -1;
			}
			if (StartElementIndex > area.ElementIndex) {
				return 1;
			}
			return 0;
		}
	}

	public static interface Filter {
		boolean accepts(ZLTextRegion region);
	}

	public static Filter AnyRegionFilter = new Filter() {
		public boolean accepts(ZLTextRegion region) {
			return true;
		}
	};

	public static Filter HyperlinkFilter = new Filter() {
		public boolean accepts(ZLTextRegion region) {
			return region.getSoul() instanceof ZLTextHyperlinkRegionSoul;
		}
	};

	public static Filter ImageOrHyperlinkFilter = new Filter() {
		public boolean accepts(ZLTextRegion region) {
			final Soul soul = region.getSoul();
			return
				soul instanceof ZLTextImageRegionSoul ||
				soul instanceof ZLTextHyperlinkRegionSoul;
		}
	};

	private final Soul mySoul;
	// this field must be accessed in synchronized context only
	private final List<ZLTextElementArea> myAreaList;
	private ZLTextElementArea[] myAreas;
	private final int myFromIndex;
	private int myToIndex;
	private ZLTextHorizontalConvexHull myHull;

	ZLTextRegion(Soul soul, List<ZLTextElementArea> list, int fromIndex) {
		mySoul = soul;
		myAreaList = list;
		myFromIndex = fromIndex;
		myToIndex = fromIndex + 1;
	}

	void extend() {
		++myToIndex;
		myHull = null;
	}

	public Soul getSoul() {
		return mySoul;
	}

	private ZLTextElementArea[] textAreas() {
		if (myAreas == null || myAreas.length != myToIndex - myFromIndex) {
			synchronized (myAreaList) {
				myAreas = new ZLTextElementArea[myToIndex - myFromIndex];
				for (int i = 0; i < myAreas.length; ++i) {
					myAreas[i] = myAreaList.get(i + myFromIndex);
				}
			}
		}
		return myAreas;
	}
	private ZLTextHorizontalConvexHull convexHull() {
		if (myHull == null) {
			myHull = new ZLTextHorizontalConvexHull(textAreas());
		}
		return myHull;
	}

	ZLTextElementArea getFirstArea() {
		return textAreas()[0];
	}

	ZLTextElementArea getLastArea() {
		final ZLTextElementArea[] areas = textAreas();
		return areas[areas.length - 1];
	}

	public int getTop() {
		return getFirstArea().YStart;
	}

	public int getBottom() {
		return getLastArea().YEnd;
	}

	void draw(ZLPaintContext context) {
		convexHull().draw(context);
	}

	int distanceTo(int x, int y) {
		return convexHull().distanceTo(x, y);
	}

	boolean isAtRightOf(ZLTextRegion other) {
		return
			other == null ||
			getFirstArea().XStart >= other.getLastArea().XEnd;
	}

	boolean isAtLeftOf(ZLTextRegion other) {
		return other == null || other.isAtRightOf(this);
	}

	boolean isUnder(ZLTextRegion other) {
		return
			other == null ||
			getFirstArea().YStart >= other.getLastArea().YEnd;
	}

	boolean isOver(ZLTextRegion other) {
		return other == null || other.isUnder(this);
	}

	boolean isExactlyUnder(ZLTextRegion other) {
		if (other == null) {
			return true;
		}
		if (!isUnder(other)) {
			return false;
		}
		final ZLTextElementArea[] areas0 = textAreas();
		final ZLTextElementArea[] areas1 = other.textAreas();
		for (ZLTextElementArea i : areas0) {
			for (ZLTextElementArea j : areas1) {
				if (i.XStart <= j.XEnd && j.XStart <= i.XEnd) {
					return true;
				}
			}
		}
		return false;
	}

	boolean isExactlyOver(ZLTextRegion other) {
		return other == null || other.isExactlyUnder(this);
	}
}
