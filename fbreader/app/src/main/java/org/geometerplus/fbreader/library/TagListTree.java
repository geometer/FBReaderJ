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

package org.geometerplus.fbreader.library;

import java.util.List;

import org.geometerplus.fbreader.book.*;

public class TagListTree extends FirstLevelTree {
	TagListTree(RootTree root) {
		super(root, ROOT_BY_TAG);
	}

	@Override
	public Status getOpeningStatus() {
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}

	@Override
	public void waitForOpening() {
		clear();
		for (Tag t : Collection.tags()) {
			if (t.Parent == null) {
				createTagSubtree(t);
			}
		}
	}

	@Override
	public boolean onBookEvent(BookEvent event, Book book) {
		switch (event) {
			case Added:
			case Updated:
			{
				// TODO: remove empty tag trees after update (?)
				final List<Tag> bookTags = book.tags();
				boolean changed = false;
				if (bookTags.isEmpty()) {
					changed &= createTagSubtree(Tag.NULL);
				} else for (Tag t : bookTags) {
					if (t.Parent == null) {
						changed &= createTagSubtree(t);
					}
				}
				return changed;
			}
			case Removed:
				// TODO: remove empty tag trees (?)
				return false;
			default:
				return false;
		}
	}
}
