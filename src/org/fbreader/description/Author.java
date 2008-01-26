package org.fbreader.description;

import java.util.*;
import org.zlibrary.core.util.*;

public interface Author {
	public String getDisplayName();
	public String getSortKey();
	public boolean isSingle();	
	
	class SingleAuthor implements Author {
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
	
	
	
	class MultiAuthor implements Author {
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

	/*class AuthorComparator {
		
		public boolean operator(Author a1, Author a2) {
			return a1.sortKey().equals(a2.sortKey());
		}
	}*/

}



