package org.geometerplus.fbreader.network.atom;

public interface ATOMFeedReader {
	void processFeedStart();

	// return true to interrupt reading; return false to continue reading
	boolean processFeedMetadata(ATOMFeedMetadata feed, boolean beforeEntries);

	// return true to interrupt reading; return false to continue reading
	boolean processFeedEntry(ATOMEntry entry);

	void processFeedEnd();
}