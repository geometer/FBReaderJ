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

import org.fbreader.util.Pair;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.network.ZLNetworkContext;
import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

public class SearchCatalogTree extends NetworkCatalogTree {
	public SearchCatalogTree(RootTree parent, SearchItem item) {
		super(parent, null, item, -1);
		item.setPattern(null);
	}

	public SearchCatalogTree(NetworkCatalogTree parent, SearchItem item) {
		super(parent, parent.getLink(), item, -1);
		item.setPattern(null);
	}

	public void setPattern(String pattern) {
		((SearchItem)Item).setPattern(pattern);
	}

	@Override
	protected boolean canUseParentCover() {
		return false;
	}

	@Override
	public boolean isContentValid() {
		return true;
	}

	@Override
	public String getName() {
		final String pattern = ((SearchItem)Item).getPattern();
		if (pattern != null && Library.getStoredLoader(this) == null) {
			return NetworkLibrary.resource().getResource("found").getValue();
		}
		return super.getName();
	}

	@Override
	public Pair<String,String> getTreeTitle() {
		return new Pair(getSummary(), null);
	}

	@Override
	public String getSummary() {
		final String pattern = ((SearchItem)Item).getPattern();
		if (pattern != null) {
			return NetworkLibrary.resource().getResource("found").getResource("summary").getValue().replace("%s", pattern);
		}
		if (Library.getStoredLoader(this) != null) {
			return NetworkLibrary.resource().getResource("search").getResource("summaryInProgress").getValue();
		}
		return super.getSummary();
	}

	public MimeType getMimeType() {
		return ((SearchItem)Item).getMimeType();
	}

	public String getUrl(String pattern) {
		return ((SearchItem)Item).getUrl(pattern);
	}

	public void startItemsLoader(ZLNetworkContext nc, String pattern) {
		new Searcher(nc, this, pattern).start();
	}

	@Override
	public ZLImage createCover() {
		final INetworkLink link = getLink();
		if (link == null) {
			return null;
		}
		final UrlInfo info = link.getUrlInfo(UrlInfo.Type.SearchIcon);
		return info != null ? createCoverFromUrl(Library, info.Url, info.Mime) : null;
	}
}
