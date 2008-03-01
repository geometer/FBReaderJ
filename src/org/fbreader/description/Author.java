package org.fbreader.description;

import java.util.*;
import org.zlibrary.core.util.*;

public abstract class Author {
	public abstract String getDisplayName();
	public abstract String getSortKey();
	public abstract boolean isSingle();	

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof Author)) {
			return false;
		}

		Author author = (Author)o;
		return getSortKey().equals(author.getSortKey()) && getDisplayName().equals(author.getDisplayName());
	}

	public int hashCode() {
		return getSortKey().hashCode() + getDisplayName().hashCode();
	}
	
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
			mySortKey = sortKey;
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

	static public class AuthorComparator implements Comparator {
		public int compare(Object aobj1, Object aobj2) {
			Author a1 = (Author)aobj1;
			Author a2 = (Author)aobj1;
			return a1.getSortKey().compareTo(a2.getSortKey());
		}
	}

}



