/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network.opds;

import java.util.*;

import org.geometerplus.fbreader.network.NetworkItem;

public class SimpleOPDSFeedHandler extends AbstractOPDSFeedHandler implements OPDSConstants {
	private final String myBaseURL;
	private final List<OPDSBookItem> myBooks = new LinkedList<OPDSBookItem>();

	private int myIndex;

	public SimpleOPDSFeedHandler(String baseURL) {
		myBaseURL = baseURL;
	}

	@Override
	public void processFeedStart() {
	}

	@Override
	public boolean processFeedMetadata(OPDSFeedMetadata feed, boolean beforeEntries) {
		return false;
	}

	@Override
	public void processFeedEnd() {
	}

	@Override
	public boolean processFeedEntry(OPDSEntry entry) {
		final OPDSBookItem item = new OPDSBookItem(null, entry, myBaseURL, myIndex++);
		for (String identifier : entry.DCIdentifiers) {
			((OPDSBookItem)item).Identifiers.add(identifier);
		}
		myBooks.add(item);
		return false;
	}

	public List<OPDSBookItem> books() {
		return Collections.unmodifiableList(myBooks);
	}
}
