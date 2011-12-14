/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.network.opds;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;
import org.geometerplus.zlibrary.core.util.ZLMiscUtil;

import org.geometerplus.fbreader.network.ICustomNetworkLink;
import org.geometerplus.fbreader.network.NetworkException;
import org.geometerplus.fbreader.network.urlInfo.*;

public class OPDSCustomNetworkLink extends OPDSNetworkLink implements ICustomNetworkLink {
	private boolean myHasChanges;

	private static String removeWWWPrefix(String siteName) {
		if (siteName != null && siteName.startsWith("www.")) {
			return siteName.substring(4);
		}
		return siteName;
	}

	public OPDSCustomNetworkLink(int id, String siteName, String title, String summary, String language, UrlInfoCollection<UrlInfoWithDate> infos) {
		super(id, removeWWWPrefix(siteName), title, summary, language, infos);
	}

	public boolean hasChanges() {
		return myHasChanges;
	}

	public void resetChanges() {
		myHasChanges = false;
	}

	public final void setSiteName(String name) {
		myHasChanges = myHasChanges || !ZLMiscUtil.equals(mySiteName, name);
		mySiteName = name;
	}

	public final void setSummary(String summary) {
		myHasChanges = myHasChanges || !ZLMiscUtil.equals(mySummary, summary);
		mySummary = summary;
	}

	public final void setTitle(String title) {
		myHasChanges = myHasChanges || !ZLMiscUtil.equals(myTitle, title);
		myTitle = title;
	}

	public final void setUrl(UrlInfo.Type type, String url) {
		myInfos.removeAllInfos(type);
		myInfos.addInfo(new UrlInfoWithDate(type, url));
		myHasChanges = true;
	}

	public final void removeUrl(UrlInfo.Type type) {
		myHasChanges = myHasChanges || myInfos.getInfo(type) != null;
		myInfos.removeAllInfos(type);
	}

	public boolean isObsolete(long milliSeconds) {
		final long old = System.currentTimeMillis() - milliSeconds;
		
		Date updateDate = getUrlInfo(UrlInfo.Type.Search).Updated;
		if (updateDate == null || updateDate.getTime() < old) {
			return true;
		}

		updateDate = getUrlInfo(UrlInfo.Type.Image).Updated;
		if (updateDate == null || updateDate.getTime() < old) {
			return true;
		}

		return false;
	}

	public void reloadInfo(final boolean urlsOnly) throws ZLNetworkException {
		final LinkedList<String> opensearchDescriptionURLs = new LinkedList<String>();
		final List<OpenSearchDescription> descriptions = Collections.synchronizedList(new LinkedList<OpenSearchDescription>());
		ZLNetworkException error = null;
		try {
			ZLNetworkManager.Instance().perform(new ZLNetworkRequest(getUrl(UrlInfo.Type.Catalog)) {
				@Override
				public void handleStream(InputStream inputStream, int length) throws IOException, ZLNetworkException {
					final OPDSCatalogInfoHandler info = new OPDSCatalogInfoHandler(getURL(), OPDSCustomNetworkLink.this, opensearchDescriptionURLs);
					new OPDSXMLReader(info, false).read(inputStream);
        
					if (!info.FeedStarted) {
						throw new ZLNetworkException(NetworkException.ERROR_NOT_AN_OPDS);
					}
					if (info.Title == null) {
						throw new ZLNetworkException(NetworkException.ERROR_NO_REQUIRED_INFORMATION);
					}
					setUrl(UrlInfo.Type.Image, info.Icon);
					if (info.DirectOpenSearchDescription != null) {
						descriptions.add(info.DirectOpenSearchDescription);
					}
					if (!urlsOnly) {
						myTitle = info.Title.toString();
						mySummary = info.Summary != null ? info.Summary.toString() : null;
					}
				}
			});
		} catch (ZLNetworkException e) {
			error = e;
		}

		if (!opensearchDescriptionURLs.isEmpty()) {
			LinkedList<ZLNetworkRequest> requests = new LinkedList<ZLNetworkRequest>();
			for (String url: opensearchDescriptionURLs) {
				requests.add(new ZLNetworkRequest(url) {
					@Override
					public void handleStream(InputStream inputStream, int length) throws IOException, ZLNetworkException {
						new OpenSearchXMLReader(getURL(), descriptions).read(inputStream);
					}
				});
			}
			try {
				ZLNetworkManager.Instance().perform(requests);
			} catch (ZLNetworkException e) {
				if (error == null) {
					error = e;
				}
			}
		}

		if (!descriptions.isEmpty()) {
			// TODO: May be do not use '%s'??? Use Description instead??? (this needs to rewrite SEARCH engine logic a little)
			setUrl(UrlInfo.Type.Search, descriptions.get(0).makeQuery("%s"));
		} else {
			setUrl(UrlInfo.Type.Search, null);
		}
		if (error != null) {
			throw error;
		}
	}
}
