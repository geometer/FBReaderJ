/*
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

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
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoWithDate;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;
import org.geometerplus.zlibrary.core.util.MimeType;

public abstract class LitresNetworkLink extends AbstractNetworkLink {
	private NetworkAuthenticationManager myAuthenticationManager;
	
	protected LitresNetworkLink(int id, String siteName, String title, String summary, String language,
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
	public ZLNetworkRequest resume(NetworkOperationData data) {
		return createNetworkData(data.ResumeURI, MimeType.APP_ATOM_XML, (LitresCatalogItem.State)data);
	}

	@Override
	public NetworkCatalogItem libraryItem() {
		final UrlInfoCollection<UrlInfo> urlMap = new UrlInfoCollection<UrlInfo>();
		urlMap.addInfo(getUrlInfo(UrlInfo.Type.Catalog));
		urlMap.addInfo(getUrlInfo(UrlInfo.Type.Image));
		urlMap.addInfo(getUrlInfo(UrlInfo.Type.Thumbnail));
		return new LitresCatalogItem(
			this,
			getTitle(),
			getSummary(),
			urlMap
		);
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
		return url;
	}
	
	// rel and type must be either null or interned String objects.
	String relation(String rel, MimeType type) {
		/*if (myRelationAliases == null) {
			return rel;
		}
		RelationAlias alias = new RelationAlias(rel, type.Name);
		String mapped = myRelationAliases.get(alias);
		if (mapped != null) {
			return mapped;
		}
		if (type != null) {
			alias = new RelationAlias(rel, null);
			mapped = myRelationAliases.get(alias);
			if (mapped != null) {
				return mapped;
			}
		}
		return rel;*/
		return "";
	}

}
