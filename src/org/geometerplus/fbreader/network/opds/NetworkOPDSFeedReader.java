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

import java.util.*;

import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.atom.*;
import org.geometerplus.fbreader.network.authentication.litres.LitResBookshelfItem;


class NetworkOPDSFeedReader implements OPDSFeedReader {

	private final String myBaseURL;
	private final OPDSCatalogItem.State myData;

	private int myIndex;

	private String myNextURL;
	private String mySkipUntilId;
	private boolean myFoundNewIds;

	private int myItemsToLoad = -1;

	/**
	 * Creates new OPDSFeedReader instance that can be used to get NetworkLibraryItem objects from OPDS feeds.
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
		final OPDSNetworkLink opdsLink = (OPDSNetworkLink) myData.Link;
		for (ATOMLink link: feed.Links) {
			final String type = ZLNetworkUtil.filterMimeType(link.getType());
			final String rel = opdsLink.relation(link.getRel(), type);
			if (OPDSConstants.MIME_APP_ATOM.equals(type) && "next".equals(rel)) {
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


	// returns BookReference.Format value for specified String. String MUST BE interned.
	private static int formatByMimeType(String mimeType) {
		if (OPDSConstants.MIME_APP_FB2ZIP.equals(mimeType)) {
			return BookReference.Format.FB2_ZIP;
		} else if (OPDSConstants.MIME_APP_EPUB.equals(mimeType)) {
			return BookReference.Format.EPUB;
		} else if (OPDSConstants.MIME_APP_MOBI.equals(mimeType)) {
			return BookReference.Format.MOBIPOCKET;
		}
		return BookReference.Format.NONE;
	}

	// returns BookReference.Type value for specified String. String MUST BE interned.
	private static int typeByRelation(String rel) {
		if (rel == null || OPDSConstants.REL_ACQUISITION.equals(rel)
				|| OPDSConstants.REL_ACQUISITION_OPEN.equals(rel)) {
			return BookReference.Type.DOWNLOAD_FULL;
		} else if (OPDSConstants.REL_ACQUISITION_SAMPLE.equals(rel)) {
			return BookReference.Type.DOWNLOAD_DEMO;
		} else if (OPDSConstants.REL_ACQUISITION_CONDITIONAL.equals(rel)) {
			return BookReference.Type.DOWNLOAD_FULL_CONDITIONAL;
		} else if (OPDSConstants.REL_ACQUISITION_SAMPLE_OR_FULL.equals(rel)) {
			return BookReference.Type.DOWNLOAD_FULL_OR_DEMO;
		} else if (OPDSConstants.REL_ACQUISITION_BUY.equals(rel)) {
			return BookReference.Type.BUY;
		} else {
			return BookReference.Type.UNKNOWN;
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

		final OPDSNetworkLink opdsLink = (OPDSNetworkLink) myData.Link;
		for (ATOMLink link: entry.Links) {
			final String type = ZLNetworkUtil.filterMimeType(link.getType());
			final String rel = opdsLink.relation(link.getRel(), type);

			if (rel == null && OPDSConstants.MIME_APP_ATOM.equals(type)) {
				return ZLNetworkUtil.url(myBaseURL, link.getHref());
			}
			int relType = BookReference.Format.NONE;
			if (rel == null || rel.startsWith(OPDSConstants.REL_ACQUISITION_PREFIX)
					|| rel.startsWith(OPDSConstants.REL_FBREADER_ACQUISITION_PREFIX)) {
				relType = formatByMimeType(type);
			}
			if (relType != BookReference.Format.NONE
					&& (id == null || idType < relType
							|| (idType == relType && OPDSConstants.REL_ACQUISITION.equals(rel)))) {
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

		final OPDSNetworkLink opdsLink = (OPDSNetworkLink) myData.Link;
		if (opdsLink.getCondition(entry.Id.Uri) == OPDSNetworkLink.FeedCondition.NEVER) {
			return tryInterrupt();
		}
		boolean hasBookLink = false;
		for (ATOMLink link: entry.Links) {
			final String type = ZLNetworkUtil.filterMimeType(link.getType());
			final String rel = opdsLink.relation(link.getRel(), type);
			if (rel == null
					? (formatByMimeType(type) != BookReference.Format.NONE)
					: (rel.startsWith(OPDSConstants.REL_ACQUISITION_PREFIX)
							|| rel.startsWith(OPDSConstants.REL_FBREADER_ACQUISITION_PREFIX))) {
				hasBookLink = true;
				break;
			}
		}

		NetworkLibraryItem item;
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

	private NetworkLibraryItem readBookItem(OPDSEntry entry) {
		final OPDSNetworkLink opdsNetworkLink = (OPDSNetworkLink) myData.Link;
		/*final String date;
		if (entry.DCIssued != null) {
			date = entry.DCIssued.getDateTime(true);
		} else {
			date = null;
		}*/

