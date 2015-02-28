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

package org.geometerplus.fbreader.network;

import org.geometerplus.zlibrary.core.network.ZLNetworkContext;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;
import org.geometerplus.fbreader.network.urlInfo.*;

public abstract class SearchItem extends NetworkCatalogItem {
	private String myPattern;

	protected SearchItem(INetworkLink link, String summary) {
		super(
			link,
			NetworkLibrary.resource().getResource("search").getValue(),
			summary,
			new UrlInfoCollection<UrlInfo>(),
			Accessibility.ALWAYS,
			FLAGS_DEFAULT
		);
	}

	public void setPattern(String pattern) {
		myPattern = pattern;
	}

	public String getPattern() {
		return myPattern;
	}

	@Override
	public boolean canBeOpened() {
		return myPattern != null;
	}

	@Override
	public void loadChildren(NetworkItemsLoader loader) throws ZLNetworkException {
	}

	public abstract void runSearch(ZLNetworkContext context, NetworkItemsLoader loader, String pattern) throws ZLNetworkException;

	@Override
	public String getStringId() {
		return "@Search";
	}

	public abstract MimeType getMimeType();
	public abstract String getUrl(String pattern);
}
