/*
 * Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network.atom;

import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public abstract class AbstractATOMFeedHandler implements ATOMFeedHandler {
	@Override
	public void processFeedStart() {
	}

	@Override
	public void processFeedEnd() {
	}

	@Override
	public boolean processFeedMetadata(ATOMFeedMetadata feed, boolean beforeEntries) {
		return false;
	}

	@Override
	public boolean processFeedEntry(ATOMEntry entry) {
		return false;
	}

	@Override
	public ATOMFeedMetadata createFeed(ZLStringMap attributes) {
		return new ATOMFeedMetadata(attributes);
	}

	@Override
	public ATOMEntry createEntry(ZLStringMap attributes) {
		return new ATOMEntry(attributes);
	}

	@Override
	public ATOMLink createLink(ZLStringMap attributes) {
		return new ATOMLink(attributes);
	}
}
