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


class NetworkOPDSFeedReader implements OPDSFeedReader {

	private final String myBaseURL;
	private final HashMap<String, Integer> myUrlConditions;
	private final NetworkOperationData myData;
	private int myIndex;


	NetworkOPDSFeedReader(String baseURL, NetworkOperationData result, Map<String, Integer> conditions) {
		myBaseURL = baseURL;
		myData = result;
		myUrlConditions = new HashMap<String, Integer>(conditions);
	}

	@Override
	public void processFeedStart() {
		++myData.ResumeCount;
	}

	private static String intern(String value) {
		if (value == null) {
			return null;
		}
		return value.intern();
	}

	@Override
	public void processFeedMetadata(OPDSFeedMetadata feed) {
		for (ATOMLink link: feed.Links) {
			String href = link.getHref();
			String rel = intern(link.getRel());
			String type = intern(link.getType());
			if (type == OPDSConstants.MIME_APP_ATOM && rel == "next") {
				myData.ResumeURI = href;
			}
		}
		myIndex = feed.OpensearchStartIndex - 1;
	}

	@Override
	public void processFeedEntry(OPDSEntry entry) {
		Integer entryCondition = myUrlConditions.get(entry.Id.Uri);
		if (entryCondition != null && entryCondition.intValue() == OPDSLink.URLCondition.URL_CONDITION_NEVER) {
			return;
		}
		boolean hasBookLink = false;
		for (ATOMLink link: entry.Links) {
			final String rel = intern(link.getRel());
			final String type = intern(link.getType());
			if ((rel == OPDSConstants.REL_ACQUISITION ||
					 rel == OPDSConstants.REL_ACQUISITION_SAMPLE ||
					 rel == null) &&
					(type == OPDSConstants.MIME_APP_EPUB ||
					 type == OPDSConstants.MIME_APP_MOBI ||
					 type == OPDSConstants.MIME_APP_FB2ZIP)) {
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
			myData.Items.add(item);
		}
	}

	@Override
	public void processFeedEnd() {
	}

	private static final String AuthorPrefix = "author:";
	private static final String AuthorsPrefix = "authors:";

	private NetworkLibraryItem readBookItem(OPDSEntry entry) {
		final String date;
		if (entry.DCIssued != null) {
			date = entry.DCIssued.getDateTime(true);
		} else {
			date = null;
		}

		LinkedList<String> tags = new LinkedList<String>();
		for (ATOMCategory category: entry.Categories) {
			String term = category.getTerm();
			if (term != null) {
				tags.add(term);
			}
		}

		HashMap<Integer, String> urlMap = new HashMap<Integer, String>();
		for (ATOMLink link: entry.Links) {
			final String href = link.getHref();
			final String rel = intern(link.getRel());
			final String type = intern(link.getType());
			if (rel == OPDSConstants.REL_COVER ||
					rel == OPDSConstants.REL_STANZA_COVER) {
				if (urlMap.get(NetworkLibraryItem.URLType.URL_COVER) == null &&
						(type == OPDSConstants.MIME_IMG_PNG ||
						 type == OPDSConstants.MIME_IMG_JPEG)) {
					urlMap.put(NetworkLibraryItem.URLType.URL_COVER, href);
				}
			} else if (rel == OPDSConstants.REL_THUMBNAIL ||
					rel == OPDSConstants.REL_STANZA_THUMBNAIL) {
				if (type == OPDSConstants.MIME_IMG_PNG ||
						type == OPDSConstants.MIME_IMG_JPEG) {
					urlMap.put(NetworkLibraryItem.URLType.URL_COVER, href);
				}
			} else if (rel == OPDSConstants.REL_ACQUISITION || rel == null) {
				if (type == OPDSConstants.MIME_APP_FB2ZIP) {
					urlMap.put(NetworkLibraryItem.URLType.URL_BOOK_FB2_ZIP, href);
				} else if (type == OPDSConstants.MIME_APP_EPUB) {
					urlMap.put(NetworkLibraryItem.URLType.URL_BOOK_EPUB, href);
				} else if (type == OPDSConstants.MIME_APP_MOBI) {
					urlMap.put(NetworkLibraryItem.URLType.URL_BOOK_MOBIPOCKET, href);
				} else if (type == OPDSConstants.MIME_APP_PDF) {
					urlMap.put(NetworkLibraryItem.URLType.URL_BOOK_PDF, href);
				}
			} else if (rel == OPDSConstants.REL_ACQUISITION_SAMPLE) {
				if (type == OPDSConstants.MIME_APP_FB2ZIP) {
					urlMap.put(NetworkLibraryItem.URLType.URL_BOOK_DEMO_FB2_ZIP, href);
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
				index = name.lastIndexOf(' '); // TODO: how about another spaces???
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

		return new NetworkBookItem(
			myData.Link,
			entry.Id.Uri,
			myIndex++,
			entry.Title,
			entry.Summary,
			entry.DCLanguage,
			date,
			null, // price
			authors,
			tags,
			entry.SeriesTitle,
			entry.SeriesIndex,
			urlMap
		);
	}

	private NetworkLibraryItem readCatalogItem(OPDSEntry entry) {
		String coverURL = null;
		String url = null;
		boolean urlIsAlternate = false;
		String htmlURL = null;
		for (ATOMLink link: entry.Links) {
			final String href = link.getHref();
			final String rel = intern(link.getRel());
			final String type = intern(link.getType());
			if (rel == OPDSConstants.REL_COVER ||
					rel == OPDSConstants.REL_STANZA_COVER) {
				if (coverURL == null &&
						(type == OPDSConstants.MIME_IMG_PNG ||
						 type == OPDSConstants.MIME_IMG_JPEG)) {
					coverURL = href;
				}
			} else if (rel == OPDSConstants.REL_THUMBNAIL ||
					rel == OPDSConstants.REL_STANZA_THUMBNAIL) {
				if (type == OPDSConstants.MIME_IMG_PNG ||
						type == OPDSConstants.MIME_IMG_JPEG) {
					coverURL = href;
				}
			} else if (rel == OPDSConstants.REL_ACQUISITION ||
					rel == ATOMConstants.REL_ALTERNATE ||
					rel == null) {
				if (type == OPDSConstants.MIME_APP_ATOM) {
					if (rel == ATOMConstants.REL_ALTERNATE) {
						if (url == null) {
							url = href;
							urlIsAlternate = true;
						}
					} else {
						url = href;
					}
				} else if (type == OPDSConstants.MIME_TEXT_HTML) {
					htmlURL = href;
				}
			}
		}

		if (url == null && htmlURL == null) {
			return null;
		}

		if (url != null && !urlIsAlternate) {
			htmlURL = null;
		}

		Integer entryCondition = myUrlConditions.get(entry.Id.Uri);
		final boolean dependsOnAccount = entryCondition != null && entryCondition.intValue() == OPDSLink.URLCondition.URL_CONDITION_SIGNED_IN;

		final String annotation;
		if (entry.Summary == null) {
			annotation = null;
		} else {
			annotation = entry.Summary.replace("\011", "").replace("\012", "");
		}

		HashMap<Integer, String> urlMap = new HashMap<Integer, String>();
		if (coverURL != null) {
			urlMap.put(NetworkLibraryItem.URLType.URL_COVER, coverURL);
		}
		if (url != null) {
			urlMap.put(NetworkLibraryItem.URLType.URL_CATALOG, ZLNetworkUtil.url(myBaseURL, url));
		}
		if (htmlURL != null) {
			urlMap.put(NetworkLibraryItem.URLType.URL_HTML_PAGE, ZLNetworkUtil.url(myBaseURL, htmlURL));
		}
		return new OPDSCatalogItem(
			myData.Link,
			entry.Title,
			annotation,
			urlMap,
			dependsOnAccount ? OPDSCatalogItem.VisibilityType.LOGGED_USERS : OPDSCatalogItem.VisibilityType.ALWAYS
		);
	}
}
