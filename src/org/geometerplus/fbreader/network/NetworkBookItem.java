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

package org.geometerplus.fbreader.network;

import java.util.*;
import java.io.File;

import org.geometerplus.zlibrary.core.filesystem.ZLPhysicalFile;
import org.geometerplus.zlibrary.core.network.ZLNetworkContext;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.network.urlInfo.*;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;

public class NetworkBookItem extends NetworkItem {
	public static enum Status {
		NotAvailable,
		Downloaded,
		ReadyForDownload,
		CanBePurchased
	};

	public static class AuthorData implements Comparable<AuthorData> {
		public final String DisplayName;
		public final String SortKey;

		/**
		 * Creates new AuthorData instance.
		 *
		 * @param displayName author's name. Must be not <code>null</code>.
		 * @param sortKey     string that defines sorting order of book's authors.
		 */
		public AuthorData(String displayName, String sortKey) {
			DisplayName = displayName.intern();
			SortKey = sortKey != null ? sortKey.intern() : DisplayName.toLowerCase().intern();
		}

		public int compareTo(AuthorData data) {
			final int key = SortKey.compareTo(data.SortKey);
			if (key != 0) {
				return key;
			}
			return DisplayName.compareTo(data.DisplayName);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof AuthorData)) {
				return false;
			}
			final AuthorData data = (AuthorData) o;
			return SortKey.equals(data.SortKey) && DisplayName.equals(data.DisplayName);
		}

		@Override
		public int hashCode() {
			return SortKey.hashCode() + DisplayName.hashCode();
		}
	}

	public final int Index;
	public final String Id;
	//public final String Language;
	//public final String Date;
	public final LinkedList<AuthorData> Authors;
	public final LinkedList<String> Tags;
	public final List<String> Identifiers = new LinkedList<String>();
	public final String SeriesTitle;
	public final float IndexInSeries;

	/**
	 * Creates new NetworkBookItem instance.
	 *
	 * @param link          corresponding NetworkLink object. Must be not <code>null</code>.
	 * @param id            string that uniquely identifies this book item. Must be not <code>null</code>.
	 * @param index         sequence number of this book in corresponding catalog
	 * @param title         title of this book. Must be not <code>null</code>.
	 * @param summary       description of this book. Can be <code>null</code>.
	 * //@param langage       string specifies language of this book. Can be <code>null</code>.
	 * //@param date          string specifies release date of this book. Can be <code>null</code>.
	 * @param authors       list of book authors. Should contain at least one author.
	 * @param tags          list of book tags. Must be not <code>null</code> (can be empty).
	 * @param seriesTitle   title of this book's series. Can be <code>null</code>.
	 * @param indexInSeries	sequence number of this book within book's series. Ignored if seriesTitle is <code>null</code>.
	 * @param urls          list of urls related to this book. Can be <code>null</code>.
	 */
	public NetworkBookItem(INetworkLink link, String id, int index,
		CharSequence title, CharSequence summary, /*String language, String date,*/
		List<AuthorData> authors, List<String> tags, String seriesTitle, float indexInSeries,
		UrlInfoCollection<?> urls) {
		super(link, title, summary, urls);
		Index = index;
		Id = id;
		//Language = language;
		//Date = date;
		Authors = new LinkedList<AuthorData>(authors);
		Tags = new LinkedList<String>(tags);
		SeriesTitle = seriesTitle;
		IndexInSeries = indexInSeries;
	}

	public boolean isFullyLoaded() {
		return true;
	}

	public boolean loadFullInformation(ZLNetworkContext nc) {
		return true;
	}

	public NetworkCatalogItem createRelatedCatalogItem(RelatedUrlInfo info) {
		return null;
	}

	public Status getStatus(IBookCollection<Book> collection) {
		if (localCopyFileName(collection) != null) {
			return Status.Downloaded;
		} else if (reference(UrlInfo.Type.Book) != null) {
			return Status.ReadyForDownload;
		} else if (buyInfo() != null) {
			return Status.CanBePurchased;
		} else {
			return Status.NotAvailable;
		}
	}

	/*
	public Status getDemoStatus() {
	}
	*/

	private BookUrlInfo getReferenceInternal(UrlInfo.Type type) {
		BookUrlInfo reference = null;
		for (UrlInfo r : getAllInfos(type)) {
			if (!(r instanceof BookUrlInfo)) {
				continue;
			}
			final BookUrlInfo br = (BookUrlInfo)r;
			if (reference == null || BookUrlInfo.isMimeBetterThan(br.Mime, reference.Mime)) {
				reference = br;
			}
		}
		return reference;
	}

	public BookUrlInfo reference(UrlInfo.Type type) {
		final BookUrlInfo reference = getReferenceInternal(type);
		if (reference != null) {
			return reference;
		}

		switch (type) {
			case Book:
				if (getReferenceInternal(UrlInfo.Type.BookConditional) != null) {
					final NetworkAuthenticationManager authManager = Link.authenticationManager();
					if (authManager == null || authManager.needPurchase(this)) {
						return null;
					}
					return authManager.downloadReference(this);
				} else if (buyInfo() == null) {
					return getReferenceInternal(UrlInfo.Type.BookFullOrDemo);
				}
				break;
			case BookDemo:
				if (buyInfo() != null) {
					return getReferenceInternal(UrlInfo.Type.BookFullOrDemo);
				}
				break;
		}

		return null;
	}

	public BookBuyUrlInfo buyInfo() {
		final UrlInfo info = reference(UrlInfo.Type.BookBuy);
		if (info != null) {
			return (BookBuyUrlInfo)info;
		}
		return (BookBuyUrlInfo)reference(UrlInfo.Type.BookBuyInBrowser);
	}

	private static final String HASH_PREFIX = "sha1:";
	public String localCopyFileName(IBookCollection<Book> collection) {
		if (collection != null) {
			for (String identifier : Identifiers) {
				if (identifier.startsWith(HASH_PREFIX)) {
					final String hash = identifier.substring(HASH_PREFIX.length());
					final Book book = collection.getBookByHash(hash);
					if (book != null) {
						final ZLPhysicalFile file = BookUtil.fileByBook(book).getPhysicalFile();
						if (file != null) {
							return file.getPath();
						}
					}
				}
			}
		}

		final boolean hasBuyReference = buyInfo() != null;
		BookUrlInfo reference = null;
		String fileName = null;
		for (UrlInfo r : getAllInfos()) {
			if (!(r instanceof BookUrlInfo)) {
				continue;
			}
			final BookUrlInfo br = (BookUrlInfo)r;
			final UrlInfo.Type type = br.InfoType;
			if ((type == UrlInfo.Type.Book ||
				 type == UrlInfo.Type.BookConditional ||
				 (!hasBuyReference && type == UrlInfo.Type.BookFullOrDemo)) &&
				(reference == null || BookUrlInfo.isMimeBetterThan(br.Mime, reference.Mime))) {
				String name = br.localCopyFileName(UrlInfo.Type.Book);
				if (name != null) {
					reference = br;
					fileName = name;
				}
			}
		}
		return fileName;
	}

	public void removeLocalFiles() {
		final boolean hasBuyReference = buyInfo() != null;
		for (UrlInfo r : getAllInfos()) {
			if (!(r instanceof BookUrlInfo)) {
				continue;
			}
			final BookUrlInfo br = (BookUrlInfo)r;
			final UrlInfo.Type type = br.InfoType;
			if (type == UrlInfo.Type.Book ||
				type == UrlInfo.Type.BookConditional ||
				(!hasBuyReference && type == UrlInfo.Type.BookFullOrDemo)) {
				String fileName = br.localCopyFileName(UrlInfo.Type.Book);
				if (fileName != null) {
					// TODO: remove a book from the library
					// TODO: remove a record from the database
					new File(fileName).delete();
				}
			}
		}
	}

	public String getStringId() {
		return "@Book:" + Id + ":" + Title;
	}
}
