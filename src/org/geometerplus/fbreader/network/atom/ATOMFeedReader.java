package org.geometerplus.fbreader.network.atom;

public interface ATOMFeedReader <T1 extends ATOMFeedMetadata, T2 extends ATOMEntry> {
	void processFeedStart();
	
	// return true to interrupt reading; return false to continue reading
	boolean processFeedMetadata(T1 feed, boolean beforeEntries);
	
	// return true to interrupt reading; return false to continue reading
	boolean processFeedEntry(T2 entry);
	
	void processFeedEnd();
}