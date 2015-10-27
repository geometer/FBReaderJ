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

import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.atom.*;
import org.geometerplus.fbreader.network.authentication.litres.LitResBookshelfItem;
import org.geometerplus.fbreader.network.authentication.litres.LitResRecommendationsItem;
import org.geometerplus.fbreader.network.urlInfo.*;

class OPDSFeedHandler extends AbstractOPDSFeedHandler implements OPDSConstants {
	private final NetworkLibrary myLibrary;
	private final NetworkCatalogItem myCatalog;
	private final String myBaseURL;
	private final OPDSCatalogItem.State myData;

	private int myIndex;

	private String myNextURL;
	private String mySkipUntilId;
	private boolean myFoundNewIds;

	private int myItemsToLoad = -1;

	/**
	 * Creates new OPDSFeedHandler instance that can be used to get NetworkItem objects from OPDS feeds.
	 *
	 * @param baseURL    string that contains URL of the OPDS feed, that will be read using this instance of the reader
	 * @param result     network results buffer. Must be created using OPDSNetworkLink corresponding to the OPDS feed,
	 *                   that will be read using this instance of the reader.
	 */
	OPDSFeedHandler(NetworkLibrary library, String baseURL, OPDSCatalogItem.State result) {
		if (!(result.Link instanceof OPDSNetworkLink)) {
			throw new IllegalArgumentException(result.Link + " is not an instance of OPDSNetworkLink class");
		}

		myLibrary = library;
		myCatalog = result.Loader.Tree.Item;
		myBaseURL = baseURL;
		myData = result;
		mySkipUntilId = myData.LastLoadedId;
		myFoundNewIds = mySkipUntilId != null;
	}

	public void processFeedStart() {
		myData.ResumeURI = myBaseURL;
	}

	public boolean processFeedMetadata(OPDSFeedMetadata feed, boolean beforeEntries) {
		if (beforeEntries) {
			myIndex = feed.OpensearchStartIndex - 1;
			if (feed.OpensearchItemsPerPage > 0) {
				myItemsToLoad = feed.OpensearchItemsPerPage;
				if (feed.OpensearchTotalResults >= 0) {
					myItemsToLoad =
						Math.min(Math.max(0, feed.OpensearchTotalResults - myIndex), myItemsToLoad);
				}
				if (myItemsToLoad == 0) {
					myData.ResumeURI = null;
					return true;
				}
			}
			if ("series".equals(feed.ViewType)) {
				myCatalog.setFlags(myCatalog.getFlags() & ~OPDSCatalogItem.FLAGS_GROUP);
			} else if ("authors".equals(feed.ViewType)) {
				myCatalog.setFlags(myCatalog.getFlags() & ~OPDSCatalogItem.FLAG_SHOW_AUTHOR);
			}
		} else {
			final OPDSNetworkLink opdsLink = (OPDSNetworkLink)myData.Link;
			for (ATOMLink link : feed.Links) {
				final MimeType mime = MimeType.get(link.getType());
				final String rel = opdsLink.relation(link.getRel(), mime);
				if (MimeType.APP_ATOM_XML.weakEquals(mime) && "next".equals(rel)) {
					myNextURL = ZLNetworkUtil.url(myBaseURL, link.getHref());
				}
			}
		}
		return false;
	}

	public void processFeedEnd() {
		if (mySkipUntilId != null) {
			// Last loaded element was not found => resume error => DO NOT RESUME
			// TODO: notify user about error???
			// TODO: do reload???
			myNextURL = null;
		}
		myData.ResumeURI = myFoundNewIds ? myNextURL : null;
		myData.LastLoadedId = null;
	}

	private boolean tryInterrupt() {
		final int noninterruptableRemainder = 10;
		return (myItemsToLoad < 0 || myItemsToLoad > noninterruptableRemainder)
				&& myData.Loader.confirmInterruption();
	}

	private String calculateEntryId(OPDSEntry entry) {
		if (entry.Id != null) {
			return entry.Id.Uri;
		}

		String id = null;
		MimeType idMime = MimeType.NULL;

		final OPDSNetworkLink opdsLink = (OPDSNetworkLink)myData.Link;
		for (ATOMLink link : entry.Links) {
			final MimeType mime = MimeType.get(link.getType());
			final String rel = opdsLink.relation(link.getRel(), mime);

			if (rel == null && MimeType.APP_ATOM_XML.weakEquals(mime)) {
				return ZLNetworkUtil.url(myBaseURL, link.getHref());
			}
			if (!BookUrlInfo.isMimeSupported(mime, myLibrary.SystemInfo)) {
				continue;
			}
			if (rel != null
				&& !rel.startsWith(REL_ACQUISITION_PREFIX)
				&& !rel.startsWith(REL_FBREADER_ACQUISITION_PREFIX)
			) {
				continue;
			}
			if (id == null
				|| BookUrlInfo.isMimeBetterThan(mime, idMime)
				|| (idMime.equals(mime) && REL_ACQUISITION.equals(rel))
			) {
				id = ZLNetworkUtil.url(myBaseURL, link.getHref());
				idMime = mime;
			}
		}
		return id;
	}

