/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

import java.io.*;
import java.util.*;

import org.geometerplus.zlibrary.core.money.Money;
import org.geometerplus.zlibrary.core.network.*;
import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;

import org.geometerplus.fbreader.network.INetworkLink;
import org.geometerplus.fbreader.network.NetworkBookItem;
import org.geometerplus.fbreader.network.atom.*;
import org.geometerplus.fbreader.network.urlInfo.*;

public class OPDSBookItem extends NetworkBookItem implements OPDSConstants {
	public static OPDSBookItem create(ZLNetworkContext nc, INetworkLink link, String url) throws ZLNetworkException {
		if (link == null || url == null) {
			return null;
		}

		final CreateBookHandler handler = new CreateBookHandler(link, url);
		nc.perform(new ZLNetworkRequest.Get(url) {
			@Override
			public void handleStream(InputStream inputStream, int length) throws IOException, ZLNetworkException {
				new OPDSXMLReader(handler, true).read(inputStream);
			}
		});
		return handler.getBook();
	}

	private static CharSequence getAnnotation(OPDSEntry entry) {
		if (entry.Content != null) {
			return entry.Content;
		}
		if (entry.Summary != null) {
			return entry.Summary;
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

	private static UrlInfoCollection<UrlInfo> getUrls(OPDSNetworkLink networkLink, OPDSEntry entry, String baseUrl) {
		final UrlInfoCollection<UrlInfo> urls = new UrlInfoCollection<UrlInfo>();
		for (ATOMLink link: entry.Links) {
			final String href = ZLNetworkUtil.url(baseUrl, link.getHref());
			final MimeType mime = MimeType.get(link.getType());
			final String rel = networkLink.relation(link.getRel(), mime);
			final UrlInfo.Type referenceType = typeByRelation(rel);
			if (REL_IMAGE_THUMBNAIL.equals(rel) || REL_THUMBNAIL.equals(rel)) {
				if (MimeType.IMAGE_PNG.equals(mime) || MimeType.IMAGE_JPEG.equals(mime)) {
					urls.addInfo(new UrlInfo(UrlInfo.Type.Thumbnail, href, mime));
				}
			} else if ((rel != null && rel.startsWith(REL_IMAGE_PREFIX)) || REL_COVER.equals(rel)) {
				if (MimeType.IMAGE_PNG.equals(mime) || MimeType.IMAGE_JPEG.equals(mime)) {
					urls.addInfo(new UrlInfo(UrlInfo.Type.Image, href, mime));
				}
			} else if (MimeType.APP_ATOM_XML.weakEquals(mime) &&
						"entry".equals(mime.getParameter("type"))) {
				urls.addInfo(new UrlInfo(UrlInfo.Type.SingleEntry, href, mime));
			} else if (UrlInfo.Type.BookBuy == referenceType) {
				final OPDSLink opdsLink = (OPDSLink)link;
				Money price = opdsLink.selectBestPrice();
				if (price == null) {
					// FIXME: HACK: price handling must be implemented not through attributes!!!
					final String priceAttribute = entry.getAttribute(OPDSXMLReader.KEY_PRICE);
					if (priceAttribute != null) {
						price = new Money(priceAttribute);
					}
				}
				if (MimeType.TEXT_HTML.equals(mime)) {
					collectReferences(urls, opdsLink, href,
							UrlInfo.Type.BookBuyInBrowser, price, true);
				} else {
					collectReferences(urls, opdsLink, href,
							UrlInfo.Type.BookBuy, price, false);
				}
			} else if (referenceType == UrlInfo.Type.Related) {
				urls.addInfo(new RelatedUrlInfo(referenceType, link.getTitle(), href, mime));
			} else if (referenceType == UrlInfo.Type.Comments) {
				urls.addInfo(new RelatedUrlInfo(referenceType, link.getTitle(), href, mime));
			} else if (referenceType == UrlInfo.Type.TOC) {
				urls.addInfo(new UrlInfo(referenceType, href, mime));
			} else if (referenceType != null) {
				if (BookUrlInfo.isMimeSupported(mime)) {
					urls.addInfo(new BookUrlInfo(referenceType, href, mime));
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
		} else if (REL_REPLIES.equals(rel)) {
			return UrlInfo.Type.Comments;
		} else {
			return null;
		}
	}

	private static void collectReferences(
		UrlInfoCollection<UrlInfo> urls,
		OPDSLink opdsLink,
		String href,
		UrlInfo.Type type,
		Money price,
		boolean addWithoutFormat
	) {
		boolean added = false;
		for (String f : opdsLink.Formats) {
			final MimeType mime = MimeType.get(f);
			if (BookUrlInfo.isMimeSupported(mime)) {
				urls.addInfo(new BookBuyUrlInfo(type, href, mime, price));
				added = true;
			}
		}
		if (!added && addWithoutFormat) {
			urls.addInfo(new BookBuyUrlInfo(type, href, MimeType.NULL, price));
		}
	}

	public OPDSBookItem(
		OPDSNetworkLink link, String id, int index,
		CharSequence title, CharSequence summary,
		List<AuthorData> authors, List<String> tags,
		String seriesTitle, float indexInSeries,
		UrlInfoCollection<?> urls
	) {
		super(
			link, id, index,
			title, summary,
			authors, tags,
			seriesTitle, indexInSeries,
			urls
		);
	}

	OPDSBookItem(OPDSNetworkLink networkLink, OPDSEntry entry, String baseUrl, int index) {
		this(
			networkLink, entry.Id.Uri, index,
			entry.Title, getAnnotation(entry),
			getAuthors(entry), getTags(entry),
			entry.SeriesTitle, entry.SeriesIndex,
			getUrls(networkLink, entry, baseUrl)
		);
	}

	private volatile boolean myInformationIsFull;

	@Override
	public synchronized boolean isFullyLoaded() {
		return myInformationIsFull || getUrl(UrlInfo.Type.SingleEntry) == null;
	}

	@Override
	public synchronized boolean loadFullInformation(ZLNetworkContext nc) {
		if (myInformationIsFull) {
			return true;
		}

		final String url = getUrl(UrlInfo.Type.SingleEntry);
		if (url == null) {
			myInformationIsFull = true;
			return true;
		}

		return nc.performQuietly(new ZLNetworkRequest.Get(url) {
			@Override
			public void handleStream(InputStream inputStream, int length) throws IOException, ZLNetworkException {
				new OPDSXMLReader(new LoadInfoHandler(url), true).read(inputStream);
				myInformationIsFull = true;
			}
		});
	}

	@Override
	public OPDSCatalogItem createRelatedCatalogItem(RelatedUrlInfo info) {
		if (MimeType.APP_ATOM_XML.weakEquals(info.Mime)) {
			return new OPDSCatalogItem((OPDSNetworkLink)Link, info);
		}
		return null;
	}

	private static abstract class SingleEntryFeedHandler extends AbstractOPDSFeedHandler {
		protected final String myUrl;

		SingleEntryFeedHandler(String url) {
			myUrl = url;
		}

		public void processFeedStart() {
		}

		public boolean processFeedMetadata(OPDSFeedMetadata feed, boolean beforeEntries) {
			return false;
		}

		public void processFeedEnd() {
		}
	}

	private class LoadInfoHandler extends SingleEntryFeedHandler {
		LoadInfoHandler(String url) {
			super(url);
		}

		public boolean processFeedEntry(OPDSEntry entry) {
			addUrls(getUrls((OPDSNetworkLink)Link, entry, myUrl));
			final CharSequence summary = getAnnotation(entry);
			if (summary != null) {
				setSummary(summary);
			}
			return false;
		}
	}

	private static class CreateBookHandler extends SingleEntryFeedHandler {
		private final INetworkLink myLink;
		private OPDSBookItem myBook;

		CreateBookHandler(INetworkLink link, String url) {
			super(url);
			myLink = link;
		}

		OPDSBookItem getBook() {
			return myBook;
		}

		public boolean processFeedEntry(OPDSEntry entry) {
			myBook = new OPDSBookItem((OPDSNetworkLink)myLink, entry, myUrl, 0);
			return false;
		}
	}
}
