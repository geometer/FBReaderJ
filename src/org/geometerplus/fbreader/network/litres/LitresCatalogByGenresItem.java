/*
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network.litres;

import java.util.LinkedList;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkCatalogItem;
import org.geometerplus.fbreader.network.litres.genre.LitresGenre;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.util.MimeType;

public class LitresCatalogByGenresItem extends LitresCatalogItem {
	LinkedList<LitresGenre> myGenres;
	public LitresCatalogByGenresItem(LinkedList<LitresGenre> tree, INetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls) {
		super(link, title, summary, urls);
		myGenres = tree;
	}

	@Override
	public void loadChildren(NetworkItemsLoader loader) throws ZLNetworkException {
		final LitresNetworkLink litresLink = (LitresNetworkLink)Link;
		myLoadingState = litresLink.createOperationData(loader);
	
		NetworkCatalogItem item = null;
		System.out.println("================== GENRES: "+myGenres.size());
		for (LitresGenre genre : myGenres) {
			System.out.println(">> "+genre.getTitle()+", "+genre.getChildren().isEmpty());
			UrlInfoCollection<UrlInfo> urlByType = new UrlInfoCollection<UrlInfo>();
			
			if (genre.getChildren().isEmpty()) {
				System.out.println(">>>> CREATE ITEM, URL "+LitresUtil.generateBooksByGenreUrl(genre.getId()));
				urlByType.addInfo(new UrlInfo(UrlInfo.Type.Catalog, LitresUtil.generateBooksByGenreUrl(genre.getId()), MimeType.APP_LITRES_XML));
				item = new LitresBooksFeedItem(Link, genre.getTitle(), "", urlByType, true);
			} else {
				System.out.println(">>>> CREATE CATALOG, URL "+LitresUtil.url("pages/catalit_genres/")+genre.getId());
				urlByType.addInfo(new UrlInfo(UrlInfo.Type.Catalog, LitresUtil.url("pages/catalit_genres/")+genre.getId(), MimeType.APP_LITRES_XML));
				item = new LitresCatalogByGenresItem(genre.getChildren(), Link, genre.getTitle(), "", urlByType);
			}
			
			if (item != null) {
				loader.onNewItem(item);
			}
		}
		loader.getTree().confirmAllItems();
	}

}