	public boolean processFeedEntry(OPDSEntry entry) {
		if (myItemsToLoad >= 0) {
			--myItemsToLoad;
		}

		if (entry.Id == null) {
			final String id = calculateEntryId(entry);
			if (id == null) {
				return tryInterrupt();
			}
			entry.Id = new ATOMId();
			entry.Id.Uri = id;
		}

		if (mySkipUntilId != null) {
			if (mySkipUntilId.equals(entry.Id.Uri)) {
				mySkipUntilId = null;
			}
			return tryInterrupt();
		}
		myData.LastLoadedId = entry.Id.Uri;
		if (!myFoundNewIds && !myData.LoadedIds.contains(entry.Id.Uri)) {
			myFoundNewIds = true;
		}
		myData.LoadedIds.add(entry.Id.Uri);

		final OPDSNetworkLink opdsLink = (OPDSNetworkLink)myData.Link;
		boolean hasBookLink = false;
		for (ATOMLink link : entry.Links) {
			final MimeType mime = MimeType.get(link.getType());
			final String rel = opdsLink.relation(link.getRel(), mime);
			if (rel == null
					? (BookUrlInfo.isMimeSupported(mime, myLibrary.SystemInfo))
					: (rel.equals(REL_RELATED)
						|| rel.startsWith(REL_ACQUISITION_PREFIX)
						|| rel.startsWith(REL_FBREADER_ACQUISITION_PREFIX))) {
				hasBookLink = true;
				break;
			}
		}

		NetworkItem item;
		if (hasBookLink) {
			item = new OPDSBookItem(myLibrary, (OPDSNetworkLink)myData.Link, entry, myBaseURL, myIndex++);
			for (String identifier : entry.DCIdentifiers) {
				((OPDSBookItem)item).Identifiers.add(identifier);
			}
		} else {
			item = readCatalogItem(entry);
		}
		if (item != null) {
			myData.Loader.onNewItem(item);
		}
		return tryInterrupt();
	}

	private NetworkItem readCatalogItem(OPDSEntry entry) {
		final OPDSNetworkLink opdsLink = (OPDSNetworkLink)myData.Link;
		final UrlInfoCollection<UrlInfo> urlMap = new UrlInfoCollection<UrlInfo>();

		boolean urlIsAlternate = false;
		String litresRel = null;
		for (ATOMLink link : entry.Links) {
			final String href = ZLNetworkUtil.url(myBaseURL, link.getHref());
			final MimeType mime = MimeType.get(link.getType());
			final String rel = opdsLink.relation(link.getRel(), mime);
			if (MimeType.IMAGE_PNG.weakEquals(mime) || MimeType.IMAGE_JPEG.weakEquals(mime)) {
				if (REL_IMAGE_THUMBNAIL.equals(rel) || REL_THUMBNAIL.equals(rel)) {
					urlMap.addInfo(new UrlInfo(UrlInfo.Type.Thumbnail, href, mime));
				} else if (REL_COVER.equals(rel) || (rel != null && rel.startsWith(REL_IMAGE_PREFIX))) {
					urlMap.addInfo(new UrlInfo(UrlInfo.Type.Image, href, mime));
				}
			} else if (MimeType.APP_ATOM_XML.weakEquals(mime)) {
				final boolean hasCatalogUrl =
					urlMap.getInfo(UrlInfo.Type.Catalog) != null;
				if (REL_ALTERNATE.equals(rel)) {
					if (!hasCatalogUrl) {
						urlMap.addInfo(new UrlInfo(UrlInfo.Type.Catalog, href, mime));
						urlIsAlternate = true;
					}
				} else if (!hasCatalogUrl || rel == null || REL_SUBSECTION.equals(rel)) {
					urlMap.addInfo(new UrlInfo(UrlInfo.Type.Catalog, href, mime));
					urlIsAlternate = false;
				}
			} else if (MimeType.TEXT_HTML.weakEquals(mime)) {
				if (REL_ACQUISITION.equals(rel) ||
					REL_ACQUISITION_OPEN.equals(rel) ||
					REL_ALTERNATE.equals(rel) ||
					rel == null) {
					urlMap.addInfo(new UrlInfo(UrlInfo.Type.HtmlPage, href, mime));
				}
			} else if (MimeType.APP_LITRES.weakEquals(mime)) {
				urlMap.addInfo(new UrlInfo(UrlInfo.Type.Catalog, href, mime));
				litresRel = rel;
			}
		}

		if (urlMap.getInfo(UrlInfo.Type.Catalog) == null &&
			urlMap.getInfo(UrlInfo.Type.HtmlPage) == null) {
			return null;
		}

		if (urlMap.getInfo(UrlInfo.Type.Catalog) != null && !urlIsAlternate) {
			urlMap.removeAllInfos(UrlInfo.Type.HtmlPage);
		}

		final CharSequence annotation;
		if (entry.Summary != null) {
			annotation = entry.Summary;
		} else if (entry.Content != null) {
			annotation = entry.Content;
		} else {
			annotation = null;
		}

		if (litresRel != null) {
			if (REL_BOOKSHELF.equals(litresRel)) {
				return new LitResBookshelfItem(
					opdsLink,
					entry.Title,
					annotation,
					urlMap
				);
			} else if (REL_RECOMMENDATIONS.equals(litresRel)) {
				return new LitResRecommendationsItem(
					opdsLink,
					entry.Title,
					annotation,
					urlMap
				);
			} else if (REL_TOPUP.equals(litresRel)) {
				return new TopUpItem(opdsLink, urlMap);
			} else {
				return null;
			}
		} else {
			return new OPDSCatalogItem(
				opdsLink,
				entry.Title,
				annotation,
				urlMap
			);
		}
	}
}
