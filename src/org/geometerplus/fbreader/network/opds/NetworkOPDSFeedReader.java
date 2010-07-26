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

	private int myItemsToLoad = -1;

	/**
	 * Creates new OPDSFeedReader instance that can be used to get NetworkLibraryItem objects from OPDS feeds.
	 *
	 * @param baseURL    string that contains URL of the OPDS feed, that will be read using this instance of the reader
	 * @param result     network results buffer. Must be created using OPDSLink corresponding to the OPDS feed, 
	 *                   that will be read using this instance of the reader.
	 */
	NetworkOPDSFeedReader(String baseURL, OPDSCatalogItem.State result) {
		myBaseURL = baseURL;
		myData = result;
		mySkipUntilId = myData.LastLoadedId;
		if (!(result.Link instanceof OPDSLink)) {
			throw new IllegalArgumentException("Parameter `result` has invalid `Link` field value: result.Link must be an instance of OPDSLink class.");
		}
	}

	public void processFeedStart() {
		myData.ResumeURI = myBaseURL;
	}

	private static String filter(String value) {
		if (value == null || value.length() == 0) {
			return null;
		}
		return value;
	}

	public void processFeedMetadata(OPDSFeedMetadata feed, boolean beforeEntries) {
		if (beforeEntries) {
			myIndex = feed.OpensearchStartIndex - 1;
			if (feed.OpensearchItemsPerPage > 0) {
				myItemsToLoad = feed.OpensearchItemsPerPage;
				final int len = feed.OpensearchTotalResults - myIndex;
				if (len > 0 && len < myItemsToLoad) {
					myItemsToLoad = len;
				}
			}
			return;
		}
		final OPDSLink opdsLink = (OPDSLink) myData.Link;
		for (ATOMLink link: feed.Links) {
			String href = link.getHref();
			String type = link.getType();
			String rel = opdsLink.relation(filter(link.getRel()), type);
			if (type == OPDSConstants.MIME_APP_ATOM && rel == "next") {
				myNextURL = href;
			}
		}
	}

	public void processFeedEnd() {
		if (mySkipUntilId != null) {
			// Last loaded element was not found => resume error => DO NOT RESUME
			// TODO: notify user about error???
			// TODO: do reload???
			myNextURL = null;
		}
		myData.ResumeURI = myNextURL;
		myData.LastLoadedId = null;
	}


	// returns BookReference.Format value for specified String. String MUST BE interned.
	private static int formatByMimeType(String mimeType) {
		if (mimeType == OPDSConstants.MIME_APP_FB2ZIP) {
			return BookReference.Format.FB2_ZIP;
		} else if (mimeType == OPDSConstants.MIME_APP_EPUB) {
			return BookReference.Format.EPUB;
		} else if (mimeType == OPDSConstants.MIME_APP_MOBI) {
			return BookReference.Format.MOBIPOCKET;
		}
		return BookReference.Format.NONE;
	}

	// returns BookReference.Type value for specified String. String MUST BE interned.
	private static int typeByRelation(String rel) {
		if (rel == null || rel == OPDSConstants.REL_ACQUISITION) {
			return BookReference.Type.DOWNLOAD_FULL;
		} else if (rel == OPDSConstants.REL_ACQUISITION_SAMPLE) {
			return BookReference.Type.DOWNLOAD_DEMO;
		} else if (rel == OPDSConstants.REL_ACQUISITION_CONDITIONAL) {
			return BookReference.Type.DOWNLOAD_FULL_CONDITIONAL;
		} else if (rel == OPDSConstants.REL_ACQUISITION_SAMPLE_OR_FULL) {
			return BookReference.Type.DOWNLOAD_FULL_OR_DEMO;
		} else if (rel == OPDSConstants.REL_ACQUISITION_BUY) {
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

	public boolean processFeedEntry(OPDSEntry entry) {
		if (myItemsToLoad >= 0) {
			--myItemsToLoad;
		}

		if (mySkipUntilId != null) {
			if (mySkipUntilId.equals(entry.Id.Uri)) {
				mySkipUntilId = null;
			}
			return tryInterrupt();
		}
		myData.LastLoadedId = entry.Id.Uri;

		final OPDSLink opdsLink = (OPDSLink) myData.Link;
		if (opdsLink.getCondition(entry.Id.Uri) == OPDSLink.FeedCondition.NEVER) {
			return tryInterrupt();
		}
		boolean hasBookLink = false;
		for (ATOMLink link: entry.Links) {
			final String type = link.getType();
			final String rel = opdsLink.relation(filter(link.getRel()), type);
			if (rel == OPDSConstants.REL_ACQUISITION ||
					rel == OPDSConstants.REL_ACQUISITION_SAMPLE ||
					rel == OPDSConstants.REL_ACQUISITION_BUY ||
					rel == OPDSConstants.REL_ACQUISITION_CONDITIONAL ||
					rel == OPDSConstants.REL_ACQUISITION_SAMPLE_OR_FULL ||
					(rel == null && formatByMimeType(type) != BookReference.Format.NONE)) {
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
		final OPDSLink opdsLink = (OPDSLink) myData.Link;
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
			final String href = link.getHref();
			final String type = link.getType();
			final String rel = opdsLink.relation(filter(link.getRel()), type);
			final int referenceType = typeByRelation(rel);
			if (rel == OPDSConstants.REL_COVER) {
				if (cover == null &&
						(type == NetworkImage.MIME_PNG ||
						 type == NetworkImage.MIME_JPEG)) {
					cover = href;
				}
			} else if (rel == OPDSConstants.REL_THUMBNAIL) {
				if (type == NetworkImage.MIME_PNG ||
						type == NetworkImage.MIME_JPEG) {
					cover = href;
				}
			} else if (referenceType == BookReference.Type.BUY) {
				// FIXME: HACK: price handling must be implemented not through attributes!!!
				String price = BuyBookReference.price(
					link.getAttribute(OPDSXMLReader.KEY_PRICE),
					link.getAttribute(OPDSXMLReader.KEY_CURRENCY)
				);
				if (price == null) {
					// FIXME: HACK: price handling must be implemented not through attributes!!!
					price = BuyBookReference.price(
						entry.getAttribute(OPDSXMLReader.KEY_PRICE),
						entry.getAttribute(OPDSXMLReader.KEY_CURRENCY)
					);
				}
				if (price == null) {
					price = "";
				}
				if (type == OPDSConstants.MIME_TEXT_HTML) {
					references.add(new BuyBookReference(
						href, BookReference.Format.NONE, BookReference.Type.BUY_IN_BROWSER, price
					));
				} else {
					int format = formatByMimeType(filter(link.getAttribute(OPDSXMLReader.KEY_FORMAT)));
					if (format != BookReference.Format.NONE) {
						references.add(new BuyBookReference(
							href, format, BookReference.Type.BUY, price
						));
					}
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
			opdsLink,
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

	private NetworkLibraryItem readCatalogItem(OPDSEntry entry) {
		final OPDSLink opdsLink = (OPDSLink) myData.Link;
		String coverURL = null;
		String url = null;
		boolean urlIsAlternate = false;
		String htmlURL = null;
		boolean litresCatalogue = false;
		int catalogType = NetworkCatalogItem.CATALOG_OTHER;
		for (ATOMLink link: entry.Links) {
			final String href = link.getHref();
			final String type = link.getType();
			final String rel = opdsLink.relation(filter(link.getRel()), type);
			if (type == NetworkImage.MIME_PNG ||
					type == NetworkImage.MIME_JPEG) {
				if (rel == OPDSConstants.REL_THUMBNAIL ||
						(coverURL == null && rel == OPDSConstants.REL_COVER)) {
					coverURL = href;
				}
			} else if (type == OPDSConstants.MIME_APP_ATOM) {
				if (rel == ATOMConstants.REL_ALTERNATE) {
					if (url == null) {
						url = href;
						urlIsAlternate = true;
					}
				} else {
					url = href;
					urlIsAlternate = false;
					if (rel == OPDSConstants.REL_CATALOG_AUTHOR) {
						catalogType = NetworkCatalogItem.CATALOG_BY_AUTHORS;
					}
				}
			} else if (type == OPDSConstants.MIME_TEXT_HTML) {
				if (rel == OPDSConstants.REL_ACQUISITION ||
						rel == ATOMConstants.REL_ALTERNATE ||
						rel == null) {
					htmlURL = href;
				}
			} else if (type == OPDSConstants.MIME_APP_LITRES) {
				if (rel == OPDSConstants.REL_BOOKSHELF) {
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

		final boolean dependsOnAccount = opdsLink.getCondition(entry.Id.Uri) == OPDSLink.FeedCondition.SIGNED_IN;

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
			urlMap.put(NetworkCatalogItem.URL_CATALOG, ZLNetworkUtil.url(myBaseURL, url));
		}
		if (htmlURL != null) {
			urlMap.put(NetworkCatalogItem.URL_HTML_PAGE, ZLNetworkUtil.url(myBaseURL, htmlURL));
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
