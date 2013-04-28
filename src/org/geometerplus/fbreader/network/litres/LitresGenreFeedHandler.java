package org.geometerplus.fbreader.network.litres;

import org.geometerplus.fbreader.network.NetworkItem;
import org.geometerplus.fbreader.network.litres.LitresCatalogItem.State;
import org.geometerplus.fbreader.network.litres.readers.LitresEntry;

public class LitresGenreFeedHandler extends LitresFeedHandler {

	LitresGenreFeedHandler(State result) {
		super(result);
	}
	
	@Override
	public boolean processFeedEntry(LitresEntry entry) {
		System.out.println("[LitresGenreFeedHandler] processFeedEntry");
		boolean hasBookLink = false;
		String myBaseURL = "";
		NetworkItem item = null;
		if (entry.myGenre != "") {
			//item = new LitresBookItem(myResult.Link, entry, myBaseURL, myIndex++);
			//item = new LitResCatalogByGenresItem(myResult.Link, entry.Title, entry.Summary, entry.myUrls);
		} else {
			//item = readCatalogItem(entry);
		}
		if (item != null) {
			myResult.Loader.onNewItem(item);
		}
		return false;
	}

}
