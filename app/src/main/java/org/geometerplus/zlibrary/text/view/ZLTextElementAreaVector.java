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

package org.geometerplus.zlibrary.text.view;

import java.util.*;

final class ZLTextElementAreaVector {
	private final List<ZLTextElementArea> myAreas =
		Collections.synchronizedList(new ArrayList<ZLTextElementArea>());
	private final List<ZLTextRegion> myElementRegions = new ArrayList<ZLTextRegion>();
	private ZLTextRegion myCurrentElementRegion;

	void clear() {
		synchronized (myAreas) {
			myElementRegions.clear();
			myCurrentElementRegion = null;
			myAreas.clear();
		}
	}

	public int size() {
		return myAreas.size();
	}

	public List<ZLTextElementArea> areas() {
		synchronized (myAreas) {
			return new ArrayList<ZLTextElementArea>(myAreas);
		}
	}

	public ZLTextElementArea getFirstArea() {
		synchronized (myAreas) {
			return myAreas.isEmpty() ? null : myAreas.get(0);
		}
	}

	public ZLTextElementArea getLastArea() {
		synchronized (myAreas) {
			return myAreas.isEmpty() ? null : myAreas.get(myAreas.size() - 1);
		}
	}

	public boolean add(ZLTextElementArea area) {
		synchronized (myAreas) {
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
				} else if (area.Element instanceof ZLTextVideoElement) {
					soul = new ZLTextVideoRegionSoul(area, (ZLTextVideoElement)area.Element);
				} else if (area.Element instanceof ZLTextWord && !((ZLTextWord)area.Element).isASpace()) {
					soul = new ZLTextWordRegionSoul(area, (ZLTextWord)area.Element);
				} else if (area.Element instanceof ExtensionElement) {
					soul = new ExtensionRegionSoul(area, (ExtensionElement)area.Element);
				}
				if (soul != null) {
					myCurrentElementRegion = new ZLTextRegion(soul, myAreas, myAreas.size());
					myElementRegions.add(myCurrentElementRegion);
				} else {
					myCurrentElementRegion = null;
				}
			}
			return myAreas.add(area);
		}
	}

	ZLTextElementArea getFirstAfter(ZLTextPosition position) {
		if (position == null) {
			return null;
		}
		synchronized (myAreas) {
			for (ZLTextElementArea area : myAreas) {
				if (position.compareTo(area) <= 0) {
					return area;
				}
			}
		}
		return null;
	}

	ZLTextElementArea getLastBefore(ZLTextPosition position) {
		if (position == null) {
			return null;
		}
		synchronized (myAreas) {
			for (int i = myAreas.size() - 1; i >= 0; --i) {
				final ZLTextElementArea area = myAreas.get(i);
				if (position.compareTo(area) > 0) {
					return area;
				}
			}
		}
		return null;
	}

	ZLTextElementArea binarySearch(int x, int y) {
		synchronized (myAreas) {
			int left = 0;
			int right = myAreas.size();
			while (left < right) {
				final int middle = (left + right) / 2;
				final ZLTextElementArea candidate = myAreas.get(middle);
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
	}

	ZLTextRegion getRegion(ZLTextRegion.Soul soul) {
		if (soul == null) {
			return null;
		}
		synchronized (myAreas) {
			for (ZLTextRegion region : myElementRegions) {
				if (soul.equals(region.getSoul())) {
					return region;
				}
			}
		}
		return null;
	}

	ZLTextRegion findRegion(int x, int y, int maxDistance, ZLTextRegion.Filter filter) {
		ZLTextRegion bestRegion = null;
		int distance = maxDistance + 1;
		synchronized (myAreas) {
			for (ZLTextRegion region : myElementRegions) {
				if (filter.accepts(region)) {
					final int d = region.distanceTo(x, y);
					if (d < distance) {
						bestRegion = region;
						distance = d;
					}
				}
			}
		}
		return bestRegion;
	}

	static class RegionPair {
		ZLTextRegion Before;
		ZLTextRegion After;
	}

	RegionPair findRegionsPair(int x, int y, int columnIndex, ZLTextRegion.Filter filter) {
		RegionPair pair = new RegionPair();
		synchronized (myAreas) {
			for (ZLTextRegion region : myElementRegions) {
				if (filter.accepts(region)) {
					if (region.isBefore(x, y, columnIndex)) {
						pair.Before = region;
					} else {
						pair.After = region;
						break;
					}
				}
			}
		}
		return pair;
	}

	protected ZLTextRegion nextRegion(ZLTextRegion currentRegion, ZLTextView.Direction direction, ZLTextRegion.Filter filter) {
		synchronized (myAreas) {
			if (myElementRegions.isEmpty()) {
				return null;
			}

			int index = currentRegion != null ? myElementRegions.indexOf(currentRegion) : -1;

			switch (direction) {
				case rightToLeft:
				case up:
					if (index == -1) {
						index = myElementRegions.size() - 1;
					} else if (index == 0) {
						return null;
					} else {
						--index;
					}
					break;
				case leftToRight:
				case down:
					if (index == myElementRegions.size() - 1) {
						return null;
					} else {
						++index;
					}
					break;
			}

			switch (direction) {
				case rightToLeft:
					for (; index >= 0; --index) {
						final ZLTextRegion candidate = myElementRegions.get(index);
						if (filter.accepts(candidate) && candidate.isAtLeftOf(currentRegion)) {
							return candidate;
						}
					}
					break;
				case leftToRight:
					for (; index < myElementRegions.size(); ++index) {
						final ZLTextRegion candidate = myElementRegions.get(index);
						if (filter.accepts(candidate) && candidate.isAtRightOf(currentRegion)) {
							return candidate;
						}
					}
					break;
				case down:
				{
					ZLTextRegion firstCandidate = null;
					for (; index < myElementRegions.size(); ++index) {
						final ZLTextRegion candidate = myElementRegions.get(index);
						if (!filter.accepts(candidate)) {
							continue;
						}
						if (candidate.isExactlyUnder(currentRegion)) {
							return candidate;
						}
						if (firstCandidate == null && candidate.isUnder(currentRegion)) {
							firstCandidate = candidate;
						}
					}
					if (firstCandidate != null) {
						return firstCandidate;
					}
					break;
				}
				case up:
					ZLTextRegion firstCandidate = null;
					for (; index >= 0; --index) {
						final ZLTextRegion candidate = myElementRegions.get(index);
						if (!filter.accepts(candidate)) {
							continue;
						}
						if (candidate.isExactlyOver(currentRegion)) {
							return candidate;
						}
						if (firstCandidate == null && candidate.isOver(currentRegion)) {
							firstCandidate = candidate;
						}
					}
					if (firstCandidate != null) {
						return firstCandidate;
					}
					break;
			}
		}
		return null;
	}
}
