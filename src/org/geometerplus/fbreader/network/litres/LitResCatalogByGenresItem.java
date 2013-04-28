package org.geometerplus.fbreader.network.litres;

import java.util.LinkedList;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkItem;
import org.geometerplus.fbreader.network.litres.genre.LitResGenre;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.util.MimeType;

public class LitResCatalogByGenresItem extends LitresCatalogItem {
	LinkedList<LitResGenre> myTree;
	public LitResCatalogByGenresItem(LinkedList<LitResGenre> tree, INetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls) {
		super(link, title, summary, urls);
		myTree = tree;
	}

	@Override
	public void loadChildren(NetworkItemsLoader loader) throws ZLNetworkException {
		System.out.println("GENRES MAP SIZE: "+myTree.size());
		final LitresNetworkLink litresLink = (LitresNetworkLink)Link;
		myLoadingState = litresLink.createOperationData(loader);
		/*
		System.out.println("[LitResCatalogByGenresItem] loadChildren()");
		String url = LitresUtil.generateBooksByGenreUrl("0");
		System.out.println("!! [LitResCatalogByGenresItem] loadChildren by "+url);
		doLoadChildren(
				litresLink.createNetworkData(url, MimeType.APP_LITRES_XML_GENRES, myLoadingState)
		);
		*/
		
		NetworkItem item = null;
		for (LitResGenre genre : myTree) {
			//shared_ptr<LitResGenre> genre = tree.at(i);
			System.out.println(">> "+genre.Title+", "+genre.Children.isEmpty());
			UrlInfoCollection<UrlInfo> urlByType = new UrlInfoCollection<UrlInfo>();
			
			if (genre.Children.isEmpty()) {
				System.out.println(">>>> CREATE ITEM, URL "+LitresUtil.generateBooksByGenreUrl(genre.Id));
				UrlInfo info = new UrlInfo(UrlInfo.Type.Catalog, LitresUtil.generateBooksByGenreUrl(genre.Id), MimeType.APP_LITRES_XML_GENRES);
				urlByType.addInfo(info);
				
				/*UrlInfoCollection urlByType = URLByType;
				urlByType[NetworkItem::URL_CATALOG] = LitResUtil::generateBooksByGenreUrl(genre->Id);
				//TODO add icon change for one genre here
				//urlByType[NetworkItem::URL_COVER] =
				children.push_back(new LitResBooksFeedItem(true, Link, genre->Title, EMPTY_STRING, urlByType, ALWAYS));
				*/
				item = new LitResBooksFeedItem(myLoadingState.Link, genre.Title, "", urlByType, true);
			} else {
				System.out.println(">>>> CREATE ITEM, URL "+LitresUtil.url("pages/catalit_genres/"));
				UrlInfo info = new UrlInfo(UrlInfo.Type.Catalog, LitresUtil.url("pages/catalit_genres/"), MimeType.APP_LITRES_XML_GENRES);
				urlByType.addInfo(info);
				//children.push_back(new LitResByGenresItem(genre->Children, Link, genre->Title, EMPTY_STRING, URLByType, ALWAYS, FLAG_NONE));
				item = new LitResCatalogByGenresItem(genre.Children, myLoadingState.Link, genre.Title, "", urlByType);
			}
			
			if (item != null) {
				myLoadingState.Loader.onNewItem(item);
			}
		}
		myLoadingState.Loader.getTree().confirmAllItems();
	}

}
