package org.geometerplus.fbreader.network.litres;

import java.io.IOException;
import java.io.InputStream;

import org.geometerplus.fbreader.network.AbstractNetworkLink;
import org.geometerplus.fbreader.network.NetworkCatalogItem;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.NetworkOperationData;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.litres.readers.LitresXMLReader;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoWithDate;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;
import org.geometerplus.zlibrary.core.util.MimeType;

public class LitresNetworkLink extends AbstractNetworkLink {
	private NetworkAuthenticationManager myAuthenticationManager;
	
	public LitresNetworkLink(int id, String siteName, String title, String summary, String language,
			UrlInfoCollection<UrlInfoWithDate> infos) {
		super(id, siteName, title, summary, language, infos);
	}
	
	ZLNetworkRequest createNetworkData(String url, final MimeType mime, final LitresCatalogItem.State result) {
		if (url == null) {
			return null;
		}
		final NetworkLibrary library = NetworkLibrary.Instance();
		final NetworkCatalogItem catalogItem = result.Loader.getTree().Item;
		library.startLoading(catalogItem);
		url = rewriteUrl(url, false);
		System.out.println("[LitresNetworkLink] createNetworkData url "+url);
		return new ZLNetworkRequest(url, mime, null, false) {
			@Override
			public void handleStream(InputStream inputStream, int length) throws IOException, ZLNetworkException {
				if (result.Loader.confirmInterruption()) {
					return;
				}
				
				String litresType = mime.getParameter("type");
				LitresFeedHandler handler = null;
				LitresXMLReader reader = new LitresXMLReader();
				if(litresType != null){
					if (litresType.equals(MimeType.APP_LITRES_XML_GENRES.getParameter("type"))) {	
						handler = new LitresGenreFeedHandler(result);
					}else{
						handler = new LitresFeedHandler(result);
					}
				}else{
					handler = new LitresFeedHandler(result);
				}
				
				reader.setHandler(handler);
				reader.read(inputStream);

				if (result.Loader.confirmInterruption() && result.LastLoadedId != null) {
					// reset state to load current page from the beginning
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
	public LitresCatalogItem.State createOperationData(NetworkItemsLoader loader) {
		return new LitresCatalogItem.State(this, loader);
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
		// TODO Auto-generated method stub
		return null;
	}

	public final void setAuthenticationManager(NetworkAuthenticationManager mgr) {
		myAuthenticationManager = mgr;
	}
	
	@Override
	public NetworkAuthenticationManager authenticationManager() {
		return myAuthenticationManager;
	}

	@Override
	public String rewriteUrl(String url, boolean isUrlExternal) {
		// TODO Auto-generated method stub
		return url;
	}

}
