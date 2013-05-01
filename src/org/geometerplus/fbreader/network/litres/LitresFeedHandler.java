package org.geometerplus.fbreader.network.litres;

import java.util.LinkedList;
import java.util.List;

import org.geometerplus.fbreader.network.NetworkBookItem;
import org.geometerplus.fbreader.network.NetworkItem;
import org.geometerplus.fbreader.network.atom.ATOMFeedHandler;
import org.geometerplus.fbreader.network.atom.ATOMLink;
import org.geometerplus.fbreader.network.litres.genre.LitResGenre;
import org.geometerplus.fbreader.network.litres.genre.LitResGenreMap;
import org.geometerplus.fbreader.network.litres.readers.LitresEntry;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public class LitresFeedHandler implements ATOMFeedHandler<LitresFeedMetadata,LitresEntry> {
	public List<NetworkBookItem> Books = new LinkedList<NetworkBookItem>();
	protected LitresCatalogItem.State myResult;
	protected int myIndex = 0;
	LitresFeedHandler(LitresCatalogItem.State result){
		if (!(result.Link instanceof LitresNetworkLink)) {
			throw new IllegalArgumentException("Parameter `result` has invalid `Link` field value: result.Link must be an instance of LitresNetworkLink class.");
		}
		myResult = result;
	}
	
	@Override
	public void processFeedStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean processFeedMetadata(LitresFeedMetadata feed,
			boolean beforeEntries) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean processFeedEntry(LitresEntry entry) {
		System.out.println("[LitresFeedHandler] processFeedEntry");
		boolean hasBookLink = true;
		String myBaseURL = "";
		NetworkItem item = null;
		if (hasBookLink) {
			item = new LitresBookItem(myResult.Link, entry, myBaseURL, myIndex++);
		}
		if (item != null) {
			myResult.Loader.onNewItem(item);
		}
		return false;
	}

	@Override
	public void processFeedEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public LitresFeedMetadata createFeed(ZLStringMap attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LitresEntry createEntry(ZLStringMap attributes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ATOMLink createLink(ZLStringMap attributes) {
		// TODO Auto-generated method stub
		return null;
	}

}
