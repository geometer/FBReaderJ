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

package org.geometerplus.fbreader.network;

import java.util.*;
import java.io.File;

import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;


public final class NetworkBookItem extends NetworkLibraryItem {

	public static class AuthorData implements Comparable<AuthorData> {
		public final String DisplayName;
		public final String SortKey;

		/**
		 * Creates new AuthorData instance. 
		 *
		 * @param displayName author's name. Must be not <code>null</code>.
		 * @param sortKey     string that defines sorting order of book's authors. Must be not <code>null</code>.
		 */
		public AuthorData(String displayName, String sortKey) {
			DisplayName = displayName.intern();
			SortKey = sortKey.intern();
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
			if (o == null || !(o instanceof AuthorData)) {
				return false;
			}
			final AuthorData data = (AuthorData) o;
			return SortKey == data.SortKey && DisplayName == data.DisplayName;
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
	public final String SeriesTitle;
	public final int IndexInSeries;

	private final LinkedList<BookReference> myReferences;

	/**
	 * Creates new NetworkLibraryItem instance.
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
	 * @param cover         cover url. Can be <code>null</code>.
	 * @param references    list of references related to this book. Must be not <code>null</code>.
	 */
	public NetworkBookItem(NetworkLink link, String id, int index,
		String title, String summary, /*String language, String date,*/
		List<AuthorData> authors, List<String> tags, String seriesTitle, int indexInSeries,
		String cover,
		List<BookReference> references) {
		super(link, title, summary, cover);
		Index = index;
		Id = id;
		//Language = language;
		//Date = date;
		Authors = new LinkedList<AuthorData>(authors);
		Tags = new LinkedList<String>(tags);
		SeriesTitle = seriesTitle;
		IndexInSeries = indexInSeries;
		myReferences = new LinkedList(references);
	}

	public BookReference reference(int type) {
		BookReference reference = null;
		for (BookReference ref: myReferences) {
			if (ref.ReferenceType == type &&
					(reference == null || ref.BookFormat > reference.BookFormat)) {
				reference = ref;
			}
		}

		if (reference == null && type == BookReference.Type.DOWNLOAD_FULL) {
			reference = this.reference(BookReference.Type.DOWNLOAD_FULL_CONDITIONAL);
			if (reference != null) {
				NetworkAuthenticationManager authManager = Link.authenticationManager();
				if (authManager == null || authManager.needPurchase(this)) {
					return null;
				}
				reference = authManager.downloadReference(this);
			}
		}

		if (reference == null &&
				type == BookReference.Type.DOWNLOAD_FULL &&
				this.reference(BookReference.Type.BUY) == null &&
				this.reference(BookReference.Type.BUY_IN_BROWSER) == null) {
			reference = this.reference(BookReference.Type.DOWNLOAD_FULL_OR_DEMO);
		}

		if (reference == null &&
				type == BookReference.Type.DOWNLOAD_DEMO &&
				(this.reference(BookReference.Type.BUY) != null ||
				 this.reference(BookReference.Type.BUY_IN_BROWSER) != null)) {
			reference = this.reference(BookReference.Type.DOWNLOAD_FULL_OR_DEMO);
		}

		return reference;
	}

	public String localCopyFileName() {
		final boolean hasBuyReference =
			this.reference(BookReference.Type.BUY) != null ||
			this.reference(BookReference.Type.BUY_IN_BROWSER) != null;
		BookReference reference = null;
		String fileName = null;
		for (BookReference ref: myReferences) {
			final int type = ref.ReferenceType;
			if ((type == BookReference.Type.DOWNLOAD_FULL ||
					type == BookReference.Type.DOWNLOAD_FULL_CONDITIONAL ||
					(!hasBuyReference && type == BookReference.Type.DOWNLOAD_FULL_OR_DEMO)) &&
					(reference == null || ref.BookFormat > reference.BookFormat)) {
				String name = ref.localCopyFileName(BookReference.Type.DOWNLOAD_FULL);
				if (name != null) {
					reference = ref;
					fileName = name;
				}
			}
		}
		return fileName;
	}

	public void removeLocalFiles() {
		final boolean hasBuyReference =
			this.reference(BookReference.Type.BUY) != null ||
			this.reference(BookReference.Type.BUY_IN_BROWSER) != null;
		for (BookReference ref: myReferences) {
			final int type = ref.ReferenceType;
			if (type == BookReference.Type.DOWNLOAD_FULL ||
					type == BookReference.Type.DOWNLOAD_FULL_CONDITIONAL ||
					(!hasBuyReference && type == BookReference.Type.DOWNLOAD_FULL_OR_DEMO)) {
				String fileName = ref.localCopyFileName(BookReference.Type.DOWNLOAD_FULL);
				if (fileName != null) {
					// TODO: remove a book from the library
					// TODO: remove a record from the database
					new File(fileName).delete();
				}
			}
		}
	}

}