		final LinkedList<String> tags = new LinkedList<String>();
		for (ATOMCategory category: entry.Categories) {
			String term = category.getTerm();
			if (term != null) {
				tags.add(term);
			}
		}

		String cover = null;
		LinkedList<BookReference> references = new LinkedList<BookReference>();
		for (ATOMLink link: entry.Links) {
			final String href = ZLNetworkUtil.url(myBaseURL, link.getHref());
			final String type = ZLNetworkUtil.filterMimeType(link.getType());
			final String rel = opdsNetworkLink.relation(link.getRel(), type);
			final int referenceType = typeByRelation(rel);
			if (OPDSConstants.REL_IMAGE_THUMBNAIL.equals(rel)
					|| OPDSConstants.REL_THUMBNAIL.equals(rel)) {
				if (NetworkImage.MIME_PNG.equals(type) ||
						NetworkImage.MIME_JPEG.equals(type)) {
					cover = href;
				}
			} else if ((rel != null && rel.startsWith(OPDSConstants.REL_IMAGE_PREFIX))
					|| OPDSConstants.REL_COVER.equals(rel)) {
				if (cover == null &&
						(NetworkImage.MIME_PNG.equals(type) ||
						 NetworkImage.MIME_JPEG.equals(type))) {
					cover = href;
				}
			} else if (BookReference.Type.BUY == referenceType) {
				final OPDSLink opdsLink = (OPDSLink) link; 
				String price = null;
				final OPDSPrice opdsPrice = opdsLink.selectBestPrice();
				if (opdsPrice != null) {
					price = BuyBookReference.price(opdsPrice.Price, opdsPrice.Currency);
				}
				if (price == null) {
					// FIXME: HACK: price handling must be implemented not through attributes!!!
					price = BuyBookReference.price(entry.getAttribute(OPDSXMLReader.KEY_PRICE), null);
				}
				if (price == null) {
					price = "";
				}
				if (OPDSConstants.MIME_TEXT_HTML.equals(type)) {
					collectReferences(references, opdsLink, href,
							BookReference.Type.BUY_IN_BROWSER, price, true);
				} else {
					collectReferences(references, opdsLink, href,
							BookReference.Type.BUY, price, false);
				}
			} else if (referenceType != BookReference.Type.UNKNOWN) {
				final int format = formatByMimeType(type);
				if (format != BookReference.Format.NONE) {
					references.add(new BookReference(href, format, referenceType));
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
			cover,
			references
		);
	}

	private void collectReferences(LinkedList<BookReference> references,
			OPDSLink opdsLink, String href, int type, String price, boolean addWithoutFormat) {
		boolean added = false;
		for (String mime: opdsLink.Formats) {
			final int format = formatByMimeType(mime);
			if (format != BookReference.Format.NONE) {
				references.add(new BuyBookReference(
					href, format, type, price
				));
				added = true;
			}
		}
		if (!added && addWithoutFormat) {
			references.add(new BuyBookReference(
				href, BookReference.Format.NONE, type, price
			));
		}
	}

	private NetworkLibraryItem readCatalogItem(OPDSEntry entry) {
		final OPDSNetworkLink opdsLink = (OPDSNetworkLink) myData.Link;
		String coverURL = null;
		String url = null;
		boolean urlIsAlternate = false;
		String htmlURL = null;
		boolean litresCatalogue = false;
		int catalogType = NetworkCatalogItem.CATALOG_OTHER;
		for (ATOMLink link: entry.Links) {
			final String href = ZLNetworkUtil.url(myBaseURL, link.getHref());
			final String type = ZLNetworkUtil.filterMimeType(link.getType());
			final String rel = opdsLink.relation(link.getRel(), type);
			if (NetworkImage.MIME_PNG.equals(type) ||
					NetworkImage.MIME_JPEG.equals(type)) {
				if (OPDSConstants.REL_IMAGE_THUMBNAIL.equals(rel) ||
						OPDSConstants.REL_THUMBNAIL.equals(rel) ||
						(coverURL == null && (OPDSConstants.REL_COVER.equals(rel) || 
								(rel != null && rel.startsWith(OPDSConstants.REL_IMAGE_PREFIX))))) {
					coverURL = href;
				}
			} else if (OPDSConstants.MIME_APP_ATOM.equals(type)) {
				if (ATOMConstants.REL_ALTERNATE.equals(rel)) {
					if (url == null) {
						url = href;
						urlIsAlternate = true;
					}
				} else if (url == null
						|| rel == null || rel.equals(OPDSConstants.REL_SUBSECTION)) {
					url = href;
					urlIsAlternate = false;
					if (OPDSConstants.REL_CATALOG_AUTHOR.equals(rel)) {
						catalogType = NetworkCatalogItem.CATALOG_BY_AUTHORS;
					}
				}
			} else if (OPDSConstants.MIME_TEXT_HTML.equals(type)) {
				if (OPDSConstants.REL_ACQUISITION.equals(rel) ||
						OPDSConstants.REL_ACQUISITION_OPEN.equals(rel) ||
						ATOMConstants.REL_ALTERNATE.equals(rel) ||
						rel == null) {
					htmlURL = href;
				}
			} else if (OPDSConstants.MIME_APP_LITRES.equals(type)) {
				if (OPDSConstants.REL_BOOKSHELF.equals(rel)) {
					litresCatalogue = true;
					url = href; // FIXME: mimeType ???
				}
			}
		}

		if (url == null && htmlURL == null) {
			return null;
		}

		if (url != null && !urlIsAlternate) {
			htmlURL = null;
		}

		final boolean dependsOnAccount =
			OPDSNetworkLink.FeedCondition.SIGNED_IN == opdsLink.getCondition(entry.Id.Uri);

		final String annotation;
		if (entry.Summary != null) {
			annotation = entry.Summary.replace("\n", "");
		} else if (entry.Content != null) {
			annotation = entry.Content.replace("\n", "");
		} else {
			annotation = null;
		}

		HashMap<Integer, String> urlMap = new HashMap<Integer, String>();
		if (url != null) {
			urlMap.put(NetworkCatalogItem.URL_CATALOG, url);
		}
		if (htmlURL != null) {
			urlMap.put(NetworkCatalogItem.URL_HTML_PAGE, htmlURL);
		}
		if (litresCatalogue) {
			return new LitResBookshelfItem(
				opdsLink,
				entry.Title,
				annotation,
				coverURL,
				urlMap,
				dependsOnAccount ? NetworkCatalogItem.VISIBLE_LOGGED_USER : NetworkCatalogItem.VISIBLE_ALWAYS
			);
		} else {
			return new OPDSCatalogItem(
				opdsLink,
				entry.Title,
				annotation,
				coverURL,
				urlMap,
				dependsOnAccount ? NetworkCatalogItem.VISIBLE_LOGGED_USER : NetworkCatalogItem.VISIBLE_ALWAYS,
				catalogType
			);
		}
	}
}
