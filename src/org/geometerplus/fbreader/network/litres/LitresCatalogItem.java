package org.geometerplus.fbreader.network.litres;

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

public class LitresCatalogItem extends NetworkURLCatalogItem {
	static class State extends NetworkOperationData {
		public String LastLoadedId;
		public final HashSet<String> LoadedIds = new HashSet<String>();

		public State(LitresNetworkLink link, NetworkItemsLoader loader) {
			super(link, loader);
		}
	}
	protected State myLoadingState;
	
	public LitresCatalogItem(INetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls) {
		this(link, title, summary, urls, Accessibility.SIGNED_IN, FLAGS_DEFAULT, null);
	}
	
	protected LitresCatalogItem(INetworkLink link, CharSequence title, CharSequence summary, UrlInfoCollection<?> urls, Accessibility accessibility, int flags, Map<String,String> extraData) {
		super(link, title, summary, urls, accessibility, flags);
		if (!(link instanceof LitresNetworkLink)) {
			throw new IllegalArgumentException("Parameter `link` has invalid value: link must be an instance of LitresNetworkLink class.");
		}
	}

	protected void doLoadChildren(ZLNetworkRequest networkRequest) throws ZLNetworkException {
		try {
			super.doLoadChildren(myLoadingState, networkRequest);
		} catch (ZLNetworkException e) {
			myLoadingState = null;
			throw e;
		}
	}
	@Override
	public void loadChildren(NetworkItemsLoader loader)
			throws ZLNetworkException {
		
		final LitresNetworkLink litresLink = (LitresNetworkLink)Link;

		myLoadingState = litresLink.createOperationData(loader);
		String url = LitresUtil.generateBooksByGenreUrl("0");
		System.out.println("!! [LitresCatalogItem] loadChildren by "+url);
		doLoadChildren(
				litresLink.createNetworkData(url, MimeType.APP_LITRES_XML, myLoadingState)
		);
	}

	@Override
	public final boolean supportsResumeLoading() {
		return true;
	}

	@Override
	public final void resumeLoading(NetworkItemsLoader loader) throws ZLNetworkException {
		if (myLoadingState != null) {
			myLoadingState.Loader = loader;
			ZLNetworkRequest networkRequest = myLoadingState.resume();
			doLoadChildren(networkRequest);
		}
	}
}
