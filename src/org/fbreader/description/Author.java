package org.fbreader.description;

import java.util.List;

public interface Author {
	public String displayName();
	public String sortKey();
	public boolean isSingle();	
	
	class SingleAuthor implements Author {
		private String myDisplayName;
		private	String mySortKey;

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

		public String displayName() {
			return myDisplayName;
		}
		
		public String sortKey() {
			return mySortKey;
		}
		
		public	boolean isSingle() {
			return true;
		}
	}
	
	
	
	class MultiAuthor implements Author {
		private List<Author> myAuthors;
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
			//myDisplayName.erase();
			//mySortKey.erase();
		}
		
		public String displayName() {
			if (myDisplayName.equals("") && !myAuthors.equals("")) {
				myDisplayName = myAuthors.get(0).displayName();
				for (int i = 1; i < myAuthors.size(); ++i) {
					myDisplayName += ", ";
					myDisplayName += myAuthors.get(i).displayName();
				}
			}
			return myDisplayName;

		}
		
		public String sortKey() {
			if (mySortKey.equals("") && !myAuthors.equals("")) {
				mySortKey = myAuthors.get(0).sortKey();
				for (int i = 1; i < myAuthors.size(); ++i) {
					String key = myAuthors.get(i).sortKey();
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

	class AuthorComparator {
		
		public boolean operator(Author a1, Author a2) {
			return a1.sortKey().equals(a2.sortKey());
		}
	}

}



