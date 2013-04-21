package org.geometerplus.fbreader.network.litres;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.util.MimeType;

public class LitResCatalogByGenresItem extends LitresCatalogItem {
	public LitResCatalogByGenresItem(INetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls) {
		super(link, title, summary, urls);
	}

	@Override
	public void loadChildren(NetworkItemsLoader loader) throws ZLNetworkException {
		System.out.println("[LitResCatalogByGenresItem] loadChildren()");
		final LitresNetworkLink litresLink = (LitresNetworkLink)Link;

		myLoadingState = litresLink.createOperationData(loader);
		String url = LitresUtil.generateBooksByGenreUrl("0");
		System.out.println("!! [LitResCatalogByGenresItem] loadChildren by "+url);
		doLoadChildren(
				litresLink.createNetworkData(url, MimeType.APP_LITRES_XML, myLoadingState)
		);
	}

}
