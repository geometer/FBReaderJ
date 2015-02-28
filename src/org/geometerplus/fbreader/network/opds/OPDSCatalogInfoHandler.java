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

import java.util.List;

import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;

import org.geometerplus.fbreader.network.atom.ATOMLink;

class OPDSCatalogInfoHandler extends AbstractOPDSFeedHandler {
	public boolean FeedStarted;
	public String Icon;
	public CharSequence Title;
	public CharSequence Summary;

	public OpenSearchDescription DirectOpenSearchDescription;
	private final List<String> myOpensearchDescriptionURLs;

	private final String myBaseURL;
	private final OPDSNetworkLink myLink;

	public OPDSCatalogInfoHandler(String baseUrl, OPDSNetworkLink link, List<String> opensearchDescriptionURLs) {
		myBaseURL = baseUrl;
		myLink = link;
		myOpensearchDescriptionURLs = opensearchDescriptionURLs;
	}

	public boolean processFeedMetadata(OPDSFeedMetadata feed, boolean beforeEntries) {
		Icon = feed.Icon != null ? ZLNetworkUtil.url(myBaseURL, feed.Icon.Uri) : null;
		Title = feed.Title;
		Summary = feed.Subtitle;

		for (ATOMLink link: feed.Links) {
			final MimeType mime = MimeType.get(link.getType());
			final String rel = myLink.relation(link.getRel(), mime);
			if ("search".equals(rel)) {
				if (MimeType.APP_OPENSEARCHDESCRIPTION.equals(mime)) {
					myOpensearchDescriptionURLs.add(ZLNetworkUtil.url(myBaseURL, link.getHref()));
				} else if (MimeType.APP_ATOM_XML.weakEquals(mime) || MimeType.TEXT_HTML.weakEquals(mime)) {
					final String tmpl = ZLNetworkUtil.url(myBaseURL, link.getHref());
					final OpenSearchDescription descr = OpenSearchDescription.createDefault(tmpl, mime);
					if (descr.isValid()) {
						DirectOpenSearchDescription = descr;
					}
				}
			}
		}
		return true;
	}

	public void processFeedStart() {
		FeedStarted = true;
	}

	public void processFeedEnd() {
	}

	public boolean processFeedEntry(OPDSEntry entry) {
		return true;
	}
}
