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

import java.util.*;

import org.geometerplus.zlibrary.core.constants.MimeTypes;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.atom.*;
import org.geometerplus.fbreader.network.authentication.litres.LitResBookshelfItem;
import org.geometerplus.fbreader.network.authentication.litres.LitResRecommendationsItem;
import org.geometerplus.fbreader.network.urlInfo.*;

class NetworkOPDSFeedReader implements OPDSFeedReader, OPDSConstants, MimeTypes {
	private final String myBaseURL;
	private final OPDSCatalogItem.State myData;

	private int myIndex;

	private String myNextURL;
	private String mySkipUntilId;
	private boolean myFoundNewIds;

	private int myItemsToLoad = -1;

	/**
	 * Creates new OPDSFeedReader instance that can be used to get NetworkItem objects from OPDS feeds.
	 *
	 * @param baseURL    string that contains URL of the OPDS feed, that will be read using this instance of the reader
	 * @param result     network results buffer. Must be created using OPDSNetworkLink corresponding to the OPDS feed, 
	 *                   that will be read using this instance of the reader.
	 */
	NetworkOPDSFeedReader(String baseURL, OPDSCatalogItem.State result) {
		myBaseURL = baseURL;
		myData = result;
		mySkipUntilId = myData.LastLoadedId;
		myFoundNewIds = mySkipUntilId != null;
		if (!(result.Link instanceof OPDSNetworkLink)) {
			throw new IllegalArgumentException("Parameter `result` has invalid `Link` field value: result.Link must be an instance of OPDSNetworkLink class.");
		}
	}

