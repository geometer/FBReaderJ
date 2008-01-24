package org.fbreader.description;

public class SingleAuthor implements Author {
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
