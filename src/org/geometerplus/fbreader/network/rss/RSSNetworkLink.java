package org.geometerplus.fbreader.network.rss;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.geometerplus.fbreader.network.AbstractNetworkLink;
import org.geometerplus.fbreader.network.ICustomNetworkLink;
import org.geometerplus.fbreader.network.NetworkCatalogItem;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.NetworkOperationData;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.opds.OPDSCatalogItem;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoWithDate;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;
import org.geometerplus.zlibrary.core.util.MimeType;

public class RSSNetworkLink extends AbstractNetworkLink implements ICustomNetworkLink {
	
	private final Map<String,String> myExtraData = new HashMap<String,String>();
	
	public RSSNetworkLink(int id, String siteName, String title, String summary,
			String language, UrlInfoCollection<UrlInfoWithDate> infos) {
		super(id, siteName, title, summary, language, infos);
	}
	
	ZLNetworkRequest createNetworkData(String url, MimeType mime, final RSSCatalogItem.State result) {
		if (url == null) {
			return null;
		}
		
		final NetworkLibrary library = NetworkLibrary.Instance();
		final NetworkCatalogItem catalogItem = result.Loader.getTree().Item;
		library.startLoading(catalogItem);

		return new ZLNetworkRequest(url, mime, null, false) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void handleStream(InputStream inputStream, int length) throws IOException, ZLNetworkException {
				if (result.Loader.confirmInterruption()) {
					return;
				}

				new RSSXMLReader(new RSSChannelHandler(getURL(), result), false).read(inputStream);
				
				if (result.Loader.confirmInterruption() && result.LastLoadedId != null) {
					result.LastLoadedId = null;
				} else {
					result.Loader.getTree().confirmAllItems();
				}
			}

			@Override
			public void doAfter(boolean success) {
				library.stopLoading(catalogItem);
			}
		};
	}

	@Override
	public RSSCatalogItem.State createOperationData(NetworkItemsLoader loader) {
		return new RSSCatalogItem.State(this, loader);
	}
	
	public final void setExtraData(Map<String,String> extraData) {
		myExtraData.clear();
		myExtraData.putAll(extraData);
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ZLNetworkRequest simpleSearchRequest(String pattern,
			NetworkOperationData data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ZLNetworkRequest resume(NetworkOperationData data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetworkCatalogItem libraryItem() {
		final UrlInfoCollection<UrlInfo> urlMap = new UrlInfoCollection<UrlInfo>();
		urlMap.addInfo(getUrlInfo(UrlInfo.Type.Catalog));
		urlMap.addInfo(getUrlInfo(UrlInfo.Type.Image));
		urlMap.addInfo(getUrlInfo(UrlInfo.Type.Thumbnail));
		return new RSSCatalogItem(
			this,
			getTitle(),
			getSummary(),
			urlMap,
			RSSCatalogItem.Accessibility.ALWAYS,
			RSSCatalogItem.FLAGS_DEFAULT | RSSCatalogItem.FLAG_ADD_SEARCH_ITEM
		);
	}

	@Override
	public NetworkAuthenticationManager authenticationManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String rewriteUrl(String url, boolean isUrlExternal) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSiteName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSummary(String summary) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUrl(
			org.geometerplus.fbreader.network.urlInfo.UrlInfo.Type type,
			String url, MimeType mime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeUrl(
			org.geometerplus.fbreader.network.urlInfo.UrlInfo.Type type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isObsolete(long milliSeconds) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reloadInfo(boolean urlsOnly, boolean quietly)
			throws ZLNetworkException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasChanges() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resetChanges() {
		// TODO Auto-generated method stub
		
	}

}
