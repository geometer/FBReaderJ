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

import java.util.Arrays;
import java.util.List;

import org.fbreader.util.Pair;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.Filter;

public class SyncTree extends FirstLevelTree {
	private final List<String> myLabels = Arrays.asList(
		Book.SYNCHRONISED_LABEL,
		Book.SYNC_FAILURE_LABEL,
		Book.SYNC_DELETED_LABEL
	);

	SyncTree(RootTree root) {
		super(root, ROOT_SYNC);
	}

	@Override
	public Pair<String,String> getTreeTitle() {
		return new Pair(getName(), null);
	}

	@Override
	public Status getOpeningStatus() {
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}

	@Override
	public void waitForOpening() {
		clear();

		final ZLResource baseResource = resource().getResource(ROOT_SYNC);
		Filter others = new Filter.HasPhysicalFile();

		for (String label : myLabels) {
			final Filter filter = new Filter.ByLabel(label);
			if (Collection.hasBooks(filter)) {
				new SyncLabelTree(this, label, filter, baseResource.getResource(label));
			}
			others = new Filter.And(others, new Filter.Not(filter));
		}
		if (Collection.hasBooks(others)) {
			new SyncLabelTree(
				this,
				Book.SYNC_TOSYNC_LABEL,
				others,
				baseResource.getResource(Book.SYNC_TOSYNC_LABEL)
			);
		}
	}
}