	public void processFeedStart() {
		myData.ResumeURI = myBaseURL;
	}

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
			return false;
		}
		final OPDSNetworkLink opdsLink = (OPDSNetworkLink)myData.Link;
		for (ATOMLink link: feed.Links) {
			final String type = ZLNetworkUtil.filterMimeType(link.getType());
			final String rel = opdsLink.relation(link.getRel(), type);
			if (MIME_APP_ATOM.equals(type) && "next".equals(rel)) {
				myNextURL = ZLNetworkUtil.url(myBaseURL, link.getHref());
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


	// returns BookUrlInfo.Format value for specified String. String MUST BE interned.
	private static int formatByMimeType(String mimeType) {
		if (MIME_APP_FB2ZIP.equals(mimeType)) {
			return BookUrlInfo.Format.FB2_ZIP;
		} else if (MIME_APP_EPUB.equals(mimeType)) {
			return BookUrlInfo.Format.EPUB;
		} else if (MIME_APP_MOBI.equals(mimeType)) {
			return BookUrlInfo.Format.MOBIPOCKET;
		}
		return BookUrlInfo.Format.NONE;
	}

	// returns UrlInfo.Type value for specified String. String MUST BE interned.
	private static UrlInfo.Type typeByRelation(String rel) {
		if (rel == null || REL_ACQUISITION.equals(rel) || REL_ACQUISITION_OPEN.equals(rel)) {
			return UrlInfo.Type.Book;
		} else if (REL_ACQUISITION_SAMPLE.equals(rel)) {
			return UrlInfo.Type.BookDemo;
		} else if (REL_ACQUISITION_CONDITIONAL.equals(rel)) {
			return UrlInfo.Type.BookConditional;
		} else if (REL_ACQUISITION_SAMPLE_OR_FULL.equals(rel)) {
			return UrlInfo.Type.BookFullOrDemo;
		} else if (REL_ACQUISITION_BUY.equals(rel)) {
			return UrlInfo.Type.BookBuy;
		} else {
			return null;
		}
	}

	private boolean tryInterrupt() {
		final int noninterruptableRemainder = 10;
		return (myItemsToLoad < 0 || myItemsToLoad > noninterruptableRemainder)
				&& myData.Listener.confirmInterrupt();
	}

	private String calculateEntryId(OPDSEntry entry) {
		if (entry.Id != null) {
			return entry.Id.Uri;
		}

		String id = null;
		int idType = 0;

		final OPDSNetworkLink opdsLink = (OPDSNetworkLink)myData.Link;
		for (ATOMLink link: entry.Links) {
			final String type = ZLNetworkUtil.filterMimeType(link.getType());
			final String rel = opdsLink.relation(link.getRel(), type);

			if (rel == null && MIME_APP_ATOM.equals(type)) {
				return ZLNetworkUtil.url(myBaseURL, link.getHref());
			}
			int relType = BookUrlInfo.Format.NONE;
			if (rel == null || rel.startsWith(REL_ACQUISITION_PREFIX)
					|| rel.startsWith(REL_FBREADER_ACQUISITION_PREFIX)) {
				relType = formatByMimeType(type);
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
			final String type = ZLNetworkUtil.filterMimeType(link.getType());
			final String rel = opdsLink.relation(link.getRel(), type);
			if (rel == null
					? (formatByMimeType(type) != BookUrlInfo.Format.NONE)
					: (rel.startsWith(REL_ACQUISITION_PREFIX)
							|| rel.startsWith(REL_FBREADER_ACQUISITION_PREFIX))) {
				hasBookLink = true;
				break;
			}
		}

		NetworkItem item;
		if (hasBookLink) {
			item = readBookItem(entry);
		} else {
			item = readCatalogItem(entry);
		}
		if (item != null) {
			myData.Listener.onNewItem(myData.Link, item);
		}
		return tryInterrupt();
	}

	private static final String AuthorPrefix = "author:";
	private static final String AuthorsPrefix = "authors:";

	private NetworkItem readBookItem(OPDSEntry entry) {
		final OPDSNetworkLink opdsNetworkLink = (OPDSNetworkLink)myData.Link;
		/*final String date;
		if (entry.DCIssued != null) {
			date = entry.DCIssued.getDateTime(true);
		} else {
			date = null;
		}*/

		final LinkedList<String> tags = new LinkedList<String>();
		for (ATOMCategory category: entry.Categories) {
			String label = category.getLabel();
			if (label == null) {
				label = category.getTerm();
			}
			if (label != null) {
				tags.add(label);
			}
		}

		final UrlInfoCollection urls = new UrlInfoCollection();
		for (ATOMLink link: entry.Links) {
			final String href = ZLNetworkUtil.url(myBaseURL, link.getHref());
			final String type = ZLNetworkUtil.filterMimeType(link.getType());
			final String rel = opdsNetworkLink.relation(link.getRel(), type);
			final UrlInfo.Type referenceType = typeByRelation(rel);
			if (REL_IMAGE_THUMBNAIL.equals(rel) || REL_THUMBNAIL.equals(rel)) {
				if (MIME_IMAGE_PNG.equals(type) || MIME_IMAGE_JPEG.equals(type)) {
					urls.addInfo(new UrlInfo(UrlInfo.Type.Thumbnail, href));
				}
			} else if ((rel != null && rel.startsWith(REL_IMAGE_PREFIX)) || REL_COVER.equals(rel)) {
				if (MIME_IMAGE_PNG.equals(type) || MIME_IMAGE_JPEG.equals(type)) {
					urls.addInfo(new UrlInfo(UrlInfo.Type.Image, href));
				}
			} else if (MIME_APP_ATOM_ENTRY.equals(type)) {
				urls.addInfo(new UrlInfo(UrlInfo.Type.SingleEntry, href));
			} else if (UrlInfo.Type.BookBuy == referenceType) {
				final OPDSLink opdsLink = (OPDSLink)link; 
				String price = null;
				final OPDSPrice opdsPrice = opdsLink.selectBestPrice();
				if (opdsPrice != null) {
					price = BookBuyUrlInfo.price(opdsPrice.Price, opdsPrice.Currency);
				}
				if (price == null) {
					// FIXME: HACK: price handling must be implemented not through attributes!!!
					price = BookBuyUrlInfo.price(entry.getAttribute(OPDSXMLReader.KEY_PRICE), null);
				}
				if (price == null) {
					price = "";
				}
				if (MIME_TEXT_HTML.equals(type)) {
					collectReferences(urls, opdsLink, href,
							UrlInfo.Type.BookBuyInBrowser, price, true);
				} else {
					collectReferences(urls, opdsLink, href,
							UrlInfo.Type.BookBuy, price, false);
				}
			} else if (referenceType != null) {
				final int format = formatByMimeType(type);
				if (format != BookUrlInfo.Format.NONE) {
					urls.addInfo(new BookUrlInfo(referenceType, format, href));
				}
			}
		}

		LinkedList<NetworkBookItem.AuthorData> authors = new LinkedList<NetworkBookItem.AuthorData>();
		for (ATOMAuthor author: entry.Authors) {
			String name = author.Name;
			final String lowerCased = name.toLowerCase();
			int index = lowerCased.indexOf(AuthorPrefix);
			if (index != -1) {
				name = name.substring(index + AuthorPrefix.length());
			} else {
				index = lowerCased.indexOf(AuthorsPrefix);
				if (index != -1) {
					name = name.substring(index + AuthorsPrefix.length());
				}
			}
			index = name.indexOf(',');
			NetworkBookItem.AuthorData authorData;
			if (index != -1) {
				final String before = name.substring(0, index).trim();
				final String after = name.substring(index + 1).trim();
				authorData = new NetworkBookItem.AuthorData(after + ' ' + before, before);
			} else {
				name = name.trim();
				index = name.lastIndexOf(' ');
				authorData = new NetworkBookItem.AuthorData(name, name.substring(index + 1));
			}
			authors.add(authorData);
		}

		//entry.dcPublisher();
		//entry.updated();
		//entry.published();
		/*for (size_t i = 0; i < entry.contributors().size(); ++i) {
			ATOMContributor &contributor = *(entry.contributors()[i]);
			std::cerr << "\t\t<contributor>" << std::endl;
			std::cerr << "\t\t\t<name>"  << contributor.name()  << "</name>"  << std::endl;
			if (!contributor.uri().empty())   std::cerr << "\t\t\t<uri>"   << contributor.uri()   << "</uri>"   << std::endl;
			if (!contributor.email().empty()) std::cerr << "\t\t\t<email>" << contributor.email() << "</email>" << std::endl;
			std::cerr << "\t\t</contributor>" << std::endl;
		}*/
		//entry.rights();

		final String annotation;
		if (entry.Summary != null) {
			annotation = entry.Summary;
		} else if (entry.Content != null) {
			annotation = entry.Content;
		} else {
			annotation = null;
		}

		return new NetworkBookItem(
			opdsNetworkLink,
			entry.Id.Uri,
			myIndex++,
			entry.Title,
			annotation,
			//entry.DCLanguage,
			//date,
			authors,
			tags,
			entry.SeriesTitle,
			entry.SeriesIndex,
			urls
		);
	}

	private void collectReferences(
		UrlInfoCollection urls,
		OPDSLink opdsLink,
		String href,
		UrlInfo.Type type,
		String price,
		boolean addWithoutFormat
	) {
		boolean added = false;
		for (String mime: opdsLink.Formats) {
			final int format = formatByMimeType(mime);
			if (format != BookUrlInfo.Format.NONE) {
				urls.addInfo(new BookBuyUrlInfo(type, format, href, price));
				added = true;
			}
		}
		if (!added && addWithoutFormat) {
			urls.addInfo(new BookBuyUrlInfo(type, BookUrlInfo.Format.NONE, href, price));
		}
	}

	private NetworkItem readCatalogItem(OPDSEntry entry) {
		final OPDSNetworkLink opdsLink = (OPDSNetworkLink)myData.Link;
		final UrlInfoCollection urlMap = new UrlInfoCollection();

		boolean urlIsAlternate = false;
		String litresRel = null;
		int catalogType = NetworkCatalogItem.FLAGS_DEFAULT;
		for (ATOMLink link : entry.Links) {
			final String href = ZLNetworkUtil.url(myBaseURL, link.getHref());
			final String type = ZLNetworkUtil.filterMimeType(link.getType());
			final String rel = opdsLink.relation(link.getRel(), type);
			if (MIME_IMAGE_PNG.equals(type) || MIME_IMAGE_JPEG.equals(type)) {
				if (REL_IMAGE_THUMBNAIL.equals(rel) || REL_THUMBNAIL.equals(rel)) {
					urlMap.addInfo(new UrlInfo(UrlInfo.Type.Thumbnail, href));
				} else if (REL_COVER.equals(rel) || (rel != null && rel.startsWith(REL_IMAGE_PREFIX))) {
					urlMap.addInfo(new UrlInfo(UrlInfo.Type.Image, href));
				}
			} else if (MIME_APP_ATOM.equals(type)) {
				final boolean hasCatalogUrl =
					urlMap.getInfo(UrlInfo.Type.Catalog) != null;
				if (REL_ALTERNATE.equals(rel)) {
					if (!hasCatalogUrl) {
						urlMap.addInfo(new UrlInfo(UrlInfo.Type.Catalog, href));
						urlIsAlternate = true;
					}
				} else if (!hasCatalogUrl || rel == null || REL_SUBSECTION.equals(rel)) {
					urlMap.addInfo(new UrlInfo(UrlInfo.Type.Catalog, href));
					urlIsAlternate = false;
					if (REL_CATALOG_AUTHOR.equals(rel)) {
						catalogType &= ~NetworkCatalogItem.FLAG_SHOW_AUTHOR;
					} else if (REL_CATALOG_SERIES.equals(rel)) {
						catalogType &= ~NetworkCatalogItem.FLAGS_GROUP;
					}
				}
			} else if (MIME_TEXT_HTML.equals(type)) {
				if (REL_ACQUISITION.equals(rel) ||
					REL_ACQUISITION_OPEN.equals(rel) ||
					REL_ALTERNATE.equals(rel) ||
					rel == null) {
					urlMap.addInfo(new UrlInfo(UrlInfo.Type.HtmlPage, href));
				}
			} else if (MIME_APP_LITRES.equals(type)) {
				urlMap.addInfo(new UrlInfo(UrlInfo.Type.Catalog, href));
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

		final String annotation;
		if (entry.Summary != null) {
			annotation = entry.Summary.replace("\n", "");
		} else if (entry.Content != null) {
			annotation = entry.Content.replace("\n", "");
		} else {
			annotation = null;
		}

		if (litresRel != null) {
			if (REL_BOOKSHELF.equals(litresRel)) {
				return new LitResBookshelfItem(
					opdsLink,
					entry.Title,
					annotation,
					urlMap,
					opdsLink.getCondition(entry.Id.Uri)
				);
			} else if (REL_RECOMMENDATIONS.equals(litresRel)) {
				return new LitResRecommendationsItem(
					opdsLink,
					entry.Title,
					annotation,
					urlMap,
					opdsLink.getCondition(entry.Id.Uri)
				);
			} else if (REL_BASKET.equals(litresRel)) {
				return null;
				/*
				return new BasketItem(
					opdsLink,
					entry.Title,
					annotation,
					urlMap,
					opdsLink.getCondition(entry.Id.Uri)
				);
				*/
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
				urlMap,
				opdsLink.getCondition(entry.Id.Uri),
				catalogType
			);
		}
	}
}
