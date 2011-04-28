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

import org.geometerplus.fbreader.network.NetworkBookItem;
import org.geometerplus.fbreader.network.atom.*;
import org.geometerplus.fbreader.network.urlInfo.*;

public class OPDSBookItem extends NetworkBookItem implements OPDSConstants, MimeTypes {
	private static String getAnnotation(OPDSEntry entry) {
		if (entry.Summary != null) {
			return entry.Summary;
		}
		if (entry.Content != null) {
			return entry.Content;
		}
		return null;
	}

	private static List<AuthorData> getAuthors(OPDSEntry entry) {
		final String AuthorPrefix = "author:";
		final String AuthorsPrefix = "authors:";

		final LinkedList<AuthorData> authors = new LinkedList<AuthorData>();
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
			final AuthorData authorData;
			if (index != -1) {
				final String before = name.substring(0, index).trim();
				final String after = name.substring(index + 1).trim();
				authorData = new AuthorData(after + ' ' + before, before);
			} else {
				name = name.trim();
				index = name.lastIndexOf(' ');
				authorData = new AuthorData(name, name.substring(index + 1));
			}
			authors.add(authorData);
		}
		return authors;
	}

	private static List<String> getTags(OPDSEntry entry) {
		final LinkedList<String> tags = new LinkedList<String>();
		for (ATOMCategory category : entry.Categories) {
			String label = category.getLabel();
			if (label == null) {
				label = category.getTerm();
			}
			if (label != null) {
				tags.add(label);
			}
		}
		return tags;
	}

	private static UrlInfoCollection getUrls(OPDSNetworkLink networkLink, OPDSEntry entry, String baseUrl) {
		final UrlInfoCollection urls = new UrlInfoCollection();
		for (ATOMLink link: entry.Links) {
			final String href = ZLNetworkUtil.url(baseUrl, link.getHref());
			final String type = ZLNetworkUtil.filterMimeType(link.getType());
			final String rel = networkLink.relation(link.getRel(), type);
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
		return urls;
	}

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
		} else if (REL_RELATED.equals(rel)) {
			return UrlInfo.Type.Related;
		} else if (REL_CONTENTS.equals(rel)) {
			return UrlInfo.Type.TOC;
		} else {
			return null;
		}
	}

	private static void collectReferences(
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

	static int formatByMimeType(String mimeType) {
		if (MIME_APP_FB2ZIP.equals(mimeType)) {
			return BookUrlInfo.Format.FB2_ZIP;
		} else if (MIME_APP_EPUB.equals(mimeType)) {
			return BookUrlInfo.Format.EPUB;
		} else if (MIME_APP_MOBI.equals(mimeType)) {
			return BookUrlInfo.Format.MOBIPOCKET;
		}
		return BookUrlInfo.Format.NONE;
	}

	OPDSBookItem(OPDSNetworkLink networkLink, OPDSEntry entry, String baseUrl, int itemIndex) {
		super(
			networkLink, entry.Id.Uri, itemIndex,
			entry.Title, getAnnotation(entry), getAuthors(entry), getTags(entry),
			entry.SeriesTitle, entry.SeriesIndex,
			getUrls(networkLink, entry, baseUrl)
		);
	}
}
