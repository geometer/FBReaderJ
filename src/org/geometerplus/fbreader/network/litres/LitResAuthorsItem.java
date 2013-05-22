package org.geometerplus.fbreader.network.litres;

import java.util.LinkedList;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkCatalogItem;
import org.geometerplus.fbreader.network.litres.author.LitresAuthor;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.util.MimeType;

public class LitResAuthorsItem extends LitresCatalogItem {
	LinkedList<LitresAuthor> initAuthorsTree;
	public LitResAuthorsItem(INetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls, LinkedList<LitresAuthor> tree) {
		super(link, title, summary, urls);
		initAuthorsTree = tree;
	}

	@Override
	public void loadChildren(NetworkItemsLoader loader) throws ZLNetworkException {
		if(initAuthorsTree != null){
			NetworkCatalogItem item = null;
			
			UrlInfoCollection<UrlInfo> urlByType = new UrlInfoCollection<UrlInfo>();
			urlByType.addInfo(new UrlInfo(UrlInfo.Type.Catalog, LitresUtil.generateAuthorsRatingUrl(), MimeType.APP_LITRES_XML_AUTHORS));
			item = new LitResAuthorsItem(Link, "Популярные авторы", "50 наиболее популярных авторов за последнюю неделю", urlByType, null);
			if (item != null) {
				loader.onNewItem(item);
			}
			for(LitresAuthor author : initAuthorsTree){
				urlByType = new UrlInfoCollection<UrlInfo>();
				urlByType.addInfo(new UrlInfo(UrlInfo.Type.Catalog, LitresUtil.generateAuthorSearchUrl(author.lastName), MimeType.APP_LITRES_XML_AUTHORS));
				item = new LitResAuthorsItem(Link, author.lastName, author.description, urlByType, null);
				if (item != null) {
					loader.onNewItem(item);
				}
			}
			loader.getTree().confirmAllItems();
		}else{
			final LitresNetworkLink litresLink = (LitresNetworkLink)Link;
			myLoadingState = litresLink.createOperationData(loader);
			UrlInfo info = myURLs.getInfo(UrlInfo.Type.Catalog);
			if(info != null){
				doLoadChildren(
					litresLink.createNetworkData(info.Url, MimeType.APP_LITRES_XML_AUTHORS, myLoadingState)
				);
			}
		}
	}

}
