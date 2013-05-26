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

package org.geometerplus.fbreader.network.opds;

import java.util.LinkedList;

import org.geometerplus.fbreader.network.NetworkCatalogItem;
import org.geometerplus.fbreader.network.NetworkItem;
import org.geometerplus.fbreader.network.TopUpItem;
import org.geometerplus.fbreader.network.atom.ATOMId;
import org.geometerplus.fbreader.network.atom.ATOMLink;
import org.geometerplus.fbreader.network.authentication.litres.LitResBookshelfItem;
import org.geometerplus.fbreader.network.litres.LitResAuthorsItem;
import org.geometerplus.fbreader.network.litres.LitresBooksFeedItem;
import org.geometerplus.fbreader.network.litres.LitresCatalogByGenresItem;
import org.geometerplus.fbreader.network.litres.LitresNetworkLink;
import org.geometerplus.fbreader.network.litres.LitresPredefinedNetworkLink;
import org.geometerplus.fbreader.network.litres.LitresRecommendCatalogItem;
import org.geometerplus.fbreader.network.litres.author.LitresAuthor;
import org.geometerplus.fbreader.network.litres.author.LitresAuthorsMap;
import org.geometerplus.fbreader.network.litres.genre.LitresGenre;
import org.geometerplus.fbreader.network.litres.genre.LitresGenreMap;
import org.geometerplus.fbreader.network.urlInfo.BookUrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;

class OPDSFeedHandler extends AbstractOPDSFeedHandler implements OPDSConstants {
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
	OPDSFeedHandler(String baseURL, OPDSCatalogItem.State result) {
		myCatalog = result.Loader.getTree().Item;
		myBaseURL = baseURL;
		myData = result;
		mySkipUntilId = myData.LastLoadedId;
		myFoundNewIds = mySkipUntilId != null;
		if (!(result.Link instanceof OPDSNetworkLink)) {
			throw new IllegalArgumentException("Parameter `result` has invalid `Link` field value: result.Link must be an instance of OPDSNetworkLink class.");
		}
	}

	@Override
	public void processFeedStart() {
		myData.ResumeURI = myBaseURL;
	}

