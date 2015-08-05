/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.fbreader.network.tree;

import org.geometerplus.zlibrary.core.image.ZLImage;

import org.geometerplus.fbreader.network.*;

public class NetworkBookTree extends NetworkTree {
	public final NetworkBookItem Book;

	private final boolean myShowAuthors;

	public NetworkBookTree(NetworkTree parent, NetworkBookItem book, boolean showAuthors) {
		super(parent);
		Book = book;
		myShowAuthors = showAuthors;
	}

	NetworkBookTree(NetworkTree parent, NetworkBookItem book, int position, boolean showAuthors) {
		super(parent, position);
		Book = book;
		myShowAuthors = showAuthors;
	}

	@Override
	protected boolean canUseParentCover() {
		return false;
	}

	@Override
	public String getName() {
		return Book.Title.toString();
	}

	@Override
	public String getSummary() {
		if (!myShowAuthors && Book.Authors.size() < 2) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		int count = 0;
		for (NetworkBookItem.AuthorData author: Book.Authors) {
			if (count++ > 0) {
				builder.append(",  ");
			}
			builder.append(author.DisplayName);
		}
		return builder.toString();
	}

	@Override
	protected ZLImage createCover() {
		return createCoverForItem(Library, Book, true);
	}

	@Override
	protected String getStringId() {
		return Book.getStringId();
	}
}
