package org.geometerplus.fbreader.network.litres;

import org.geometerplus.fbreader.network.atom.ATOMFeedHandler;
import org.geometerplus.fbreader.network.atom.ATOMLink;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;

public class LitresFeedHandler implements ATOMFeedHandler<LitresFeedMetadata,LitresEntry> {
	
	
	LitresFeedHandler(LitresCatalogItem.State result){
		if (!(result.Link instanceof LitresNetworkLink)) {
			throw new IllegalArgumentException("Parameter `result` has invalid `Link` field value: result.Link must be an instance of LitresNetworkLink class.");
		}
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
		// TODO Auto-generated method stub
		System.out.println(">>> [LitresFeedHandler] processFeedEntry");
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
