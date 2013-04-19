package org.geometerplus.fbreader.network.authentication.litres;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkURLCatalogItem;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

public class LitResByGenresItem extends NetworkURLCatalogItem {
	public LitResByGenresItem(INetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls) {
		super(link, title, summary, urls, Accessibility.SIGNED_IN, FLAGS_DEFAULT);
	}

	@Override
	public void loadChildren(NetworkItemsLoader loader)
			throws ZLNetworkException {
		// TODO Auto-generated method stub
		
	}

}
