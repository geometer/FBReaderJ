/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.zlibrary.core.view;

import java.util.*;

public class UnionHull implements Hull {
	private final List<Hull> myComponents;

	public UnionHull(Hull ... components) {
		myComponents = new ArrayList<Hull>(Arrays.asList(components));
	}

	public void draw(ZLPaintContext context, int mode) {
		for (Hull h : myComponents) {
			h.draw(context, mode);
		}
	}

	public int distanceTo(int x, int y) {
		int dist = Integer.MAX_VALUE;
		for (Hull h : myComponents) {
			dist = Math.min(dist, h.distanceTo(x, y));
		}
		return dist;
	}

	public boolean isBefore(int x, int y) {
		for (Hull h : myComponents) {
			if (h.isBefore(x, y)) {
				return true;
			}
		}
		return false;
	}
}
