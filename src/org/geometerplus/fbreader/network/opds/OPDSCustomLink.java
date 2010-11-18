/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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
import java.net.URLConnection;
import java.util.*;

import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;
import org.geometerplus.zlibrary.core.util.ZLMiscUtil;

import org.geometerplus.fbreader.network.ICustomNetworkLink;
import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkException;


class OPDSCustomLink extends OPDSNetworkLink implements ICustomNetworkLink {

	private int myId;
	private SaveLinkListener myListener;

	private boolean myHasChanges;

	OPDSCustomLink(int id, String siteName, String title, String summary, String icon, Map<String, String> links) {
		super(siteName, title, summary, icon, null, links, false);
		myId = id;
	}

	public int getId() {
		return myId;
	}

	public void setId(int id) {
		myId = id;
	}

	public void setSaveLinkListener(SaveLinkListener listener) {
		myListener = listener;
	}

	public void saveLink() {
		if (myListener != null) {
			myListener.onSaveLink(this);
		} else {
			throw new RuntimeException("Unable to save link: SaveLinkListener hasn't been set");
		}
	}

	public boolean hasChanges() {
		return myHasChanges;
	}

	public void resetChanges() {
		myHasChanges = false;
	}


	public final void setIcon(String icon) {
		myHasChanges = myHasChanges || !ZLMiscUtil.equals(myIcon, icon);
		myIcon = icon;
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

	public final void setLink(String urlKey, String url) {
		if (url == null) {
			removeLink(urlKey);
		} else {
			final String oldUrl = myLinks.put(urlKey, url);
			myHasChanges = myHasChanges || !url.equals(oldUrl);
		}
	}

	public final void removeLink(String urlKey) {
		final String oldUrl = myLinks.remove(urlKey);
		myHasChanges = myHasChanges || oldUrl != null;
	}


	public void reloadInfo() throws ZLNetworkException {
		final LinkedList<String> opensearchDescriptionURLs = new LinkedList<String>();
		final List<OpenSearchDescription> descriptions = Collections.synchronizedList(new LinkedList<OpenSearchDescription>());

		ZLNetworkException error = null;
		try {
			ZLNetworkManager.Instance().perform(new ZLNetworkRequest(getLink(INetworkLink.URL_MAIN)) {
				@Override
				public void handleStream(URLConnection connection, InputStream inputStream) throws IOException, ZLNetworkException {
					final CatalogInfoReader info = new CatalogInfoReader(URL, OPDSCustomLink.this, opensearchDescriptionURLs);
					new OPDSXMLReader(info).read(inputStream);
        
					if (!info.FeedStarted) {
						throw new ZLNetworkException(NetworkException.ERROR_NOT_AN_OPDS);
					}
					if (info.Title == null) {
						throw new ZLNetworkException(NetworkException.ERROR_NO_REQUIRED_INFORMATION);
					}
					myTitle = info.Title;
					if (info.Icon != null) {
						myIcon = info.Icon;
					}
					if (info.Summary != null) {
						mySummary = info.Summary;
					}
					if (info.DirectOpenSearchDescription != null) {
						descriptions.add(info.DirectOpenSearchDescription);
					}
				}
			});
		} catch (ZLNetworkException e) {
			error = e;
		}

		// TODO: Use ALL available descriptions and not only Direct
		if (descriptions.isEmpty() && !opensearchDescriptionURLs.isEmpty()) {
			LinkedList<ZLNetworkRequest> requests = new LinkedList<ZLNetworkRequest>();
			for (String url: opensearchDescriptionURLs) {
				requests.add(new ZLNetworkRequest(url) {
					@Override
					public void handleStream(URLConnection connection, InputStream inputStream) throws IOException, ZLNetworkException {
						new OpenSearchXMLReader(URL, descriptions, 20).read(inputStream);
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
			setLink(URL_SEARCH, descriptions.get(0).makeQuery("%s"));
		}
		if (error != null) {
			throw error;
		}
	}
}
