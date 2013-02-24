package org.geometerplus.fbreader.network.rss;

import java.util.HashSet;
import java.util.Map;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkOperationData;
import org.geometerplus.fbreader.network.NetworkURLCatalogItem;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;
import org.geometerplus.zlibrary.core.util.MimeType;

public class RSSCatalogItem extends NetworkURLCatalogItem {
	static class State extends NetworkOperationData {
		public String LastLoadedId;
		public final HashSet<String> LoadedIds = new HashSet<String>();

		public State(RSSNetworkLink link, NetworkItemsLoader loader) {
			super(link, loader);
		}
	}
	private State myLoadingState;
	
	protected RSSCatalogItem(INetworkLink link, CharSequence title,
			CharSequence summary, UrlInfoCollection<?> urls,
			Accessibility accessibility, int flags) {
		super(link, title, summary, urls, accessibility, flags);
	}

	@Override
	public void loadChildren(NetworkItemsLoader loader) throws ZLNetworkException {
		
		final RSSNetworkLink rssLink = (RSSNetworkLink)Link;
		myLoadingState = rssLink.createOperationData(loader);
		
		doLoadChildren(rssLink.createNetworkData(getCatalogUrl(), MimeType.APP_RSS_XML, myLoadingState));
	}
	
	private void doLoadChildren(ZLNetworkRequest networkRequest) throws ZLNetworkException {
		try {
			super.doLoadChildren(myLoadingState, networkRequest);
		} catch (ZLNetworkException e) {
			myLoadingState = null;
			throw e;
		}
	}

}
