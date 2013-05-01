package org.geometerplus.fbreader.network.litres;

import java.util.LinkedList;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkCatalogItem;
import org.geometerplus.fbreader.network.litres.genre.LitResGenre;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.util.MimeType;

public class LitResCatalogByGenresItem extends LitresCatalogItem {
	LinkedList<LitResGenre> myGenres;
	public LitResCatalogByGenresItem(LinkedList<LitResGenre> tree, INetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls) {
		super(link, title, summary, urls);
		myGenres = tree;
	}

	@Override
	public void loadChildren(NetworkItemsLoader loader) throws ZLNetworkException {
		final LitresNetworkLink litresLink = (LitresNetworkLink)Link;
		myLoadingState = litresLink.createOperationData(loader);
	
		NetworkCatalogItem item = null;
		System.out.println("================== GENRES: "+myGenres.size());
		for (LitResGenre genre : myGenres) {
			System.out.println(">> "+genre.Title+", "+genre.Children.isEmpty());
			UrlInfoCollection<UrlInfo> urlByType = new UrlInfoCollection<UrlInfo>();
			
			if (genre.Children.isEmpty()) {
				System.out.println(">>>> CREATE ITEM, URL "+LitresUtil.generateBooksByGenreUrl(genre.Id));
				UrlInfo info = new UrlInfo(UrlInfo.Type.Catalog, LitresUtil.generateBooksByGenreUrl(genre.Id), MimeType.APP_LITRES_XML);
				urlByType.addInfo(info);
				item = new LitResBooksFeedItem(Link, genre.Title, "", urlByType, true);
			} else {
				System.out.println(">>>> CREATE CATALOG, URL "+LitresUtil.url("pages/catalit_genres/"));
				UrlInfo info = new UrlInfo(UrlInfo.Type.Catalog, LitresUtil.url("pages/catalit_genres/"), MimeType.APP_LITRES_XML_GENRES);
				urlByType.addInfo(info);
				item = new LitResCatalogByGenresItem(genre.Children, Link, genre.Title, "", urlByType);
			}
			
			if (item != null) {
				loader.onNewItem(item);
			}
		}
		loader.getTree().confirmAllItems();
	}

}
