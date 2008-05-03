/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.description;

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

public abstract class Author implements Comparable {
	public abstract String getDisplayName();
	public abstract String getSortKey();
	public abstract boolean isSingle();	
	
	static class SingleAuthor extends Author {
		private final String myDisplayName;
		private final String mySortKey;

		public static Author create(String displayName, String sortKey) {
			return new SingleAuthor(displayName, sortKey);
		}
		
		public static Author create() {
			return create("Unknown Author", "___");
		}
		
		private SingleAuthor(String displayName, String sortKey) {
			myDisplayName = displayName;
			mySortKey = sortKey.toLowerCase();
		}

		public String getDisplayName() {
			return myDisplayName;
		}
		
		public String getSortKey() {
			return mySortKey;
		}
		
		public	boolean isSingle() {
			return true;
		}
	}
	
	static class MultiAuthor extends Author {
		private final ArrayList myAuthors = new ArrayList();
		private String myDisplayName;
		private	String mySortKey;

		public static Author create(Author author) {
			return new MultiAuthor(author); 
		}
						
		private MultiAuthor(Author author) {
			addAuthor(author); 
		}

		public void addAuthor(Author author) {
			myAuthors.add(author);
			myDisplayName = ""; 
			mySortKey = "";
		}
		
		public String getDisplayName() {
			if ((myDisplayName.length() == 0) && (myAuthors.size() != 0)) {
				myDisplayName = ((Author)myAuthors.get(0)).getDisplayName();
				for (int i = 1; i < myAuthors.size(); ++i) {
					myDisplayName += ", ";
					myDisplayName += ((Author)myAuthors.get(i)).getDisplayName();
				}
			}
			return myDisplayName;
		}
		
		public String getSortKey() {
			if ((mySortKey.length() == 0) && (myAuthors.size() != 0)) {
				mySortKey = ((Author)myAuthors.get(0)).getSortKey();
				for (int i = 1; i < myAuthors.size(); ++i) {
					String key = ((Author)myAuthors.get(i)).getSortKey();
					//key < mySortKey
					if (key.compareTo(mySortKey) < 0) {
						mySortKey = key;
					}
				}
			}
			return mySortKey;
		}
		
		public boolean isSingle() {
			return false;
		}
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Author)) {
			return false;
		}
		Author a = (Author)o;
		return getSortKey().equals(a.getSortKey()) && getDisplayName().equals(a.getDisplayName());
	}

	public int hashCode() {
		return getSortKey().hashCode() + getDisplayName().hashCode();
	}

	public int compareTo(Object o) {
		final Author a = (Author)o;

		final int result = getSortKey().compareTo(a.getSortKey());
		if (result != 0) {
			return result;
		}

		return a.getDisplayName().compareTo(getDisplayName());
	}
}
