/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import org.fbreader.util.ComparisonUtil;

import org.geometerplus.zlibrary.core.network.*;
import org.geometerplus.zlibrary.core.util.MimeType;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.urlInfo.*;

public class OPDSCustomNetworkLink extends OPDSNetworkLink implements ICustomNetworkLink {
	private final Type myType;
	private boolean myHasChanges;

	public OPDSCustomNetworkLink(NetworkLibrary library, int id, Type type, String title, String summary, String language, UrlInfoCollection<UrlInfoWithDate> infos) {
		super(library, id, title, summary, language, infos);
		myType = type;
	}

	public Type getType() {
		return myType;
	}

	public boolean hasChanges() {
		return myHasChanges;
	}

	public void resetChanges() {
		myHasChanges = false;
	}

	public final void setSummary(String summary) {
		myHasChanges = myHasChanges || !ComparisonUtil.equal(mySummary, summary);
		mySummary = summary;
	}

	public final void setTitle(String title) {
		myHasChanges = myHasChanges || !ComparisonUtil.equal(myTitle, title);
		myTitle = title;
	}

	public final void setUrl(UrlInfo.Type type, String url, MimeType mime) {
		myInfos.removeAllInfos(type);
		myInfos.addInfo(new UrlInfoWithDate(type, url, mime));
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

	public void reloadInfo(ZLNetworkContext nc, final boolean urlsOnly, boolean quietly) throws ZLNetworkException {
		final LinkedList<String> opensearchDescriptionURLs = new LinkedList<String>();
		final List<OpenSearchDescription> descriptions = Collections.synchronizedList(new LinkedList<OpenSearchDescription>());

		ZLNetworkException error = null;
		try {
			nc.perform(new ZLNetworkRequest.Get(getUrl(UrlInfo.Type.Catalog), quietly) {
				@Override
				public void handleStream(InputStream inputStream, int length) throws IOException, ZLNetworkException {
					final OPDSCatalogInfoHandler info = new OPDSCatalogInfoHandler(getURL(), OPDSCustomNetworkLink.this, opensearchDescriptionURLs);
					new OPDSXMLReader(myLibrary, info, false).read(inputStream);

					if (!info.FeedStarted) {
						throw ZLNetworkException.forCode(NetworkException.ERROR_NOT_AN_OPDS);
					}
					if (info.Title == null) {
						throw ZLNetworkException.forCode(NetworkException.ERROR_NO_REQUIRED_INFORMATION);
					}
					setUrl(UrlInfo.Type.Image, info.Icon, MimeType.IMAGE_AUTO);
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
			for (String url : opensearchDescriptionURLs) {
				requests.add(new ZLNetworkRequest.Get(url, quietly) {
					@Override
					public void handleStream(InputStream inputStream, int length) throws IOException, ZLNetworkException {
						new OpenSearchXMLReader(getURL(), descriptions).read(inputStream);
					}
				});
			}
			try {
				nc.perform(requests);
			} catch (ZLNetworkException e) {
				// we do ignore errors in opensearch description loading/parsing
				e.printStackTrace();
			}
		}

		if (!descriptions.isEmpty()) {
			// TODO: May be do not use '%s'??? Use Description instead??? (this needs to rewrite SEARCH engine logic a little)
			final OpenSearchDescription d = descriptions.get(0);
			setUrl(UrlInfo.Type.Search, d.makeQuery("%s"), d.Mime);
		} else {
			setUrl(UrlInfo.Type.Search, null, null);
		}
		if (error != null) {
			throw error;
		}
	}
}
