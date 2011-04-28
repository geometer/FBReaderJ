/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

import java.util.List;

import org.geometerplus.fbreader.network.NetworkBookItem;
import org.geometerplus.fbreader.network.urlInfo.*;

public class OPDSBookItem extends NetworkBookItem {
	public OPDSBookItem(
		OPDSNetworkLink link, String id, int index, String title, String summary,
		List<AuthorData> authors, List<String> tags, String seriesTitle, float indexInSeries,
		UrlInfoCollection urls
	) {
		super(link, id, index, title, summary, authors, tags, seriesTitle, indexInSeries, urls);
	}
}