	@Override
	public boolean processFeedMetadata(OPDSFeedMetadata feed, boolean beforeEntries) {
		
		if (beforeEntries) {
			myIndex = feed.OpensearchStartIndex - 1;
			if (feed.OpensearchItemsPerPage > 0) {
				myItemsToLoad = feed.OpensearchItemsPerPage;
				final int len = feed.OpensearchTotalResults - myIndex;
				if (len > 0 && len < myItemsToLoad) {
					myItemsToLoad = len;
				}
			}
			if ("series".equals(feed.ViewType)) {
				myCatalog.setFlags(myCatalog.getFlags() & ~NetworkCatalogItem.FLAGS_GROUP);
			} else if ("authors".equals(feed.ViewType)) {
				myCatalog.setFlags(myCatalog.getFlags() & ~NetworkCatalogItem.FLAG_SHOW_AUTHOR);
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

	@Override
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
		int idType = 0;

		final OPDSNetworkLink opdsLink = (OPDSNetworkLink)myData.Link;
		for (ATOMLink link : entry.Links) {
			final MimeType mime = MimeType.get(link.getType());
			final String rel = opdsLink.relation(link.getRel(), mime);

			if (rel == null && MimeType.APP_ATOM_XML.weakEquals(mime)) {
				return ZLNetworkUtil.url(myBaseURL, link.getHref());
			}
			int relType = BookUrlInfo.Format.NONE;
			if (rel == null || rel.startsWith(REL_ACQUISITION_PREFIX)
					|| rel.startsWith(REL_FBREADER_ACQUISITION_PREFIX)) {
				relType = OPDSBookItem.formatByMimeType(mime);
			}
			if (relType != BookUrlInfo.Format.NONE
					&& (id == null || idType < relType
							|| (idType == relType && REL_ACQUISITION.equals(rel)))) {
				id = ZLNetworkUtil.url(myBaseURL, link.getHref());
				idType = relType;
			}
		}
		return id;
	}

	@Override
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
		for (ATOMLink link: entry.Links) {
			final MimeType mime = MimeType.get(link.getType());
			final String rel = opdsLink.relation(link.getRel(), mime);
			if (rel == null
					? (OPDSBookItem.formatByMimeType(mime) != BookUrlInfo.Format.NONE)
					: (rel.startsWith(REL_ACQUISITION_PREFIX)
							|| rel.startsWith(REL_FBREADER_ACQUISITION_PREFIX))) {
				hasBookLink = true;
				break;
			}
		}

		NetworkItem item;
		if (hasBookLink) {
			item = new OPDSBookItem((OPDSNetworkLink)myData.Link, entry, myBaseURL, myIndex++);
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
		MimeType litresMime = null;
		for (ATOMLink link : entry.Links) {
			//final String href = ZLNetworkUtil.url(myBaseURL, link.getHref());
			//final MimeType mime = MimeType.get(link.getType());
			MimeType mime;   
			String href;
			
			//Hack by udmv. Remove it.
			if(entry.Id.Uri.equals(new String("authors.php5"))){
				href = ZLNetworkUtil.url(myBaseURL, link.getHref());
				mime = MimeType.APP_LITRES_XML_AUTHORS;
			}else if(entry.Id.Uri.equals(new String("recommend.php5"))){
				href = ZLNetworkUtil.url(myBaseURL, link.getHref());
				mime = MimeType.APP_LITRES_XML_RECOMMEND;
			}else{
				href = ZLNetworkUtil.url(myBaseURL, link.getHref());
				mime = MimeType.get(link.getType());
			}
			
			final String rel = opdsLink.relation(link.getRel(), mime);
			System.out.println(entry.Id.Uri+" Catalog: "+href+", "+entry.Title+", "+mime.toString());
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
			} else if (MimeType.APP_LITRES_XML.weakEquals(mime)) {
				urlMap.addInfo(new UrlInfo(UrlInfo.Type.Catalog, href, mime));
				litresRel = rel;
				litresMime = mime;
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
		
		if (litresMime != null) {
			return createLitresCatalogItem(entry.Id.Uri, litresMime, litresRel, opdsLink, entry.Title, annotation, urlMap);
		} else {
			return new OPDSCatalogItem(
				opdsLink,
				entry.Title,
				annotation,
				urlMap
			);
		}
	}
	
	public NetworkItem createLitresCatalogItem(String id, final MimeType mime, final String rel,
			final OPDSNetworkLink link, CharSequence title,
			CharSequence annotation, final UrlInfoCollection<UrlInfo> urlMap){
		
		LitresNetworkLink litresLink = new LitresPredefinedNetworkLink(
				link.getId(),
				id,
				link.getSiteName(),
				link.getTitle(),
				link.getSummary(),
				link.getLanguage(),
				link.urlInfoMap()
			);
		litresLink.setAuthenticationManager(link.authenticationManager());
		final String TYPE = "type";
		final String NO = "no";
		
		String litresType = mime.getParameter(TYPE);
		
		if (REL_BOOKSHELF.equals(rel)) {
			return new LitResBookshelfItem(
					link,
					title,
				annotation,
				urlMap
			);
		} /*else if (REL_RECOMMENDATIONS.equals(rel)) {
			return new LitResRecommendationsItem(
					link,
					title,
				annotation,
				urlMap
			);
		} */else if (REL_TOPUP.equals(rel)) {
			return new TopUpItem(link, urlMap);
		}
		
		if(litresType != null){
			if (litresType.equals(MimeType.APP_LITRES_XML_BOOKS.getParameter(TYPE))) {
				int flags = NetworkCatalogItem.FLAGS_DEFAULT;
				if (mime.getParameter("groupSeries") == NO) {
					flags &= ~NetworkCatalogItem.FLAG_GROUP_MORE_THAN_1_BOOK_BY_SERIES;
				}
				if (mime.getParameter("showAuthor") == "false") {
					flags &= ~NetworkCatalogItem.FLAG_SHOW_AUTHOR;
				}
				boolean sort = mime.getParameter("sort") != NO;
				return new LitresBooksFeedItem(
						litresLink,
						title,
						annotation,
						urlMap,
						sort
						);
			} else if(litresType.equals(MimeType.APP_LITRES_XML_RECOMMEND.getParameter(TYPE))){
				return new LitresRecommendCatalogItem(litresLink,
						title,
						annotation,
						urlMap);
			} else if (litresType.equals(MimeType.APP_LITRES_XML_GENRES.getParameter(TYPE))) {
				LinkedList<LitresGenre> tree = LitresGenreMap.Instance().genresTree();
				return new LitresCatalogByGenresItem(
						tree,
						litresLink,
						title,
						annotation,
						urlMap
						);
			} else if (litresType.equals(MimeType.APP_LITRES_XML_AUTHORS.getParameter(TYPE))) {
				LinkedList<LitresAuthor> tree = LitresAuthorsMap.Instance().authorsTree();
				return new LitResAuthorsItem(
						litresLink,
						title,
						annotation,
						urlMap, tree
						);
			} 
		}
		return null;
	}
}
