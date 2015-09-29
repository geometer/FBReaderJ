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

import java.util.Collections;

import org.geometerplus.fbreader.book.*;

public class SeriesListTree extends FirstLevelTree {
	SeriesListTree(RootTree root) {
		super(root, ROOT_BY_SERIES);
	}

	@Override
	public Status getOpeningStatus() {
		if (!Collection.hasSeries()) {
			return Status.CANNOT_OPEN;
		}
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}

	@Override
	public String getOpeningStatusMessage() {
		return getOpeningStatus() == Status.CANNOT_OPEN
			? "noSeries" : super.getOpeningStatusMessage();
	}

	@Override
	public void waitForOpening() {
		clear();
		for (String s : Collection.series()) {
			createSeriesSubtree(s);
		}
	}

	@Override
	public boolean onBookEvent(BookEvent event, Book book) {
		switch (event) {
			case Added:
			case Updated:
			{
				// TODO: remove empty series tree after update (?)
				final SeriesInfo info = book.getSeriesInfo();
				// TODO: pass series
				return info != null && createSeriesSubtree(info.Series.getTitle());
			}
			case Removed:
				// TODO: remove empty series tree (?)
				return false;
			default:
				return false;
		}
	}

	private boolean createSeriesSubtree(String seriesTitle) {
		// TODO: pass series as parameter
		final Series series = new Series(seriesTitle);
		final SeriesTree temp = new SeriesTree(Collection, PluginCollection, series, null);
		int position = Collections.binarySearch(subtrees(), temp);
		if (position >= 0) {
			return false;
		} else {
			new SeriesTree(this, series, null, - position - 1);
			return true;
		}
	}
}
