package org.geometerplus.fbreader.network.litres;

import java.util.List;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkBookItem;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.authentication.litres.LitResAuthenticationManager;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.util.MimeType;

public class LitresRecommendCatalogItem extends LitresCatalogItem {

	public LitresRecommendCatalogItem(INetworkLink link, CharSequence title,
			CharSequence summary, UrlInfoCollection<?> urls) {
		super(link, title, summary, urls);
	}
	
	@Override
	public void loadChildren(NetworkItemsLoader loader) throws ZLNetworkException{
		final LitresNetworkLink litresLink = (LitresNetworkLink)Link;
		myLoadingState = litresLink.createOperationData(loader);
		//UrlInfo info = myURLs.getInfo(UrlInfo.Type.Catalog);
		//if(info != null)
		
		List<NetworkBookItem> books = null;
		final NetworkAuthenticationManager mgr = Link.authenticationManager();
		if (mgr != null && mgr instanceof LitResAuthenticationManager) {
			books = mgr.purchasedBooks();
		}
		
		if(books == null) return;
		
		NetworkBookItem book = null;
		int limit = (books.size() < 5) ? books.size() : 5;
		for(int i=0;i<limit;i++){
			
			book = books.get(i);
			
			String url = "http://robot.litres.ru/pages/catalit_browser/?rating=with&limit=0,10&art="+book.Id;
			System.out.println(i+">>>> load by url = "+url);
			doLoadChildren(
					litresLink.createNetworkData(url, MimeType.APP_LITRES_XML_RECOMMEND, myLoadingState, null)
			);
			for(;;){
				if(myLoadingState.loadFinished){
					System.out.println("Yes!");
					break;
				}
			}
		}
	}
}
