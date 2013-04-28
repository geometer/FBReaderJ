package org.geometerplus.fbreader.network.litres;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.util.MimeType;

public class LitResBooksFeedItem extends LitresCatalogItem {
	private boolean myShouldSort;
	public LitResBooksFeedItem(INetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls, boolean sort) {
		super(link, title, summary, urls);
		myShouldSort = sort;
	}

	@Override
	public void loadChildren(NetworkItemsLoader loader) throws ZLNetworkException {
		final LitresNetworkLink litresLink = (LitresNetworkLink)Link;
		myLoadingState = litresLink.createOperationData(loader);
		UrlInfo info = myURLs.getInfo(UrlInfo.Type.Catalog);
		if(info != null)
		System.out.println("!! [LitResBooksFeedItem] loadChildren by "+ info.Url);
		doLoadChildren(
				litresLink.createNetworkData(info.Url, MimeType.APP_LITRES_XML, myLoadingState)
		);
	}
}
