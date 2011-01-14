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

package org.geometerplus.zlibrary.core.util;

import java.util.Set;
import java.util.TreeSet;

public abstract class ZLVisitedLinkManager {
	private static ZLVisitedLinkManager ourInstance;
	private static Set<String> visitedLinks;
	
	public static ZLVisitedLinkManager Instance() {
		return ourInstance;
	}

	protected ZLVisitedLinkManager() {
		ourInstance = this;
		reset();
	}

	private String stripId(String id) {
		int index = id.indexOf('#');
		return index != -1 ? id.substring(0, index) : id;
	}

	public void markLinkVisited(String id) {
		visitedLinks.add(stripId(id));
	}

	public boolean isLinkVisited(String id) {
		return visitedLinks.contains(stripId(id));
	}

	public Set<String> getVisitedLinks() {
		return visitedLinks;
	}

	public void reset() {
		visitedLinks = new TreeSet<String>();
	}
}
